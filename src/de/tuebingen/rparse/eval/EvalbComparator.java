/*******************************************************************************
 * File EvalbComparator.java
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
package de.tuebingen.rparse.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeLabel;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * Implements constituency parse evaluation. This is an extension of the famous evalb program. The difference of course
 * is that this class can handle trees with discontinuities, i.e., a bracket is not defined by two indices delimiting a
 * span, but by an array of integers denoting the terminals dominated by a certain non-terminal. For 1-LCFRS/sRCG, this
 * should yield the same result as the evalb program itself, but you should check that yourself.
 * 
 * @author wmaier
 */
public class EvalbComparator extends IncrementalComparator<Tree> {

    private boolean gf;

    private int     totalMatch;

    private int     totalMatchU;

    private int     totalKey;

    private int     totalAnswer;

    protected EvalbComparator(boolean ignoreMissing, int highGapBlock,
            int lowGapBlock, boolean gf, Numberer nb) {
        super(ignoreMissing, highGapBlock, lowGapBlock);
        this.gf = gf;
        // for sentence-wise evaluation
        System.out
                .println("sent. prec.  rec.   fb1     uprec.  urec. ufb1    match umatch  gold  test");
        System.out
                .println("============================================================================");
        totalMatch = 0;
        totalMatchU = 0;
        totalKey = 0;
        totalAnswer = 0;
    }

    /*
     * Represents a single bracketing, with methods for labeled and unlabeled comparison, and fields for marking the
     * bracketing as "used" (when it matches), just as in evalb.
     */
    private class Bracket {

        String  label;

        int[]   terminals;

        boolean used;

        boolean usedU;

        Bracket(NodeLabel nlabel, int[] terminals) {
            label = nlabel.getTag();
            if (gf) {
                label += "-" + nlabel.getEdge();
            }
            this.terminals = terminals;
            used = false;
            usedU = false;
        }

        boolean labeledEquals(Bracket b) throws EvalException {
            if (b == null) {
                throw new EvalException("Bracket to be compared was null");
            }
            if (label == null || b.label == null) {
                throw new EvalException(
                        "Trying to do labeled comparison, but label was null");
            }
            if (!b.label.equals(this.label)) {
                return false;
            }
            if (!unlabeledEquals(b)) {
                return false;
            }
            return true;
        }

        boolean unlabeledEquals(Bracket b) throws EvalException {
            if (b == null) {
                throw new EvalException("Bracket to be compared was null");
            }
            if (b.terminals.length != this.terminals.length) {
                return false;
            }
            for (int i = 0; i < this.terminals.length; ++i) {
                if (b.terminals[i] != this.terminals[i]) {
                    return false;
                }
            }
            return true;
        }

    }

    @Override
    protected void compare(Tree key, Tree answer, int id) throws EvalException {
        if (key == null || answer == null) {
            throw new EvalException("Tree passed to evaluation was null: " + id);
        }

        List<Bracket> keyBrackets = new ArrayList<Bracket>();
        for (Node n : key.getRoot().getNodes()) {
            if (n.hasChildren()) {
                List<Integer> termdom = n.calcTermdom();
                Collections.sort(termdom);
                keyBrackets.add(new Bracket(n.getLabel(), Utilities
                        .asIntArray(termdom)));
            }
        }

        List<Bracket> answerBrackets = new ArrayList<Bracket>();
        for (Node n : answer.getRoot().getNodes()) {
            if (n.hasChildren()) {
                List<Integer> termdom = n.calcTermdom();
                Collections.sort(termdom);
                answerBrackets.add(new Bracket(n.getLabel(), Utilities
                        .asIntArray(termdom)));
            }
        }

        evaluateSentence(keyBrackets, answerBrackets, id);
    }

    @Override
    protected void missingAnswer(Tree key, int id) throws EvalException {
        if (key == null) {
            throw new EvalException("Tree passed to evaluation was null: " + id);
        }

        List<Bracket> keyBrackets = new ArrayList<Bracket>();
        for (Node n : key.getRoot().getNodes()) {
            if (n.hasChildren()) {
                List<Integer> termdom = n.calcTermdom();
                Collections.sort(termdom);
                keyBrackets.add(new Bracket(n.getLabel(), Utilities
                        .asIntArray(termdom)));
            }
        }

        evaluateSentence(keyBrackets, new ArrayList<Bracket>(), id);
    }

    /*
     * Evaluate two lists of bracketings against each other
     */
    private void evaluateSentence(List<Bracket> keyBrackets,
            List<Bracket> answerBrackets, int id) throws EvalException {
        int sentMatch = 0;
        int sentMatchU = 0;
        for (int i = 0; i < keyBrackets.size(); ++i) {
            Bracket keyBracket = keyBrackets.get(i);
            for (int j = 0; j < answerBrackets.size(); ++j) {
                Bracket answerBracket = answerBrackets.get(j);
                // labeled
                if (keyBracket.labeledEquals(answerBracket) && !keyBracket.used
                        && !answerBracket.used) {
                    // set used marks if brackets match
                    keyBracket.used = true;
                    answerBracket.used = true;
                    ++sentMatch;
                }
                // unlabeled
                if (keyBracket.unlabeledEquals(answerBracket)
                        && !keyBracket.usedU && !answerBracket.usedU) {
                    // set used marks if brackets match
                    keyBracket.usedU = true;
                    answerBracket.usedU = true;
                    ++sentMatchU;
                }
            }
        }
        Double sentPrec = 0.0;
        if (answerBrackets.size() > 0) {
            sentPrec = 100 * (sentMatch / new Double(answerBrackets.size()));
        }
        Double sentRec = 0.0;
        if (keyBrackets.size() > 0) {
            sentRec = 100 * (sentMatch / new Double(keyBrackets.size()));
        }
        Double sentFb1 = 0.0;
        if (sentPrec + sentRec > 0) {
            sentFb1 = 2 * sentPrec * sentRec / (sentPrec + sentRec);
        }
        Double sentUprec = 0.0;
        if (answerBrackets.size() > 0) {
            sentUprec = 100 * (sentMatchU / new Double(answerBrackets.size()));
        }
        Double sentUrec = 0.0;
        if (keyBrackets.size() > 0) {
            sentUrec = 100 * (sentMatchU / new Double(keyBrackets.size()));
        }
        Double sentUFb1 = 0.0;
        if (sentUprec + sentUrec > 0) {
            sentUFb1 = 2 * sentUprec * sentUrec / (sentUprec + sentUrec);
        }
        // print result for this sentence
        try {
            String s = String
                    .format("%4d %6.2f %6.2f %6.2f   %6.2f %6.2f %6.2f    %3d    %3d    %3d  %3d",
                            id, sentPrec, sentRec, sentFb1, sentUprec,
                            sentUrec, sentUFb1, sentMatch, sentMatchU,
                            keyBrackets.size(), answerBrackets.size());
            System.out.println(s);
        } catch (IllegalFormatException e) {
            logger.warning("Could not format results: " + e.getMessage());
        }
        totalMatch += sentMatch;
        totalMatchU += sentMatchU;
        totalKey += keyBrackets.size();
        totalAnswer += answerBrackets.size();
    }

    @Override
    protected void done(int tooLong, int missing, int highGapBlocked,
            int lowGapBlocked) {
        logger.info("Finished evaluation.");

        Double totalPrec = 0.0;
        if (totalAnswer > 0) {
            totalPrec = 100 * (totalMatch / new Double(totalAnswer));
        }
        Double totalRec = 0.0;
        if (totalKey > 0) {
            totalRec = 100 * (totalMatch / new Double(totalKey));
        }
        Double totalFb1 = 0.0;
        if (totalPrec + totalRec > 0) {
            totalFb1 = 2 * totalPrec * totalRec / (totalPrec + totalRec);
        }
        Double totalUprec = 0.0;
        if (totalAnswer > 0) {
            totalUprec = 100 * (totalMatchU / new Double(totalAnswer));
        }
        Double totalUrec = 0.0;
        if (totalKey > 0) {
            totalUrec = 100 * (totalMatchU / new Double(totalKey));
        }
        Double totalUFb1 = 0.0;
        if (totalUprec + totalUrec > 0) {
            totalUFb1 = 2 * totalUprec * totalUrec / (totalUprec + totalUrec);
        }

        System.out.println("Summary: ");
        System.out.println("=========");
        System.out.println();
        printTooLong(tooLong);
        printMissing(missing);
        printHighGapBlocked(highGapBlocked);
        printLowGapBlocked(lowGapBlocked);
        System.out.println();
        System.out.println("Total edges in key             : " + totalKey);
        System.out.println("Total edges in answer          : " + totalAnswer);
        System.out.println("Total matching edges (labeled) : " + totalMatch);
        System.out.println("Total matching edges (unlab.)  : " + totalMatchU);
        System.out.println();
        try {
            String s = "";
            s = String.format("LP  : %6.2f ", totalPrec);
            System.out.println(s);
            s = String.format("LR  : %6.2f ", totalRec);
            System.out.println(s);
            s = String.format("LF1 : %6.2f ", totalFb1);
            System.out.println(s);
            s = String.format("UP  : %6.2f ", totalUprec);
            System.out.println(s);
            s = String.format("UR  : %6.2f ", totalUrec);
            System.out.println(s);
            s = String.format("UF1 : %6.2f ", totalUFb1);
            System.out.println(s);
        } catch (IllegalFormatException e) {
            logger.warning("Could not format results: " + e.getMessage());
        }

    }

}
