/*******************************************************************************
 * File EstimatesFactory.java
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

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * A factory for estimate.
 * 
 * @author wmaier
 */
public class EstimatesFactory {

    /**
     * Get an estimate
     * 
     * @param type
     *            The desired type
     * @param bg
     *            The binary grammar on which to compute the estimate
     * @param nb
     *            The numberer of the grammar
     * @param sentlen
     *            The maximum sentence length up to which to compute the estimate
     * @return The actual estimate
     * @throws UnknownTaskException
     *             If there is no such estimate
     * @throws GrammarException 
     *             If there was a problem with constructing the estimate
     */
    public final static Estimate getEstimates(String type, BinaryRCG bg,
            Numberer nb, int sentlen) throws UnknownTaskException, GrammarException {

        if (type == null) {
            throw new UnknownTaskException("Got null as requested estimate");
        }

        if (EstimateTypes.OFF.equals(type)) {
            return new BypassEstimates(bg, nb, sentlen);
        }

        if (EstimateTypes.SX.equals(type)) {
            return new SXFull(bg, nb, sentlen);
        }

        if (EstimateTypes.SXDEDUCTION.equals(type)) {
            return new SXFullDeduction(bg, nb, sentlen);
        }

        if (EstimateTypes.SXSIMPLE.equals(type)) {
            return new SXSimple(bg, nb, sentlen);
        }

        if (EstimateTypes.SXSIMPLE_LR.equals(type)) {
            return new SXSimpleLR(bg, nb, sentlen);
        }

        if (EstimateTypes.SXSIMPLE_LN.equals(type)) {
            return new SXLN(bg, nb, sentlen);
        }

        throw new UnknownTaskException("Outside estimation method " + type
                + " not known.");

    }

}
