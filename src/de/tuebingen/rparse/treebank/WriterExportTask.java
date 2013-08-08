/*******************************************************************************
 * File WriterExportTask.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import de.tuebingen.rparse.misc.Test;

/**
 * Export task which writes all sentences from the treebank to the same writer.
 * 
 * @author ke, wmaier
 * @param <S>
 */
public class WriterExportTask<S> extends ExportTask<S> {

    protected SentenceWriter<S> sentenceWriter;

    protected Writer            writer;

    /**
     * Constructs a new WriterExportTask.
     * 
     * @param sentenceWriter
     *            The underlying sentence writer which handles the formatting of a single sentence
     * @param writer
     *            The writer where everything is written to
     * @param filters
     *            Filters which allow for the exclusion of sentences. They get passed to the underlying ExportTask.
     */
    public WriterExportTask(SentenceWriter<S> sentenceWriter, Writer writer,
            List<Test<S>> filters) {
        super(filters);
        this.sentenceWriter = sentenceWriter;
        this.writer = writer;
    }

    @Override
    public void exportSentence(S sentence) throws TreebankException {
        try {
            sentenceWriter.write(sentence, writer);
        } catch (IOException e) {
            throw new TreebankException("could not write to writer", e);
        }
    }

    @Override
    public void exportDone() {
    }

}
