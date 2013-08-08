/*******************************************************************************
 * File SimpleInsideItem.java
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

/**
 * An item for the CYK-style computation of the simple inside estimate.
 * 
 * @author wmaier
 */
public class SimpleInsideItem implements Comparable<SimpleInsideItem> {

    protected int    label;
    protected int    len;
    protected double score;

    public SimpleInsideItem(int label, int len, double score) {
        this.label = label;
        this.len = len;
        this.score = score;
    }

    @Override
    public int compareTo(SimpleInsideItem o) {
        if (score < o.score)
            return 1;
        if (score > o.score)
            return -1;
        return 0;
    }

    public String toString() {
        return "[" + label + ":" + len + "]";
    }

}
