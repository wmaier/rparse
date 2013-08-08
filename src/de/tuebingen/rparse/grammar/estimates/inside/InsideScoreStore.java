/*******************************************************************************
 * File InsideScoreStore.java
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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Inside estimate, needed for the full SX estimates.
 * 
 * @author wmaier
 */
public class InsideScoreStore extends HashMap<Integer, Map<BitSet, Double>> {

    private static final long serialVersionUID = 5454375790350799937L;

    private Numberer          nb;

    public InsideScoreStore(Numberer nb) {
        this.nb = nb;
    }

    /**
     * Update the score of a state
     * 
     * @param state
     * @param vec
     * @param score
     */
    public void updateScore(int state, BitSet vec, double score) {
        if (!super.containsKey(state))
            super.put(state, new HashMap<BitSet, Double>());
        if (super.get(state).containsKey(vec)) {
            double oldscore = super.get(state).get(vec);
            if (score < oldscore)
                score = oldscore;
        }
        super.get(state).put(vec, score);
    }

    /**
     * Check if a state has a score
     * 
     * @param state
     * @param vec
     * @return
     */
    public boolean hasScore(int state, BitSet vec) {
        return super.containsKey(state) && super.get(state).containsKey(vec);
    }

    /**
     * Get the estimated inside score of a state + vector
     * 
     * @param state
     * @param vec
     * @return
     */
    public double getScore(int state, BitSet vec) {
        if (super.containsKey(state) && super.get(state).containsKey(vec))
            return super.get(state).get(vec);
        else
            return Double.NEGATIVE_INFINITY;
    }

    @Override
    public String toString() {
        String ret = "";
        for (Integer state : super.keySet()) {
            ret += nb.getObjectWithId(GrammarConstants.PREDLABEL, state) + "\n";
            for (BitSet bs : super.get(state).keySet()) {
                ret += "[";
                int cnt = 0;
                for (int i = 0; i < bs.length(); ++i) {
                    if (bs.get(i)) {
                        cnt++;
                    } else {
                        ret += cnt;
                        cnt = 0;
                    }
                }
                ret += cnt > 0 ? cnt : "";
                ret += "(";
                for (int i = 0; i < bs.length(); ++i) {
                    ret += bs.get(i) ? "1" : "0";
                }
                ret += ")]:" + super.get(state).get(bs) + "\n";
            }
        }
        return ret;
    }

}
