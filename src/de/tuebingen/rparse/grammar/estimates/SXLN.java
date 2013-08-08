/*******************************************************************************
 * File SXLN.java
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
 * The monotonous LN estimate (Kallmeyer & Maier (2011), sec. 4.4)
 * 
 * @author wmaier
 */
public class SXLN extends Estimate {

    private static final long                 serialVersionUID = 8200609813492536536L;

    transient private PriorityQueue<SXLNItem> agenda;

    transient private SimpleInsideScoreStore  insidescores;

    private SXLNChart                         outsidescores;

    public SXLN(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
        insidescores = new SimpleInsideScoreStore(nb);
        outsidescores = new SXLNChart(sentlen, bg.getPreterminals().size()
                + bg.clByParent.keySet().size());
        agenda = new PriorityQueue<SXLNItem>();
        logger.info("Computing LN estimate...");
    }

    @Override
    public String getStats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double get(int slen, int state, BitSet vechc, int[] tags) {
        if (slen > maxlen)
            return 0.0;
        return outsidescores.getScore(state, slen, vechc.cardinality());
    }

    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        if (slen > maxlen)
            return 0.0;
        return outsidescores.getScore(state, slen, (lr - ll) + (rr - rl));
    }

    @Override
    public void process() throws GrammarException {
        SimpleInsideEstimates ie = new SimpleInsideEstimates();
        ie.doInside(bg, insidescores, maxlen);
        logger.info("computing outside probabilities...");
        doOutside();
        logger.info("finished.");
    }

    private void doOutside() throws GrammarException {

        // Axiom
        for (int slen = 1; slen <= maxlen; ++slen) {
            SXLNItem nit = new SXLNItem(bg.startSymbol, slen, slen, 0.0);
            agenda.add(nit);
            outsidescores.updateScore(nit.state, nit.slen, nit.len, nit.score);
        }

        while (!agenda.isEmpty()) {
            SXLNItem it = agenda.poll();
            // double oscore = outsidescores.updateScore(it.state, it.len, it.left, it.right, it.gaps, it.score);
            if (it.score == outsidescores.getScore(it.state, it.slen, it.len)) {
                if (agenda.size() % 1000 == 0)
                    System.err.print("\r agenda size: "
                            + (agenda.size() / 1000) + "k     ");
                if (bg.clByParent.containsKey(it.state)) {
                    for (BinaryClause bc : bg.clByParent.get(it.state)) {
                        if (bc.rc == -1) {
                            // X --> A
                            if (outsidescores.getScore(bc.lc, it.slen, it.len) > it.score
                                    + bc.score) {
                                SXLNItem nit = new SXLNItem(bc.lc, it.slen,
                                        it.len, it.score + bc.score);
                                agenda.add(nit);
                                outsidescores.updateScore(nit.state, nit.slen,
                                        nit.len, nit.score);
                            }
                        } else {
                            int lcarity = bg.getArity(bc.lc);
                            if (lcarity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + bc.lc);
                            int rcarity = bg.getArity(bc.rc);
                            if (rcarity == -1)
                                throw new GrammarException(
                                        "Couldn't determine arity of state "
                                                + bc.rc);

                            // X --> A B
                            // binary-left
                            for (int lenA = lcarity; lenA <= it.len - rcarity; ++lenA) {
                                int lenB = it.len - lenA;
                                double insidescore = insidescores.getScore(
                                        bc.rc, lenB);
                                if (outsidescores
                                        .getScore(bc.lc, it.slen, lenA) > it.score
                                        + insidescore + bc.score) {
                                    SXLNItem nit = new SXLNItem(bc.lc, it.slen,
                                            lenA, it.score + insidescore
                                                    + bc.score);
                                    agenda.add(nit);
                                    outsidescores.updateScore(nit.state,
                                            nit.slen, nit.len, nit.score);
                                }
                            }

                            // X --> B A
                            // binary-right (A is right)
                            for (int lenA = rcarity; lenA <= it.len - lcarity; ++lenA) {
                                int lenB = it.len - lenA;
                                double insidescore = insidescores.getScore(
                                        bc.lc, lenB);
                                if (outsidescores
                                        .getScore(bc.rc, it.slen, lenA) > it.score
                                        + insidescore + bc.score) {
                                    SXLNItem nit = new SXLNItem(bc.rc, it.slen,
                                            lenA, it.score + insidescore
                                                    + bc.score);
                                    agenda.add(nit);
                                    outsidescores.updateScore(nit.state,
                                            nit.slen, nit.len, nit.score);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private class SXLNChart implements Serializable {

        private static final long serialVersionUID = -5971206204723017708L;

        // state -> slen -> len -> cost
        private double[][][]      store;

        public SXLNChart(int maxlen, int statecnt) {
            store = new double[statecnt][][];
            for (int i = 0; i < statecnt; ++i) {
                store[i] = new double[maxlen + 1][];
                for (int j = 0; j <= maxlen; ++j) {
                    store[i][j] = new double[maxlen + 1];
                    Arrays.fill(store[i][j], Double.POSITIVE_INFINITY);
                }
            }
        }

        public double updateScore(int state, int slen, int len, double score) {
            score = Math.min(store[countForLabel(state)][slen][len], score);
            store[countForLabel(state)][slen][len] = score;
            return score;
        }

        public double getScore(int state, int slen, int len) {
            // System.err.println("fetching state: " + state + ", slen: " + slen + ", len: " + len);
            if (slen > maxlen)
                return 0.0;
            return store[countForLabel(state)][slen][len];
        }

    }

    private class SXLNItem implements Comparable<SXLNItem> {

        int    state;
        int    slen;
        int    len;
        double score;

        int    hc;

        public SXLNItem(int state, int slen, int len, double score) {
            this.state = state;
            this.slen = slen;
            this.len = len;
            this.score = score;

            hc = 31 + state;
            hc *= 31 + slen;
            hc *= 31 + len;
        }

        @Override
        public int compareTo(SXLNItem o) {
            if (score > o.score)
                return 1;
            if (score < o.score)
                return -1;
            return 0;
        }

        public boolean equals(Object o) {
            if (o instanceof SXLNItem) {
                SXLNItem other = (SXLNItem) o;
                return other.state == this.state && other.slen == this.slen
                        && other.len == this.len;
            }
            return false;
        }

        public int hashCode() {
            return this.hc;
        }

        public String toString() {
            return "[" + state + ":" + slen + " len" + len + ":" + score + "]";
        }

    }

}
