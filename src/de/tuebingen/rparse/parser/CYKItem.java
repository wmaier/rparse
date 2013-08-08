/*******************************************************************************
 * File CYKItem.java
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

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;

/**
 * A parser item. Must implement "comparable" to enable the priority queue ordering
 * 
 * @author wmaier
 */
public class CYKItem implements Comparable<CYKItem> {

    // an item is defined by label and bit vector
    public int     pl;
    public BitSet  rvec;

    // backpointers and scores
    public CYKItem olc;
    public CYKItem orc;
    public double  iscore;
    public double  oscore;
    public double  ascore;

    // supplementary information for faster parsing speed

    // indicates if this item is of arity one
    public boolean iscf;

    // the first terminal of the span if CF
    public int     start;

    // the last terminal of the span if CF
    public int     end;

    // the length of the span if CF.
    public int     length;

    public CYKItem(int pl, double iscore, BitSet rvec, CYKItem olc,
            CYKItem orc, int length, boolean iscf, int start, int end) {
        this.pl = pl;
        this.rvec = rvec;
        this.length = length;
        this.iscore = iscore;
        oscore = 0.0;
        this.iscf = iscf;
        this.start = start;
        this.end = end;
        this.olc = olc;
        this.orc = orc;
        // System.err.println("item: " + this + ":%" + summary);
    }

    public int hashCode() {
        int hc = 5381;
        hc = ((hc << 5) + hc) + rvec.hashCode();
        hc = ((hc << 5) + hc) + pl;
        return hc;
    }

    @Override
    public int compareTo(CYKItem o) {
        if (iscore + oscore < o.iscore + o.oscore)
            return -1;
        if (iscore + oscore > o.iscore + o.oscore)
            return 1;
        return 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(pl);
        sb.append(":");
        for (int i = 0; i < length; ++i)
            if (rvec.get(i) == false)
                sb.append(0);
            else
                sb.append(1);
        sb.append("]:(");
        sb.append(iscore);
        sb.append(",");
        sb.append(oscore);
        sb.append("):");
        sb.append(hashCode());
        return sb.toString();
    }

    /**
     * Note: this class has a natural ordering that is INCONSISTENT with equals. This is no bug, it's a feature.
     */
    public boolean equals(Object o) {
        if (!(o instanceof CYKItem))
            return false;
        return ((CYKItem) o).pl == this.pl
                && ((CYKItem) o).rvec.equals(this.rvec);
    }

    public String print(Numberer nb) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(nb.getObjectWithId(GrammarConstants.PREDLABEL, pl));
        sb.append(":");
        for (int i = 0; i < length; ++i)
            if (rvec.get(i) == false)
                sb.append(0);
            else
                sb.append(1);
        sb.append("]:(");
        sb.append(iscore);
        sb.append(",");
        sb.append(oscore);
        sb.append("):");
        sb.append(hashCode());
        return sb.toString();
    }

}
