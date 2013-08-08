/*******************************************************************************
 * File ParserDataWriterFactory.java
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

import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * Get parser data writers from a factory
 * 
 * @author wmaier
 */
public class ParserDataWriterFactory {

    /**
     * Get a parser data writer
     * 
     * @param type
     *            The type of the parser data writer
     * @param options
     *            The options to the parser data writer
     * @return The parser data writer
     * @throws UnknownTaskException
     */
    public static ParserDataWriter getWriter(String type, String options)
            throws UnknownTaskException {

        if (type.contains(ParserDataFormats.RPARSE)) {
            return new ParserDataWriterRparse(options);
        }

        throw new UnknownTaskException("No writer for format " + type);

    }

}
