/*******************************************************************************
 * File ConstituentProcessingTaskFactory.java
 * 
 * Authors:
 *    Wolfgang Maier, Kilian Evang
 *    
 * Copyright:
 *    Wolfgang Maier, Kilian Evang, 2011
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
package de.tuebingen.rparse.treebank.constituent;

import java.io.IOException;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.MultiTask;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.TreebankGetter;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.negra.split.SRCCategorySplitter;
import de.tuebingen.rparse.treebank.constituent.negra.split.VPByGFCategorySplitter;

public class ConstituentProcessingTaskFactory {

    /**
     * Returns a constituency processing task.
     * 
     * @param task
     *            The name of the task.
     * @param params
     *            The parameters to be passed to the task.
     * @param nb
     *            The current numberer.
     * @return The actual processing task.
     * @throws UnknownTaskException
     *             If an unknown task is requested.
     * @throws IOException
     *             If unexpected I/O occurs.
     * @throws TreebankException
     *             If the is a treebank-related problem.
     */
    public static ProcessingTask<? super Tree> getProcessingTask(String task,
            String params, Numberer nb) throws UnknownTaskException,
            IOException, TreebankException {

        if (ConstituentProcessingTasks.TREEBANK_GETTER.equals(task)) {
            return new TreebankGetter<Tree>();
        }

        if (ConstituentProcessingTasks.PUNCTUATION_LOWERER.equals(task)) {
            MultiTask<Tree> result = new MultiTask<Tree>();
            result.addTask(new PunctuationLowererLeftToRightOrder());
            result.addTask(new BalancingPunctuationLowerer());
            return result;
        }

        if (ConstituentProcessingTasks.HEAD_LABELER.equals(task)) {
            return new HeadLabeler();
        }
        
        if (ConstituentProcessingTasks.NEGRA_S_SPLIT.equals(task)) {
        	return new SRCCategorySplitter();
        }

        if (ConstituentProcessingTasks.NEGRA_VP_SPLIT.equals(task)) {
        	return new VPByGFCategorySplitter();
        }

        throw new UnknownTaskException(task);

    }

    public static ProcessingTask<? super Tree> getProcessingTask(String task,
            Numberer nb) throws UnknownTaskException, IOException,
            TreebankException {
        int index = task.indexOf("-");

        if (index == -1) {
            index = task.length();
        }

        String params = task.substring(index);
        task = task.substring(0, index);

        return getProcessingTask(task, params, nb);
    }

}
