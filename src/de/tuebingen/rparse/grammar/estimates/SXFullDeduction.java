/*******************************************************************************
 * File SXFullDeduction.java
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

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.estimates.inside.InsideEstimates;
import de.tuebingen.rparse.grammar.estimates.inside.InsideScoreStore;
import de.tuebingen.rparse.misc.Numberer;

/**
 * The same as SXFull, this version uses deduction and tabulation. Explained in Kallmeyer & Maier (2011), Sec. 4.1
 * 
 * @author wmaier
 */
public class SXFullDeduction extends Estimate {

    private static final long          serialVersionUID = -1449621475072228904L;

    private SXFullChart                chart;

    // label --> vec --> score
    transient private InsideScoreStore insidescores;

    transient private SXFullAgenda     agenda;

    public SXFullDeduction(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
        insidescores = new InsideScoreStore(nb);
        System.err.println("SX full deduction");
    }

    @Override
    public String getStats() {
        return "";
    }

    @Override
    public double get(int slen, int state, BitSet vec, int[] tags) {
        if (slen > maxlen)
            return 0.0;
        if (state == bg.startSymbol && vec.cardinality() == vec.length()
                && vec.length() == slen) {
            return 0.0;
        } else {
            BitSet lookup = new BitSet();
            int start = vec.nextSetBit(0);
            for (int i = start; i < slen; ++i)
                lookup.set(i - start, vec.get(i));
            BitSet full = new BitSet();
            full.set(0, slen);
            return chart.getScore(false, slen, bg.startSymbol, state, full,
                    lookup, start);

        }
    }

    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        throw new UnsupportedOperationException("Use SXFullTwo instead");
    }

    @Override
    @SuppressWarnings("unused")
    public void process() throws GrammarException {

        InsideEstimates il = new InsideEstimates();
        il.doInside(bg, insidescores, maxlen);

        agenda = new SXFullAgenda();
        chart = new SXFullChart();

        for (int len = 1; len < maxlen; ++len) {
            System.err.println("len: " + len);
            if (len == 1) {
                SXFullItem it = null;
                BitSet vec = new BitSet();
                vec.set(0);
                for (int postag : bg.getPreterminals()) {
                    // axiom
                    it = new SXFullItem(postag, postag, vec, vec, 0, true, 0.0);
                    chart.add(true, 1, postag, it);
                    agenda.offer(it);
                }
            } else {
                BitSet shifted = new BitSet();
                for (int leftlen = 1; leftlen < len; ++leftlen) {
                    if (!chart.get(true).containsKey(leftlen))
                        continue;
                    for (int lefttag : chart.get(true).get(leftlen).keySet()) {
                        for (int lfoot : chart.get(true).get(leftlen)
                                .get(lefttag).keySet()) {
                            for (BitSet lrootv : chart.get(true).get(leftlen)
                                    .get(lefttag).get(lfoot).keySet()) {
                                for (BitSet lfootv : chart.get(true)
                                        .get(leftlen).get(lefttag).get(lfoot)
                                        .get(lrootv).keySet()) {

                                    if (bg.clByLc.containsKey(lefttag)) {
                                        for (BinaryClause bc : bg.clByLc
                                                .get(lefttag)) {
                                            if (bc.rc == -1)
                                                continue;
                                            if (chart.get(true).containsKey(
                                                    len - leftlen)
                                                    && chart.get(true)
                                                            .get(len - leftlen)
                                                            .containsKey(bc.rc)) {
                                                int righttag = bc.rc;
                                                for (int rfoot : chart
                                                        .get(true)
                                                        .get(len - leftlen)
                                                        .get(righttag).keySet()) {
                                                    for (BitSet rrootv : chart
                                                            .get(true)
                                                            .get(len - leftlen)
                                                            .get(righttag)
                                                            .get(rfoot)
                                                            .keySet()) {
                                                        for (BitSet rfootv : chart
                                                                .get(true)
                                                                .get(len
                                                                        - leftlen)
                                                                .get(righttag)
                                                                .get(rfoot)
                                                                .get(rrootv)
                                                                .keySet()) {
                                                            for (int rightshift = 1; rightshift < maxlen
                                                                    - rrootv.length(); ++rightshift) {
                                                                shifted.set(
                                                                        0,
                                                                        rightshift,
                                                                        false);
                                                                for (int copy = 0; copy < rrootv
                                                                        .length(); ++copy)
                                                                    shifted.set(
                                                                            copy
                                                                                    + rightshift,
                                                                            rrootv.get(copy));
                                                                BitSet comp = compareBitVectors(
                                                                        Math.max(
                                                                                lrootv.length(),
                                                                                shifted.length()),
                                                                        lrootv,
                                                                        shifted,
                                                                        bc.yf);
                                                                if (comp != null) {

                                                                    // binary-right
                                                                    BitSet avec = null;
                                                                    avec = buildLengthVec(lrootv);
                                                                    double ascore = insidescores
                                                                            .getScore(
                                                                                    lefttag,
                                                                                    avec);
                                                                    SXFullItem parentit = new SXFullItem(
                                                                            bc.lhs,
                                                                            righttag,
                                                                            comp,
                                                                            rrootv,
                                                                            rightshift,
                                                                            false,
                                                                            ascore
                                                                                    + bc.score);
                                                                    agenda.add(parentit);
                                                                    chart.add(
                                                                            false,
                                                                            parentit.rootv
                                                                                    .cardinality(),
                                                                            parentit.root,
                                                                            parentit);

                                                                    // binary-left
                                                                    BitSet bvec = null;
                                                                    bvec = buildLengthVec(rrootv);
                                                                    double bscore = insidescores
                                                                            .getScore(
                                                                                    righttag,
                                                                                    bvec);
                                                                    parentit = new SXFullItem(
                                                                            bc.lhs,
                                                                            lefttag,
                                                                            comp,
                                                                            lrootv,
                                                                            0,
                                                                            false,
                                                                            bscore
                                                                                    + bc.score);
                                                                    agenda.add(parentit);
                                                                    chart.add(
                                                                            false,
                                                                            parentit.rootv
                                                                                    .cardinality(),
                                                                            parentit.root,
                                                                            parentit);
                                                                }
                                                                shifted.clear();
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

                    // chart: labelequal --> |rootv| --> root --> {items}
                    while (!agenda.isEmpty()) {
                        SXFullItem it = agenda.poll();
                        if (it.eq) {
                            // A A \rho \rho
                            if (bg.clByLc.containsKey(it.root)) {
                                for (BinaryClause bc : bg.clByLc.get(it.root)) {
                                    if (bc.rc == -1) {
                                        // unary
                                        SXFullItem nit = new SXFullItem(bc.lhs,
                                                it.root, it.rootv, it.footv, 0,
                                                false, bc.score);
                                        if (chart.add(false,
                                                it.rootv.cardinality(),
                                                it.root, nit))
                                            agenda.add(nit);
                                    }
                                }
                            }
                        } else {
                            // not A B \rho_A \rho_B
                            SXFullItem parentitshort = new SXFullItem(it.root,
                                    it.root, it.rootv, it.rootv, 0, true, 0.0);
                            if (chart.add(true,
                                    parentitshort.rootv.cardinality(),
                                    parentitshort.root, parentitshort))
                                agenda.add(parentitshort);

                            // labelequal --> |rootv| --> root --> foot --> rootv --> footv --> score
                            if (chart.get(false).containsKey(
                                    it.footv.cardinality())) {
                                if (chart.get(false)
                                        .get(it.footv.cardinality())
                                        .containsKey(it.foot)) {
                                    for (int bfoot : chart.get(false)
                                            .get(it.footv.cardinality())
                                            .get(it.foot).keySet()) {
                                        for (BitSet brootv : chart.get(false)
                                                .get(it.footv.cardinality())
                                                .get(it.foot).get(bfoot)
                                                .keySet()) {
                                            for (BitSet bfootv : chart
                                                    .get(false)
                                                    .get(it.footv.cardinality())
                                                    .get(it.foot).get(bfoot)
                                                    .get(brootv).keySet()) {
                                                if (it.footv.equals(brootv)) {
                                                    for (int bshift : chart
                                                            .get(false)
                                                            .get(it.footv
                                                                    .cardinality())
                                                            .get(it.foot)
                                                            .get(bfoot)
                                                            .get(brootv)
                                                            .get(bfootv)
                                                            .keySet()) {
                                                        double bscore = chart
                                                                .get(false)
                                                                .get(it.footv
                                                                        .cardinality())
                                                                .get(it.foot)
                                                                .get(bfoot)
                                                                .get(brootv)
                                                                .get(bfootv)
                                                                .get(bshift);
                                                        SXFullItem nit = new SXFullItem(
                                                                it.root,
                                                                bfoot,
                                                                it.rootv,
                                                                bfootv,
                                                                it.shift
                                                                        + bshift,
                                                                false,
                                                                it.score
                                                                        + bscore);
                                                        if (chart
                                                                .add(false,
                                                                        nit.rootv
                                                                                .cardinality(),
                                                                        nit.root,
                                                                        nit))
                                                            agenda.add(nit);
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

        System.err.println("ready");

    }

    private class SXFullItem {

        int     root;
        int     foot;
        BitSet  rootv;
        BitSet  footv;
        int     shift;
        boolean eq;
        double  score;

        public SXFullItem(int root, int foot, BitSet rootv, BitSet footv,
                int shift, boolean eq, double score) {
            this.root = root;
            this.rootv = rootv;
            this.foot = foot;
            this.footv = footv;
            this.score = score;
            this.shift = shift;
            this.eq = eq;
        }

        public String toString() {
            return "[" + this.root + "(" + rootv + "):" + this.foot + "("
                    + footv + "):" + this.score + "]";
        }

    }

    // labelequal --> |rootv| --> root --> foot --> rootv --> footv --> int --> score
    private class SXFullChart
            extends
                HashMap<Boolean, Map<Integer, Map<Integer, Map<Integer, Map<BitSet, Map<BitSet, Map<Integer, Double>>>>>>> {

        private static final long serialVersionUID = 4861605681033769287L;

        public SXFullChart() {
            super.put(
                    true,
                    new HashMap<Integer, Map<Integer, Map<Integer, Map<BitSet, Map<BitSet, Map<Integer, Double>>>>>>());
            super.put(
                    false,
                    new HashMap<Integer, Map<Integer, Map<Integer, Map<BitSet, Map<BitSet, Map<Integer, Double>>>>>>());
        }

        public boolean hasScore(boolean eq, int rootvlen, int root, int foot,
                BitSet rootv, BitSet footv, int shift) {
            return (super.get(eq).containsKey(rootvlen)
                    && super.get(eq).get(rootvlen).containsKey(root)
                    && super.get(eq).get(rootvlen).get(root).containsKey(foot)
                    && super.get(eq).get(rootvlen).get(root).get(foot)
                            .containsKey(rootv)
                    && super.get(eq).get(rootvlen).get(root).get(foot)
                            .get(rootv).containsKey(footv) && super.get(eq)
                    .get(rootvlen).get(root).get(foot).get(rootv).get(footv)
                    .containsKey(shift));
        }

        public void setScore(boolean eq, int rootvlen, int root, int foot,
                BitSet rootv, BitSet footv, int shift, double score) {
            if (!super.get(eq).containsKey(rootvlen))
                super.get(eq)
                        .put(rootvlen,
                                new HashMap<Integer, Map<Integer, Map<BitSet, Map<BitSet, Map<Integer, Double>>>>>());
            if (!super.get(eq).get(rootvlen).containsKey(root))
                super.get(eq)
                        .get(rootvlen)
                        .put(root,
                                new HashMap<Integer, Map<BitSet, Map<BitSet, Map<Integer, Double>>>>());
            if (!super.get(eq).get(rootvlen).get(root).containsKey(foot))
                super.get(eq)
                        .get(rootvlen)
                        .get(root)
                        .put(foot,
                                new HashMap<BitSet, Map<BitSet, Map<Integer, Double>>>());
            if (!super.get(eq).get(rootvlen).get(root).get(foot)
                    .containsKey(rootv))
                super.get(eq)
                        .get(rootvlen)
                        .get(root)
                        .get(foot)
                        .put(rootv, new HashMap<BitSet, Map<Integer, Double>>());
            if (!super.get(eq).get(rootvlen).get(root).get(foot).get(rootv)
                    .containsKey(footv))
                super.get(eq).get(rootvlen).get(root).get(foot).get(rootv)
                        .put(footv, new HashMap<Integer, Double>());
            super.get(eq).get(rootvlen).get(root).get(foot).get(rootv)
                    .get(footv).put(shift, score);
        }

        public double getScore(boolean eq, int rootvlen, int root, int foot,
                BitSet rootv, BitSet footv, int shift) {
            if (hasScore(eq, rootvlen, root, foot, rootv, footv, shift))
                return super.get(eq).get(rootvlen).get(root).get(foot)
                        .get(rootv).get(footv).get(shift);
            else
                return Double.NEGATIVE_INFINITY;
        }

        public boolean add(boolean eq, int rootvlen, int root, SXFullItem it) {
            if (hasScore(it.eq, rootvlen, root, it.foot, it.rootv, it.footv,
                    it.shift)) {
                double oldscore = getScore(it.eq, rootvlen, root, it.foot,
                        it.rootv, it.footv, it.shift);
                double newscore = Math.max(it.score, oldscore);
                setScore(it.eq, rootvlen, it.root, it.foot, it.rootv, it.footv,
                        it.shift, newscore);
                return oldscore != newscore;
            } else {
                setScore(it.eq, rootvlen, it.root, it.foot, it.rootv, it.footv,
                        it.shift, it.score);
                return true;
            }
        }

    }

    private class SXFullAgenda extends LinkedList<SXFullItem> {

        private static final long                                            serialVersionUID = 1L;

        // Items: int root, int foot, BitSet rootv, BitSet footv, boolean eq,
        HashMap<Integer, Map<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>> cacheTrue;
        HashMap<Integer, Map<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>> cacheFalse;

        public SXFullAgenda() {
            cacheTrue = new HashMap<Integer, Map<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>>();
            cacheFalse = new HashMap<Integer, Map<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>>();
        }

        @Override
        public boolean add(SXFullItem it) {
            if (it.eq) {
                if (cacheTrue.containsKey(it.root)
                        && cacheTrue.get(it.root).containsKey(it.foot)
                        && cacheTrue.get(it.root).get(it.foot)
                                .containsKey(it.rootv)
                        && cacheTrue.get(it.root).get(it.foot).get(it.rootv)
                                .containsKey(it.footv)) {
                    SXFullItem oit = cacheTrue.get(it.root).get(it.foot)
                            .get(it.rootv).get(it.footv);
                    double max = Math.max(it.score, oit.score);
                    boolean change = max > oit.score;
                    oit.score = max;
                    return change;
                } else {
                    if (!cacheTrue.containsKey(it.root))
                        cacheTrue
                                .put(it.root,
                                        new HashMap<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>());
                    if (!cacheTrue.get(it.root).containsKey(it.foot))
                        cacheTrue.get(it.root).put(it.foot,
                                new HashMap<BitSet, Map<BitSet, SXFullItem>>());
                    if (!cacheTrue.get(it.root).get(it.foot)
                            .containsKey(it.rootv))
                        cacheTrue
                                .get(it.root)
                                .get(it.foot)
                                .put(it.rootv,
                                        new HashMap<BitSet, SXFullItem>());
                    cacheTrue.get(it.root).get(it.foot).get(it.rootv)
                            .put(it.footv, it);
                    super.add(it);
                    return true;
                }
            } else {
                if (cacheFalse.containsKey(it.root)
                        && cacheFalse.get(it.root).containsKey(it.foot)
                        && cacheFalse.get(it.root).get(it.foot)
                                .containsKey(it.rootv)
                        && cacheFalse.get(it.root).get(it.foot).get(it.rootv)
                                .containsKey(it.footv)) {
                    SXFullItem oit = cacheFalse.get(it.root).get(it.foot)
                            .get(it.rootv).get(it.footv);
                    double max = Math.max(it.score, oit.score);
                    boolean change = max > oit.score;
                    oit.score = max;
                    return change;
                } else {
                    if (!cacheFalse.containsKey(it.root))
                        cacheFalse
                                .put(it.root,
                                        new HashMap<Integer, Map<BitSet, Map<BitSet, SXFullItem>>>());
                    if (!cacheFalse.get(it.root).containsKey(it.foot))
                        cacheFalse.get(it.root).put(it.foot,
                                new HashMap<BitSet, Map<BitSet, SXFullItem>>());
                    if (!cacheFalse.get(it.root).get(it.foot)
                            .containsKey(it.rootv))
                        cacheFalse
                                .get(it.root)
                                .get(it.foot)
                                .put(it.rootv,
                                        new HashMap<BitSet, SXFullItem>());
                    cacheFalse.get(it.root).get(it.foot).get(it.rootv)
                            .put(it.footv, it);
                    super.add(it);
                    return true;
                }
            }
        }

    }

    private static BitSet buildLengthVec(BitSet vec) {
        BitSet ret = new BitSet();
        int retpos = 0;
        for (int i = 0; i < vec.length(); ++i) {
            if (vec.get(i)) {
                ret.set(retpos);
                retpos++;
            } else if (i > 0 && vec.get(i - 1) && !vec.get(i)) {
                ret.set(retpos, false);
                retpos++;
            }
        }
        return ret;
    }

    // This can be done more efficiently. However, the whole thing will most likely 
    // still be too slow, so we leave it as it is.
    private static BitSet compareBitVectors(int len, BitSet lit, BitSet rit,
            boolean[][] goalyf) {

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

}
