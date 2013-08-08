/*******************************************************************************
 * File DepComparator.java
 * 
 * Authors:
 *    Wolfgang Maier 
 *    
 * Copyright:
 *    Wolfgang Maier, 2012
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
import java.util.IllegalFormatException;
import java.util.List;

import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNode;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Evaluation of dependency parses. All metrics should stay here, we do not want different classes for different
 * metrics. This is checked against the evaluation module of the MST parser, but you should check that again for
 * yourself.
 * 
 * @author wmaier
 */
public class DepComparator
        extends
            IncrementalComparator<DependencyForest<DependencyForestNodeLabel, String>> {

    private int cc;

    private int ccU;

    private int totalMatch;

    private int totalMatchU;

    private int totalKey;

    private int totalAnswer;

    private int totalSentences;

    /**
     * Gets a new comparator for dependencies.
     * 
     * @param highGapBlock
     * @param lowGapBlock
     */
    protected DepComparator(int highGapBlock, int lowGapBlock) {
        super(true, highGapBlock, lowGapBlock);
        cc = 0;
        ccU = 0;
        totalMatch = 0;
        totalMatchU = 0;
        totalKey = 0;
        totalAnswer = 0;
        totalSentences = 0;
    }

    @Override
    protected void compare(
            DependencyForest<DependencyForestNodeLabel, String> key,
            DependencyForest<DependencyForestNodeLabel, String> answer, int id)
            throws EvalException {
        totalSentences++;
        List<DepBracket> keyBrackets = new ArrayList<DepBracket>();
        for (DependencyForestNode<DependencyForestNodeLabel, String> n : key
                .nodes()) {
            int headId = n.getHead() == null ? 0 : n.getHead().getID();
            String relation = n.getRelation();
            if (relation == null) {
                if (headId != 0) {
                    throw new EvalException(
                            "Relation label null, but head not 0");
                }
                relation = "top";
            }
            keyBrackets.add(new DepBracket(n.getID(), headId, relation));
        }
        List<DepBracket> answerBrackets = new ArrayList<DepBracket>();
        for (DependencyForestNode<DependencyForestNodeLabel, String> n : answer
                .nodes()) {
            int headId = n.getHead() == null ? 0 : n.getHead().getID();
            String relation = n.getRelation();
            if (relation == null) {
                if (headId != 0) {
                    throw new EvalException(
                            "Relation label null, but head not 0");
                }
                relation = "top";
            }
            answerBrackets.add(new DepBracket(n.getID(), headId, relation));
        }
        int sentMatch = 0;
        int sentMatchU = 0;
        for (int i = 0; i < keyBrackets.size(); ++i) {
            DepBracket keyBracket = keyBrackets.get(i);
            for (int j = 0; j < answerBrackets.size(); ++j) {
                DepBracket answerBracket = answerBrackets.get(j);
                if (keyBracket.labeledEquals(answerBracket) && !keyBracket.used
                        && !answerBracket.used) {
                    keyBracket.used = true;
                    answerBracket.used = true;
                    ++sentMatch;
                }
                if (keyBracket.unlabeledEquals(answerBracket)
                        && !keyBracket.usedU && !answerBracket.usedU) {
                    keyBracket.usedU = true;
                    answerBracket.usedU = true;
                    ++sentMatchU;
                }
            }
        }

        if (keyBrackets.size() == answerBrackets.size()
                && sentMatch == keyBrackets.size()) {
            cc++;
        }
        if (keyBrackets.size() == answerBrackets.size()
                && sentMatchU == keyBrackets.size()) {
            ccU++;
        }
        totalMatch += sentMatch;
        totalMatchU += sentMatchU;
        totalKey += keyBrackets.size();
        totalAnswer += answerBrackets.size();
    }

    @Override
    protected void missingAnswer(
            DependencyForest<DependencyForestNodeLabel, String> key, int id)
            throws EvalException {
        throw new EvalException(
                "Cannot handle missing answers in dependency evaluation, use ignore option");
    }

    @Override
    protected void done(int tooLong, int missing, int highGapBlocked,
            int lowGapBlocked) {
        if (missing > 0) {
            logger.severe("Missing sentences was > 0 for dependencies, even though this is not supported");
        }
        double uacc = 0.0;
        if (totalAnswer > 0) {
            uacc = 100 * (totalMatchU / new Double(totalAnswer));
        }
        double lacc = 0.0;
        if (totalKey > 0) {
            lacc = 100 * (totalMatch / new Double(totalKey));
        }
        double ucc = 0.0;
        if (totalSentences > 0) {
            ucc = 100 * (ccU / new Double(totalSentences));
        }
        double lcc = 0.0;
        if (totalSentences > 0) {
            lcc = 100 * (cc / new Double(totalSentences));
        }

        System.out.println("Dependency Evaluation Summary: ");
        System.out.println("===============================");
        System.out.println();
        printTooLong(tooLong);
        printMissing(missing);
        printHighGapBlocked(highGapBlocked);
        printLowGapBlocked(lowGapBlocked);
        System.out.println();
        System.out.println("Total brackets in key             : " + totalKey);
        System.out
                .println("Total brackets in answer          : " + totalAnswer);
        System.out.println("Total matching brackets (labeled) : " + totalMatch);
        System.out
                .println("Total matching brackets (unlab.)  : " + totalMatchU);
        System.out.println();
        try {
            String s = "";
            s = String.format("Unlab. accuracy      : %6.2f ", uacc);
            System.out.println(s);
            s = String.format("Lab. accuracy        : %6.2f ", lacc);
            System.out.println(s);
            s = String.format("Unlab. comp. correct : %6.2f ", ucc);
            System.out.println(s);
            s = String.format("Lab. comp. correct   : %6.2f ", lcc);
            System.out.println(s);
        } catch (IllegalFormatException e) {
            logger.warning("Could not format results: " + e.getMessage());
        }
    }

    /*
     * Represents a single dependency edge, with methods for labeled and unlabeled comparison, just as in the EVALB
     * constituency comparator.
     */
    private class DepBracket {

        public int    self;
        public int    head;
        public String rel;
        boolean       used;
        boolean       usedU;

        public DepBracket(int self, int head, String rel) {
            this.self = self;
            this.head = head;
            this.rel = rel;
            used = false;
            usedU = false;
        }

        public String toString() {
            return "(" + head + " " + self + " " + rel + ")";
        }

        public boolean unlabeledEquals(DepBracket b) throws EvalException {
            if (b == null) {
                throw new EvalException("Bracket to be compared was null");
            }
            if (head != b.head) {
                return false;
            }
            if (self != b.self) {
                return false;
            }
            return true;
        }

        public boolean labeledEquals(DepBracket b) throws EvalException {
            if (b == null) {
                throw new EvalException("Bracket to be compared was null");
            }
            if (rel == null || b.rel == null) {
                throw new EvalException(
                        "Trying to compare labeled brackets, but relation was null");
            }
            if (!rel.equals(b.rel)) {
                return false;
            }
            if (!unlabeledEquals(b)) {
                return false;
            }
            return true;
        }

    }

}
