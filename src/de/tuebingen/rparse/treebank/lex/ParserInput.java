/*******************************************************************************
 * File ParserInput.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.HasSize;

/**
 * Formats which extend this class can be used as parser input (for testing)
 * 
 * @author wmaier
 */
public abstract class ParserInput implements HasSize {

    /**
     * Get the parser input string as a list of integers (backed by a Numberer).
     * 
     * @return The corresponding value.
     */
    public abstract int[] getWords();

    /**
     * Get the pos tags of the parser input as a list of integers (backed by a Numberer).
     * 
     * @return The corresponding value.
     */
    public abstract int[] getTags();

    /**
     * Get all the pos tags in the input sentence as a set of integers (backed by a Numberer).
     * 
     * @return The corresponding {@link Set}.
     */
    public abstract Set<Integer> tagsAsSet();

    /**
     * A {@code toString()} adaption for the parser input.
     * 
     * @param nb
     *            The numberer in order to decode the words and pos tags.
     * @return A human-readable string representation of this ParserInput instance.
     */
    public String parserInputPrint(Numberer nb) {
        List<String> ret = new ArrayList<String>();
        int[] words = getWords();
        int[] tags = getTags();
        for (int i = 0; i < words.length; i++) {
            String word = "";
            String mw = (String) nb.getObjectWithId(LexiconConstants.INPUTWORD, words[i]);
            String mt = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, tags[i]);
            if (mw != null && mt != null)
                word = mw + " / " + mt;
            else
                word = "null";
            ret.add(word);
        }
        return "(" + words.length + ")" + ret;
    }

    /**
     * Set a word in the sentence.
     * 
     * @param i
     *            The index of the word to set.
     * @param number
     *            The integer representing the word (backed by a Numberer).
     */
    public abstract void setWord(int i, Integer number);

    /**
     * Set a pos tag in the sentence.
     * 
     * @param i
     *            The index of the pos tag to set.
     * @param number
     *            The integer representing the word (backed by a numberer).
     */
    public abstract void setTag(int i, Integer number);

}
