/*******************************************************************************
 * File MultiTask.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * Bundles multiple {@link TreebankProcessingTaks}s, permitting to execute them
 * with a single go through the treebank.
 * 
 * @author ke, wmaier
 * 
 */
public class MultiTask<S> extends ProcessingTask<S> {
	
	private List<ProcessingTask<? super S>> tasks;
	
	/**
	 * Construct a new multitask
	 */
	public MultiTask() {
		tasks = new ArrayList<ProcessingTask<? super S>>();
	}
	
	/**
	 * Add a task to this multi task.
	 * @param task The task.
	 */
	public void addTask(ProcessingTask<? super S> task) {
		tasks.add(task);
	}
	
	@Override
	public void processSentence(S sentence) throws TreebankException {
		for (ProcessingTask<? super S> task : tasks) {
			task.processSentence(sentence);
		}
	}
	
	@Override
	public void done() throws TreebankException {
		for (ProcessingTask<? super S> task : tasks) {
			task.done();
		}
		
	}
	
	/**
	 * Return the description string of all tasks.
	 * @return The corresponding value.
	 */
	public String summary() {
		String ret = "";
		for (ProcessingTask<? super S> task : tasks) {
			if (task instanceof MultiTask<?>) {
				ret += ((MultiTask<? super S>) task).summary();
			} else {
				ret += task.getClass().getSimpleName() + " ";
			}
		}
		return ret;
	}
	
}
