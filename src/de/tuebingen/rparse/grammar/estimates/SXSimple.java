/*******************************************************************************
 * File SXSimple.java
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
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.estimates.inside.SimpleInsideEstimates;
import de.tuebingen.rparse.grammar.estimates.inside.SimpleInsideScoreStore;
import de.tuebingen.rparse.misc.Numberer;

/**
 * SXSimple estimate from Kallmeyer & Maier (2011), sec. 4.2
 * 
 * @author wmaier
 */
public class SXSimple extends Estimate {

    private static final long                serialVersionUID = 7307380916727423680L;

    transient private SimpleInsideScoreStore insidescores;

    transient private SXSimpleAgenda         agenda;

    private SXSimpleChart                    outsidescores;

    public SXSimple(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
        insidescores = new SimpleInsideScoreStore(nb);
        agenda = new SXSimpleAgenda();
        this.outsidescores = new SXSimpleChart(sentlen, bg.getPreterminals()
                .size() + bg.clByParent.keySet().size());
        logger.info("Computing SX Simple estimate...");
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
        logger.finest("slen: " + slen + ", len: " + len + ", left: " + left
                + ", right: " + right + ", gaps: " + gaps);
        // return outsidescores.store[countForLabel(state)][len][left][right][gaps];
        return outsidescores.getScore(state, len, left, right, gaps);
    }

    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        throw new UnsupportedOperationException("SXSimple not supported for Arity-Two-Parser");
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
    private class SXSimpleChart implements Serializable {

        private static final long serialVersionUID = -5971206204723017708L;

        private double[][][][][]  store;

        public SXSimpleChart(int maxlen, int statecnt) {
            store = new double[statecnt][][][][];
            for (int i = 0; i < statecnt; ++i) {
                store[i] = new double[maxlen + 1][][][];
                for (int j = 0; j <= maxlen; ++j) {
                    store[i][j] = new double[maxlen + 1][][];
                    for (int k = 0; k <= maxlen - j; ++k) {
                        store[i][j][k] = new double[maxlen + 1][];
                        for (int l = 0; l <= maxlen - j - k; ++l) {
                            store[i][j][k][l] = new double[maxlen + 1];
                            Arrays.fill(store[i][j][k][l],
                                    Double.POSITIVE_INFINITY);
                        }
                    }
                }
            }
        }

        public double updateScore(int state, int length, int left, int right,
                int gaps, double score) {
            score = Math.min(
                    store[countForLabel(state)][length][left][right][gaps],
                    score);
            store[countForLabel(state)][length][left][right][gaps] = score;
            return score;
        }

        public double getScore(int state, int length, int left, int right,
                int gaps) {
            logger.finest("len: " + length + ", l: " + left + ", r " + right
                    + ", gaps " + gaps);
            if (length + left + right + gaps > maxlen)
                return 0.0;
            return store[countForLabel(state)][length][left][right][gaps];
        }

    }

    private class SXSimpleItem implements Comparable<SXSimpleItem> {

        int    state;
        int    len;
        int    left;
        int    right;
        int    gaps;
        double score;

        int    hc;

        public SXSimpleItem(int state, int len, int left, int right, int gaps,
                double score) {
            this.state = state;
            this.len = len;
            this.left = left;
            this.right = right;
            this.gaps = gaps;
            this.score = score;

            hc = 31 + state;
            hc *= 31 + len;
            hc *= 31 + left;
            hc *= 31 + right;
            hc *= 31 + gaps;
        }

        @Override
        public int compareTo(SXSimpleItem o) {
            if (score > o.score)
                return 1;
            if (score < o.score)
                return -1;
            return 0;
        }

        public boolean equals(Object o) {
            if (o instanceof SXSimpleItem) {
                SXSimpleItem other = (SXSimpleItem) o;
                return other.state == this.state && other.len == this.len
                        && other.left == this.left && other.right == this.right
                        && other.gaps == this.gaps;
            }
            return false;
        }

        public int hashCode() {
            return this.hc;
        }

        public String toString() {
            return "[" + state + ":" + len + " l" + left + " r" + right + " g"
                    + gaps + ":" + score + "]";
        }

    }

    private class SXSimpleAgenda extends PriorityQueue<SXSimpleItem> {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(SXSimpleItem it) {
            super.add(it);
            return true;
        }

        @Override
        public SXSimpleItem poll() {
            return super.poll();
        }

    }

    private void doOutside() throws GrammarException {

        for (int len = 1; len <= maxlen; ++len) {
            SXSimpleItem nit = new SXSimpleItem(bg.startSymbol, len, 0, 0, 0,
                    0.0);
            agenda.add(nit);
            outsidescores.updateScore(nit.state, nit.len, nit.left, nit.right,
                    nit.gaps, nit.score);
        }

        while (!agenda.isEmpty()) {
            SXSimpleItem it = agenda.poll();
            // double oscore = outsidescores.updateScore(it.state, it.len, it.left, it.right, it.gaps, it.score);
            if (it.score == outsidescores.getScore(it.state, it.len, it.left,
                    it.right, it.gaps)) {
                if (agenda.size() % 1000 == 0)
                    System.err.print("\r agenda size: "
                            + (agenda.size() / 1000) + "k     ");
                int totlen = it.len + it.left + it.right + it.gaps;
                if (bg.clByParent.containsKey(it.state)) {
                    for (BinaryClause bc : bg.clByParent.get(it.state)) {
                        if (bc.rc == -1) {
                            // X --> A
                            if (outsidescores.getScore(bc.lc, it.len, it.left,
                                    it.right, it.gaps) < it.score + bc.score) {
                                SXSimpleItem nit = new SXSimpleItem(bc.lc,
                                        it.len, it.left, it.right, it.gaps,
                                        it.score + bc.score);
                                agenda.add(nit);
                                outsidescores.updateScore(nit.state, nit.len,
                                        nit.left, nit.right, nit.gaps,
                                        nit.score);
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
                            int arity = bg.getArity(bc.lc);
                            if (arity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + bc.lc);
                            for (int lenA = arity; lenA < it.len; ++lenA) {
                                int lenB = it.len - lenA;
                                // System.err.println("len " + (lenA + lenB) + ", lena: " + lenA + ", lenb: " + lenB);
                                double insidescore = insidescores.getScore(
                                        bc.rc, lenB);
                                int la = it.left;
                                for (int ga = arity - 1; ga <= totlen; ++ga) {
                                    for (int ra = it.right + addRight; ra <= totlen
                                            - ga; ++ra) {
                                        if (lenA + la + ra + ga == it.len
                                                + it.left + it.right + it.gaps
                                                && la == it.left
                                                && ((addRight > 0 && ra >= it.right
                                                        + addRight) || (addRight == 0 && ra == it.right))
                                                && ga >= addGaps) {
                                            if (outsidescores.getScore(bc.lc,
                                                    lenA, la, ra, ga) < it.score
                                                    + insidescore + bc.score) {
                                                SXSimpleItem nit = new SXSimpleItem(
                                                        bc.lc, lenA, la, ra,
                                                        ga, it.score
                                                                + insidescore
                                                                + bc.score);
                                                agenda.add(nit);
                                                outsidescores.updateScore(
                                                        nit.state, nit.len,
                                                        nit.left, nit.right,
                                                        nit.gaps, nit.score);
                                            }
                                        }
                                    }
                                }
                            }

                            // X --> B A
                            int addLeft = 0;
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
                                            ++addLeft;
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

                            arity = bg.getArity(bc.rc);
                            if (arity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + bc.lc);
                            for (int lenA = arity; lenA < it.len; ++lenA) {
                                int lenB = it.len - lenA;
                                double insidescore = insidescores.getScore(
                                        bc.lc, lenB);
                                // int ra = it.right;
                                for (int ga = arity - 1; ga <= totlen; ++ga) {
                                    for (int la = it.left + addLeft; la <= totlen
                                            - ga; ++la) {
                                        for (int ra = it.right + addRight; ra <= totlen
                                                - la - ga; ++ra) {
                                            if (lenA + la + ra + ga == it.len
                                                    + it.left + it.right
                                                    + it.gaps
                                                    && la >= it.left + addLeft
                                                    && ((addRight > 0 && ra >= it.right
                                                            + addRight) || (addRight == 0 && ra == it.right))
                                                    && ga >= addGaps) {
                                                if (outsidescores
                                                        .getScore(bc.rc, lenA,
                                                                la, ra, ga) < it.score
                                                        + insidescore
                                                        + bc.score) {
                                                    SXSimpleItem nit = new SXSimpleItem(
                                                            bc.rc,
                                                            lenA,
                                                            la,
                                                            ra,
                                                            ga,
                                                            it.score
                                                                    + insidescore
                                                                    + bc.score);
                                                    agenda.add(nit);
                                                    outsidescores
                                                            .updateScore(
                                                                    nit.state,
                                                                    nit.len,
                                                                    nit.left,
                                                                    nit.right,
                                                                    nit.gaps,
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

}
