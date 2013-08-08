/*******************************************************************************
 * File NodeLocalHeadFinder.java
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

import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rparse.treebank.TreebankException;

/**
 * This class must be implemented for a finder class of head daugthers in local
 * treebank trees. The convention is that an implementation overrides
 * {@link getHead}.
 * 
 * @author wmaier
 * 
 */
public abstract class NodeLocalHeadFinder extends HeadFinder<Tree> {

	@Override
	public void annotate(Tree t) throws TreebankException {
		if (t.getRoot() == null)
			throw new TreebankException("Tree root node is null");
		for (Node n : t.getRoot().getNodes(new ArrayList<Node>())) {
			String parentLabel = n.getLabel().getTag();
			List<Node> children = n.getChildren();
			if (children.size() > 0) {
				String[] childrenLabels = new String[children.size()];
				String[] childrenEdges = new String[children.size()];
				for (int i = 0; i < children.size(); ++i) {
					childrenLabels[i] = children.get(i).getLabel().getTag();
					childrenEdges[i] = children.get(i).getLabel().getEdge();
				}
				int headpos = getHead(parentLabel, childrenLabels,
						childrenEdges);
				// System.err.println("Head of " + parentLabel +
				// " with children " + Arrays.toString(childrenLabels) + " is "
				// + headpos);
				children.get(headpos).setIsHead(true);
			}
		}
	}

	abstract public int getHead(String lhs, String[] rhs, String[] rhsedges)
			throws TreebankException;

	@Override
	public void done() throws TreebankException {
	}

}
