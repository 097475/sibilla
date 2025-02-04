/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package quasylab.sibilla.core.simulator;

import java.io.Serializable;
import java.util.function.Supplier;

import org.apache.commons.math3.random.RandomGenerator;

import quasylab.sibilla.core.simulator.util.WeightedElement;
import quasylab.sibilla.core.simulator.util.WeightedStructure;

/**
 * @author loreti
 *
 */
public class SimulationTask<S> implements Supplier<Trajectory<S>>, Serializable {

	private static final long serialVersionUID = -504798938865475892L;

	private double time;
	private RandomGenerator random;
	private SimulationUnit<S> unit;
	private S currentState;
	private SimulationStatus status;
	private Trajectory<S> trajectory;
	private long startTime = 0, elapsedTime = 0;
	
	public SimulationTask( RandomGenerator random , SimulationUnit<S> unit) {
		this.random = random;
		this.unit = unit;
		this.status = SimulationStatus.INIT;
		this.getClass();//TODO: Is this needed?
	}

	public void reset(){
		time = 0;
		status = SimulationStatus.INIT;
		startTime = 0;
		elapsedTime = 0;
	}
	

	@Override
	public Trajectory<S> get() {
		long startTime = System.nanoTime();
		running();
		this.currentState = this.unit.getState();
		this.trajectory = new Trajectory<>();
		this.trajectory.add(time, currentState);
		while (!unit.getStoppingPredicate().test(this.time, currentState)&&(!isCancelled())) {
			step();
		}
		this.trajectory.setSuccesfull(this.unit.getReachPredicate().test(currentState));
		completed(true);
		this.trajectory.setGenerationTime(System.nanoTime()-startTime);
		return this.getTrajectory();
	}
	
	private synchronized void running() {
		startTime = System.nanoTime();
		if (!isCancelled()) {
			this.status = SimulationStatus.RUNNING;
		}
		
	}

	private synchronized void completed(boolean b) {
		if (this.status != SimulationStatus.CANCELLED) {
			this.status = SimulationStatus.COMPLETED;
		}
		elapsedTime = System.nanoTime() - startTime;
	}

	private void step() {
		WeightedStructure<StepFunction<S>> agents = 
				this.unit.getModel().getActivities( random , currentState );
		double totalRate = agents.getTotalWeight();
		if (totalRate == 0.0) {
			cancel();
			return;
		}
		double dt = (1.0 / totalRate) * Math.log(1 / (random.nextDouble()));
		double select = random.nextDouble() * totalRate;
		WeightedElement<StepFunction<S>> wa = agents.select(select);
		if (wa == null) {
			cancel();
			return;
		}
		currentState = wa.getElement().step(random,time,dt);
		time += dt;
		trajectory.add(time, currentState);

	}

	public synchronized void cancel() {
		if (!this.isCompleted()) {
			this.status = SimulationStatus.CANCELLED; 			
		}
	}

	public synchronized boolean isCompleted() {
		return this.status == SimulationStatus.COMPLETED;
	}

	public synchronized boolean isRunning() {
		return this.status == SimulationStatus.RUNNING;
	}

	public synchronized boolean isCancelled() {
		return (this.status==SimulationStatus.CANCELLED);
	}

	public Trajectory<S> getTrajectory() {
		return trajectory;		
	}

	public long getElapsedTime(){
		return elapsedTime;
	}

}
