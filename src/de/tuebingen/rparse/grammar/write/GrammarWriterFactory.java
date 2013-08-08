/*******************************************************************************
 * File GrammarWriterFactory.java
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
package de.tuebingen.rparse.grammar.write;

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownFormatException;

/**
 * Get writers which write grammars (not parser data, i.e., with lexicon, those are in the other package).
 * 
 * @author wmaier
 */
public class GrammarWriterFactory {

    /**
     * Get a writer.
     * 
     * @param format
     *            The desired format.
     * @param params
     *            The parameter string.
     * @return A grammar writer.
     * @throws GrammarException
     *             If something goes wrong with the creation of the writer.
     * @throws ParameterException
     *             If the parameter string cannot be parsed.
     * @throws UnknownFormatException
     *             If an unknown format is requested.
     */
    public static GrammarWriter<RCG> getRCGWriter(String format, String params)
            throws GrammarException, ParameterException, UnknownFormatException {

        if (format == null) {
            throw new UnknownFormatException(
                    "Requested null as grammar output format");
        }

        if (GrammarFormats.RCG_RPARSE.equals(format)) {
            return new RCGWriterRparse(params);
        }

        throw new UnknownFormatException(format);
    }

    /**
     * Get a writer for a binary grammar.
     * 
     * @param format
     *            The desired format.
     * @return The grammar writer.
     * @throws GrammarException
     *             If something is wrong with the grammar, or writer cannot be created.
     */
    public static GrammarWriter<BinaryRCG> getBinaryRCGWriter(String format)
            throws GrammarException {

        if (GrammarFormats.BINARYRCG_RPARSE.equals(format)) {
            return new BinaryRCGWriterRparse();
        }

        throw new GrammarException("Unknown binary grammar format " + format);

    }

}
