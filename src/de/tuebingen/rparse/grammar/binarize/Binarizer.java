/*******************************************************************************
 * File Binarizer.java
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

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;

/**
 * Interface for the binarizer, visible to the UI
 * 
 * @author wmaier
 */
public interface Binarizer {

    /**
     * To be used in names of binarization non-terminals.
     */
    public static final String NEW_PRED_NAME_PREFIX = "@";

    /**
     * To be used in names of binarization non-terminals.
     */
    public static final String NEW_PRED_NAME_SUFFIX = "X";

    /**
     * Subclasses must simply provide a method which takes an unbinarized grammar and returns a binarized one.
     * 
     * @param g
     *            The unbinarized grammar
     * @return The binarized grammar
     * @throws GrammarException
     *             If something goes wrong with the binarization.
     */
    public BinaryRCG binarize(RCG g) throws GrammarException;

    /**
     * Indicates if the grammar which results of the binarization is supposed to be re-trained (Markovization yes,
     * deterministic binarization no).
     * 
     * @return The corresponding boolean value.
     */
    public boolean doRetrain();

}
