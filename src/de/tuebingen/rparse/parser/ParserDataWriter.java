/*******************************************************************************
 * File ParserDataWriter.java
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
package de.tuebingen.rparse.parser;

import java.io.File;
import java.io.IOException;

/**
 * An interface for writing out parser data (grammar, lexicon, ...), in different formats for various parsers.
 * 
 * @author wmaier
 */
public interface ParserDataWriter {

    /**
     * Write the parser data
     * 
     * @param g
     *            The parser data
     * @param d
     *            The directory were all corresponding files will be written to
     * @param pref
     *            The prefix all parser data files will have (the names are controlled by the writers below)
     * @param encoding
     *            The output encoding for everything
     * @throws IOException
     *             Thrown when anything goes wrong (Other types of exceptions occurring in subclasses are to be
     *             converted in IOExceptions).
     */
    public void write(ParserData g, File d, String pref, String encoding)
            throws IOException;

    public void close();

}
