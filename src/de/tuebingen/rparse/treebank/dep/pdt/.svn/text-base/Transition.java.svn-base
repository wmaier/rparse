/*******************************************************************************
 * File Transition.java
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
package de.tuebingen.rparse.treebank.dep.pdt;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transition in a dependency graph
 * @author wmaier
 *
 */
public class Transition {
	
	private List<Integer> deps;
	private int head;
	private String lab;
	
	public Transition() {
		deps = new ArrayList<Integer>();
		head = -1;
		lab = "";
	}
		
	public int getHead() {
		return head;
	}
	
	public void setHead(int head) {
		this.head = head;
	}

	public List<Integer> getDeps() {
		return deps;
	}

	public void setDeps(List<Integer> deps) {
		this.deps = deps;
	}
	
	public void addDep(Integer dep) {
		deps.add(dep);
	}

	public void setLabel(String lab) {
		this.lab = lab;
	}
	
	public String getLabel() {
		return lab;
	}
	
	@Override
	public String toString() {
		String ret = lab + " : " + head + " -> ";
		for (Integer i : deps) {
			ret += i + " ";
		}
		return ret;
	}
	
}
