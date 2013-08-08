/*******************************************************************************
 * File TreebankProcessor.java
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
package de.tuebingen.rparse.treebank;

import java.io.IOException;
import java.io.Reader;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.Ranges;

/**
 * Abstract superclass for processors of treebanks in various formats. A treebank is a sequence of sentences, read from
 * a single {@link Reader}. Processors support processing either the whole sequence or a specified range therein.
 * 
 * @author ke
 * @param <S>
 *            Type of the analysis of a sentence, e.g. tree or dependency graph.
 */
public abstract class TreebankProcessor<S> {

    /**
     * A logger which can also be used by the subclasses.
     */
    protected Logger logger;

    public TreebankProcessor() {
        logger = Logger.getLogger(TreebankProcessor.class.getPackage()
                .getName());
    }

    /**
     * Carry out the given task on a treebank read from a {@link Reader}. Implementations must read the treebank from
     * the given {@link Reader} in their respective format and call
     * <ol>
     * <li>the processing task's {@code process} method on every sentence in the specified range (if the range extends
     * beyond what's available in the input, implementations should handle the available part, if any, normally, and do
     * nothing about the unavailable parts)</li>
     * <li>the processing task's {@code done} method when processing is finished</li>
     * </ol>
     * 
     * @param treebankReader
     * @param task
     * @param r
     *            A set of {@link Ranges} denoting the sentences to process. Can be null if no ranges are set, i.e., if
     *            all sentences are to be processed.
     * @throws IOException
     * @throws TreebankException
     */
    public abstract void process(Reader treebankReader,
            ProcessingTask<? super S> task, Ranges ranges, Integer maxlen)
            throws IOException, TreebankException;

    /**
     * Carry out the given task on a treebank read from a {@link reader}.
     * 
     * @param treebankReader
     * @param task
     * @throws IOException
     * @throws TreebankException
     */
    public final void process(Reader treebankReader,
            ProcessingTask<? super S> task) throws IOException,
            TreebankException {
        process(treebankReader, task, new Ranges(""), Integer.MAX_VALUE);
    }

    /**
     * Returns the logger of this
     * 
     * @return
     */
    public final Logger getLogger() {
        return logger;
    }

    /**
     * Basic range checking
     * 
     * @param sentenceNumber
     *            The sentence number to be checked for compatibility with a range specification
     * @param r
     *            The ranges
     * @return A corresponding boolean value indicating if the sentence lies in the range or not
     */
    protected boolean isInRange(int sentenceNumber, Ranges r) {
        if (r == null)
            return true;
        if (r.isUnbounded())
            return true;
        for (Integer i : r) {
            if (sentenceNumber == i)
                return true;
            if (i > sentenceNumber)
                return false;
        }
        return false;
    }

}
