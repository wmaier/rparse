/*******************************************************************************
 * File EvalTypes.java
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
package de.tuebingen.rparse.eval;

/**
 * Available evaluation metrics for constituents. For dependencies, we limit ourselves to a single class since there is
 * not much competition to the attachment score.
 * 
 * @author wmaier
 */
public class EvalTypes {

    /**
     * Tree-distance measure (Emms, 2008), whole trees
     */
    public static final String TREEDISTWHOLE           = "treedistwhole";

    /**
     * Tree-distance measure (Emms, 2008), roof trees
     */
    public static final String TREEDISTROOF            = "treedistroof";

    /**
     * EVALB bracket scoring, extension of the EVALB program
     */
    public static final String EVALB                   = "evalb";

    /**
     * Dependency evaluation
     */
    public static final String DEP                     = "dep";

}
