/*******************************************************************************
 * File BinarizerTypes.java
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

/**
 * Available binarizers.
 * 
 * @author wmaier
 */
public class BinarizerTypes {

    // binarizer types

    /**
     * Head-outward
     */
    public final static String HEADDRIVEN    = "headdriven";

    /**
     * clause-optimal, as in Kallmeyer&Maier(2011)
     */
    public static final String OPTIMAL       = "optimal";

    /**
     * As head-outward, but first add sisters to the left, and then to the right (head-outward is vice versa)
     */
    public static final String HEADDRIVEN_KM = "km";

    /**
     * Strictly left-to-right
     */
    public static final String L_TO_R        = "ltor";

    /**
     * Strictly right-to-left
     */
    public static final String R_TO_L        = "rtol";

    /**
     * Deterministic left to right
     */
    public static final String DETERMINISTIC = "detlr";

}
