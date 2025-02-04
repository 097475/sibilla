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
	/**
 * 
 */
package quasylab.sibilla.core.simulator.pm;

import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * @author loreti
 *
 */
public class BroadcastRule implements PopulationRule {
	
	final private Function<PopulationState,Double> rateFunction;
	
	final private int senderIndex;
	
	final private Function<RandomGenerator,Integer> step;
	
	final private BroadcastReceiver[] receivers;
	
	final private String name;

	/**
	 * @param rateFunction
	 * @param senderIndex
	 * @param receivers
	 */
	public BroadcastRule(String name, 
			Function<PopulationState, Double> rateFunction, 
			int senderIndex,
			Function<RandomGenerator,Integer> step,
			BroadcastReceiver ... receivers) {
		super();
		this.rateFunction = rateFunction;
		this.senderIndex = senderIndex;
		this.receivers = receivers;
		this.step = step;
		this.name = name;
	}


	@Override
	public PopulationTransition apply(RandomGenerator r, PopulationState state) {
		if (state.getOccupancy(senderIndex)>0) {
			double rate = rateFunction.apply(state);
			if (rate > 0.0) {
				return new PopulationTransition(
					this.name, 
					state.getOccupancy(senderIndex)*rate, 
					(rg -> BroadcastRule.getDrift(name,rg,senderIndex,state,step,receivers))
				);
			}
		}
		return null;
	}

	public static Update getDrift(
			String name,
			RandomGenerator r, 
			int sender, 
			PopulationState state,
			Function<RandomGenerator,Integer> step, 
			BroadcastReceiver[] receivers
	) {
		Update result = new Update(name);
		result.consume(sender,1);
		result.produce(step.apply(r), 1);
		for( int i=0 ; i<receivers.length ; i++ ) {
			double pop = state.getOccupancy(receivers[i].receiver);
			if (receivers[i].receiver==sender) {
				pop = pop -1 ;
			}
			int counter = 0;
			double rp = receivers[i].receivingProbability.apply(state);
			for ( int j=0 ; j<pop ; j++ ) {
				if (r.nextDouble()<rp) {
					counter++;
					result.produce(receivers[i].step.apply(r), 1);
				}
			}
			result.consume(receivers[i].receiver, counter);
			if ( state.getOccupancy(receivers[i].receiver)+result.get(receivers[i].receiver)<0  ) {
				throw new IllegalArgumentException("!!!!");
			}
		}		
		return result;
	}
	
	public static class BroadcastReceiver {
		
		private final int receiver;
		
		private final Function<PopulationState,Double> receivingProbability;
		
		private final Function<RandomGenerator,Integer> step;
		
		/**
		 * @param receiver
		 * @param receivingProbability
		 */
		public BroadcastReceiver(int receiver, 
				Function<PopulationState, Double> receivingProbability,
				Function<RandomGenerator,Integer> step) {
			super();
			this.receiver = receiver;
			this.receivingProbability = receivingProbability;
			this.step = step;
		}
		
		/**
		 * @return the receiver
		 */
		public int getReceiver() {
			return receiver;
		}



		/**
		 * @return the receivingProbability
		 */
		public Function<PopulationState, Double> getReceivingProbability() {
			return receivingProbability;
		}
			
	}
}
