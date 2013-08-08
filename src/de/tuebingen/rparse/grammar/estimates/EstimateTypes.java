/*******************************************************************************
 * File EstimateTypes.java
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

/**
 * Different types of context-summary outside estimates from Kallmeyer & Maier (2011)
 * 
 * @author wmaier
 */
public class EstimateTypes {

    /**
     * No estimate (bypass)
     */
    public final static String OFF         = "off";

    /**
     * Full SX, Kallmeyer & Maier (2011), sec. 4.1
     */
    public final static String SX          = "sx";

    /**
     * Full SX with deduction, also sec. 4.1
     */
    public final static String SXDEDUCTION = "sxdeduction";

    /**
     * SX simple, Kallmeyer & Maier (2011), sec. 4.2
     */
    public final static String SXSIMPLE    = "sxsimple";

    /**
     * LR, Kallmeyer & Maier (2011), sec. 4.3
     */
    public final static String SXSIMPLE_LR = "lr";

    /**
     * LN, Kallmeyer & Maier (2011), sec. 4.4
     */
    public static final String SXSIMPLE_LN = "ln";
    
}
