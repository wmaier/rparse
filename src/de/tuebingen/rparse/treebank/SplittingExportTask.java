/*******************************************************************************
 * File SplittingExportTask.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import de.tuebingen.rparse.misc.Test;

/**
 * Export task which writes every sentence from the input treebank to a separate file in a specified directory,
 * following the specified naming scheme. The constructor expects a {@link SentenceWriter} so it knows how to serialize
 * any given sentence.
 * 
 * @author ke, wmaier
 * @param <S>
 *            The type of sentences to be serialized (constituents or dependencies).
 */
public class SplittingExportTask<S extends HasID> extends ExportTask<S> {

    /**
     * A sentence writer, used to write single sentences
     */
    protected SentenceWriter<S> sentenceWriter;

    /**
     * The directory where to write everything
     */
    protected File              directory;

    /**
     * A prefix for all files
     */
    protected String            prefix;

    /**
     * A suffix for all files 
     */
    protected String            suffix;

    /**
     * Construct a new splitting export task.
     * 
     * @param sentenceWriter
     *            The sentence writer which serializes single sentences.
     * @param directory
     *            The directory where to write the results.
     * @param prefix
     *            The prefix for all files in the directory
     * @param suffix
     *            The suffix for all files in the directory
     * @param filters
     *            A list of tests, to be used as filters. Only those sentences are written which pass all the filters.
     */
    public SplittingExportTask(SentenceWriter<S> sentenceWriter,
            File directory, String prefix, String suffix, List<Test<S>> filters) {
        super(filters);
        this.sentenceWriter = sentenceWriter;
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;

        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    @Override
    public void exportSentence(S sentence) throws TreebankException {
        try {
            // get the sentence id
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(directory, prefix
                            + sentence.getId() + suffix))));
            sentenceWriter.write(sentence, writer);
            writer.close();
        } catch (FileNotFoundException e) {
            throw new TreebankException("error splitting into files", e);
        } catch (IOException e) {
            throw new TreebankException("error splitting into files", e);
        }
    }

    @Override
    public void exportDone() {
    }

}
