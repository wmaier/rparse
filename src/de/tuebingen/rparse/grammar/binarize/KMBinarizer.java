/*******************************************************************************
 * File KMBinarizer.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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
 * Head-outward binarization. Sisters are added as in Klein & Manning (2003) (the other way round as in
 * HeadDrivenBinarizer).
 * 
 * @author wmaier
 */
public class KMBinarizer extends MarkovizingBinarizer {

    public KMBinarizer(String params, int v, int h, boolean noArities)
            throws ParameterException {
        super(params, v, h, noArities);
    }

    @Override
    protected int[] determineNewOrder(Clause clause) {
        int headpos = clause.getHeadPos();
        int length = clause.rhsnames.length;
        int[] result = new int[length];
        int resultPos = 0;

        for (int i = headpos; i < length; i++) {
            result[resultPos++] = i;
        }

        for (int i = headpos - 1; i >= 0; i--) {
            result[resultPos++] = i;
        }

        return result;
    }

}
