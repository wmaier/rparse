/*******************************************************************************
 * File InsideAgenda.java
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
import java.util.PriorityQueue;

/**
 * An agenda for computing the inside estimate.
 * 
 * @author wmaier
 */
public class InsideAgenda extends PriorityQueue<InsideItem> {

    private static final long                     serialVersionUID = -5241852591651004464L;

    // label --> vec --> item
    private Map<Integer, Map<BitSet, InsideItem>> chart;

    public InsideAgenda() {
        chart = new HashMap<Integer, Map<BitSet, InsideItem>>();
    }

    @Override
    public InsideItem poll() {
        InsideItem it = super.poll();
        if (it != null) {
            chart.get(it.label).remove(it.vec);
            if (chart.get(it.label).isEmpty())
                chart.remove(it.label);
        }
        return it;
    }

    @Override
    public boolean add(InsideItem it) {
        if (chart.containsKey(it.label)
                && chart.get(it.label).containsKey(it.vec)) {
            InsideItem oit = chart.get(it.label).get(it.vec);
            oit.score = Math.max(oit.score, it.score);
        } else {
            super.add(it);
            if (!chart.containsKey(it.label))
                chart.put(it.label, new HashMap<BitSet, InsideItem>());
            chart.get(it.label).put(it.vec, it);
        }
        return true;
    }
}
