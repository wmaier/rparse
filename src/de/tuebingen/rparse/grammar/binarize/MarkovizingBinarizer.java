/*******************************************************************************
 * File MarkovizingBinarizer.java
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
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.treebank.constituent.ConstituentParentAnnotator;

/**
 * Abstract class below all those binarizers that offer markovization on labels.
 * 
 * @author wmaier
 */
abstract public class MarkovizingBinarizer extends ReorderingBinarizer {

    /**
     * The degree of vertical markovization
     */
    protected int     v;

    /**
     * The degree of horizontal markovization
     */
    protected int     h;

    /**
     * Annotation of the head on the binarization non-terminals
     */
    protected boolean headAnnotation;

    /**
     * Remove the arities from the labels in the markovization parts of the binarization non-terminals
     */
    protected boolean noArities;

    public MarkovizingBinarizer(String params, int v, int h, boolean noArities)
            throws ParameterException {
        super(params);
        this.v = v;
        this.h = h;
        this.noArities = noArities;
        String[] paramArray = params.split("-");
        this.headAnnotation = Utilities.arrayContains(paramArray,
                "headAnnotation");
    }

    @Override
    protected String getNextName(Clause cl, String basename, String infix,
            int rhspos, int arity, Numberer nb) {
        if (headAnnotation)
            basename += "%"
                    + (String) nb.getObjectWithId(GrammarConstants.PREDLABEL,
                            cl.rhsnames[cl.getHeadPos()]);
        // v
        String vert = "";
        if (v > 0) {
            if (!cl.getVerticalContext().equals("")) // && rhspos >= 0)
                vert += cl.getVerticalContext();
        }
        // h
        String horiz = "";
        if (h > 0) {
            int i = rhspos + 1;
            int cnt = 0;
            for (; i >= 0 && cnt < h; --i) {
                ++cnt;
                String nonTerminal = (String) nb.getObjectWithId(
                        GrammarConstants.PREDLABEL, cl.rhsnames[i]);
                if (noArities) {
                    nonTerminal = Utilities.removeArity(nonTerminal);
                }
                horiz += ConstituentParentAnnotator.HORIZONTALSEP + nonTerminal;
            }
        }
        String arityString = String.valueOf(arity);
        return Binarizer.NEW_PRED_NAME_PREFIX + basename + vert + horiz
                + Binarizer.NEW_PRED_NAME_SUFFIX + arityString;
    }

    @Override
    public final boolean doRetrain() {
        return true;
    }

}
