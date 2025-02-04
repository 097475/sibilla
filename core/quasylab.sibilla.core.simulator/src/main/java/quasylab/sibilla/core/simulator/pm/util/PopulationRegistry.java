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

package quasylab.sibilla.core.simulator.pm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import quasylab.sibilla.core.simulator.pm.PopulationState;

/**
 * @author loreti
 *
 */
public class PopulationRegistry {
	
	private int count;
	
	private final Map<Tuple,Integer> map; 
	private final ArrayList<Tuple> elements;

	public PopulationRegistry() {
		this.count = 0;
		this.map = new HashMap<PopulationRegistry.Tuple, Integer>();
		this.elements = new ArrayList<>();
	}
	
	public void register( Object ... values ) {
		Tuple t = new Tuple(values);
		if (!map.containsKey(t)) {
			map.put(t, count++);
		}
	}
	
	public int indexOf( Object ... values ) {
		return map.getOrDefault(new Tuple(values), -1);
	}
	
	private Tuple tupleOf( int idx ) {
		return elements.get(idx);
	}
	
	public int size() {
		return count;
	}
	
	public PopulationState createPopulationState( Function<Object[],Integer> population ) {
		PopulationState state = new PopulationState(count,i -> population.apply(tupleOf(i).values));
		return state;
	}
	
	private static class Tuple {
		
		private Object[] values;
		
		public Tuple( Object ... values ) {
			this.values = values;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.deepHashCode(values);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple other = (Tuple) obj;
			if (!Arrays.deepEquals(values, other.values))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[" + Arrays.toString(values) + "]";
		}


		
	}
}
