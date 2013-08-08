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

import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

public abstract class AbstractPunctuationLowerer extends ProcessingTask<Tree> {

    private Tree tree;
    
	@Override
	public void done() throws TreebankException {
		// do nothing
	}
	
	protected Tree getTree() {
	    return tree;
	}
	
	protected void setTree(Tree tree) {
	    this.tree = tree;
	}
	
	@Override
	public void processSentence(Tree sentence) throws TreebankException {
	    tree = sentence;
		boolean exportNumbering = sentence.hasExportNumbering();
		Node root = sentence.getRoot();

		for (Node node : root.getChildren()) {			
			if (node.getLabel().isPunct()) {
				lower(node, root);
			}
		}

		if (exportNumbering) {
			sentence.calcExportNumbering();
		}
	}

	/**
	 * Specifies how the nodes are lowered 
	 * @param node the node to be moved
	 * @param candidate new ancestor, and possible parent, of {@link node}
	 * @throws TreebankException
	 */
	abstract public boolean lower(Node node, Node candidate) throws TreebankException;

	
}
