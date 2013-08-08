/*******************************************************************************
 * File TreedistComparator.java
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

import java.util.Collections;

import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * A comparator using tree edit distance and Dice/Jaccard normalization for comparing whole or roof trees, cf. Emms
 * (2008).
 * 
 * @author ke
 */
public class TreedistComparator extends IncrementalComparator<Tree> {

    int     count                 = 0;

    double  diceSum               = 0;

    double  jaccardSum            = 0;

    long    diceNumeratorSum      = 0;

    long    diceDenominatorSum    = 0;

    long    jaccardNumeratorSum   = 0;

    long    jaccardDenominatorSum = 0;

    boolean doRoof;

    protected TreedistComparator(boolean doRoof, boolean ignoreMissing,
            int gapblock, int lowGapBlock) throws EvalException {
        super(ignoreMissing, gapblock, lowGapBlock);

        if (!ignoreMissing) {
            throw new EvalException(
                    "Treedist comparator must ignore missing answer trees.");
        }

        this.doRoof = doRoof;
    }

    @Override
    protected void compare(Tree sentence1, Tree sentence2, int id)
            throws EvalException {
        if (doRoof) {
            sentence1.reduceToRoof();
            sentence2.reduceToRoof();
        }

        TreeEditDistanceComputer computer = new TreeEditDistanceComputer(
                Collections.singletonList(sentence1.getRoot()),
                Collections.singletonList(sentence2.getRoot()));
        EditStats stats = computer.editStats();
        doCompare(sentence1, sentence2, id, stats);
    }

    protected void doCompare(Tree sentence1, Tree sentence2, int id,
            EditStats stats) {
        double diceNumerator = diceNumerator(stats);
        double diceDenominator = diceDenominator(stats, sentence1, sentence2);
        double jaccardNumerator = jaccardNumerator(stats);
        double jaccardDenominator = jaccardDenominator(stats);
        double dice = 1 - (diceNumerator / diceDenominator);
        double jaccard = 1 - (jaccardNumerator / jaccardDenominator);
        diceNumeratorSum += diceNumerator;
        diceDenominatorSum += diceDenominator;
        jaccardNumeratorSum += jaccardNumerator;
        jaccardDenominatorSum += jaccardDenominator;
        diceSum += dice;
        jaccardSum += jaccard;
        count++;
        System.out.print(id);
        System.out.print("\t");
        System.out.print("Length: " + sentence1.size());
        System.out.print("\t");
        System.out.print("TED: " + distance(stats));
        System.out.print("\t");
        System.out.print("Dice: " + dice);
        System.out.print("\t");
        System.out.println("Jaccard: " + jaccard);
    }

    @Override
    protected void missingAnswer(Tree keyTree, int id) throws EvalException {
        throw new EvalException(
                "Treedist comparator cannot handle missing answers, use ignore option.");
    }

    private int distance(EditStats stats) {
        return stats.deleted + stats.inserted + stats.swapped;
    }

    private long jaccardNumerator(EditStats stats) {
        return stats.deleted + stats.inserted + stats.swapped;
    }

    private long jaccardDenominator(EditStats stats) {
        return stats.deleted + stats.inserted + stats.swapped + stats.matched;
    }

    private long diceNumerator(EditStats stats) {
        return stats.deleted + stats.inserted + stats.swapped;
    }

    private long diceDenominator(EditStats stats, Tree sentence1, Tree sentence2) {
        return sentence1.getNodes().size() + sentence2.getNodes().size();
    }

    @Override
    protected void done(int tooLong, int missing, int blocked, int lblock) {
        System.out.println();
        printTooLong(tooLong);
        printMissing(missing);
        printHighGapBlocked(blocked);
        printLowGapBlocked(lblock);
        System.out.println();
        System.out.println("Dice micro-average    : " + diceSum / count);
        System.out.println("Jaccard micro-average : " + jaccardSum / count);
        System.out.println("Dice macro-average    : "
                + (1 - ((double) diceNumeratorSum) / diceDenominatorSum));
        System.out.println("Jaccard macro-average : "
                + (1 - ((double) jaccardNumeratorSum) / jaccardDenominatorSum));
    }

}
