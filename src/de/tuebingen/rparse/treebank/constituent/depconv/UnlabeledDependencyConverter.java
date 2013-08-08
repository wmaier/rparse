/*******************************************************************************
 * File UnlabeledDependencyConverter.java
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
package de.tuebingen.rparse.treebank.constituent.depconv;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.HeadFinder;
import de.tuebingen.rparse.treebank.constituent.HeadFinderFactory;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Implements the generic conversion procedure described in Lin (1995) "A dependency-based method for
 * evaluating broad-coverage parsers"

 * @author wmaier
 *
 */
public class UnlabeledDependencyConverter implements DependencyConverter {
	
	protected HeadFinder<Tree> hf;
	
	protected Map<Node,Integer> nodeToId;
	protected Map<Integer,Node> idToNode;
	
	public UnlabeledDependencyConverter(String hftype) throws TreebankException, UnknownTaskException, IOException {
		try {
			hf = HeadFinderFactory.getHeadFinder(hftype);
		} catch (Exception e) {
			throw new TreebankException("Must supply a headfinder for dependency conversion");
		}
		nodeToId = new HashMap<Node,Integer>();
		idToNode = new HashMap<Integer,Node>();
	}

	@Override
	public DependencyForest<DependencyForestNodeLabel, String> processSentence(Tree t) throws TreebankException {
		// mark heads in the input tree
		markHeads(t);
		// prepare the dep graph with the terminals
		DependencyForest<DependencyForestNodeLabel, String> deps = initializeDepGraph(t);
		// draw the edges
		makeDeps(t.getRoot(), deps);
		// set ID
		deps.id = t.getId();
		// return it
		return deps;
	}
	
	protected void markHeads(Tree t) throws TreebankException {
		hf.processSentence(t);
	}
	
	protected DependencyForest<DependencyForestNodeLabel, String> initializeDepGraph(Tree t) {
		DependencyForest<DependencyForestNodeLabel, String> ret = new DependencyForest<DependencyForestNodeLabel, String>();
		int id = 1;
		for (Node term : t.getOrderedTerminals()) {
			DependencyForestNodeLabel lab = new DependencyForestNodeLabel(term.getLabel().getWord(), term.getLabel().getTag());
			ret.addNode(lab);
			nodeToId.put(term,id);
			idToNode.put(id,term);
			++id;
		}
		return ret;
	}
	
	protected Node makeDeps(Node root, DependencyForest<DependencyForestNodeLabel, String> deps) {
		if (!root.hasChildren()) {
			return root;
		}
		int headChildIndex = root.getHeadChildIndex();
		Node headChild = root.getChildren().get(headChildIndex);
		Node lexHead = makeDeps(headChild, deps);
		List<Node> children = root.getChildren();
		for (int i = 0; i < children.size(); ++i) {
			if (i != headChildIndex) {
				Node child = children.get(i);
				Node lexHeadOfChild = makeDeps(child, deps);
				addDepRel(lexHead, lexHeadOfChild, deps);
			}
		}
		return lexHead;
	}
	
	protected void addDepRel(Node head, Node modifier, DependencyForest<DependencyForestNodeLabel, String> deps) {
		deps.addEdge(nodeToId.get(modifier), nodeToId.get(head), "NONE");
	}
	
}
