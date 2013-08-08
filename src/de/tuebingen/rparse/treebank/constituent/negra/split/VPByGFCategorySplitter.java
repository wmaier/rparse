/*******************************************************************************
 * File VPByGFCategorySplitter.java
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

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;

/**
 * Category splitting for NeGra parsing experiments (Maier, 2012)
 * @author wmaier
 *
 */
public class VPByGFCategorySplitter extends CategorySplitter {

	@Override
	protected void doSplit(Node n) throws TreebankException {
		if (n.getLabel().getTag().equals("VP")) {
			String gf = n.getLabel().getEdge();
			if (gf != null) {
				gf.replaceAll("-", "");
				if (gf.length() > 0) {
					n.getLabel().setTag("VP-" + gf);
					countSplit();
				}
			}
		}
	}

}
