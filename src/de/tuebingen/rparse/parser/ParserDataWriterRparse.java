/*******************************************************************************
 * File ParserDataWriterRparse.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.write.GrammarFormats;
import de.tuebingen.rparse.grammar.write.GrammarWriter;
import de.tuebingen.rparse.grammar.write.GrammarWriterFactory;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.lex.LexiconException;
import de.tuebingen.rparse.treebank.lex.LexiconFormats;
import de.tuebingen.rparse.treebank.lex.LexiconWriter;
import de.tuebingen.rparse.treebank.lex.LexiconWriterFactory;

/**
 * Writes parser data (lexicons, grammars and such) to files, using our in-house format.
 * 
 * @author wmaier
 */
public class ParserDataWriterRparse implements ParserDataWriter {

    private String options;

    public ParserDataWriterRparse(String options) {
        this.options = options;
    }

    @Override
    public void write(ParserData pd, File d, String pref, String encoding)
            throws IOException {
        if (!d.isDirectory()) {
            throw new IOException(
                    "Output location for parser data must be a directory, got "
                            + d.toString());
        }
        try {
            String grammarPath = d.getAbsolutePath() + File.separator + pref
                    + ".gram";
            GrammarWriter<RCG> gw = GrammarWriterFactory.getRCGWriter(
                    GrammarFormats.RCG_RPARSE, options);
            Writer w = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(grammarPath), encoding));
            gw.write(pd.g, w);
            LexiconWriter lw = LexiconWriterFactory.getWriter(
                    LexiconFormats.RPARSE, options);
            lw.write(pd, d, pref, encoding);
        } catch (GrammarException e) {
            throw new IOException(e.getMessage());
        } catch (LexiconException e) {
            throw new IOException(e.getMessage());
        } catch (ParameterException e) {
            throw new IOException(e.getMessage());
        } catch (UnknownFormatException e) {
            throw new IOException(e.getMessage());
        }

    }

    @Override
    public void close() {

    }

}
