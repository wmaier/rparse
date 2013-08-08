/*******************************************************************************
 * File ParserInputReaderFactory.java
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
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.constituent.process.ConstituentInputFormats;
import de.tuebingen.rparse.treebank.constituent.process.IncrementalExportProcessor;
import de.tuebingen.rparse.treebank.constituent.process.IncrementalMrgProcessor;

/**
 * All this is acceptable as input for the parser
 * @author wmaier
 *
 */
public class ParserInputReaderFactory {
	
	public static IncrementalTreebankProcessor<? extends ParserInput> getParserInputReader(String format, Numberer nb) {

		if (ParserInputFormats.RPARSE_TAGGED.equals(format)) { 
			return new RparseParserInputReader(true, nb);
		}
		
		if (ConstituentInputFormats.EXPORT.equals(format)) {
			return new IncrementalExportProcessor(nb);
		}
		
		if (ConstituentInputFormats.MRG.equals(format)) {
			return new IncrementalMrgProcessor(nb);
		}
		
		if (ConstituentInputFormats.TREETAGGER.equals(format)) {
			return new TreeTaggerOutputReader(nb);
		}
		
		throw new UnsupportedOperationException(format + " not known");
		 
	}
	
}
