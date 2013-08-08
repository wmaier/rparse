/*******************************************************************************
 * File SXFull.java
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.estimates.inside.InsideEstimates;
import de.tuebingen.rparse.grammar.estimates.inside.InsideScoreStore;
import de.tuebingen.rparse.misc.Numberer;

/**
 * The adapation of Klein & Manning's SX estimate, see Kallmeyer & Maier (2011), 4.1
 * 
 * @author wmaier
 */
public class SXFull extends Estimate {

    private static final long          serialVersionUID = 358078328699490275L;

    private SXFullChart                outsidescores;

    transient private int              startstate;

    // sentlen -> arity -> blockno -> vecs
    transient private VectorStore      vecstore;

    transient private InsideScoreStore insidescores;

    public SXFull(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
        startstate = bg.startSymbol;
        vecstore = new VectorStore();
        outsidescores = new SXFullChart();
        insidescores = new InsideScoreStore(nb);
        System.err.println("SX full");
    }

    @Override
    public double get(int slen, int state, BitSet vec, int[] tags) {
        return outsidescores.getScore(state, slen, vec);
    }
    
    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        throw new UnsupportedOperationException("Use SXFullTwo instead");
    }

    @Override
    public String getStats() {
        return null;
    }

    @Override
    public void process() throws GrammarException {
        InsideEstimates il = new InsideEstimates();
        il.doInside(bg, insidescores, maxlen);
        doOutside();
    }

    private void doOutside() throws GrammarException {
        logger.info("Computing outside estimates...");

        BitSet vec = new BitSet(1);
        vec.set(0);
        vecstore.add(1, 1, 1, vec);
        vec = (BitSet) vec.clone();
        vec.set(0, false);
        vecstore.add(1, 0, 1, vec);
        for (int slen = 2; slen <= maxlen; ++slen) {
            logger.info("Computing vectors for length " + slen + "... ");
            for (int arity : vecstore.get(slen - 1).keySet()) {
                for (int blocks : vecstore.get(slen - 1).get(arity).keySet()) {
                    for (BitSet ovec : vecstore.get(slen - 1).get(arity)
                            .get(blocks)) {
                        // copy vector, append set field
                        BitSet nvec = null;
                        nvec = (BitSet) ovec.clone();
                        nvec.set(slen - 1);
                        if (ovec.get(slen - 2))
                            vecstore.add(slen, arity, blocks, nvec);
                        else
                            vecstore.add(slen, arity + 1, blocks + 1, nvec);
                        nvec = (BitSet) ovec.clone();
                        nvec.set(slen - 1, false);
                        if (ovec.get(slen - 2))
                            vecstore.add(slen, arity, blocks + 1, nvec);
                        else
                            vecstore.add(slen, arity, blocks, nvec);
                    }
                }
            }
            logger.info("finished.");
            logger.info("Computing outside estimates... ");

            for (int postag : bg.getPreterminals()) {
                BitSet v = new BitSet();
                for (int i = 0; i < slen; ++i) {
                    v.set(0, slen - 1, false);
                    v.set(i);
                    int blocks = i == 0 || i == slen - 1 ? 2 : 3;
                    outsideComputeEstimate(postag, v, slen, 1, blocks,
                            new ArrayList<Integer>());
                }
            }

            // maybe yes, maybe not.
            vecstore.get(slen - 1).clear();
            System.gc();

            logger.info("finished.");
        }

    }

    private double outsideComputeEstimate(int state, BitSet vec, int len,
            int arity, int blocks, List<Integer> unaryChain)
            throws GrammarException {

        double score = Double.NEGATIVE_INFINITY;

        int lcarity = -1;
        int rcarity = -1;
        double insidescore = 0.0;
        double outsidescore = 0.0;
        int parentblocks = -1;
        int parentarity = -1;
        BitSet parentvec = null;
        BitSet clonevec = null;

        if (state == startstate) {
            score = outsidescores.getScore(state, len, vec);
            if (blocks == 1 && vec.get(0))
                score = outsidescores.updateScore(state, len, vec, 0.0);
            else
                score = outsidescores.updateScore(state, len, vec,
                        Double.NEGATIVE_INFINITY);
        }

        else {

            clonevec = bg.clByRc.containsKey(state)
                    ? (BitSet) vec.clone()
                    : null;

            if (bg.clByLc.containsKey(state)) {
                for (BinaryClause bc : bg.clByLc.get(state)) {
                    if (bc.rc == -1) {
                        if (unaryChain == null)
                            unaryChain = new ArrayList<Integer>();
                        if (!unaryChain.contains(bc.lhs)) {
                            unaryChain.add(bc.lhs);
                            // BitSet parentvec = (BitSet) vec.clone();
                            outsidescore = outsideComputeEstimate(bc.lhs, vec,
                                    len, arity, blocks, unaryChain);
                            score = outsidescore + bc.score;
                            score = outsidescores.updateScore(state, len, vec,
                                    score);
                        }
                    } else {
                        rcarity = -1;
                        if (bg.getArity(bc.rc) != -1) {
                            rcarity = bg.getArity(bc.rc);
                        } else if (bc.rcPt) {
                            rcarity = 1;
                        } else {
                            throw new GrammarException(
                                    "SXEstimates outside: Couldn't determine the arity of state "
                                            + nb.getObjectWithId(
                                                    GrammarConstants.PREDLABEL,
                                                    bc.rc));
                        }

                        if (!vecstore.get(len).containsKey(rcarity))
                            continue;

                        for (int rcblocks : vecstore.get(len).get(rcarity)
                                .keySet()) {
                            for (BitSet rcvec : vecstore.get(len).get(rcarity)
                                    .get(rcblocks)) {
                                if (vec.intersects(rcvec))
                                    continue;
                                if (rcarity == 1 && arity == 1) {
                                    if (vec.length() == rcvec.nextSetBit(0)) {
                                        insidescore = insidescores.getScore(
                                                bc.rc,
                                                outsideBuildLengthVec(len,
                                                        rcvec, rcarity));
                                        parentvec = (BitSet) vec.clone();
                                        parentvec.xor(rcvec);
                                        parentblocks = 3;
                                        if (parentvec.get(0))
                                            parentblocks--;
                                        if (parentvec.get(len - 1))
                                            parentblocks--;
                                        parentblocks = 3;
                                        if (vec.get(0) ^ rcvec.get(0))
                                            parentblocks--;
                                        if (vec.get(len - 1)
                                                ^ rcvec.get(len - 1))
                                            parentblocks--;
                                        outsidescore = outsideComputeEstimate(
                                                bc.lhs, parentvec, len, 1,
                                                parentblocks, null);
                                        score = insidescore + outsidescore
                                                + bc.score;
                                        score = outsidescores.updateScore(
                                                state, len, vec, score);
                                    }
                                } else {
                                    parentvec = outsideComposeBitVec(len, vec,
                                            rcvec, bc.yf);
                                    if (parentvec != null) {
                                        insidescore = insidescores.getScore(
                                                bc.rc,
                                                outsideBuildLengthVec(len,
                                                        rcvec, rcarity));
                                        parentblocks = 0;
                                        parentarity = parentvec.get(0) ? 1 : 0;
                                        for (int i = 0; i < len; ++i) {
                                            if (i == 0
                                                    || parentvec.get(i) != parentvec
                                                            .get(i - 1))
                                                ++parentblocks;
                                            if (i > 0 && parentvec.get(i)
                                                    && !parentvec.get(i - 1))
                                                ++parentarity;
                                        }
                                        outsidescore = outsideComputeEstimate(
                                                bc.lhs, parentvec, len,
                                                parentarity, parentblocks, null);
                                        score = insidescore + outsidescore
                                                + bc.score;
                                        score = outsidescores.updateScore(
                                                state, len, vec, score);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bg.clByRc.containsKey(state)) {
                vec = clonevec;
                for (BinaryClause bc : bg.clByRc.get(state)) {
                    logger.fine("        clause: " + bc);

                    lcarity = -1;
                    if (bg.getArity(bc.lc) != -1) {
                        lcarity = bg.getArity(bc.lc);
                    } else if (bc.lcPt) {
                        lcarity = 1;
                    } else {
                        throw new GrammarException(
                                "SXEstimates outside: Couldn't determine the arity of state "
                                        + nb.getObjectWithId(
                                                GrammarConstants.PREDLABEL,
                                                bc.lc));
                    }

                    if (!vecstore.get(len).containsKey(lcarity))
                        continue;
                    for (int rcblocks : vecstore.get(len).get(lcarity).keySet()) {
                        for (BitSet lcvec : vecstore.get(len).get(lcarity)
                                .get(rcblocks)) {
                            if (vec.intersects(lcvec))
                                continue;
                            if (lcarity == 1 && arity == 1) {
                                if (lcvec.length() == vec.nextSetBit(0)) {
                                    insidescore = insidescores.getScore(
                                            bc.lc,
                                            outsideBuildLengthVec(len, lcvec,
                                                    lcarity));
                                    parentvec = (BitSet) vec.clone();
                                    parentvec.xor(lcvec);
                                    parentblocks = 3;
                                    if (parentvec.get(0))
                                        parentblocks--;
                                    if (parentvec.get(len - 1))
                                        parentblocks--;
                                    outsidescore = outsideComputeEstimate(
                                            bc.lhs, parentvec, len, 1,
                                            parentblocks, null);
                                    score = insidescore + outsidescore
                                            + bc.score;
                                    score = outsidescores.updateScore(state,
                                            len, vec, score);
                                }
                            } else {
                                parentvec = outsideComposeBitVec(len, lcvec,
                                        vec, bc.yf);
                                if (parentvec != null) {

                                    if (parentvec.cardinality() != lcvec
                                            .cardinality() + vec.cardinality())
                                        System.err.println("cardinality"
                                                + lcvec + " " + vec + " "
                                                + parentvec);

                                    logger.finer("              rc---> "
                                            + bc.lhs + " " + lcvec + " " + vec
                                            + " : " + parentvec);

                                    insidescore = insidescores.getScore(
                                            bc.lc,
                                            outsideBuildLengthVec(len, lcvec,
                                                    lcarity));
                                    parentblocks = 0;
                                    parentarity = parentvec.get(0) ? 1 : 0;
                                    for (int i = 0; i < len; ++i) {
                                        if (i == 0
                                                || parentvec.get(i) != parentvec
                                                        .get(i - 1))
                                            ++parentblocks;
                                        if (i > 0 && parentvec.get(i)
                                                && !parentvec.get(i - 1))
                                            ++parentarity;
                                    }
                                    outsidescore = outsideComputeEstimate(
                                            bc.lhs, parentvec, len,
                                            parentarity, parentblocks, null);
                                    score = insidescore + outsidescore
                                            + bc.score;
                                    score = outsidescores.updateScore(state,
                                            len, vec, score);
                                }
                            }
                        }
                    }
                }
            }

        }
        logger.finer("      # finished. Score: " + score);
        return score;
    }

    private static BitSet outsideComposeBitVec(int len, BitSet lit, BitSet rit,
            boolean[][] goalyf) {
        // try to compose yield functions

        int arg = 0;
        int argc = 0;

        for (int i = 0; i < len; ++i) {
            if (lit.get(i) && !rit.get(i)) {
                if (arg == goalyf.length || argc == goalyf[arg].length
                        || goalyf[arg][argc])
                    return null;
                else if (i + 1 < len && !lit.get(i + 1))
                    argc++;
            } else if (!lit.get(i) && rit.get(i)) {
                if (arg == goalyf.length || argc == goalyf[arg].length
                        || !goalyf[arg][argc])
                    return null;
                else if (i + 1 < len && !rit.get(i + 1))
                    argc++;
            } else if (lit.get(i) && rit.get(i)) {
                return null;
            } else if (!lit.get(i) && !rit.get(i)) {
                if (i > 0 && (lit.get(i - 1) ^ rit.get(i - 1))) {
                    arg++;
                    argc = 0;
                }
            }
        }

        BitSet yp = (BitSet) lit.clone();
        yp.xor(rit);

        return yp;

    }

    private static BitSet outsideBuildLengthVec(int len, BitSet rcvec,
            int rcarity) {
        BitSet ret = new BitSet();
        int retpos = 0;
        int arl = 0;
        for (int i = 0; i < len; ++i) {
            if (rcvec.get(i)) {
                ++arl;
            } else if (i > 0 && !rcvec.get(i) && rcvec.get(i - 1)) {
                ret.set(retpos, retpos + arl);
                ret.set(retpos + arl + 1, false);
                retpos += arl + 1;
                arl = 0;
            }
        }
        if (arl > 0) {
            ret.set(retpos, retpos + arl);
            ret.set(retpos + arl + 1, false);
        }
        return ret;
    }

    // len -> state -> vec -> score
    private class SXFullChart
            extends
                HashMap<Integer, Map<Integer, Map<BitSet, Double>>> {

        private static final long serialVersionUID = 1L;

        public double updateScore(int state, int len, BitSet vec, double score) {
            if (!super.containsKey(len))
                super.put(len, new HashMap<Integer, Map<BitSet, Double>>());
            if (!super.get(len).containsKey(state))
                super.get(len).put(state, new HashMap<BitSet, Double>());
            if (super.get(len).get(state).containsKey(vec))
                score = Math.max(score, super.get(len).get(state).get(vec));
            super.get(len).get(state).put(vec, score);
            return score;
        }

        public double getScore(int state, int len, BitSet vec) {
            if (super.containsKey(len) && super.get(len).containsKey(state)
                    && super.get(len).get(state).containsKey(vec))
                return super.get(len).get(state).get(vec);
            else
                return Double.NEGATIVE_INFINITY;
        }

        public String toString() {
            String ret = "";
            for (Integer len : super.keySet()) {
                ret += "l" + len + "\n";
                for (Integer state : super.get(len).keySet()) {
                    ret += "s"
                            + nb.getObjectWithId(GrammarConstants.PREDLABEL,
                                    state) + "\n";
                    for (BitSet vec : super.get(len).get(state).keySet()) {
                        String p = "";
                        for (int i = 0; i < len; ++i)
                            p += vec.get(i) ? "1" : "0";
                        ret += "[" + p + "]:"
                                + super.get(len).get(state).get(vec);
                        ret += "\n";
                    }
                    ret += "\n";
                }
            }
            return ret;
        }

        /*
         * public void print(Integer state, int len) { if (super.containsKey(len)) { if
         * (super.get(len).containsKey(state)) { for (BitSet vec : super.get(len).get(state).keySet()) { String p =
         * String.valueOf(vec); for (int i = 0; i < len; ++i) p += vec.get(i) ? "1" : "0"; System.err.println("[" + p +
         * "]:" + super.get(len).get(state).get(vec)); } } } }
         */
    }

    // slen -> arity -> blockno -> vecs
    private class VectorStore
            extends
                HashMap<Integer, Map<Integer, Map<Integer, Set<BitSet>>>> {

        private static final long serialVersionUID = 1L;

        private int               maxArity         = 1;

        public VectorStore() {
        }

        private void init(int slen, int arity) {
            if (!(super.containsKey(slen)))
                super.put(slen,
                        new HashMap<Integer, Map<Integer, Set<BitSet>>>());
            if (!(super.get(slen).containsKey(arity)))
                super.get(slen).put(arity, new HashMap<Integer, Set<BitSet>>());
        }

        private void init(int slen, int arity, int blocks) {
            init(slen, arity);
            if (!(super.get(slen).get(arity).containsKey(blocks)))
                super.get(slen).get(arity).put(blocks, new HashSet<BitSet>());
        }

        public void add(int slen, int arity, int blocks, BitSet vec) {
            init(slen, arity, blocks);
            maxArity = Math.max(arity, maxArity);
            super.get(slen).get(arity).get(blocks).add(vec);
        }

        public String toString() {
            String ret = "";
            for (int slen : super.keySet()) {
                ret += "{l" + slen + "=";
                for (int arity : super.get(slen).keySet()) {
                    if (!super.get(slen).get(arity).isEmpty()) {
                        ret += "{a" + arity + "=";
                        for (Integer blocks : super.get(slen).get(arity)
                                .keySet()) {
                            if (!super.get(slen).get(arity).get(blocks)
                                    .isEmpty()) {
                                ret += "{b" + blocks + "={";
                                for (BitSet vec : super.get(slen).get(arity)
                                        .get(blocks)) {
                                    ret += "[";
                                    for (int i = 0; i < slen; ++i)
                                        ret += vec.get(i) ? "1" : "0";
                                    ret += "]";
                                }
                                ret += "}";
                            }
                        }
                        ret += "}";
                    }
                }
                ret += "}";
            }
            return ret;
        }

        /*
         * public void enumerate() { String ret = ""; for (Integer slen : super.keySet()) { ret += "l" + slen + "\n";
         * List<Integer> arlist = new ArrayList<Integer>(); for (Integer ar : super.get(slen).keySet()) arlist.add(ar);
         * Collections.sort(arlist); for (int ar : arlist) { ret += "  a" + ar + "\n"; for (int blocks :
         * super.get(slen).get(ar).keySet()) { ret += "  b" + blocks + "\n"; for (BitSet vec :
         * super.get(slen).get(ar).get(blocks)) { ret += "    ["; for (int i = 0; i < slen; ++i) ret += vec.get(i) ? "1"
         * : "0"; ret += "]\n"; } } } } System.err.println(ret); }
         */

    }

}
