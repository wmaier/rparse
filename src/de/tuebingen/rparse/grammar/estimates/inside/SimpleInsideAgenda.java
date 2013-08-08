/*******************************************************************************
 * File SimpleInsideAgenda.java
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

import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

/**
 * The agenda for computing the simple inside estimate.
 * 
 * @author wmaier
 */
public class SimpleInsideAgenda extends FibonacciHeap<SimpleInsideItem> {

    private SimpleInsideChart chart;

    public SimpleInsideAgenda() {
        chart = new SimpleInsideChart(10000);
    }

    public SimpleInsideItem poll() {
        FibonacciHeapNode<SimpleInsideItem> heapNode = super.removeMin();
        SimpleInsideItem it = heapNode.getData();
        chart.get(it.label).remove(it.len);
        if (chart.get(it.label).isEmpty())
            chart.remove(it.label);
        return it;
    }

    public boolean add(SimpleInsideItem it) {
        FibonacciHeapNode<SimpleInsideItem> onode = chart.getNode(it);
        if (onode != null) {
            SimpleInsideItem oit = onode.getData();
            if (oit.score > it.score) {
                super.decreaseKey(onode, it.score);
                oit.score = it.score;
            }
        } else {
            FibonacciHeapNode<SimpleInsideItem> heapNode = new FibonacciHeapNode<SimpleInsideItem>(
                    it);
            if (!chart.containsKey(it.label))
                chart.put(
                        it.label,
                        new HashMap<Integer, FibonacciHeapNode<SimpleInsideItem>>());
            chart.get(it.label).put(it.len, heapNode);
            super.insert(heapNode, it.score);
        }
        return true;
    }

    private class SimpleInsideChart
            extends
                HashMap<Integer, Map<Integer, FibonacciHeapNode<SimpleInsideItem>>> {

        private static final long serialVersionUID = 8293965370724625822L;

        public SimpleInsideChart(int capacity) {
            super(capacity);
        }

        public FibonacciHeapNode<SimpleInsideItem> getNode(SimpleInsideItem it) {
            if (super.containsKey(it.label)
                    && super.get(it.label).containsKey(it.len))
                return super.get(it.label).get(it.len);
            return null;
        }

    }

}
