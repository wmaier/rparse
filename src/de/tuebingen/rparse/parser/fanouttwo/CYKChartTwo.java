/*******************************************************************************
 * File CYKChartTwo.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2012
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

import de.tuebingen.rparse.grammar.fanouttwo.BinaryRCGTwo;



public class CYKChartTwo {

    // label -> l1 -> r1 -> l2 -> r2
    Map<Integer,Map<Integer,Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>>> charttwo;
    // label -> l1 -> r1 
    Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>> chartone;

    public CYKChartTwo(BinaryRCGTwo bg) {
        chartone = new HashMap<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>();
        charttwo = new HashMap<Integer,Map<Integer,Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>>>();
    }
    
    public void add(CYKItemTwo item) {
        if (item.onetwo == 1) {
            if (!chartone.containsKey(item.label)) {
                chartone.put(item.label, new HashMap<Integer,Map<Integer,CYKItemTwo>>());
            }
            if (!chartone.get(item.label).containsKey(item.ll)) {
                chartone.get(item.label).put( item.ll, new HashMap<Integer,CYKItemTwo>());
            }
            chartone.get(item.label).get( item.ll).put( item.lr, item);
        } else if (item.onetwo == 2) {
            if (!charttwo.containsKey(item.label)) {
                charttwo.put(item.label, new HashMap<Integer,Map<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>>());
            }
            if (!charttwo.get(item.label).containsKey(item.ll)) {
                charttwo.get(item.label).put( item.ll, new HashMap<Integer,Map<Integer,Map<Integer,CYKItemTwo>>>());
            }
            if (!charttwo.get(item.label).get(item.ll).containsKey(item.lr)) {
                charttwo.get(item.label).get(item.ll).put( item.lr, new HashMap<Integer,Map<Integer,CYKItemTwo>>());
            }
            if (!charttwo.get(item.label).get(item.ll).get(item.lr).containsKey(item.rl)) {
                charttwo.get(item.label).get(item.ll).get(item.lr).put( item.rl, new HashMap<Integer,CYKItemTwo>());
            }
            charttwo.get(item.label).get(item.ll).get(item.lr).get(item.rl).put(item.rr,item);
        } else {
            throw new IllegalStateException("Only one or two spans allowed");
        }
        
    }
    
    public boolean hasLabelOne(int label) {
        return chartone.containsKey(label);
    }
    
    private boolean hasScoreOne(CYKItemTwo item) {
        return chartone.containsKey(item.label)
                && chartone.get(item.label).containsKey(item.ll)
                && chartone.get(item.label).get(item.ll).containsKey(item.lr);
    }
    
    public boolean hasLabelTwo(int label) {
        return charttwo.containsKey(label);
    }
    
    private boolean hasScoreTwo(CYKItemTwo item) {
        return charttwo.containsKey(item.label)
                && charttwo.get(item.label).containsKey(item.ll)
                && charttwo.get(item.label).get(item.ll).containsKey(item.lr)
                && charttwo.get(item.label).get(item.ll).get(item.lr).containsKey(item.rl)
                && charttwo.get(item.label).get(item.ll).get(item.lr).get(item.rl).containsKey(item.rr);
    }    
    
    public int size() {
        return 0; // TODO
    }

    public boolean hasScore(CYKItemTwo it) {
        if (it.onetwo == 1) {
            return hasScoreOne(it);
        } else if (it.onetwo == 2) {
            return hasScoreTwo(it);
        } else {
            throw new IllegalStateException("Wrong type of item");
        }
    }
    
    
}
