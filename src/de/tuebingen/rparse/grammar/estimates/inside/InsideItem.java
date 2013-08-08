/*******************************************************************************
 * File InsideItem.java
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

/**
 * Very simple item for the computation of the inside estimate.
 * 
 * @author wmaier
 */
public class InsideItem extends SimpleInsideItem {

    protected BitSet vec;

    public InsideItem(int label, BitSet vec, double score) {
        super(label, 0, score);
        this.vec = vec;
    }

    @Override
    public String toString() {
        String ret = "[";
        ret += label + ":" + vec.toString();
        return ret + "]";
    }

}
