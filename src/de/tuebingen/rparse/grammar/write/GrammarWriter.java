/*******************************************************************************
 * File GrammarWriter.java
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

import java.io.IOException;

import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * An interface for grammar writing.
 * 
 * @author wmaier
 * @param <S>
 *            The type of grammar type be written.
 */
public interface GrammarWriter<S> {

    /**
     * Write a grammar to a writer.
     * 
     * @param g
     *            The grammar.
     * @param path
     *            Where to create the output files
     * @throws IOException
     *             If there are IO problems.
     * @throws GrammarException
     *             If there is a problem with the grammar.
     */
    public void write(S g, Lexicon l, String path, String encoding) throws IOException, GrammarException;

}
