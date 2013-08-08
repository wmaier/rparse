/*******************************************************************************
 * File ProcessingTask.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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
package de.tuebingen.rparse.treebank;

import java.io.IOException;

/**
 * Do something with a tree/graph.
 * 
 * @author ke, wmaier
 * @param <S>
 *            The type of structures to be treated (constituents or dependencies)
 */
public abstract class ProcessingTask<S> {

    /**
     * Do something with the sentence
     * 
     * @param sentence
     *            The sentence
     * @throws TreebankException
     *             If the sentence cannot be interpreted.
     */
    public abstract void processSentence(S sentence) throws TreebankException;

    /**
     * Called to indicate that processing has finished. This implementation does nothing.
     * 
     * @throws IOException
     * @throws TreebankException
     */
    public void done() throws TreebankException {
        // do nothing
    }

}
