/*******************************************************************************
 * File ConstituentParentAnnotator.java
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
package de.tuebingen.rparse.treebank.constituent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rparse.misc.NullTest;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.VerticalMarkovizer;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Produces annotation for vertical markovization (parent annotation) in constituency trees.
 * 
 * @author wmaier
 */
public class ConstituentParentAnnotator extends ProcessingTask<Tree>
        implements
            VerticalMarkovizer,
            Serializable {

    private static final long  serialVersionUID = -7675583929943804748L;

    /**
     * Separates the vertical annotation from the label
     */
    public final static String VERTICALSEP      = "^";

    /**
     * Separates the horizontal annotaiton from the label // TODO constants should be moved
     */
    public final static String HORIZONTALSEP    = "-";

    // depth of vertical annotation
    private int                v;

    // controls if the label in the histories have arities or not
    private boolean            markovNoArities;

    /**
     * A numberer to get the names of the labels
     */
    protected Numberer         nb;

    /**
     * Constructs a new constituent parent annotator
     * 
     * @param v
     *            The depth of the annotation
     * @param markovNoArities
     *            If this is true, there will be no arity symbols on the vertical history symbols
     * @param nb
     *            A numberer which contains the names/numbers for the labels
     */
    public ConstituentParentAnnotator(int v, boolean markovNoArities,
            Numberer nb) {
        this.v = v;
        this.markovNoArities = markovNoArities;
        this.nb = nb;
    }

    @Override
    public void processSentence(Tree t) throws TreebankException {
        List<Node> orderedTerminals = t.getOrderedTerminals();
        for (Node n : t.getRoot().getNodes(new ArrayList<Node>())) {
            String b = "";
            // add parent annotation
            Node dom = n;
            int domind = 0;
            for (; dom != null && domind < v; ++domind) {
                b += VERTICALSEP + dom.getLabel().getTag();
                if (!markovNoArities) {
                    int blockdegree = dom.calcGapDegree(orderedTerminals,
                            new NullTest<Node>()) + 1;
                    b += String.valueOf(blockdegree);
                }
                dom = dom.getPa();
            }
            // set the annotation fields in the node
            n.getLabel().setVertical(b);
            n.getLabel().setVerticalDepth(v);
        }
    }

    /**
     * Get the numberer which maps the labels to numbers
     * 
     * @return The numberer
     */
    public Numberer getNumberer() {
        return nb;
    }

    /**
     * Get the vertical annotation depth
     * 
     * @return The corresponding value
     */
    public int getV() {
        return v;
    }

}
