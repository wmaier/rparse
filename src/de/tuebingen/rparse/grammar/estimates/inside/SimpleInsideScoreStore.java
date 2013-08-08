/*******************************************************************************
 * File SimpleInsideScoreStore.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Simplified inside score estimate, needed for all estimates other than SX. Summarizes the spans by the sum of their
 * lengths.
 * 
 * @author wmaier
 */
public class SimpleInsideScoreStore {

    private HashMap<Integer, Map<Integer, Double>> chart;

    private Numberer                               nb;

    public SimpleInsideScoreStore(Numberer nb) {
        this.nb = nb;
        chart = new HashMap<Integer, Map<Integer, Double>>();
    }

    /**
     * Update the score of a state
     * 
     * @param state
     * @param len
     * @param score
     */
    public void updateScore(int state, int len, double score) {
        if (!chart.containsKey(state))
            chart.put(state, new HashMap<Integer, Double>());
        if (chart.get(state).containsKey(len)) {
            double oldscore = chart.get(state).get(len);
            if (score < oldscore) {
                chart.get(state).put(len, score);
            }
        } else {
            chart.get(state).put(len, score);
        }
    }

    /**
     * Get the score of a state
     * 
     * @param rc
     * @param rclen
     * @return
     */
    public double getScore(int rc, int rclen) {
        if (chart.containsKey(rc) && chart.get(rc).containsKey(rclen))
            return chart.get(rc).get(rclen);
        else
            return Double.POSITIVE_INFINITY;
    }

    /**
     * Check if there is a score
     * 
     * @param label
     * @param len
     * @return
     */
    public boolean hasScore(int label, int len) {
        return chart.containsKey(label) && chart.get(label).containsKey(len);
    }

    @Override
    public String toString() {
        String ret = "";
        for (Integer state : chart.keySet()) {
            ret += nb.getObjectWithId(GrammarConstants.PREDLABEL, state) + "\n";
            for (int len : chart.get(state).keySet())
                ret += "[" + len + "]:" + chart.get(state).get(len) + "\n";
        }
        return ret;
    }

    /**
     * Check if the chart contains a state
     * 
     * @param rc
     * @return
     */
    public boolean hasLabel(int rc) {
        return chart.containsKey(rc);
    }

    /**
     * Get all lengths for a certain state
     * 
     * @param state
     * @return
     */
    public Set<Integer> getLengthsForLabel(int label) {
        return chart.get(label).keySet();
    }
}
