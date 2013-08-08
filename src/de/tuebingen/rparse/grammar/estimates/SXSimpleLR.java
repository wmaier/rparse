/*******************************************************************************
 * File SXSimpleLR.java
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
package de.tuebingen.rparse.grammar.estimates;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.PriorityQueue;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.estimates.inside.SimpleInsideEstimates;
import de.tuebingen.rparse.grammar.estimates.inside.SimpleInsideScoreStore;
import de.tuebingen.rparse.misc.Numberer;

/**
 * SX Simple LR estimate, Kallmeyer & Maier (2011) section 4.3
 * 
 * @author wmaier
 */
public class SXSimpleLR extends Estimate {

    private static final long                serialVersionUID = 7307380916727423680L;

    transient private SimpleInsideScoreStore insidescores;

    transient private SXSimpleLRAgenda       agenda;

    private SXSimpleLRChart                  outsidescores;

    public SXSimpleLR(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
        insidescores = new SimpleInsideScoreStore(nb);
        agenda = new SXSimpleLRAgenda();
        this.outsidescores = new SXSimpleLRChart(sentlen, bg.getPreterminals()
                .size() + bg.clByParent.keySet().size());
        logger.info("Computing SX Simple LR estimate...");
    }

    @Override
    public String getStats() {
        return "";
    }

    @Override
    public double get(int slen, int state, BitSet vechc, int[] tags) {
        if (slen > maxlen)
            return 0.0;
        int len = vechc.cardinality();
        int left = vechc.nextSetBit(0);
        int gaps = vechc.length() - len - left;
        int right = slen - len - left - gaps;
        int lr = left + right;
        logger.finest("slen: " + slen + ", len: " + len + ", left: " + left
                + ", right: " + right + ", gaps: " + gaps);
        // return outsidescores.store[countForLabel(state)][len][left][right][gaps];
        return outsidescores.getScore(state, len, lr, gaps);
    }

    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        throw new UnsupportedOperationException("SXSimpleLR not supported for arity-two-parser");
    }

    @Override
    public void process() throws GrammarException {
        SimpleInsideEstimates ie = new SimpleInsideEstimates();
        ie.doInside(bg, insidescores, maxlen);
        logger.info("computing outside probabilities...");
        doOutside();
        logger.info("finished.");
    }

    // state -> len -> left -> right -> gaps -> cost
    private class SXSimpleLRChart implements Serializable {

        private static final long   serialVersionUID = -5971206204723017708L;

        private static final double INIT             = Double.POSITIVE_INFINITY;

        private double[][][][]      store;

        public SXSimpleLRChart(int maxlen, int statecnt) {
            store = new double[statecnt][][][];
            for (int i = 0; i < statecnt; ++i) {
                store[i] = new double[maxlen + 1][][];
                for (int j = 0; j <= maxlen; ++j) {
                    store[i][j] = new double[maxlen + 1][];
                    for (int k = 0; k <= maxlen - j; ++k) {
                        store[i][j][k] = new double[maxlen + 1];
                        Arrays.fill(store[i][j][k], INIT);
                    }
                }
            }
        }

        public double updateScore(int state, int length, int lr, int gaps,
                double score) {
            score = Math.min(store[countForLabel(state)][length][lr][gaps],
                    score);
            store[countForLabel(state)][length][lr][gaps] = score;
            return score;
        }

        public double getScore(int state, int length, int lr, int gaps) {
            // System.err.println("len: " + length + ", l: " + left + ", r " + right + ", gaps " + gaps);
            if (length + lr + gaps > maxlen)
                return 0.0;
            return store[countForLabel(state)][length][lr][gaps];
        }

    }

    private class SXSimpleLRItem implements Comparable<SXSimpleLRItem> {

        int    state;
        int    len;
        int    lr;
        int    gaps;
        double score;

        int    hc;

        public SXSimpleLRItem(int state, int len, int lr, int gaps, double score) {
            this.state = state;
            this.len = len;
            this.lr = lr;
            this.gaps = gaps;
            this.score = score;

            hc = 31 + state;
            hc *= 31 + len;
            hc *= 31 + lr;
            hc *= 31 + gaps;
        }

        @Override
        public int compareTo(SXSimpleLRItem o) {
            if (score > o.score)
                return 1;
            if (score < o.score)
                return -1;
            return 0;
        }

        public boolean equals(Object o) {
            if (o instanceof SXSimpleLRItem) {
                SXSimpleLRItem other = (SXSimpleLRItem) o;
                return other.state == this.state && other.len == this.len
                        && other.lr == this.lr && other.gaps == this.gaps;
            }
            return false;
        }

        public int hashCode() {
            return this.hc;
        }

        public String toString() {
            return "[" + state + ":" + len + " lr" + lr + " g" + gaps + ":"
                    + score + "]";
        }

    }

    private class SXSimpleLRAgenda extends PriorityQueue<SXSimpleLRItem> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(SXSimpleLRItem it) {
            return super.add(it);
        }

        @Override
        public SXSimpleLRItem poll() {
            return super.poll();
        }

    }

    private void doOutside() throws GrammarException {

        for (int len = 1; len <= maxlen; ++len) {
            SXSimpleLRItem nit = new SXSimpleLRItem(bg.startSymbol, len, 0, 0, 0.0);
            agenda.add(nit);
            outsidescores.updateScore(nit.state, nit.len, nit.lr, nit.gaps,
                    nit.score);
        }

        while (!agenda.isEmpty()) {
            SXSimpleLRItem it = agenda.poll();
            // double oscore = outsidescores.updateScore(it.state, it.len, it.left, it.right, it.gaps, it.score);
            if (it.score == outsidescores.getScore(it.state, it.len, it.lr,
                    it.gaps)) {
                logger.finer("\r agenda size: " + (agenda.size() / 1000)
                        + "k     ");
                int totlen = it.len + it.lr + it.gaps;
                if (bg.clByParent.containsKey(it.state)) {
                    for (BinaryClause bc : bg.clByParent.get(it.state)) {
                        if (bc.rc == -1) {
                            // X --> A
                            if (outsidescores.getScore(bc.lc, it.len, it.lr,
                                    it.gaps) > it.score + bc.score) {
                                SXSimpleLRItem nit = new SXSimpleLRItem(bc.lc, it.len, it.lr,
                                        it.gaps, it.score + bc.score);
                                agenda.add(nit);
                                outsidescores.updateScore(nit.state, nit.len,
                                        nit.lr, nit.gaps, nit.score);
                            }
                        } else {
                            // X --> A B
                            int addGaps = 0;
                            int addRight = 0;
                            boolean stopAddRight = false;
                            for (int i = bc.yf.length - 1; i >= 0; --i) {
                                for (int j = bc.yf[i].length - 1; j >= 0; --j) {
                                    if (!stopAddRight && !bc.yf[i][j])
                                        stopAddRight = true;
                                    if (bc.yf[i][j]) {
                                        if (!stopAddRight)
                                            ++addRight;
                                        else
                                            ++addGaps;
                                    }
                                }
                            }

                            int lcarity = bg.getArity(bc.lc);
                            if (lcarity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + nb.getObjectWithId(
                                                        GrammarConstants.PREDLABEL,
                                                        bc.lc));
                            int rcarity = bg.getArity(bc.rc);
                            if (rcarity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + nb.getObjectWithId(
                                                        GrammarConstants.PREDLABEL,
                                                        bc.rc));

                            for (int lenA = lcarity; lenA <= it.len - rcarity; ++lenA) {
                                int lenB = it.len - lenA;

                                // binary-left
                                double insidescore = insidescores.getScore(
                                        bc.rc, lenB);
                                for (int lr = it.lr; lr <= it.lr + lenB; ++lr) {
                                    if (addRight == 0 && !(lr == it.lr))
                                        continue;
                                    for (int ga = lcarity - 1; ga <= totlen; ++ga) {
                                        if (lenA + lr + ga == it.len + it.lr
                                                + it.gaps
                                                && ga >= addGaps) {
                                            if (outsidescores.getScore(bc.lc,
                                                    lenA, lr, ga) > it.score
                                                    + insidescore + bc.score) {
                                                SXSimpleLRItem nit = new SXSimpleLRItem(bc.lc,
                                                        lenA, lr, ga, it.score
                                                                + insidescore
                                                                + bc.score);
                                                agenda.add(nit);
                                                outsidescores.updateScore(
                                                        nit.state, nit.len,
                                                        nit.lr, nit.gaps,
                                                        nit.score);
                                            }
                                        }
                                    }
                                }
                            }

                            // X --> B A
                            // int addLeft = 0;
                            addRight = 0;
                            addGaps = 0;
                            boolean stopAddLeft = false;
                            for (int i = 0; i < bc.yf.length; ++i) {
                                for (int j = 0; j < bc.yf[i].length; ++j) {
                                    if (!stopAddLeft && bc.yf[i][j])
                                        stopAddLeft = true;
                                    if (!bc.yf[i][j]) {
                                        if (stopAddLeft) {
                                            ++addGaps;
                                        } else {
                                            // ++addLeft;
                                        }
                                    }
                                }
                            }
                            stopAddRight = false;
                            for (int i = bc.yf.length - 1; i >= 0; --i) {
                                for (int j = bc.yf[i].length - 1; j >= 0; --j) {
                                    if (!stopAddRight && bc.yf[i][j])
                                        stopAddRight = true;
                                    if (!bc.yf[i][j]) {
                                        if (!stopAddRight)
                                            ++addRight;
                                    }
                                }
                            }
                            addGaps -= addRight;

                            // binary-right (A is right)
                            for (int lenA = rcarity; lenA <= it.len - lcarity; ++lenA) {
                                int lenB = it.len - lenA;
                                double insidescore = insidescores.getScore(
                                        bc.lc, lenB);
                                for (int lr = it.lr; lr <= it.lr + lenB; ++lr) {
                                    for (int ga = rcarity - 1; ga <= totlen; ++ga) {
                                        if (lenA + lr + ga == it.len + it.lr
                                                + it.gaps
                                                && ga >= addGaps) {
                                            if (outsidescores.getScore(bc.rc,
                                                    lenA, lr, ga) > it.score
                                                    + insidescore + bc.score) {
                                                SXSimpleLRItem nit = new SXSimpleLRItem(bc.rc,
                                                        lenA, lr, ga, it.score
                                                                + insidescore
                                                                + bc.score);
                                                agenda.add(nit);
                                                outsidescores.updateScore(
                                                        nit.state, nit.len,
                                                        nit.lr, nit.gaps,
                                                        nit.score);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
