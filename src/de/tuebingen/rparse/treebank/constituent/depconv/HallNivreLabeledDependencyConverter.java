/*******************************************************************************
 * File HallNivreLabeledDependencyConverter.java
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
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNode;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Labeled dependencies as in Hall & Nivre (2008)
 * @author wmaier
 *
 */
public class HallNivreLabeledDependencyConverter extends
		AbstractLabeledDependencyConverter {

	public HallNivreLabeledDependencyConverter(String task)
			throws TreebankException, UnknownTaskException, IOException {
		super(task);
	}

	@Override
	protected void labelDeps(
			DependencyForest<DependencyForestNodeLabel, String> deps) {
		for (DependencyForestNode<DependencyForestNodeLabel, String> n : deps.nodes()) {
			int id = n.getID();
			String relation = "[" 
				+ (String) super.idToNode.get(id).getProperty(LabeledDependencyAnnotator.DEPENDENCY_RELATION)
				+ ","
				+ (String) super.idToNode.get(id).getProperty(LabeledDependencyAnnotator.HEAD_RELATIONS)
				+ ","
				+ (String) super.idToNode.get(id).getProperty(LabeledDependencyAnnotator.CONSTITUENT_LAB)
				+ ","
				+ String.valueOf(super.idToNode.get(id).getProperty(LabeledDependencyAnnotator.ATTACHMENT))
				+ "]";
			n.setRelation(relation);
		}
	}

	
}
