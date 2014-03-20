/*******************************************************************************
 * File GrammarExtractionTask.java
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
package de.tuebingen.rparse.grammar.read;

import java.util.logging.Logger;

import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.treebank.HasID;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Suitable name for {@link ProcessingTask}s which extract grammars from treebanks. If ever we want to extract grammars
 * other than RCGs, we subclass this class.
 * 
 * @author wmaier
 * @param <T>
 *            The type of structures where the grammar is to be extracted from (constituencies or dependencies)
 */
abstract public class GrammarExtractionTask<T extends HasID>
        extends
            ProcessingTask<T> {

    private Logger logger;

    public GrammarExtractionTask() {
        logger = Logger.getLogger(GrammarExtractionTask.class.getPackage()
                .getName());
    }

    @Override
    public final void processSentence(T t) throws TreebankException {
        try {
            logger.finer(String.valueOf(t.getId()));
            extract(t);
        } catch (GrammarException e) {
            throw new TreebankException("Could not extract Grammar", e);
        }
    }

    @Override
    abstract public void done() throws TreebankException;

    abstract public void extract(T t) throws GrammarException;

}
