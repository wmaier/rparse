/*******************************************************************************
 * File BypassEstimates.java
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
package de.tuebingen.rparse.grammar.estimates;

import java.util.BitSet;

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Dummy estimate so the parser can always use some estimate (i.e., we save a lot of if-statements in the parser).
 * 
 * @author wmaier
 */
public class BypassEstimates extends Estimate {

    public BypassEstimates(BinaryRCG bg, Numberer nb, int sentlen) {
        super(bg, nb, sentlen);
    }

    private static final long serialVersionUID = 1L;

    @Override
    public String getStats() {
        return "";
    }

    @Override
    public void process() {
    }

    @Override
    public double get(int slen, int state, BitSet vechc, int tags[]) {
        return 0.0;
    }

    @Override
    public double get(int slen, int state, int ll, int lr, int rl, int rr) {
        return 0.0;
    }

}
