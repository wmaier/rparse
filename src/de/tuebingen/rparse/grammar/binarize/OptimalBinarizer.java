/*******************************************************************************
 * File OptimalBinarizer.java
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * This head-driven binarizer chooses the head by minimizing first the maximal arity of the resulting predicates, and
 * then the total number of variables.
 * 
 * @author ke
 */
public class OptimalBinarizer extends MarkovizingBinarizer {

    public OptimalBinarizer(String params, int v, int h, boolean noArities)
            throws ParameterException {
        super(params, v, h, noArities);
    }

    private static class ReorderingPlan {

        private final Clause clause;

        private int[]        newOrder;

        int                  nextPos;

        List<Integer>        currentCharacteristicString;

        public ReorderingPlan(Clause clause) {
            this.clause = clause;
            newOrder = new int[clause.rhsnames.length];
            nextPos = 0;
            currentCharacteristicString = initialCharacteristicString(clause);
        }

        private static List<Integer> initialCharacteristicString(Clause clause) {
            List<Integer> result = new LinkedList<Integer>();

            for (int[] lhsarg : clause.lhsargs) {
                for (int var : lhsarg) {
                    result.add(var);
                }

                result.add(-1); // separator
            }

            return result;
        }

        /**
         * Records a decision: we choose the predicate at this position next.
         * 
         * @param rhsPos
         */
        public void decide(int rhsPos) {
            newOrder[nextPos++] = rhsPos;
            maskOutVariables(currentCharacteristicString,
                    clause.rhsargs[rhsPos]);
        }

        private static void maskOutVariables(
                List<Integer> characteristicString, int[] variables) {
            int size = characteristicString.size();

            for (int i = 0; i < size; i++) {
                if (contains(variables, characteristicString.get(i))) {
                    characteristicString.remove(i);
                    characteristicString.add(i, -1);
                }
            }
        }

        /**
         * Returns the arity of the rest predicate if we were to choose the predicate at a certain position.
         * 
         * @param rhsPos
         * @return
         */
        public int getRestPredicateArity(int rhsPos) {
            return characteristicStringArity(currentCharacteristicString,
                    clause.rhsargs[rhsPos]);
        }

        private static int characteristicStringArity(
                List<Integer> characteristicString, int[] separatingVariables) {
            boolean inSubstring = false;
            int result = 0;

            for (int variable : characteristicString) {
                if (inSubstring) {
                    if (variable == -1
                            || contains(separatingVariables, variable)) {
                        inSubstring = false;
                    }
                } else {
                    if (variable != -1
                            && !contains(separatingVariables, variable)) {
                        result++;
                        inSubstring = true;
                    }
                }
            }

            return result;
        }

        public int[] getNewOrder() {
            return newOrder;
        }

    }

    protected int determineNextRhsPos(Set<Integer> remainingPositions,
            ReorderingPlan plan, Clause clause) {
        int arity = Integer.MAX_VALUE;
        int vars = Integer.MAX_VALUE;
        int candidate = 0;

        for (int i : remainingPositions) {
            // arity of the rest predicate that would result if we picked i
            // as head position
            int restPredicateArity = plan.getRestPredicateArity(i);

            // number of arguments of the RHS predicate at position i
            int rhsPredicateArgs = clause.rhsargs[i].length;

            if ((restPredicateArity < arity && rhsPredicateArgs < arity)
                    || (restPredicateArity <= arity
                            && rhsPredicateArgs <= arity && restPredicateArity
                            + rhsPredicateArgs < vars)) {
                arity = Math.max(restPredicateArity, rhsPredicateArgs);
                vars = restPredicateArity + rhsPredicateArgs;
                candidate = i;
            }
        }

        return candidate;
    }

    private static boolean contains(int[] haystack, int needle) {
        for (int element : haystack) {
            if (element == needle) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected int[] determineNewOrder(Clause clause) {
        ReorderingPlan plan = new ReorderingPlan(clause);
        Set<Integer> remainingPositions = new HashSet<Integer>();

        for (int i = 0; i < clause.rhsargs.length; i++) {
            remainingPositions.add(i);
        }

        while (remainingPositions.size() > 1) {
            int decision = determineNextRhsPos(remainingPositions, plan, clause);
            plan.decide(decision);
            remainingPositions.remove(decision);
        }

        plan.decide(remainingPositions.iterator().next());

        int[] newOrder = plan.getNewOrder();
        
        int[] reverseNewOrder = new int[newOrder.length];
        for (int i = 0; i < newOrder.length; ++i) {
            reverseNewOrder[newOrder.length - 1 - i] = newOrder[i];
        }
        return reverseNewOrder;
    }

}
