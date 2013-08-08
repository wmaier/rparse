/*******************************************************************************
 * File DirectoryTreebankProcessor.java
 * 
 * Authors:
 *    Wolfgang Maier
 *        
 * Copyright:
 *    Wolfgang Maier, 2011
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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import de.tuebingen.rparse.misc.Ranges;

/**
 * A treebank processor which recurses over all files in a certain directory.
 * 
 * @author wmaier
 * @param <S>
 *            The type of structures to process.
 */
public abstract class DirectoryTreebankProcessor<S>
        extends
            TreebankProcessor<S> {

    /**
     * Process all files in a certain directory with the underlying treebank processor.
     * 
     * @param treebankDirectory
     *            The directory to recurse
     * @param encoding
     *            The encoding of all files in the directory (It is not possible to specify individual encodings)
     * @param filter
     *            A glob filter on the files
     * @param task
     *            The processing task
     * @param ranges
     *            Sentence range to process
     * @param maxlen
     *            Maximum length of a sentence, longer sentences will not be processed.
     * @throws IOException
     *             On unexpected I/O
     * @throws TreebankException
     *             If something goes wrong during processing.
     */
    public void processDirectory(File treebankDirectory, String encoding,
            FileFilter filter, ProcessingTask<? super S> task, Ranges ranges,
            Integer maxlen) throws IOException, TreebankException {
        for (File f : treebankDirectory.listFiles()) {
            if (filter.accept(f)) {
                logger.info("Processing: " + f.getAbsoluteFile().toString());
                Reader treebankReader = new InputStreamReader(
                        new FileInputStream(f), encoding);
                process(treebankReader, task, ranges, maxlen);
            }
        }
    }

    /**
     * Called when we are finished processing some file in the directory
     */
    public void fileDone() {
    }

}
