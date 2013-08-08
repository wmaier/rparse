/*******************************************************************************
 * File TrainingMethod.java
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
package de.tuebingen.rparse.grammar;

import java.util.logging.Logger;

import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * An abstract class for training of a grammar.
 * 
 * @author wmaier
 */
public abstract class TrainingMethod implements GrammarProcessingTask {

    private boolean  doBinarized;

    protected Logger logger;

    public TrainingMethod(boolean doBinarized) {
        setDoBinarized(doBinarized);
        logger = Logger.getLogger(TrainingMethod.class.getPackage().getName());
    }

    /**
     * Train the binarized grammar?
     * 
     * @return True if it is the case
     */
    public boolean doBinarized() {
        return doBinarized;
    }

    /**
     * Set to true if the binarized grammar should be trained.
     * 
     * @param doBinarized
     */
    public void setDoBinarized(boolean doBinarized) {
        this.doBinarized = doBinarized;
    }

    /**
     * Return the trained grammar
     * 
     * @return
     */
    abstract public RCG getGrammar();

    /**
     * Return the trained binary grammar
     * 
     * @return
     */
    abstract public BinaryRCG getBinaryGrammar();

    /**
     * Return the lexicon
     * 
     * @return
     */
    abstract public Lexicon getLexicon();

    /**
     * Set the binarized grammar
     * 
     * @param bg
     */
    abstract public void setBinarizedGrammar(BinaryRCG bg);

}
