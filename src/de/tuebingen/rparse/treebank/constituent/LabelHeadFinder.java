/*******************************************************************************
 * File LabelHeadFinder.java
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

import java.util.Arrays;

import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Marks the leftmost child with a Penn-Treebank-style edge label including
 * {@code "HD"} as head. Use together with {@link HeadLabeler} to preserve
 * explicit head annotation across serialization to a text file format.
 * @author ke
 *
 */
public class LabelHeadFinder extends NodeLocalHeadFinder {
	
	public LabelHeadFinder(String params) {
		// do nothing
	}

	@Override
	public int getHead(String lhs, String[] rhs, String[] rhsedges) throws TreebankException {
		for (int i = 0; i < rhsedges.length; i++) {
			if (containsHD(rhsedges[i])) {
				return i;
			}
		}
		
		throw new TreebankException("No HD label: " + lhs + " -> " + Arrays.toString(rhs));
	}

	private boolean containsHD(String rhsedge) {
		String[] labels = rhsedge.split("-");
		
		for (String label : labels) {
			if ("HD".equals(label)) {
				return true;
			}
		}
		
		return false;
	}

}
