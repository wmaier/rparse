/*******************************************************************************
 * File NodeProcessingTask.java
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
 * Abstract superclass for tree processing tasks that carry out a specific
 * operation on each node in the tree.
 * @author ke
 *
 */
public abstract class NodeProcessingTask extends ProcessingTask<Tree> {

	@Override
	public void processSentence(Tree sentence) throws TreebankException {
		for (Node node : sentence.getRoot().getNodes()) {
			processNode(node);
		}
		
		sentence.calcTermNums();
		sentence.calcExportNumbering();
	}

	protected abstract void processNode(Node node) throws TreebankException;

}
