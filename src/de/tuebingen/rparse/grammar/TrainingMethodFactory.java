/*******************************************************************************
 * File TrainingMethodFactory.java
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

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Factory which gives us training methods.
 * 
 * @author wmaier
 */
public class TrainingMethodFactory {

    /**
     * Get a training method.
     * 
     * @param trainingMethod
     *            The desired type.
     * @param g
     *            The grammar
     * @param bg
     *            The binarized grammar
     * @param l
     *            The lexicon
     * @param nb
     *            The numberer
     * @param params
     *            Extra parameters for ClassParameters
     * @return The TrainingMethod
     * @throws ParameterException
     *             If the parameter string cannot be parsed
     * @throws UnknownTaskException
     *             If an unknown training method is requested.
     */
    public static TrainingMethod getTrainingMethod(String trainingMethod,
            RCG g, BinaryRCG bg, Lexicon l, Numberer nb, String params)
            throws ParameterException, UnknownTaskException {

        if (trainingMethod == null) {
            throw new UnknownTaskException(
                    "Got null as requested training method");
        }

        if (TrainingMethods.MLE.equals(trainingMethod)) {
            return new MleTrainer(g, bg, l, nb);
        }

        throw new UnknownTaskException(trainingMethod);
    }

}
