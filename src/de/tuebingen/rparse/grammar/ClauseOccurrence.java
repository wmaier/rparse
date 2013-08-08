/*******************************************************************************
 * File ClauseOccurrence.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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

import java.io.Serializable;

import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.dep.DependencyForestNode;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

/**
 * Add fields here for everything that needs to be known about a particular "occurrence" of a clause in a treebank.
 * 
 * @author ke, wmaier
 */
public class ClauseOccurrence implements Serializable {

    private static final long serialVersionUID = 8721235586179332211L;

    private final String      vertical;

    private ClauseOccurrence(String vertical) {
        this.vertical = vertical;
    }

    /**
     * Get the vertical occurrence string of this clause occurrence.
     * 
     * @return The corresponding value.
     */
    public String getVertical() {
        return vertical;
    }

    @Override
    public String toString() {
        return "occ vertical " + vertical;
    }

    /**
     * For constituency: Create an occurrence from a node. Relies on that the node has been preprocessed, i.e., that its
     * {@code getVertical()} yields already the right string.
     * 
     * @param n
     *            The node.
     * @return The ClauseOccurrence instance.
     */
    public static ClauseOccurrence create(Node n) {
        return create(n.getLabel().getVertical());
    }

    /**
     * For dependencies: Create an occurrence from a node. Relies on that the node has been preprocessed, i.e., that its
     * {@code getVertical()} yields already the right string.
     * 
     * @param n
     *            The node
     * @return The ClauseOccurrence instance.
     */
    public static ClauseOccurrence create(
            DependencyForestNode<DependencyForestNodeLabel, String> n) {
        String vertical = n.getGraph().getVertical(n.getID());
        return create(vertical);
    }

    /**
     * A simple string as vertical context.
     * 
     * @param vertical
     *            A string representing the vertical context
     * @return the ClauseOccurrence
     */
    public static ClauseOccurrence create(String vertical) {
        return new ClauseOccurrence(vertical);
    }

}
