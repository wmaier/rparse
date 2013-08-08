/*******************************************************************************
 * File LeftToRightBinarizer.java
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
package de.tuebingen.rparse.grammar.binarize;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * Left-to-right binarization. Same order as the deterministic one, but uses markovization.
 * 
 * @author wmaier
 */
public class LeftToRightBinarizer extends MarkovizingBinarizer {

    public LeftToRightBinarizer(String params, int v, int h, boolean noArities)
            throws ParameterException {
        super(params, v, h, noArities);
    }

    @Override
    protected int[] determineNewOrder(Clause clause) {
        int headpos = clause.rhsnames.length - 1;
        int length = clause.rhsnames.length;
        int[] result = new int[length];
        int resultPos = 0;

        for (int i = headpos; i >= 0; i--) {
            result[resultPos++] = i;
        }

        for (int i = headpos + 1; i < length; i++) {
            result[resultPos++] = i;
        }

        return result;
    }

}
