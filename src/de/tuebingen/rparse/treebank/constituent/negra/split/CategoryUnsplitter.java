/*******************************************************************************
 * File CategoryUnsplitter.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeProcessingTask;

/**
 * Undo all the splits!
 * 
 * @author wmaier
 * 
 */
public class CategoryUnsplitter extends NodeProcessingTask {

	private static final Map<String, String> CATMAP = new HashMap<String, String>();
	private static final List<String> CATSUFFIXMAP = new ArrayList<String>();
	private static final List<String> CATPREFIXMAP = new ArrayList<String>();

	private int splitcount = 0;

	static {
		// VP coarse
		CATMAP.put("VPPP", "VP");
		CATMAP.put("VPINF", "VP");
		CATMAP.put("VPZU", "VP");
		// VP fin
		CATMAP.put("VPFIN", "VP");
		// Heavy NP & PP
		CATMAP.put("HNP", "NP");
		CATMAP.put("HPP", "PP");
		// S by Prounoun
		CATMAP.put("SPW", "S");
		CATMAP.put("SPR", "S");
		// S by RC
		CATMAP.put("SRC", "S");
		// PH-RE
		CATSUFFIXMAP.add("-PH");
		CATSUFFIXMAP.add("-RE");
		CATSUFFIXMAP.add("-GL");
		CATSUFFIXMAP.add("-GR");
		// VP fine
		CATPREFIXMAP.add("VP-");
		// NP case
		CATPREFIXMAP.add("NP-");
	}

	@Override
	protected void processNode(Node node) throws TreebankException {
		String tag = node.getLabel().getTag();

		for (String suffix : CATSUFFIXMAP) {
			if (tag.endsWith(suffix)) {
				++splitcount;
				node.getLabel().setTag(
						tag.substring(0, tag.lastIndexOf(suffix)));
			}
		}

		for (String prefix : CATPREFIXMAP) {
			if (tag.startsWith(prefix)) {
				++splitcount;
				node.getLabel().setTag(tag.substring(0, prefix.length() - 1));
			}
		}

		if (CATMAP.containsKey(tag)) {
			++splitcount;
			node.getLabel().setTag(CATMAP.get(tag));
		}

	}

	@Override
	public void done() throws TreebankException {
		Logger.getLogger(CategorySplitter.class.getPackage().getName()).fine(
				this.getClass().getCanonicalName() + ": " + splitcount
						+ " un-splits");
	}

}
