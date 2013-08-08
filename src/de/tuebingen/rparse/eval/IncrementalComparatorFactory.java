/*******************************************************************************
 * File IncrementalComparatorFactory.java
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

import java.util.logging.Logger;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Factory which delivers comparators for constituency trees.
 * 
 * @author wmaier
 */
public class IncrementalComparatorFactory {

    /**
     * Get a comparator for parse evaluation of constituency parses.
     * 
     * @param model
     *            The metric to get
     * @param ignoreMissing
     *            Ignore missing sentences or make them influence the result
     * @param highGapBlock
     *            Exclude sentences with more gaps than this threshold
     * @param lowGapBlock
     *            Exclude sentences with less gaps than this threshold
     * @param gf
     *            Consider grammatical functions (Not offered by all comparators)
     * @param nb
     *            A numberer from the parser
     * @return The requested comparator
     * @throws EvalException
     *             Thrown if there is any problem
     * @throws UnknownTaskException
     */
    public static IncrementalComparator<Tree> getConstituentComparator(
            String model, boolean ignoreMissing, int highGapBlock,
            int lowGapBlock, boolean gf, Numberer nb) throws EvalException,
            UnknownTaskException {

        if (model == null) {
            throw new UnknownTaskException("Got null as requested model");
        }

        Logger logger = Logger.getLogger(IncrementalComparatorFactory.class
                .getPackage().getName());
        logger.info("Getting comparator for " + model);

        if (model.equals(EvalTypes.TREEDISTWHOLE)) {
            return new TreedistComparator(false, ignoreMissing, highGapBlock,
                    lowGapBlock);
        }

        if (model.equals(EvalTypes.TREEDISTROOF)) {
            return new TreedistComparator(true, ignoreMissing, highGapBlock,
                    lowGapBlock);
        }

        if (model.equals(EvalTypes.EVALB)) {
            return new EvalbComparator(ignoreMissing, highGapBlock,
                    lowGapBlock, gf, nb);
        }

        throw new UnknownTaskException(
                "Comparator for unknown model requested.");
    }

    /**
     * Get a comparator for dependencies
     * 
     * @param highGapBlock
     *            Exclude sentences with more gaps than this threshold
     * @param lowGapBlock
     *            Exclude sentences with less gaps than this threshold
     * @return The comparator
     * @throws EvalException
     *             If something goes wrong
     */
    public static IncrementalComparator<DependencyForest<DependencyForestNodeLabel, String>> getDependencyComparator(
            int highGapBlock, int lowGapBlock) {

        return new DepComparator(highGapBlock, lowGapBlock);
    }

}
