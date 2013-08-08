/*******************************************************************************
 * File IncrementalConstituentProcessorFactory.java
 * 
 * Authors:
 *    Kilian Evang
 *    
 * Copyright:
 *    Kilian Evang, 2011
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
package de.tuebingen.rparse.treebank.constituent.process;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.constituent.Tree;


public class IncrementalConstituentProcessorFactory {

    public static IncrementalTreebankProcessor<Tree> getTreebankProcessor(
            String format, Numberer nb) throws UnknownFormatException {
        if (format == null) 
            throw new UnknownFormatException("null");

        if (format.startsWith(ConstituentInputFormats.EXPORT)) 
            return new IncrementalExportProcessor(nb);

        if (format.startsWith(ConstituentInputFormats.MRG)) 
            return new IncrementalMrgProcessor(nb);
  
        throw new UnknownFormatException(format);
    }
    
}
