/*******************************************************************************
 * File ConstituentSentenceWriterFactory.java
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
package de.tuebingen.rparse.treebank.constituent.write;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.constituent.depconv.DependencyWriter;
import de.tuebingen.rparse.treebank.dep.DependencyInputFormats;

public class ConstituentSentenceWriterFactory {

	public static SentenceWriter<Tree> getSentenceWriter(String format)
			throws UnknownFormatException {
		
		if (format == null) {
			throw new UnknownFormatException(null);
		}

		if (format.startsWith(ConstituentOutputFormats.BRACKETS)) {
			return new BracketsTreeWriter(format);
		}

        if (format.startsWith(ConstituentOutputFormats.EXPORT_CONSECUTIVE)) {
            return new ExportConsecutiveNumbersTreeWriter(format);
        }

        if (format.startsWith(ConstituentOutputFormats.EXPORT)) {
			return new ExportTreeWriter(format);
		}

		if (format.startsWith(ConstituentOutputFormats.TERMINALS)) {
			return new TerminalsTreeWriter(format);
		}

		if (DependencyInputFormats.isDependencyFormat(format)) {
			return new DependencyWriter(format.substring(format.indexOf("-") + 1));
		}
		
		throw new UnknownFormatException(format);
	}

}
