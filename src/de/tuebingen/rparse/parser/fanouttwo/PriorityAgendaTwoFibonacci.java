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
public class PriorityAgendaTwoFibonacci extends FibonacciHeap<CYKItemTwo>
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
    public PriorityAgendaTwoFibonacci(Numberer nb) {
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
        FibonacciHeapNode<CYKItemTwo> heapNode = super.removeMin();
        // get its data field
        CYKItemTwo it = heapNode.getData();
        // remove it from our "chart" 
        chart.removeNode(it);
        return it;
    }

    @Override
    public void push(CYKItemTwo it) {
        FibonacciHeapNode<CYKItemTwo> onode = chart.getNode(it);
        if (onode != null) {
            CYKItemTwo oit = onode.getData();
            // update? also update backpointers
            if (oit.iscore + oit.oscore > it.iscore + it.oscore) {
                super.decreaseKey(onode, it.iscore + it.oscore);
                oit.olc = it.olc;
                oit.orc = it.orc;
                oit.iscore = it.iscore;
                oit.oscore = it.oscore;
            }
        } else {
            // create a new heap node for the item
            FibonacciHeapNode<CYKItemTwo> heapNode = new FibonacciHeapNode<CYKItemTwo>(
                    it);
            // make sure we find it later
            chart.addNode(heapNode);
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
     * label -> ll -> lr -> rl -> rr -> node
     */
    private class NodeStore
            extends
                HashMap<Integer, Map<Integer,Map<Integer,Map<Integer,Map<Integer,FibonacciHeapNode<CYKItemTwo>>>>>> {

        private static final long serialVersionUID = 1L;

        public NodeStore(int capacity) {
            super(capacity);
        }

        public FibonacciHeapNode<CYKItemTwo> getNode(CYKItemTwo it) {
            if (super.containsKey(it.label)
                    && super.get(it.label).containsKey(it.ll)
                    && super.get(it.label).get(it.ll).containsKey(it.lr) 
                    && super.get(it.label).get(it.ll).get(it.lr).containsKey(it.rl)
                    && super.get(it.label).get(it.ll).get(it.lr).get(it.rl).containsKey(it.rr))
                return super.get(it.label).get(it.ll).get(it.lr).get(it.rl).get(it.rr);
            return null;
        }
        
        public void addNode(FibonacciHeapNode<CYKItemTwo> heapNode) {
            CYKItemTwo it = heapNode.getData();
            if (!super.containsKey(it.label)) 
                super.put(it.label, new HashMap<Integer,Map<Integer,Map<Integer,Map<Integer,FibonacciHeapNode<CYKItemTwo>>>>>());
            if (!super.get(it.label).containsKey(it.ll))
                super.get(it.label).put(it.ll, new HashMap<Integer,Map<Integer,Map<Integer,FibonacciHeapNode<CYKItemTwo>>>>());
            if (!super.get(it.label).get(it.ll).containsKey(it.lr))
                super.get(it.label).get(it.ll).put(it.lr, new HashMap<Integer,Map<Integer,FibonacciHeapNode<CYKItemTwo>>>());
            if (!super.get(it.label).get(it.ll).get(it.lr).containsKey(it.rl))
                super.get(it.label).get(it.ll).get(it.lr).put(it.rl, new HashMap<Integer,FibonacciHeapNode<CYKItemTwo>>());
            super.get(it.label).get(it.ll).get(it.lr).get(it.rl).put(it.rr, heapNode);
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

}
