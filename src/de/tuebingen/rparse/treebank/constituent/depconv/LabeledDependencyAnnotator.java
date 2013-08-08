/*******************************************************************************
 * File LabeledDependencyAnnotator.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * 
 * Creates some annotations in trees which are necessary before converting them
 * to labeled dependencies (labeling as described in Hall & Nivre (2008))
 * 
 * @author wmaier
 * 
 */
public class LabeledDependencyAnnotator {
	
	/**
	 * lexical element is the head of its parent
	 */
	public static final String IS_HEAD = "ishead";
	
	/**
	 * maximal projection edge label
	 */
	public static final String DEPENDENCY_RELATION = "deprel";
	
	/**
	 * maximal projection node label
	 */
	public static final String DEPENDENCY_RELATION_LABEL = "deprellabel";
	
	/**
	 * path of edge labels up to maximal projection
	 */
	public static final String HEAD_RELATIONS = "headrel";
	
	/**
	 * path of node labeles up to maximal projection
	 */
	public static final String CONSTITUENT_LAB = "const";
	
	/**
	 * length of path to max projection
	 */
	public static final String ATTACHMENT = "attachment";
	
	
	public LabeledDependencyAnnotator() {
		// do nothing
	}
	
	
	/**
	 * annotate tree with - dependency relation - head relations - constituent
	 * labels - attachment as in Hall & Nivre (2008) (PaGe workshop)
	 * 
	 * @param t
	 *            a tree with all heads marked
	 */
	public static void annotate(Tree t) {
		Logger logger = Logger.getLogger(LabeledDependencyAnnotator.class.getPackage().getName());
		for (Node term : t.getOrderedTerminals()) {
			Node dependencyRelation = term;
			Node parent = dependencyRelation.getPa();
			int attachment = 0;
			List<Node> headRelationList = new ArrayList<Node>();
			headRelationList.add(dependencyRelation);
			while (parent != null
					&& dependencyRelation.getChildIndex() == parent
							.getHeadChildIndex()) {
				dependencyRelation = parent;
				headRelationList.add(dependencyRelation);
				parent = parent.getPa();
				attachment++;
			}
			String headRelations = "";
			String constRelations = "";
			if (headRelationList.size() > 1) {
				headRelations = headRelationList.get(0).getLabel().getEdge();
				constRelations = headRelationList.get(0).getPa().getLabel()
						.getTag();
				for (Node e : headRelationList.subList(1, headRelationList
						.size() - 1)) {
					headRelations += "|" + e.getLabel().getEdge();
					constRelations += "|" + e.getPa().getLabel().getTag();
				}
			} else {
				headRelations = "*";
				constRelations = "*";
			}
			logger.finer("GF of highest constituent with lexical head " + term
					+ ": " + dependencyRelation.getLabel().getEdge()
					+ ", depth: " + attachment + ", head rel: " + headRelations
					+ ", const rel: " + constRelations);
			boolean isHead = term.getChildIndex() == term.getPa()
					.getHeadChildIndex();
			
			term.setProperty(DEPENDENCY_RELATION, dependencyRelation.getLabel()
					.getEdge());
			term.setProperty(DEPENDENCY_RELATION_LABEL, dependencyRelation
					.getLabel().getTag());
			term.setProperty(IS_HEAD, String.valueOf(isHead));
			term.setProperty(HEAD_RELATIONS, headRelations);
			term.setProperty(CONSTITUENT_LAB, constRelations);
			term.setProperty(ATTACHMENT, attachment);
		}
	}
	
}
