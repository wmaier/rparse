/*******************************************************************************
 * File ParsingTypes.java
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

public abstract class ParsingTypes {

    /**
     * Uses Java PriorityQueue with remove + insert to simulate decreaseKey
     */
    public static final String RCG_CYK_NAIVE = "cyknaive";

    /**
     * Uses JGraphT FibonacciHeap with proper decreaseKey Operation
     */
    public static final String RCG_CYK_FIBO  = "cyk";

    /**
     * The parser for (2,2) LCFRS 
     */
	public static final String RCGTWO_CYK = "cyktwo";

}
