/*******************************************************************************
 * File WordTagTranslator.java
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
package de.tuebingen.rparse.treebank.lex;

import java.util.Map;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.ProcessingTask;

/**
 * Convenience: process parser input such that certain character sequences in both words and pos tags are replaced with
 * other sequences from a given dictionary, which is to be specified by a subclass.
 * 
 * @author wmaier
 */
public abstract class WordTagTranslator extends ProcessingTask<ParserInput> {

    private Numberer nb;

    /**
     * Construct a new word-tag-translator.
     * 
     * @param nb
     *            The number which knows all the numbers for the labels.
     */
    public WordTagTranslator(Numberer nb) {
        this.nb = nb;
    }

    @Override
    public void processSentence(ParserInput sentence) {
        int i = 0;

        for (int word : sentence.getWords()) {
            sentence.setWord(i++, nb.number(LexiconConstants.INPUTWORD,
                    translate((String) nb.getObjectWithId(
                            LexiconConstants.INPUTWORD, word))));
        }

        i = 0;

        for (int tag : sentence.getTags()) {
            sentence.setTag(i++, nb.number(GrammarConstants.PREDLABEL,
                    translate((String) nb.getObjectWithId(
                            GrammarConstants.PREDLABEL, tag))));
        }
    }

    private String translate(String string) {
        Map<String, String> map = getMap();

        for (String target : getMap().keySet()) {
            string = string.replace(target, map.get(target));
        }

        return string;
    }

    /**
     * Classes which extend this one have to provide the actual map.
     * 
     * @return The actual map.
     */
    protected abstract Map<String, String> getMap();

}
