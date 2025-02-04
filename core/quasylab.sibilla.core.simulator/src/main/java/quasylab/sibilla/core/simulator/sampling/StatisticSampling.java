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
package quasylab.sibilla.core.simulator.sampling;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.function.Function;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;


/**
 * @author loreti
 *
 */
public class StatisticSampling<S> implements SamplingFunction<S> {

	private SummaryStatistics[] data;
	private Measure<S> measure;
	private double last_measure;
	private double dt;
	private double next_time;
	private int current_index;
	private double new_measure;

	public StatisticSampling(int samples, double dt, Measure<S> measure) {
		this.data = new SummaryStatistics[samples];
		this.measure = measure;
		this.dt = dt;
		init();
	}

	private void init() {
		for (int i = 0; i < data.length; i++) {
			data[i] = new SummaryStatistics();
		}
	}

	@Override
	public void sample(double time, S context) {
		this.new_measure = measure.measure(context);
		if ((time >= this.next_time) && (this.current_index < this.data.length)) {
			recordMeasure(time);
		} else {
			this.last_measure = this.new_measure;
		}
	}

	private void recordMeasure(double time) {
		while ((this.next_time<time)&&(this.current_index<this.data.length)) {
			this.recordSample();
		} 
		this.last_measure = this.new_measure;		
		if (this.next_time == time) {
			this.recordSample();
		}
	}
	
	private void recordSample() {
		this.data[this.current_index].addValue(this.last_measure);
		this.current_index++;
		this.next_time += this.dt;
	}
	

	@Override
	public void end(double time) {
		while (this.current_index < this.data.length) {
			this.data[this.current_index].addValue(this.last_measure);
			this.current_index++;
			this.next_time += this.dt;
		}
	}

	@Override
	public void start() {
		this.current_index = 0;
		this.next_time = 0;
	}
	
	public String getName() {
		return measure.getName();
	}

	public void printTimeSeries(PrintStream out) {
		double time = 0.0;

		for (int i = 0; i < this.data.length; i++) {
			out.println(time + "\t" + this.data[i].getMean() + "\t" + this.data[i].getStandardDeviation());
			time += dt;
		}
	}

	public void printTimeSeries(PrintStream out, char separator) {
		double time = 0.0;
		for (int i = 0; i < this.data.length; i++) {
			out.println(""+time + separator 
					+ this.data[i].getMean() 
					+ separator + this.data[i].getStandardDeviation());
			time += dt;
		}
	}
	
	
	public void printName(PrintStream out){
		out.print(this.measure.getName());
	}
	
	public void printlnName(PrintStream out){
		out.println(this.measure.getName());
	}

	@Override
	public LinkedList<SimulationTimeSeries> getSimulationTimeSeries( int replications ) {
		SimulationTimeSeries stt = new SimulationTimeSeries(measure.getName(), dt, replications, data);
		LinkedList<SimulationTimeSeries> toReturn = new LinkedList<>();
		toReturn.add(stt);
		return toReturn;
	}

	public int getSize() {
		return data.length;
	}
	
	public static <S> StatisticSampling<S> measure( String name, int samplings, double deadline, Function<S,Double> m) {
		return new StatisticSampling<S>(samplings, deadline/samplings, 
				new Measure<S>() {

			@Override
			public double measure(S t) {
				// TODO Auto-generated method stub
				return m.apply( t );
			}

			@Override
			public String getName() {
				return name;
			}

		});
		
	}

}