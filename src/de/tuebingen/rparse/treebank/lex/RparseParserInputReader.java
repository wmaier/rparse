/*******************************************************************************
 * File RparseParserInputReader.java
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;

/**
 * Reads parser input, one word per line with POS tag, separated by a slash.
 * 
 * @author wmaier
 */
public class RparseParserInputReader
        extends
            IncrementalTreebankProcessor<ParserInput> {

    protected boolean tagged;
    private Scanner   scanner;
    private Numberer  nb;

    public RparseParserInputReader(boolean tagged, Numberer nb) {
        this.tagged = tagged;
        this.nb = nb;
    }

    @Override
    public void doInitialize(Reader reader) {
        scanner = new Scanner(reader);
    }

    @Override
    public int getLength(ParserInput sentence) {
        return sentence.size();
    }

    @Override
    public ParserInput getNextSentence() {
        List<String> sentence = new ArrayList<String>();

        // go to next non-empty line
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (!line.trim().isEmpty()) {
                sentence.add(line);
                break;
            }
        }

        // go to next empty line
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (!line.isEmpty()) {
                sentence.add(line);
            } else {
                try {
                    return parseRparseInput(sentence, nb);
                } catch (LexiconException e) {
                    System.err.println(e.getMessage());
                    return null;
                }
            }
        }
        
        if (!sentence.isEmpty()) {
        	logger.warning("Is there no newline at the end of the data?");
        }

        // no next sentence
        return null;
    }

    /**
     * Create a parser input instance from a list of slash-separated word/tag combinations.
     * 
     * @param sentence
     *            The list of word/tag combinations
     * @param nb
     *            The numberer to get the numbers for all labels.
     * @return A parser input instance.
     * @throws LexiconException
     *             If some word is not tagged.
     */
    public static SimpleParserInput parseRparseInput(List<String> sentence,
            Numberer nb) throws LexiconException {
        SimpleParserInput ret = new SimpleParserInput(sentence.size());

        for (int i = 0; i < sentence.size(); ++i) {
            String line = sentence.get(i);
            int spind = line.lastIndexOf('/');

            if (spind == -1)
                throw new LexiconException(
                        "All words must be tagged. Unknown word model not yet implemented.");
            String word = line.substring(0, spind);
            ret.setWord(i, nb.number(LexiconConstants.INPUTWORD, word));
            String tag = line.substring(spind + 1);
            Integer tagn = nb.getIntWithId(GrammarConstants.PREDLABEL, tag
                    + "1");

            if (tagn == null)
                tagn = nb.number(GrammarConstants.PREDLABEL, tag + "1");
            ret.setTag(i, tagn);
        }
        return ret;
    }

}
