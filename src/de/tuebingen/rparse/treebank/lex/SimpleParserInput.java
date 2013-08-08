/*******************************************************************************
 * File SimpleParserInput.java
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
package de.tuebingen.rparse.treebank.lex;

import java.util.HashSet;
import java.util.Set;

/**
 * Standard rparse parser input format: words and pos-tags, separated by a slash.
 * @author wmaier
 *
 */
public class SimpleParserInput extends ParserInput {

	private int size;
	
	private int[] words;
	
	private int[] tags;
	
	private HashSet<Integer> tagset;

	/**
	 * Construct a new simple parser input (a single sentence).
	 * @param size The length of the sentence.
	 */
	public SimpleParserInput(int size) {
		this.size = size;
		words = new int[size];
		tags = new int[size];
		tagset = new HashSet<Integer>();
	}

	@Override
	public int[] getWords() {
		return words;
	}

	@Override
	public int[] getTags() {
		return tags;
	}

	@Override
	public Set<Integer> tagsAsSet() {
		return tagset;
	}

	@Override
	public void setWord(int i, Integer wordn) {
		words[i] = wordn;
	}

	@Override
	public void setTag(int i, Integer tagn) {
		tags[i] = tagn;
		tagset.add(tagn);
	}

	@Override
	public int size() {
		return size;
	}

}
