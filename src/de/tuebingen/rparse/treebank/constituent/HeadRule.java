/*******************************************************************************
 * File HeadRule.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2011
 * 
 * This file is part of rparse, see <www.wolfgang-maier.net/rparse>.
 * 
 * rparse is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 * 
 * rparse is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the  GNU General Public 
 * License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.tuebingen.rparse.treebank.constituent;

import java.util.Arrays;

/**
 * Defines a head rule with a search direction and a set of labels. 
 * 
 * @author wmaier
 *
 */
public class HeadRule {

	private String direction;
	private String[] labels;
	
	public HeadRule(String direction, String[] labels) {
		this.direction = direction;
		this.labels = labels;
	}
	
	public String getDirection() {
		return direction;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}
	
	public int numberOfLabels() {
		return labels.length;
	}

	public String toString() {
		String ret = "[";
		ret += direction + ", ";
		ret += Arrays.toString(labels);
		ret += "]";
		return ret;
	}
	
}
