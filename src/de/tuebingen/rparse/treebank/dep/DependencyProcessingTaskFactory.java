/*******************************************************************************
 * File DependencyProcessingTaskFactory.java
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
package de.tuebingen.rparse.treebank.dep;

import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankGetter;
import de.tuebingen.rparse.treebank.UnknownTaskException;

public class DependencyProcessingTaskFactory {

    public static ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> getProcessingTask(
            String name, String params) throws UnknownTaskException {
        
        if (DependencyProcessingTasks.DEP_TREEBANK_GETTER.equals(name)) {
        	return new TreebankGetter<DependencyForest<DependencyForestNodeLabel, String>>();
        }
        
        throw new UnknownTaskException(name);
    }

}
