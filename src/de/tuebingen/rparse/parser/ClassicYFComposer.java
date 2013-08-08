/*******************************************************************************
 * File ClassicYFComposer.java
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

import de.tuebingen.rparse.misc.IntegerContainer;

/**
 * Composes two range vectors. Linear in size of the vectors. Much slower than the other implementation.
 * 
 * @author wmaier
 */
public class ClassicYFComposer extends YieldFunctionComposer {

    // can get serialized
    private static final long serialVersionUID = -829666387336377643L;

    @Override
    public BitSet doComposition(CYKItem lit, CYKItem rit, boolean[][] goalyf,
            IntegerContainer s, IntegerContainer e) {

        int arg = 0;
        int argc = 0;

        for (int i = 0; i < lit.length; ++i) {
            if (lit.rvec.get(i) && !rit.rvec.get(i)) {
                if (arg == goalyf.length || argc == goalyf[arg].length
                        || goalyf[arg][argc])
                    return null;
                else if (i + 1 < lit.length && !lit.rvec.get(i + 1))
                    argc++;
            } else if (!lit.rvec.get(i) && rit.rvec.get(i)) {
                if (arg == goalyf.length || argc == goalyf[arg].length
                        || !goalyf[arg][argc])
                    return null;
                else if (i + 1 < lit.length && !rit.rvec.get(i + 1))
                    argc++;
            } else if (lit.rvec.get(i) && rit.rvec.get(i)) {
                return null;
            } else if (!lit.rvec.get(i) && !rit.rvec.get(i)) {
                if (i > 0 && (lit.rvec.get(i - 1) ^ rit.rvec.get(i - 1))) {
                    arg++;
                    argc = 0;
                }
            }
        }

        BitSet yp = (BitSet) lit.rvec.clone();
        yp.xor(rit.rvec);
        return yp;
    }

}
