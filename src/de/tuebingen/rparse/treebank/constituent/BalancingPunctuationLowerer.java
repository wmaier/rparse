/*******************************************************************************
 * File BalancingPunctuationLowerer.java
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Punctuation lowerer which tries to attach punctuation to the same node if it comes in pairs (parentheses and such).
 * 
 * @author ke
 */
public class BalancingPunctuationLowerer extends ProcessingTask<Tree> {

    private static final Map<String, String> MATCH_MAP;

    static {
        MATCH_MAP = new HashMap<String, String>(2);
        MATCH_MAP.put("\"", "\"");
        MATCH_MAP.put("[", "]");
        MATCH_MAP.put("-", "-");
    }

    @Override
    public void done() throws TreebankException {
        // do nothing
    }

    @Override
    public void processSentence(Tree sentence) throws TreebankException {
        boolean exportNumbering = sentence.hasExportNumbering();
        Map<String, Node> leftTerminalByWord = new HashMap<String, Node>(2);

        terminals : for (Node terminal : sentence.getTerminals()) {
            String word = terminal.getLabel().getWord();

            // Try to find a left match for the terminal:
            for (String leftWord : leftTerminalByWord.keySet()) {
                if (word.equals(MATCH_MAP.get(leftWord))) {
                    Node leftTerminal = leftTerminalByWord.get(leftWord);
                    Node parent = leftTerminal.getPa();
                    List<Integer> termdom = parent.calcTermdom();

                    if (Collections.max(termdom).equals(
                            terminal.getLabel().getNum() - 1)) {
                        // move right sign to the left
                        terminal.moveAsChild(parent);
                    } else {
                        parent = terminal.getPa();
                        termdom = parent.calcTermdom();

                        if (Collections.min(termdom).equals(
                                leftTerminal.getLabel().getNum() + 1)) {
                            // move left sign to the right
                            leftTerminal.moveAsChild(parent, 0);
                        }
                    }

                    leftTerminalByWord.remove(word);
                    continue terminals;
                }
            }

            // If there is no left match, memorize this terminal as left match:
            if (MATCH_MAP.containsKey(terminal.getLabel().getWord())) {
                leftTerminalByWord.put(word, terminal);
            }
        }

        if (exportNumbering) {
            sentence.calcExportNumbering();
        }
    }

}
