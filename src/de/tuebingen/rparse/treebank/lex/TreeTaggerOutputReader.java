/*******************************************************************************
 * File TreeTaggerOutputReader.java
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;

/**
 * Connector which allows to use the output from the TreeTagger as parser input, instead of using the gold POS tagging.
 * 
 * @author ke
 */
public class TreeTaggerOutputReader
        extends
            IncrementalTreebankProcessor<ParserInput> {

    private Scanner  scanner;

    private Numberer nb;

    /**
     * Construct a new tree tagger output reader
     * @param nb A numberer is needed to know the numbers for the labels.
     */
    public TreeTaggerOutputReader(Numberer nb) {
        this.nb = nb;
    }

    @Override
    protected void doInitialize(Reader reader) {
        scanner = new Scanner(reader);
    }

    @Override
    protected ParserInput getNextSentence() {
        String line;

        do {
            if (!scanner.hasNextLine()) {
                return null;
            }

            line = scanner.nextLine().trim();
        } while ("".equals(line));

        if (!"<s>".equals(line)) {
            // *slaps himself* an unchecked exception is still better than
            // failing silently, though
            throw new RuntimeException(
                    "Sentence must start with <s> on a line by itself.");
        }

        List<String> words = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();

        while (true) {
            if (!scanner.hasNextLine()) {
                // (see above)
                throw new RuntimeException(
                        "Sentence must end with </s> on a line by itself.");
            }

            line = scanner.nextLine().trim();

            if ("</s>".equals(line)) {
                break;
            }

            String[] fields = line.split("\\t", 3);

            if (fields.length < 2) {
                // (see above)
                throw new RuntimeException("Tab-separated token, tag expected.");
            }

            words.add(fields[0]);
            tags.add(fields[1]);
        }

        SimpleParserInput result = new SimpleParserInput(words.size());
        int i = 0;

        for (String word : words) {
            result.setWord(i, nb.number(LexiconConstants.INPUTWORD, word));
            result.setTag(i,
                    nb.number(GrammarConstants.PREDLABEL, tags.get(i) + "1"));
            i++;
        }

        return result;
    }

    @Override
    public int getLength(ParserInput sentence) {
        return sentence.size();
    }

}
