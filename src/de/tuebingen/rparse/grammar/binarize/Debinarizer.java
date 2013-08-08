/*******************************************************************************
 * File Debinarizer.java
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
package de.tuebingen.rparse.grammar.binarize;

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * Un-binarize the binary trees which come out of the parser.
 * 
 * @author ke
 */
public class Debinarizer {

    /**
     * Recursively un-binarize a binary tree from the parser output (destructive)
     * 
     * @param tree
     *            The tree to un-binarize.
     */
    public static void debinarize(Tree tree) {
        boolean exportNumbering = tree.hasExportNumbering();
        Node root = tree.getRoot();
        debinarize(root, tree);

        if (exportNumbering) {
            tree.calcExportNumbering();
        }
    }

    private static void debinarize(Node node, Tree tree) {
        int pos = 0;

        for (Node child : node.getChildren()) {
            pos = debinarize(child, node, pos, tree);
        }
    }

    private static int debinarize(Node node, Node newParent, int pos, Tree tree) {
        if (node.getLc() != null
                && node.getLabel().getTag()
                        .startsWith(Binarizer.NEW_PRED_NAME_PREFIX)) {
            node.unlink();

            for (Node child : node.getChildren()) {
                pos = debinarize(child, newParent, pos, tree);
            }

            return pos;
        } else {
            debinarize(node, tree);

            try {
                node.moveAsChild(newParent, pos);
            } catch (TreebankException e) {
                throw new RuntimeException(
                        "unexpected error while moving node", e);
            }

            return pos + 1;
        }
    }

}
