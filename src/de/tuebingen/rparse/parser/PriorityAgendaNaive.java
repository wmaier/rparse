/*******************************************************************************
 * File PriorityAgendaNaive.java
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
package de.tuebingen.rparse.parser;

import java.util.BitSet;
import java.util.HashMap;
import java.util.PriorityQueue;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Priority agenda, naive implementation. Simulates the decrease-key operation by removing the node and adding it again.
 * The implementation is based on the Java Standard Library implementation {@code PriorityAgenda}.
 * 
 * @author wmaier
 */
public class PriorityAgendaNaive extends PriorityQueue<CYKItem>
        implements
            PriorityAgenda {

    private static final long serialVersionUID = 3418341024980090886L;

    // chart structure helps us finding the right instances in the agenda
    private CYKChart          chart;

    private long              agendaMaxSize;

    private long              addCount;

    protected int             decreaseKeyCount = 0;

    protected Numberer        nb;

    /**
     * Constructor
     * 
     * @param nb
     *            A numberer
     */
    public PriorityAgendaNaive(Numberer nb) {
        agendaMaxSize = 0;
        addCount = 0;
        chart = new CYKChart();
        this.nb = nb;
    }

    @Override
    public void clear() {
        super.clear();
        agendaMaxSize = 0;
        addCount = 0;
    }

    @Override
    public CYKItem poll() {
        CYKItem it = super.poll();
        chart.get(it.pl).remove(it.rvec);
        if (chart.get(it.pl).isEmpty())
            chart.remove(it.pl);
        return it;
    }

    @Override
    public void push(CYKItem it) {
        // check if we have that item already
        CYKItem oit = chart.getItem(it);
        if (oit != null) {
            // is old
            if (oit.iscore + oit.oscore > it.iscore + oit.oscore) {
                // decrease-key
                // no updating of backpointers necessary
                super.remove(oit);
                super.offer(it);
                decreaseKeyCount++;
            }
        } else {
            // is new
            if (!chart.containsKey(it.pl))
                chart.put(it.pl, new HashMap<BitSet, CYKItem>());
            chart.get(it.pl).put(it.rvec, it);
            super.offer(it);
            ++addCount;
        }
        agendaMaxSize = Math.max(super.size(), agendaMaxSize);
    }

    @Override
    public String getStats() {
        return "Agenda stats: Max size: " + agendaMaxSize + ", adds: "
                + addCount;
    }

    @Override
    public String toString() {
        String ret = "";
        for (CYKItem it : this) {
            ret += it.print(nb) + "\n";
        }
        return ret;
    }

}
