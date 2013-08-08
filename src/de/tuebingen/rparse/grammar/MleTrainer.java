/*******************************************************************************
 * File MleTrainer.java
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
package de.tuebingen.rparse.grammar;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Maximum likelihood training of the grammar
 * 
 * @author wmaier
 */
public class MleTrainer extends TrainingMethod {

    protected Numberer  nb;

    protected RCG       g;

    protected BinaryRCG bg;

    protected Lexicon   l;

    public MleTrainer(RCG g, BinaryRCG bg, Lexicon l, Numberer nb) {
        super(false);
        this.g = g;
        this.bg = bg;
        this.l = l;
        this.nb = nb;
    }

    @Override
    public void process() throws GrammarException {
        int count = 0;
        if (!doBinarized()) {
            for (int pl : g.getClausesByLhsLabel().keySet()) {
                int lhscount = 0;
                for (Clause c : g.getClausesByLhsLabel().get(pl)) {
                    lhscount += g.getClauseOccurrenceCount(c);
                }
                for (Clause c : g.getClausesByLhsLabel().get(pl)) {
                    c.setScore(new Double(g.getClauseOccurrenceCount(c))
                            / new Double(lhscount));
                    ++count;
                }
            }
        } else {
            for (int pl : bg.clByParent.keySet()) {
                int lhscount = 0;
                for (BinaryClause c : bg.clByParent.get(pl)) {
                    lhscount += bg.cnt.get(c);
                }
                for (BinaryClause c : bg.clByParent.get(pl)) {
                    c.score = new Double(bg.cnt.get(c)) / new Double(lhscount);
                    ++count;
                }
            }
        }
        logger.info("MLE estimate of " + count + " clauses.");
    }

    @Override
    public RCG getGrammar() {
        return g;
    }

    @Override
    public BinaryRCG getBinaryGrammar() {
        return bg;
    }

    @Override
    public void setBinarizedGrammar(BinaryRCG bg) {
        this.bg = bg;
    }

    @Override
    public Lexicon getLexicon() {
        return l;
    }

}
