/*******************************************************************************
 * File DependencyProcessingTaskFactory.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2015
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
package de.tuebingen.rparse.treebank.dep;

import java.util.HashMap;
import java.util.Map;

import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

public class DependencyGapAnalyzer extends ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> {

	private Map<Integer,Integer> degs;
	
	public DependencyGapAnalyzer() {
		degs = new HashMap<>();
	}
	
	@Override
	public void processSentence(DependencyForest<DependencyForestNodeLabel, String> sentence) throws TreebankException {
		int gd = sentence.getGapDegree();
		if (!degs.containsKey(gd)) {
			degs.put(gd, 0);
		}
		degs.put(gd, degs.get(gd) + 1);
	}


	@Override
	public void done() throws TreebankException {
		System.out.println("gap degree stats: ");
		for (int deg : degs.keySet()) {
			System.err.println(degs.get(deg) + " sent. with gap degree " + deg);
		}
	}

}
