/*******************************************************************************
 * File LexiconWriterFactory.java
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
package de.tuebingen.rparse.treebank.lex;

/**
 * A factory which delivers writers that write lexicons in different formats.
 * 
 * @author wmaier
 */
public class LexiconWriterFactory {

    /**
     * Gets a lexicon writer.
     * 
     * @param format
     *            The type of the desired lexicon writer.
     * @param options
     *            A parameter string for the lexicon writer.
     * @return The actual lexicon writer.
     * @throws LexiconException
     *             If an unknown lexicon format is requested. TODO should be a unknown task exception.
     */
    public static LexiconWriter getWriter(String format, String options)
            throws LexiconException {

        if (LexiconFormats.RPARSE.equals(format)) {
            return new RparseLexiconWriter(options);
        }

        throw new LexiconException("Unknown lexicon format " + format);

    }
}
