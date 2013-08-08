/*******************************************************************************
 * File IncrementalComparator.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier 2011
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

import java.util.NoSuchElementException;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.Ranges;
import de.tuebingen.rparse.treebank.HasGapDegree;
import de.tuebingen.rparse.treebank.HasID;
import de.tuebingen.rparse.treebank.HasSize;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Compare two treebanks incrementally, i.e., sentence by sentence. The {@code compare} method of this class is
 * abstract. Subclasses can provide their evaluation strategy there. This class relies on
 * {@link IncrementalTreebankProcessor<T>} to read treebanks sentence by sentence. If a data format does not provide
 * explicit sentence numbers (such as the CONLL dependency format) then the treebank processor will number the sentences
 * consecutively. Consequently, for all sentences without a parse, when using a format such as CONLL, an fake sentence
 * must be provided in order to ensure that the right sentences are compared. In formats with an explicit sentence
 * numbering, such as export, sentences without a parse can just be left out since the corresponding processors will
 * provide the explicit numbering to the comparator.
 * 
 * @author wmaier
 * @param <T>
 *            The type of trees in the treebank, either constituency trees or dependencies. {@code HasSize},
 *            {@code HasID} and {@code HasGapDegree} must be implemented in order to manage the exclusion of sentences
 *            by length, ranges or gaps, respectively.
 */
public abstract class IncrementalComparator<T extends HasSize & HasID & HasGapDegree> {

    /**
     * Ignore missing sentences or make them influence the result.
     */
    protected final boolean ignoreMissing;

    /**
     * Exclude sentences with more gaps than this threshold
     */
    protected final int     highGapBlock;

    /**
     * Exclude sentences with less gaps than this threshold
     */
    protected final int     lowGapBlock;

    /**
     * A {@link Logger} which can also be used by the subclasses
     */
    protected Logger        logger;

    protected IncrementalComparator(int highGapBlock, int lowGapBlock) {
        this(false, highGapBlock, lowGapBlock);
    }

    protected IncrementalComparator(boolean ignoreMissing, int highGapBlock,
            int lowGapBlock) {
        logger = Logger.getLogger(IncrementalComparator.class.getPackage()
                .getName());
        this.ignoreMissing = ignoreMissing;
        this.highGapBlock = highGapBlock;
        this.lowGapBlock = lowGapBlock;
        if (highGapBlock < Integer.MAX_VALUE) {
            logger.info("Ignore sentences which have constituents with more than "
                    + highGapBlock + " gaps in the gold data.");
        }
        if (lowGapBlock > 0) {
            logger.info("Ignore sentences which have constituents with less than "
                    + lowGapBlock + " gaps in the gold data.");
        }
    }

    /**
     * Run the actual evaluation. This method calls the subclass implementations of the abstract methods in this class.
     * 
     * @param keyProcessor
     *            Treebank processor for reading the gold data
     * @param keyTask
     *            Preprocessing task for the gold data
     * @param answerProcessor
     *            Treebank processor for reading the parser output
     * @param answerTask
     *            Preprocessing task for the parser output
     * @param ranges
     *            Sentences to consider for evaluation in range notation (cf. {@link Ranges.java})
     * @param maxlen
     *            Maximum sentence length to be included in evaluation
     * @throws TreebankException
     *             If there is a problem reading the treebanks, this gets thrown
     * @throws EvalException
     *             If there is a problem regarding the evaluation, this gets thrown
     */
    public final void evaluate(IncrementalTreebankProcessor<T> keyProcessor,
            ProcessingTask<T> keyTask,
            IncrementalTreebankProcessor<T> answerProcessor,
            ProcessingTask<T> answerTask, Ranges ranges, int maxlen)
            throws TreebankException, EvalException {
        int tooLong = 0;
        int missing = 0;
        int highGapBlocked = 0;
        int lowGapBlocked = 0;

        int next = 1;
        T answerTree = null;
        int answerID = -1;

        for (int i : ranges) {
            while (i > next) {
                try {
                    keyProcessor.skipNext();
                } catch (NoSuchElementException e) {
                    break;
                }

                next++;
            }

            if (!keyProcessor.hasNext()) {
                break;
            }

            T keyTree = keyProcessor.next();
            next++;

            if (keyTree.size() <= maxlen) {

                if (keyTree.getGapDegree() <= highGapBlock) {

                    if (keyTree.getGapDegree() >= lowGapBlock) {

                        int keyID = keyTree.getId();

                        if (answerTree == null) {
                            if (!answerProcessor.hasNext()) {
                                missing++;

                                if (!ignoreMissing) {
                                    missingAnswer(keyTree, keyID);
                                }

                                continue;
                            }

                            answerTree = answerProcessor.next();
                            answerID = answerTree.getId();
                        }

                        while (answerID < keyID && answerProcessor.hasNext()) {
                            answerTree = answerProcessor.next();
                            answerID = answerTree.getId();
                        }

                        if (answerID == keyID) {
                            keyTask.processSentence(keyTree);
                            answerTask.processSentence(answerTree);
                            compare(keyTree, answerTree, keyID);
                            answerTree = null;
                        } else {
                            missing++;

                            if (!ignoreMissing) {
                                missingAnswer(keyTree, keyID);
                            }
                        }

                    } else {
                        lowGapBlocked++;
                    }

                } else {
                    highGapBlocked++;
                }

            } else {
                tooLong++;
            }
        }

        // Evaluation finished, compute final result now
        done(tooLong, missing, highGapBlocked, lowGapBlocked);
    }

    /**
     * Subclass method must perform the actual evaluation in this method.
     * 
     * @param key
     *            The gold tree
     * @param answer
     *            The parser output tree
     * @param id
     *            The id of the trees
     * @throws EvalException
     *             Thrown on problems.
     */
    protected abstract void compare(T sentence1, T sentence2, int id)
            throws EvalException;

    /**
     * Called when there is a tree in the gold data which has no corresponding tree in the parser output.
     * 
     * @param keyTree
     *            The gold tree
     * @param id
     *            The id of the gold tree
     * @throws EvalException
     *             Thrown on problems.
     */
    protected abstract void missingAnswer(T keyTree, int id)
            throws EvalException;

    /**
     * Called after all sentences are read.
     * 
     * @param tooLong
     *            Sentences which have been excluded for being too long
     * @param missing
     *            Sentences not present in the parser output
     * @param highGapBlocked
     *            Sentences excluded because they have more gaps than this threshold
     * @param lowGapBlocked
     *            Sentences excluded because they have less gaps than this threshold
     */
    protected abstract void done(int tooLong, int missing, int highGapBlocked,
            int lowGapBlocked);

    /**
     * Convenience method to be used within the subclasses
     * 
     * @param tooLong
     */
    protected void printTooLong(int tooLong) {
        System.out
                .println("Sentences with > maxlen, ignored                         : "
                        + tooLong);
    }

    /**
     * Convenience method to be used within the subclasses
     * 
     * @param highGapBlocked
     */
    protected void printHighGapBlocked(int highGapBlocked) {
        System.out.println("Sentences with gap degree > " + highGapBlock
                + ", ignored: " + highGapBlocked);
    }

    /**
     * Convenience method to be used within the subclasses
     * 
     * @param lowGapBlocked
     */
    protected void printLowGapBlocked(int lowGapBlocked) {
        System.out.println("Sentences with gap degree < " + lowGapBlock
                + ", ignored : " + lowGapBlocked);
    }

    /**
     * Convenience method to be used within the subclasses
     * 
     * @param missing
     */
    protected void printMissing(int missing) {
        if (ignoreMissing) {
            System.out
                    .println("Sentences missing in answer, ignored                     : "
                            + missing);
        } else {
            System.out
                    .println("Sentences missing in answer, affecting result            : "
                            + missing);
        }
    }

}
