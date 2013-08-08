/*******************************************************************************
 * File ReorderingBinarizer.java
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
package de.tuebingen.rparse.grammar.binarize;

import java.util.Arrays;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * This is the superclass for all binarizers which are such that the head ends up being the lowest subtree:
 * 
 * @author ke, wmaier
 */
public abstract class ReorderingBinarizer extends AbstractBinarizer {

    private int isLeftToRightCount;

    private int isRightToLeftCount;

    private int isMixedCount;

    private int totalCount;

    private int binarizedCount;

    public ReorderingBinarizer(String params) throws ParameterException {
        super(params);
        isLeftToRightCount = 0;
        isRightToLeftCount = 0;
        isMixedCount = 0;
        totalCount = 0;
        binarizedCount = 0;
    }

    protected abstract int[] determineNewOrder(Clause clause);

    @Override
    protected final Clause preprocessClause(Clause clause, Numberer nb) {
        int[] newOrder = determineNewOrder(clause);
        testOrder(newOrder); // safety check, lest subclasses screw up
        collectStats(newOrder, clause);

        int[] newRhsNames = new int[clause.rhsnames.length];
        int[][] newRhsArgs = new int[clause.rhsargs.length][];
        boolean[] newRhsPTerm = new boolean[clause.getRhspterm().length];
        int newRhsPos = 0;

        for (int i : newOrder) {
            newRhsNames[newRhsPos] = clause.rhsnames[i];
            newRhsArgs[newRhsPos] = clause.rhsargs[i];
            newRhsPTerm[newRhsPos] = clause.getRhspterm()[i];
            newRhsPos++;
        }

        int[] inverseNewRhsNames = new int[clause.rhsnames.length];
        int[][] inverseNewRhsArgs = new int[clause.rhsargs.length][];
        boolean[] inverseNewRhsPTerm = new boolean[clause.getRhspterm().length];
        int inverseNewRhsPos = clause.rhsnames.length - 1;
        for (int i = 0; i < clause.rhsnames.length && inverseNewRhsPos >= 0; ++i) {
            inverseNewRhsNames[i] = newRhsNames[inverseNewRhsPos];
            inverseNewRhsArgs[i] = newRhsArgs[inverseNewRhsPos];
            inverseNewRhsPTerm[i] = newRhsPTerm[inverseNewRhsPos];
            inverseNewRhsPos--;
        }
        newRhsNames = inverseNewRhsNames;
        newRhsArgs = inverseNewRhsArgs;
        newRhsPTerm = inverseNewRhsPTerm;

        clause.rhsnames = newRhsNames;
        clause.rhsargs = newRhsArgs;
        clause.setRhspterm(newRhsPTerm);

        return clause;

    }

    private void testOrder(int[] order) {
        boolean[] seen = new boolean[order.length];

        for (int position : order) {
            try {
                seen[position] = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException(getClass()
                        + " produced order with invalid position " + position
                        + ".", e);
            }
        }

        for (boolean s : seen) {
            if (!s) {
                throw new RuntimeException(getClass()
                        + " produced incomplete order.");
            }
        }

    }

    private void collectStats(int[] order, Clause clause) {

        if (clause.rhsnames.length > 2) {
            boolean isltor = true;
            boolean isrtol = true;

            for (int i = 1; i < order.length; ++i) {
                isltor &= order[i] == order[i - 1] + 1;
                isrtol &= order[i] == order[i - 1] - 1;
            }

            if (isltor) {
                isLeftToRightCount++;
            }
            if (isrtol) {
                isRightToLeftCount++;
            }
            if (!isltor && !isrtol)
                isMixedCount++;

            if (isltor && isrtol) {
                System.err.println(Arrays.toString(order));
            }

            ++binarizedCount;
        }

        ++totalCount;
    }

    @Override
    public String getStats() {
        // orders are inversed, therefore ascending (left-to-right) is right to left and vice versa
        return "l-to-r: " + isRightToLeftCount + ", r-to-l: "
                + isLeftToRightCount + ", mixed: " + isMixedCount + "\n"
                + "bin: " + binarizedCount + ", tot: " + totalCount + "\n";
    }

}
