/*******************************************************************************
 * File SimpleInsideEstimates.java
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
 * The actual simple inside estimate, summarizes the spans of an item by the sum of their span lengths.
 * 
 * @author wmaier
 */
public class SimpleInsideEstimates {

    private Logger logger;

    public SimpleInsideEstimates() {
        logger = Logger.getLogger(SimpleInsideEstimates.class.getPackage()
                .getName());
    }

    /**
     * Compute the simple inside estimate of a binary grammar
     * 
     * @param bg
     *            The grammar
     * @param insidescores
     *            Chart where to put the scores
     * @param maxlen
     *            Maximum length up to which we compute the estimate
     * @throws GrammarException
     *             If there is a problem
     */
    public void doInside(BinaryRCG bg, SimpleInsideScoreStore insidescores,
            int maxlen) throws GrammarException {
        logger.info("Computing inside scores...");

        if (insidescores == null)
            throw new NullPointerException(
                    "Must supply an initialized inside score store");

        SimpleInsideAgenda agenda = new SimpleInsideAgenda();
        SimpleInsideItem it = null;
        BitSet vec = new BitSet();
        vec.set(0);
        for (int preterm : bg.getPreterminals()) {
            it = new SimpleInsideItem(preterm, 1, 0.0);
            agenda.add(it);
        }
        System.err.println();
        List<SimpleInsideItem> transport = new ArrayList<SimpleInsideItem>();
        while (!agenda.isEmpty()) {
            it = agenda.poll();
            logger.fine("\r inside agenda size: " + agenda.size());
            insidescores.updateScore(it.label, it.len, it.score);
            // left child
            if (bg.clByLc.containsKey(it.label)) {
                if (bg.clByLc.containsKey(it.label)) {
                    for (BinaryClause bc : bg.clByLc.get(it.label)) {
                        double lcscore = it.score;
                        if (bc.rc != -1) {
                            // non-unary
                            if (insidescores.hasLabel(bc.rc))
                                for (int rclen : insidescores
                                        .getLengthsForLabel(bc.rc))
                                    if (it.len + rclen <= maxlen)
                                        transport.add(new SimpleInsideItem(
                                                bc.lhs, it.len + rclen, lcscore
                                                        + insidescores
                                                                .getScore(
                                                                        bc.rc,
                                                                        rclen)
                                                        + bc.score));
                        } else
                            // unary
                            transport.add(new SimpleInsideItem(bc.lhs, it.len,
                                    it.score + bc.score));
                    }
                }
            }
            // right child
            if (bg.clByRc.containsKey(it.label)) {
                for (BinaryClause bc : bg.clByRc.get(it.label)) {
                    double rcscore = it.score;
                    if (insidescores.hasLabel(bc.lc)) {
                        for (int lclen : insidescores.getLengthsForLabel(bc.lc))
                            if (lclen + it.len <= maxlen)
                                transport.add(new SimpleInsideItem(bc.lhs,
                                        lclen + it.len, insidescores.getScore(
                                                bc.lc, lclen)
                                                + rcscore
                                                + bc.score));
                    }
                }
            }
            for (SimpleInsideItem sitem : transport) {
                if (!insidescores.hasScore(sitem.label, sitem.len))
                    agenda.add(sitem);
            }
            transport.clear();
        }
        logger.info("finished.");
    }

}
