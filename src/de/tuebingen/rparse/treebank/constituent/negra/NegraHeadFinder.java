/*******************************************************************************
 * File NegraHeadFinder.java
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
package de.tuebingen.rparse.treebank.constituent.negra;

import java.io.IOException;

import de.tuebingen.rparse.treebank.constituent.RuleBasedHeadFinder;

/**
 * Head rules for finding head daughter of local trees in NeGra.
 * 
 * @author wmaier
 * 
 */
public class NegraHeadFinder extends RuleBasedHeadFinder {

	public final static String HEAD = "HD";

	public NegraHeadFinder(String params) throws IOException {
		super(params, NegraHeadFinder.class.getResourceAsStream("negra.headrules"));
	}

	@Override
	public int getHead(String lhs, String[] rhs, String[] rhsedges) {
		//System.err.println("Searching head of " + lhs + "/" + Arrays.toString(rhs) + "/" + Arrays.toString(rhsedges));
		
		for (int i = 0; i < rhsedges.length; ++i) {
			if (rhsedges[i].equals(HEAD))
				return i;
		}
		
		
		return super.getHead(lhs, rhs, rhsedges);
	}

}
