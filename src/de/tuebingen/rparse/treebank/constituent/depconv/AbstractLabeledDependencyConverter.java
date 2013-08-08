/*******************************************************************************
 * File AbstractLabeledDependencyConverter.java
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

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Creates labeled dependencies from a constituent tree following a labeling scheme
 * which must be implemented by every class subclassing this one.
 * 
 * @author wmaier
 *
 */
abstract public class AbstractLabeledDependencyConverter extends UnlabeledDependencyConverter {

	public AbstractLabeledDependencyConverter(String task) throws TreebankException,
			UnknownTaskException, IOException {
		super(task);
	}

	@Override
	public DependencyForest<DependencyForestNodeLabel, String> processSentence(
			Tree t) throws TreebankException {
		// mark heads in the input tree
		super.markHeads(t);
		// make label pre-annotations
		LabeledDependencyAnnotator.annotate(t);
		// prepare the dep graph with the terminals
		DependencyForest<DependencyForestNodeLabel, String> deps = initializeDepGraph(t);
		// draw the edges
		makeDeps(t.getRoot(), deps);
		// annotate the edges
		labelDeps(deps);
		// set ID
		deps.id = t.getId();
		// return it
		return deps;
	}

	/**
	 * Introduce dependency labels on this graph
	 * @param deps
	 */
	abstract protected void labelDeps(DependencyForest<DependencyForestNodeLabel, String> deps);
	
	
}
