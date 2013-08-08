/*******************************************************************************
 * File ExportTask.java
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
package de.tuebingen.rparse.treebank;

import java.util.List;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.Test;

/**
 * Processing task that exports a treebank to a different format.
 * 
 * @author ke, wmaier
 * @param <S>
 *            The type of structures to be written (constituents or dependencies)
 */
public abstract class ExportTask<S> extends ProcessingTask<S> {

    /**
     * Counts how many sentences are excluded
     */
    private int           exclusionCount;

    /**
     * A logger, can also be used by the subclasses.
     */
    private Logger        logger;

    /**
     * A list of filters, to manage the exclusion of sentences.
     */
    private List<Test<S>> filters;

    /**
     * Construct an export task.
     * 
     * @param filters
     *            A list of filters. Only the sentences which pass all the filters will be included in the output.
     */
    protected ExportTask(List<Test<S>> filters) {
        exclusionCount = 0;
        logger = Logger.getLogger(ExportTask.class.getPackage().getName());
        this.filters = filters;
    }

    @Override
    public final void processSentence(S sentence) throws TreebankException {
        // TODO: I don't understand the following comment. Does the filtering work at all?
        // check for null here, so we can make processors which throw out
        // certain sentences
        boolean include = true;
        for (Test<S> test : filters) {
            include &= test.test(sentence);
        }
        if (!include) {
            exclusionCount++;
        } else {
            exportSentence(sentence);
        }
    }

    @Override
    public final void done() throws TreebankException {
        if (exclusionCount > 0) {
            logger.info(exclusionCount + " sentences not processed.");
        }
        exportDone();
    }

    /**
     * Specifies in subclasses how the sentence, resp. its structure, should be written.
     * 
     * @param sentence
     *            The structure over the sentence.
     * @throws TreebankException
     *             Thrown if anything goes wrong during writing.
     */
    abstract public void exportSentence(S sentence) throws TreebankException;

    /**
     * Called after finishing the exporting of a single sentence.
     */
    abstract public void exportDone();

}
