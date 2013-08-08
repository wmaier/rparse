/*******************************************************************************
 * File PunctuationLowerer.java
 * 
 * Authors:
 *    Kilian Evang
 *    
 * Copyright:
 *    Kilian Evang, 2011
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

import java.util.Collections;
import java.util.List;

import de.tuebingen.rparse.treebank.TreebankException;

public class PunctuationLowererLeftToRightOrder extends AbstractPunctuationLowerer {

    /**
	 * Lower all nodes based on a left-to-right ordering of all children
	 * @param node the node to be moved
	 * @param candidateTarget new ancestor, and possible parent, of {@link node}
	 * @throws TreebankException
	 */
	public boolean lower(Node node, Node candidateTarget) throws TreebankException {
		List<Node> children = candidateTarget.getChildren();
		int size = children.size();
		int punctuationNum = node.getLabel().getNum();

		for (int i = 0; i < size; i++) {
			Node child = children.get(i);
			List<Integer> termdomOfTargetChild = child.calcTermdom();
			
			if (punctuationNum < Collections.min(termdomOfTargetChild)) {
			    //System.err.println("moving " + node + " as " + i + "th child of " + candidate);
				node.moveAsChild(candidateTarget, i);
				break;
			} else {
			    if (punctuationNum < Collections.max(termdomOfTargetChild)) {
					lower(node, child);
					break;
				}
			}
		}
		return true;
	}

}
