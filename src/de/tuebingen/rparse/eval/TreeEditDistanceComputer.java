/*******************************************************************************
 * File TreeEditDistanceComputer.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.treebank.constituent.Node;

/**
 * An implementation of the simple tree edit distance described in Bille (2005), section 3.2.1, using top-down dynamic
 * programming (i.e. caching of results).
 * 
 * @author ke
 */
public class TreeEditDistanceComputer {

    private static final List<Node>                     EMPTY_FOREST = Collections
                                                                             .emptyList();

    private List<Node>                                  forest1;

    private List<Node>                                  forest2;

    private Map<List<Node>, Map<List<Node>, EditStats>> cache;

    public TreeEditDistanceComputer(List<Node> forest1, List<Node> forest2) {
        this.forest1 = forest1;
        this.forest2 = forest2;
        cache = new HashMap<List<Node>, Map<List<Node>, EditStats>>();
    }

    public EditStats editStats() {
        return editStats(forest1, forest2);
    }

    /**
     * This method looks biggish, but all it does is handle caching. computeEditStats does the real work.
     * 
     * @param forest1
     *            Gold data
     * @param forest2
     *            Parser output
     * @return The edit script
     */
    private EditStats editStats(List<Node> forest1, List<Node> forest2) {
        if (cache.containsKey(forest1)) {
            Map<List<Node>, EditStats> statsByForest2 = cache.get(forest1);

            if (statsByForest2.containsKey(forest2)) {
                return statsByForest2.get(forest2);
            }

            EditStats result = computeEditStats(forest1, forest2);
            statsByForest2.put(forest2, result);
            return result;
        } else {
            Map<List<Node>, EditStats> statsByForest2 = new HashMap<List<Node>, EditStats>();
            cache.put(forest1, statsByForest2);
            EditStats result = computeEditStats(forest1, forest2);
            statsByForest2.put(forest2, result);
            return result;
        }
    }

    private EditStats computeEditStats(List<Node> forest1, List<Node> forest2) {
        if (forest2.isEmpty()) {
            if (forest1.isEmpty()) {
                return new EditStats();
            }

            return editStats(deleteRightmostRoot(forest1), EMPTY_FOREST)
                    .delete(rightmostRoot(forest1));
        } else {
            if (forest1.isEmpty()) {
                return editStats(EMPTY_FOREST, deleteRightmostRoot(forest2))
                        .insert(rightmostRoot(forest2));
            }

            Node v = rightmostRoot(forest1);
            Node w = rightmostRoot(forest2);

            EditStats deleteStats = editStats(deleteRightmostRoot(forest1),
                    forest2).delete(v);
            EditStats insertStats = editStats(forest1,
                    deleteRightmostRoot(forest2)).insert(w);
            EditStats matchOrSwapStats = editStats(v.getChildren(),
                    w.getChildren()).add(
                    editStats(deleteRightmostTree(forest1),
                            deleteRightmostTree(forest2)));

            if (v.getLabel().getTag().equals(w.getLabel().getTag())) {
                matchOrSwapStats = matchOrSwapStats.match(v, w);
            } else {
                matchOrSwapStats = matchOrSwapStats.swap(v, w);
            }

            return EditStats.min(deleteStats, insertStats, matchOrSwapStats);
        }
    }

    private Node rightmostRoot(List<Node> forest) {
        return forest.get(forest.size() - 1);
    }

    private List<Node> deleteRightmostRoot(List<Node> forest) {
        int limit = forest.size() - 1;
        Node rightmostRoot = forest.get(limit);
        List<Node> children = rightmostRoot.getChildren();
        List<Node> result = new ArrayList<Node>(limit + children.size());

        for (int i = 0; i < limit; i++) {
            result.add(forest.get(i));
        }

        result.addAll(children);
        return result;
    }

    private List<Node> deleteRightmostTree(List<Node> forest) {
        int limit = forest.size() - 1;
        List<Node> result = new ArrayList<Node>(limit);

        for (int i = 0; i < limit; i++) {
            result.add(forest.get(i));
        }

        return result;
    }

}
