/*******************************************************************************
 * File PTBHeadFinder.java
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
package de.tuebingen.rparse.treebank.constituent.ptb;

import java.io.IOException;

import de.tuebingen.rparse.treebank.constituent.RuleBasedHeadFinder;

/**
 * Implements the head-finding algorithm of Collins (1999, p. 238 ff.)
 * 
 * @author ke
 * 
 */
public class PTBHeadFinder extends RuleBasedHeadFinder {

	private boolean dptbVariant;

	public PTBHeadFinder(String params, boolean dptbVariant) throws IOException {
		super(params, PTBHeadFinder.class.getResourceAsStream("ptb.headrules"));
		this.dptbVariant = dptbVariant;
	}

	public PTBHeadFinder(boolean dptbVariant) throws IOException {
		this("", dptbVariant);
	}

	@Override
	public int getHead(String lhs, String[] rhs, String[] rhsedges) {
		int head;

		if ("NP".equals(lhs)) {
			// Special treatment for NPs:
			head = getNPHead(rhs);
		} else {
			head = super.getHead(lhs, rhs, rhsedges);
		}

		// Special treatment for coordinated phrases:
		if (head >= 2) {
			String previousTag = rhs[head - 1];

			if ("CC".equals(previousTag) || "CONJP".equals(previousTag)) {
				for (int i = head - 2; i >= 0; i--) {
					if (!PTBUtil.isPunctuationTag(rhs[i])) {
						return i;
					}
				}
			}
		}

		return head;
	}

	private int getNPHead(String[] rhs) {
		int last = rhs.length - 1;

		if ("POS".equals(rhs[last])) {
			return last;
		}

		for (int i = last; i >= 0; i--) {
			if (matches(rhs[i], "NN") || matches(rhs[i], "NNP")
					|| matches(rhs[i], "NNPS") || matches(rhs[i], "NNS")
					|| matches(rhs[i], "NX") || matches(rhs[i], "POS")
					|| matches(rhs[i], "JR")) {
				return i;
			}
		}

		for (int i = 0; i < rhs.length; i++) {
			if (matches(rhs[i], "NP")) {
				return i;
			}
		}

		for (int i = last; i >= 0; i--) {
			if ("$".equals(rhs[i]) || matches(rhs[i], "ADJP")
					|| matches(rhs[i], "PRN")) {
				return i;
			}
		}

		for (int i = last; i >= 0; i--) {
			if (matches(rhs[i], "CD")) {
				return i;
			}
		}

		for (int i = last; i >= 0; i--) {
			if (matches(rhs[i], "JJ") || matches(rhs[i], "JJS")
					|| matches(rhs[i], "RB") || matches(rhs[i], "QP")) {
				return i;
			}
		}

		return last;
	}

	@Override
	protected boolean matches(String tag, String tagPattern) {
		if (super.matches(tag, tagPattern)) {
			return true;
		}

		if (dptbVariant && tag.startsWith("WH")
				&& tag.substring(2).equals(tagPattern)) {
			return true;
		}

		return false;
	}

}
