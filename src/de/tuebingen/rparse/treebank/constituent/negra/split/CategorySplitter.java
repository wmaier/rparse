/*******************************************************************************
 * File CategorySplitter.java
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
package de.tuebingen.rparse.treebank.constituent.negra.split;

import java.util.logging.Logger;

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeProcessingTask;

/**
 * Abstract base class for the category splitters, offers a counter
 * 
 * @author wmaier
 * 
 */
abstract public class CategorySplitter extends NodeProcessingTask {

	private int splitcount;

	public CategorySplitter() {
		splitcount = 0;
	}

	public int getSplitCount() {
		return splitcount;
	}

	public void countSplit() {
		++splitcount;
	}

	@Override
	protected final void processNode(Node node) throws TreebankException {
		doSplit(node);
	}

	abstract protected void doSplit(Node node) throws TreebankException;

	@Override
	public void done() throws TreebankException {
		Logger.getLogger(CategorySplitter.class.getPackage().getName()).fine(
				this.getClass().getCanonicalName() + ": " + splitcount
						+ " splits");
	}

}
