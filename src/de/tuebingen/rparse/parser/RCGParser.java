/*******************************************************************************
 * File RCGParser.java
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

import java.io.IOException;
import java.io.Writer;

import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.lex.ParserInput;

/**
 * Interface for the core parser
 * 
 * @author wmaier
 */
public interface RCGParser {

    /**
     * Parse a sentence.
     * 
     * @param parserInput
     *            The unparsed sentence in {@link ParserInput} format. ParserInput can also be used to pass parsed data
     *            to the parser. The parsed data is then automatically stripped off.
     * @return true if there is a parse
     */
    public boolean parse(ParserInput parserInput);

    /**
     * Reset the parser after parsing a sentence.
     */
    public void reset();

    /**
     * After calling {@code parse()}, get the parser output.
     * 
     * @return The parser output or null if parse() has not been called before, or reset() has already been called.
     */
    public Tree getResult();

    /**
     * Write the resulting tree somewhere.
     * 
     * @param w
     *            Writer where to write the tree
     * @param scnt
     *            The sentence identifier
     * @param task
     *            A post-processing task
     * @throws IOException
     *             If something goes wrong with writing
     * @throws TreebankException
     *             If something goes wrong with the post-processing
     */
    public void writeResult(Writer w, int scnt, ProcessingTask<Tree> task)
            throws IOException, TreebankException;

    /**
     * Interpret the parser output as a result of dependency parsing. Convert the tree back into dependency format and
     * write the resulting dependency structure somewhere.
     * 
     * @param w
     *            The writer where to write the result.
     * @param scnt
     *            The sentence identifiere
     * @param task
     *            A post-processing task on the dependency forest resulting from the conversion.
     * @throws IOException
     *             If something goes wrong with writing
     * @throws TreebankException
     *             If something goes wrong with post-processing
     */
    public void writeDependencyResult(
            Writer w,
            int scnt,
            ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task)
            throws IOException, TreebankException;

    /**
     * Get statistics on parsing. Gets new values for every sentence.
     * 
     * @return A String containing the statistics in human-readable format.
     */
    public String getStats();

}
