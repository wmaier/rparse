/*******************************************************************************
 * File YieldFunctionComposerFactory.java
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

import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * Factory for different classes which are used to compute the compatibility of two range vectors and a yield function
 * in the complete rule of the CYK parser.
 * 
 * @author wmaier
 */
public class YieldFunctionComposerFactory {

    /**
     * Get a yield function composer
     * 
     * @param type
     *            The desired type, see the corresponding constants interface.
     * @param params
     *            Params to the composer
     * @return The composer
     * @throws UnknownTaskException
     *             If an unknown composer is requested
     * @throws ParameterException
     *             If something is wrong with the composer parameters
     */
    public static YieldFunctionComposer getYieldFunctionComposer(String type,
            String params) throws UnknownTaskException, ParameterException {

        if (YieldFunctionComposerTypes.CLASSIC.equals(type)) {
            return new ClassicYFComposer();
        }

        if (YieldFunctionComposerTypes.FAST.equals(type)) {
            return new FastYFComposer();
        }

        if (YieldFunctionComposerTypes.GAPS.equals(type)) {
            return new GapfilterYFComposer(params);
        }

        if (YieldFunctionComposerTypes.WELL.equals(type)) {
            return new WellnestedYFComposer();
        }

        throw new UnknownTaskException(type);
    }

}
