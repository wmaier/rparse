/*******************************************************************************
 * File ParserInputProcessingTaskFactory.java
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

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * Factory which delivers processing tasks that work on parser input.
 * 
 * @author wmaier
 */
public class ParserInputProcessingTaskFactory {

    /**
     * Get a processing task for parser input.
     * 
     * @param task
     *            The descriptor string of the task.
     * @param params
     *            A parameter string for the task.
     * @param nb
     *            The current numberer.
     * @return The actual task.
     * @throws UnknownTaskException
     *             If the task is not known.
     */
    public static ProcessingTask<ParserInput> getProcessingTask(String task,
            String params, Numberer nb) throws UnknownTaskException {
        if (ParserInputProcessingTasks.BRACKET_SQUARER.equals(task)) {
            return new BracketSquarer(nb);
        }

        throw new UnknownTaskException("Unknown parser input processing task: "
                + task);
    }

}
