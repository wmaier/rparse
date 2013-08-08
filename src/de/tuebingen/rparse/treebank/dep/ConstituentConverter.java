/*******************************************************************************
 * File ConstituentConverter.java
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
package de.tuebingen.rparse.treebank.dep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * Build a dependency graph object from a graph present as a tree class
 * 
 * @author wmaier
 * 
 */
public class ConstituentConverter {

	/**
	 * Build a dependency graph object from a graph present as a tree class
	 * 
	 */
	public static final DependencyForest<DependencyForestNodeLabel, String> convert(
			Tree t) {
		DependencyForest<DependencyForestNodeLabel, String> ret = new DependencyForest<DependencyForestNodeLabel, String>();
		for (Node n : t.getOrderedTerminals())
			ret.addNode(new DependencyForestNodeLabel(n.getLabel().getWord(), n
					.getLabel().getTag(), n.getLabel().getTag()));

		List<Node> nodes = t.getRoot().getNodes(new ArrayList<Node>());
		// make a head table
		Map<Node, Integer> headMap = new HashMap<Node, Integer>();
		int head = 0;
		for (Node n : nodes) {
			for (Node c : n.getChildren())
				if (!c.hasChildren())
					head = c.getLabel().getNum();
			headMap.put(n, head);
		}
		// build the graph
		for (Node n : nodes) {
			if (n.getLc() == null)
				continue;
			String relation = n.getLabel().getTag();
			head = 0;
			if (n.getPa() != null) {
				head = headMap.get(n.getPa());
				int modifier = headMap.get(n);
				ret.addEdge(modifier, head, relation);
			}
		}

		return ret;

	}

}
