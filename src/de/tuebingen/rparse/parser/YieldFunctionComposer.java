/*******************************************************************************
 * File YieldFunctionComposer.java
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

import java.io.Serializable;
import java.util.BitSet;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.IntegerContainer;


/**
 * Class below yield function composer makes a counter available
 * 
 * @author wmaier
 */
public abstract class YieldFunctionComposer implements Serializable {

    private static final long serialVersionUID = -3344346159814832536L;

    private int               compositionCount;
    
    transient protected Logger logger;

    /**
     * Constructor
     */
    public YieldFunctionComposer() {
        compositionCount = 0;
        logger = Logger.getLogger(YieldFunctionComposer.class.getPackage().getName());
    }

    /**
     * Reset the counter. Subclasses can do more things.
     */
    public void reset() {
        compositionCount = 0;
    }

    /**
     * Returns the counter. Subclasses can do more elaborate things.
     * 
     * @return
     */
    public String stats() {
        return String.valueOf(compositionCount);
    }

    /**
     * This gets called from the parser and updates the count
     * 
     * @param lit
     *            Left range vector
     * @param rit
     *            Right range vector
     * @param yf
     *            Yield function
     * @param s
     *            In case yield function is CF, start of yield
     * @param e
     *            In case yield function is CF, end of yield
     * @return The composed range vectors
     */
    public final BitSet composeYields(CYKItem lit, CYKItem rit, boolean[][] yf,
            IntegerContainer s, IntegerContainer e) {
        ++compositionCount;
        return doComposition(lit, rit, yf, s, e);
    }

    /**
     * Does the actual composition. Must be provided by subclasses.
     * 
     * @param lit
     *            Left range vector
     * @param rit
     *            Right range vector
     * @param yf
     *            Yield function
     * @param s
     *            In case yield function is CF, start of yield
     * @param e
     *            In case yield function is CF, end of yield
     * @return The composed range vectors
     */
    public abstract BitSet doComposition(CYKItem lit, CYKItem rit,
            boolean[][] yf, IntegerContainer s, IntegerContainer e);

}
