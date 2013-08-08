/*******************************************************************************
 * File SentenceWriter.java
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

import java.io.IOException;
import java.io.Writer;

/**
 * Writes syntactic structures (dependencies or constituents) to a writer in a certain format.
 * 
 * @author wmaier
 * @param <S>
 *            The type of structures to be written (constituents or dependencies)
 */
public interface SentenceWriter<S> {

    /**
     * Used by {@link ExportTask}s. Any class which writes syntactic structures in a certain format should implement
     * this interface.
     * 
     * @param sentence
     *            The sentence to be written
     * @param writer
     *            The writer where to write the formatted sentence
     * @throws TreebankException
     *             If we cannot interpret the given sentence
     * @throws IOException
     *             If there is unexpected I/O
     */
    public void write(S sentence, Writer writer) throws TreebankException,
            IOException;

}
