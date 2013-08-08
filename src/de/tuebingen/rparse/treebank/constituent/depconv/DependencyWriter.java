/*******************************************************************************
 * File DependencyWriter.java
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
package de.tuebingen.rparse.treebank.constituent.depconv;

import java.io.IOException;
import java.io.Writer;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.dep.DependencyInputFormats;
import de.tuebingen.rparse.treebank.dep.DependencySentenceWriterFactory;

/**
 * Encapsulates a dependency converter. This way, it can be used as if it was a
 * regular output format
 * 
 * @author wmaier
 * 
 */
public class DependencyWriter implements SentenceWriter<Tree> {

	private DependencyConverter dc;

	private SentenceWriter<DependencyForest<DependencyForestNodeLabel, String>> w;

	public DependencyWriter(String format) throws UnknownFormatException {
		int hfind = format.indexOf('-');
		String hftype = null;
		if (hfind > -1 && format.length() > hfind + 1) {
			hftype = format.substring(hfind + 1);
			format = format.substring(0, hfind);
		}
		try {
			dc = DependencyConverterFactory.getDependencyConverter(format,
					hftype);
		} catch (Exception e) {
			throw new UnknownFormatException(
					"Could not get dependency converter: " + e.getMessage());
		}
		w = DependencySentenceWriterFactory
				.getSentenceWriter(DependencyInputFormats.CONLL);
	}

	@Override
	public void write(Tree t, Writer writer) throws TreebankException,
			IOException {
		w.write(dc.processSentence(t), writer);
	}

}
