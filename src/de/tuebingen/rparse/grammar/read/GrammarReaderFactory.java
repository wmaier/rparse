/*******************************************************************************
 * File GrammarReaderFactory.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2014
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
package de.tuebingen.rparse.grammar.read;

import java.io.File;
import java.io.FileNotFoundException;

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Get grammar readers.
 * 
 * @author wmaier
 */
public class GrammarReaderFactory {

    /**
     * Get a Reader.
     * 
     */
    public static GrammarReader<RCG> getRCGReader(String format, File f, Lexicon l, Numberer nb)
            throws GrammarException, ParameterException, UnknownFormatException, FileNotFoundException {

        if (format == null) {
            throw new UnknownFormatException(
                    "Requested null as grammar input format");
        }

        if (GrammarFormats.RCG_RPARSE.equals(format)) {
            return new RCGReader(f, l, nb);
        }

        throw new UnknownFormatException(format);
    }

    /**
     * Get a reader for a binary grammar.
     * @param nb 
     * @param file 
     * 
     */
    public static GrammarReader<BinaryRCG> getBinaryRCGReader(String format, File file, Numberer nb)
            throws GrammarException, UnknownFormatException, FileNotFoundException {

        if (GrammarFormats.RCG_RPARSE.equals(format)) {
            return new BinaryRCGReaderRCG(file, nb);
        }

        if (GrammarFormats.RCG_PMCFG.equals(format)) {
        	return new BinaryRCGReaderPMCFG(file, nb);
        }

        throw new UnknownFormatException("Unknown binary grammar format " + format);

    }

}
