/*******************************************************************************
 * File InsideEstimates.java
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
package de.tuebingen.rparse.grammar.estimates.inside;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;

/**
 * Compute the inside estimate, needed for SX estimates. See the corresponding score store: The spans are summarized by
 * their lengths, which we represent by a bit vector, just as in the parser. Hint: This is not efficient enough.
 * 
 * @author wmaier
 */
public class InsideEstimates {

    private int    maxlen;
    private Logger logger;

    public InsideEstimates() {
        maxlen = 0;
        logger = Logger.getLogger(InsideEstimates.class.getPackage().getName());
    }

    /**
     * Perform the actual computation.
     * 
     * @param bg
     *            The binary grammar
     * @param insidescores
     *            The chart where to put the result
     * @param maxlen
     *            the maximum sentence length
     * @throws GrammarException
     */
    public void doInside(BinaryRCG bg, InsideScoreStore insidescores, int maxlen)
            throws GrammarException {
        logger.info("Computing inside scores...");
        this.maxlen = maxlen;
        InsideAgenda agenda = new InsideAgenda();
        InsideItem it = null;
        BitSet vec = new BitSet();
        vec.set(0);
        for (int preterm : bg.getPreterminals()) {
            it = new InsideItem(preterm, (BitSet) vec.clone(), 0.0);
            agenda.add(it);
        }
        List<InsideItem> transport = new ArrayList<InsideItem>();
        while (!agenda.isEmpty()) {
            it = agenda.poll();
            if (agenda.size() % 1000 == 0)
                logger.finer("\r Agenda size: " + agenda.size() / 1000 + "k   ");
            insidescores.updateScore(it.label, it.vec, it.score);
            // left child
            if (bg.clByLc.containsKey(it.label)) {
                if (bg.clByLc.containsKey(it.label)) {
                    for (BinaryClause bc : bg.clByLc.get(it.label)) {
                        double lcscore = it.score;
                        if (bc.rc != -1) {
                            // non-unary
                            if (insidescores.containsKey(bc.rc)) {
                                for (BitSet rcvechc : insidescores.get(bc.rc)
                                        .keySet()) {
                                    double rcscore = insidescores.getScore(
                                            bc.rc, rcvechc);
                                    BitSet pvec = insideComposeLengthVec(
                                            it.vec, rcvechc, bc.yf);
                                    if (pvec != null)
                                        transport.add(new InsideItem(bc.lhs,
                                                pvec, lcscore + rcscore
                                                        + bc.score));
                                }
                            }
                        } else {
                            // unary
                            transport.add(new InsideItem(bc.lhs,
                                    (BitSet) it.vec.clone(), it.score
                                            + bc.score));
                        }
                    }
                }
            }
            // right child
            if (bg.clByRc.containsKey(it.label)) {
                for (BinaryClause bc : bg.clByRc.get(it.label)) {
                    double rcscore = it.score;
                    if (insidescores.containsKey(bc.lc)) {
                        for (BitSet lcvechc : insidescores.get(bc.lc).keySet()) {
                            double lcscore = insidescores.getScore(bc.lc,
                                    lcvechc);
                            BitSet pvec = insideComposeLengthVec(lcvechc,
                                    it.vec, bc.yf);
                            if (pvec != null)
                                transport.add(new InsideItem(bc.lhs, pvec,
                                        lcscore + rcscore + bc.score));
                        }
                    }
                }
            }
            for (InsideItem sit : transport) {
                if (!insidescores.hasScore(sit.label, sit.vec))
                    agenda.add(sit);
            }
            transport.clear();
        }
        logger.info("finished.");
    }

    private BitSet insideComposeLengthVec(BitSet lc, BitSet rc, boolean[][] yf) {
        if (lc == null || rc == null || yf == null
                || yf.length + lc.cardinality() + rc.cardinality() > maxlen + 1)
            return null;

        BitSet ret = new BitSet();
        int retpos = 0;
        int l = 0;
        int r = 0;
        for (int i = 0; i < yf.length; ++i) {
            for (int j = 0; j < yf[i].length; ++j) {
                // ret[i] += !yf[i][j] ? lc[l++] : rc[r++];
                if (!yf[i][j]) {
                    int subarglength = lc.nextClearBit(l) - l;
                    ret.set(retpos, retpos + subarglength);
                    retpos += subarglength;
                    l = subarglength + 1;
                } else {
                    int subarglength = rc.nextClearBit(r) - r;
                    ret.set(retpos, retpos + subarglength);
                    retpos += subarglength;
                    r = subarglength + 1;
                }
            }
            retpos++;
            ret.set(retpos, false);
        }
        return ret;
    }

}
