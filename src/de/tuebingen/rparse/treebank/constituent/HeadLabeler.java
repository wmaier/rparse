/*******************************************************************************
 * File HeadLabeler.java
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

/**
 * Adds the edge label -HD to all nodes that are explicitly marked as head.
 * Assumes that this label is not yet present on any nodes.
 * @author ke
 *
 */
public class HeadLabeler extends ProcessingTask<Tree> {

	@Override
	public void processSentence(Tree sentence) throws TreebankException {
		for (Node node : sentence.getRoot().getNodes()) {
			if (node.isHead()) {
				labelAsHead(node);
			}
		}
	}

	private void labelAsHead(Node node) {
		NodeLabel label = node.getLabel();
		String edgeLabels = label.getEdge();
		
		if ("--".equals(edgeLabels)) {
			edgeLabels = "HD";
		} else {
			edgeLabels += "-HD";
		}
		
		label.setEdge(edgeLabels);
	}

}
