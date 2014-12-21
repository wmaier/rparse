/*******************************************************************************
 * File PriorityAgendaFibonacci.java
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
package de.tuebingen.rparse.parser.fanouttwo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Priority queue (Nederhof 2003) with some extras. Implemented on basis of the fibonacci heap implementation of the
 * JGraphT library.
 * 
 * @author wmaier
 */
public class PriorityAgendaTwoPrio extends PriorityQueue<CYKItemTwo>
        implements
            PriorityAgendaTwo {

    // The node store helps us finding the right heap node
    private NodeStore  chart;

    // watches the agenda grow
    private long       agendaMaxSize;

    // checks how often we perform the add operation
    private long       addCount;

    // A numberer
    protected Numberer nb;

    /**
     * Constructor.
     * 
     * @param nb
     *            A numberer
     */
    public PriorityAgendaTwoPrio(Numberer nb) {
    	super(new Comparator<CYKItemTwo>() {
    		public int compare(CYKItemTwo o1, CYKItemTwo o2) {
    			return Double.compare(o1.iscore + o1.oscore, o2.iscore + o2.oscore);
    		}
    	});
    	agendaMaxSize = 0;
        addCount = 0;
        chart = new NodeStore(10000);
        this.nb = nb;
    }

    @Override
    public void clear() {
        super.clear();
        agendaMaxSize = 0;
        addCount = 0;
    }

    @Override
    public CYKItemTwo poll() {
        // get the heap node
        CYKItemTwo it = super.poll();
        // remove it from our "chart" 
        chart.removeNode(it);
        return it;
    }

    @Override
    public void push(CYKItemTwo it) {
        CYKItemTwo oit = chart.getNode(it);
        if (oit != null) {
            // update? also update backpointers
            if (oit.iscore + oit.oscore > it.iscore + it.oscore) {
            	super.remove(oit);
                oit.olc = it.olc;
                oit.orc = it.orc;
                oit.iscore = it.iscore;
                oit.oscore = it.oscore;
                super.add(oit);
            }
        } else {
            // make sure we find it later
            chart.addNode(it);
            // put it on the heap
            super.add(it);
            ++addCount;
        }
        agendaMaxSize = Math.max(super.size(), agendaMaxSize);
    }

    @Override
    public String getStats() {
        return "Agenda stats: Max size: " + agendaMaxSize + ", adds: "
                + addCount;
    }

    /*
     * Keeps track of heap nodes 
     * label -> ll -> lr -> rl -> rr -> node
     */
    private class NodeStore
            extends
                HashMap<Integer, Map<Integer,Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>>> {

        private static final long serialVersionUID = 1L;

        public NodeStore(int capacity) {
            super(capacity);
        }

        public CYKItemTwo getNode(CYKItemTwo it) {
            if (super.containsKey(it.label)
                    && super.get(it.label).containsKey(it.ll)
                    && super.get(it.label).get(it.ll).containsKey(it.lr) 
                    && super.get(it.label).get(it.ll).get(it.lr).containsKey(it.rl)
                    && super.get(it.label).get(it.ll).get(it.lr).get(it.rl).containsKey(it.rr))
                return super.get(it.label).get(it.ll).get(it.lr).get(it.rl).get(it.rr);
            return null;
        }
        
        public void addNode(CYKItemTwo it) {
            if (!super.containsKey(it.label)) 
                super.put(it.label, new HashMap<Integer,Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>>());
            if (!super.get(it.label).containsKey(it.ll))
                super.get(it.label).put(it.ll, new HashMap<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>());
            if (!super.get(it.label).get(it.ll).containsKey(it.lr))
                super.get(it.label).get(it.ll).put(it.lr, new HashMap<Integer,Map<Integer,CYKItemTwo>>());
            if (!super.get(it.label).get(it.ll).get(it.lr).containsKey(it.rl))
                super.get(it.label).get(it.ll).get(it.lr).put(it.rl, new HashMap<Integer,CYKItemTwo>());
            super.get(it.label).get(it.ll).get(it.lr).get(it.rl).put(it.rr, it);
        }
        
        public void removeNode(CYKItemTwo it) {
            super.get(it.label).get(it.ll).get(it.lr).get(it.rl).remove(it.rr);
            if (super.get(it.label).get(it.ll).get(it.lr).get(it.rl).isEmpty()) {
                super.get(it.label).get(it.ll).get(it.lr).remove(it.rl);
                if (super.get(it.label).get(it.ll).get(it.lr).isEmpty()) {
                    super.get(it.label).get(it.ll).remove(it.lr);
                    if (super.get(it.label).get(it.ll).isEmpty()) {
                        super.get(it.label).remove(it.ll);
                        if (super.get(it.label).isEmpty()) 
                            super.remove(it.label);
                    }
                }
            }
        }

    }

	private static final long serialVersionUID = -1454532493714048364L;

}
