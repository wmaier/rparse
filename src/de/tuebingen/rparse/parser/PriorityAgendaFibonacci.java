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
package de.tuebingen.rparse.parser;

import java.util.BitSet;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Priority queue (Nederhof 2003) with some extras. Implemented on basis of the fibonacci heap implementation of the
 * JGraphT library.
 * 
 * @author wmaier
 */
public class PriorityAgendaFibonacci extends FibonacciHeap<CYKItem>
        implements
            PriorityAgenda {

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
    public PriorityAgendaFibonacci(Numberer nb) {
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
    public CYKItem poll() {
        // get the heap node
        FibonacciHeapNode<CYKItem> heapNode = super.removeMin();
        // get its data field
        CYKItem it = heapNode.getData();
        // remove it from our "chart"
        chart.get(it.pl).remove(it.rvec);
        if (chart.get(it.pl).isEmpty()) {
            chart.remove(it.pl);
        }
        return it;
    }

    @Override
    public void push(CYKItem it) {
        FibonacciHeapNode<CYKItem> onode = chart.getNode(it);
        if (onode != null) {
            CYKItem oit = onode.getData();
            // update? also update backpointers
            if (oit.iscore + oit.oscore > it.iscore + it.oscore) {
                super.decreaseKey(onode, it.iscore + it.oscore);
                oit.olc = it.olc;
                oit.orc = it.orc;
                oit.iscore = it.iscore;
                oit.oscore = it.oscore;
                oit.iscf = it.iscf;
                oit.start = it.start;
                oit.end = it.end;
            }
        } else {
            // create a new heap node for the item
            FibonacciHeapNode<CYKItem> heapNode = new FibonacciHeapNode<CYKItem>(
                    it);
            // make sure we find it later
            if (!chart.containsKey(it.pl))
                chart.put(it.pl,
                        new HashMap<BitSet, FibonacciHeapNode<CYKItem>>());
            chart.get(it.pl).put(it.rvec, heapNode);
            // put it on the heap
            super.insert(heapNode, it.iscore + it.oscore);
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
     */
    private class NodeStore
            extends
                HashMap<Integer, Map<BitSet, FibonacciHeapNode<CYKItem>>> {

        private static final long serialVersionUID = 1L;

        public NodeStore(int capacity) {
            super(capacity);
        }

        public FibonacciHeapNode<CYKItem> getNode(CYKItem it) {
            if (super.containsKey(it.pl)
                    && super.get(it.pl).containsKey(it.rvec))
                return super.get(it.pl).get(it.rvec);
            return null;
        }

    }

}
