/*******************************************************************************
 * File EditOperation.java
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

import de.tuebingen.rparse.treebank.constituent.Node;

/**
 * An edit operation in the context of the tree distance metric.
 * 
 * @author ke
 */
public class EditOperation {

    /**
     * The type of the operation
     */
    public final EditOperationType type;

    /**
     * Status of the node before the operation
     */
    public final Node              before;

    /**
     * Status of the node after the operation
     */
    public final Node              after;

    private EditOperation(EditOperationType type, Node before, Node after) {
        this.type = type;
        this.before = before;
        this.after = after;
    }

    public static EditOperation delete(Node deletedNode) {
        return new EditOperation(EditOperationType.DELETE, deletedNode, null);
    }

    public static EditOperation insert(Node insertedNode) {
        return new EditOperation(EditOperationType.INSERT, null, insertedNode);
    }

    public static EditOperation swap(Node oldNode, Node newNode) {
        return new EditOperation(EditOperationType.SWAP, oldNode, newNode);
    }

    public static EditOperation match(Node nodeMatchedFrom, Node nodeMatchedTo) {
        return new EditOperation(EditOperationType.MATCH, nodeMatchedFrom,
                nodeMatchedTo);
    }

}
