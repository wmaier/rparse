/*******************************************************************************
 * File EditStats.java
 * 
 * Authors:
 *    Kilian Evang
 *    
 * Copyright:
 *    Kilian Evang, 2011
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

import de.tuebingen.rparse.misc.LinkedListNode;
import de.tuebingen.rparse.treebank.constituent.Node;

/**
 * Collects an edit script, and, for convenience, delete/insert/swap/match counts for scoring it.
 * 
 * @author ke
 */
public class EditStats implements Comparable<EditStats> {

    public final int                           deleted;

    public final int                           inserted;

    public final int                           swapped;

    public final int                           matched;

    public final LinkedListNode<EditOperation> editScript;

    public EditStats() {
        this(0, 0, 0, 0, null);
    }

    public EditStats(int deleted, int inserted, int swapped, int matched,
            LinkedListNode<EditOperation> editScript) {
        this.deleted = deleted;
        this.inserted = inserted;
        this.swapped = swapped;
        this.matched = matched;
        this.editScript = editScript;
    }

    public EditStats delete(Node deletedNode) {
        return new EditStats(deleted + 1, inserted, swapped, matched,
                new LinkedListNode<EditOperation>(
                        EditOperation.delete(deletedNode), editScript));
    }

    public EditStats insert(Node insertedNode) {
        return new EditStats(deleted, inserted + 1, swapped, matched,
                new LinkedListNode<EditOperation>(
                        EditOperation.insert(insertedNode), editScript));
    }

    public EditStats swap(Node oldNode, Node newNode) {
        return new EditStats(deleted, inserted, swapped + 1, matched,
                new LinkedListNode<EditOperation>(EditOperation.swap(oldNode,
                        newNode), editScript));
    }

    public EditStats match(Node nodeMatchedFrom, Node nodeMatchedTo) {
        return new EditStats(deleted, inserted, swapped, matched + 1,
                new LinkedListNode<EditOperation>(EditOperation.match(
                        nodeMatchedFrom, nodeMatchedTo), editScript));
    }

    private int distance() {
        return deleted + inserted + swapped;
    }

    /**
     * Orders {@link EditStats} objects by tree edit distance in increasing order.
     * 
     * @param o
     *            The {@link EditStats}
     * @return
     */
    @Override
    public int compareTo(EditStats o) {
        return distance() - o.distance();
    }

    public EditStats add(EditStats o) {
        return new EditStats(deleted + o.deleted, inserted + o.inserted,
                swapped + o.swapped, matched + o.matched,
                LinkedListNode.append(o.editScript, editScript));
    }

    public static EditStats min(EditStats... candidates) {
        EditStats champion = candidates[0];
        int record = champion.distance();

        for (int i = 1; i < candidates.length; i++) {
            int distance = candidates[i].distance();

            if (distance < record) {
                champion = candidates[i];
                record = distance;
            }
        }

        return champion;
    }

}
