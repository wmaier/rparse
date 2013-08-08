/*******************************************************************************
 * File GrammarConstants.java
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
package de.tuebingen.rparse.grammar;

/**
 * Constants related to grammars.
 * 
 * @author wmaier
 */
public interface GrammarConstants {

    /**
     * Default start predicate
     */
    public final static String DEFAULTSTART = "VROOT1";

    /**
     * Constant for use in the numberer
     */
    public static final String GORN         = "gorn";

    /**
     * Constant for use in the numberer
     */
    public final static String PREDLABEL    = "predlabel";

    /**
     * Separation of arguments in clauses
     */
    public static final String ARG_SEP      = ",";

    /**
     * Separation between LHS and RHS in clauses
     */
    public static final String LHSRHS_SEP   = "-->";

    /**
     * Marking of heads in unbinarized grammars
     */
    public static final String HEAD_MARKER  = "'";

}
