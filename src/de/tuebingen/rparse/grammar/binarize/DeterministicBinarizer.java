/*******************************************************************************
 * File DeterministicBinarizer.java
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

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * Provides a continuous numbering of binarization non-terminals for deterministic binarization.
 * 
 * @author wmaier
 */
public abstract class DeterministicBinarizer extends ReorderingBinarizer {

    private int labelCount;

    public DeterministicBinarizer(String params) throws ParameterException {
        super(params);
        labelCount = 0;
    }

    @Override
    protected String getNextName(Clause clause, String basename, String infix,
            int rhspos, int arity, Numberer nb) {
        labelCount++;
        String arityString = String.valueOf(arity);
        String labelCountString = String.valueOf(labelCount);
        return Binarizer.NEW_PRED_NAME_PREFIX + labelCountString
                + Binarizer.NEW_PRED_NAME_SUFFIX + arityString;
    }

    @Override
    public boolean doRetrain() {
        return false;
    }

}
