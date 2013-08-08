/*******************************************************************************
 * File BinarizerFactory.java
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

import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * Gets an available binarizer.
 * 
 * @author wmaier
 */
public class BinarizerFactory {

    /**
     * Get a binarizer.
     * 
     * @param binarizerType
     *            The desired type.
     * @param v
     *            Vertical markovization (if applicable)
     * @param h
     *            Horizontal markovization (if applicable)
     * @param noArities
     *            Use arity suffixes on element labels of binarization non-terminals
     * @param params
     *            More parameters
     * @return A binarizer
     * @throws UnknownTaskException
     *             If an unknown type is selected.
     * @throws ParameterException
     *             If there is a problem with the parameter string.
     */
    public static Binarizer getBinarizer(String binarizerType, int v, int h,
            boolean noArities, String params) throws UnknownTaskException,
            ParameterException {
        
        if (binarizerType == null) {
            throw new UnknownTaskException("Got null as requested binarizer");
        }

        if (BinarizerTypes.DETERMINISTIC.equals(binarizerType)) {
            return new DeterministicLeftToRightBinarizer(params);
        }

        if (BinarizerTypes.HEADDRIVEN.equals(binarizerType)) {
            return new HeadDrivenBinarizer(params, v, h, noArities);
        }

        if (BinarizerTypes.HEADDRIVEN_KM.equals(binarizerType)) {
            return new KMBinarizer(params, v, h, noArities);
        }

        if (BinarizerTypes.OPTIMAL.equals(binarizerType)) {
            return new OptimalBinarizer(params, v, h, noArities);
        }

        if (BinarizerTypes.L_TO_R.equals(binarizerType)) {
            return new LeftToRightBinarizer(params, v, h, noArities);
        }

        if (BinarizerTypes.R_TO_L.equals(binarizerType)) {
            return new RightToLeftBinarizer(params, v, h, noArities);
        }

        throw new UnknownTaskException(binarizerType);

    }

}
