/*******************************************************************************
 * File RparseLexiconWriter.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.parser.ParserData;

/**
 * Writes a lexicon in our in-house format
 * @author wmaier
 *
 */
public class RparseLexiconWriter implements LexiconWriter {

	protected String options;
	
	public RparseLexiconWriter(String options) {
		this.options = options;
	}
	
	@Override
	public void write(ParserData pd, File d, String pref, String encoding) throws IOException {
		String lexiconPath = d.getAbsolutePath() + File.separator + pref + ".lex";
		BufferedWriter lw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lexiconPath), encoding));
		for (Integer i : pd.l.getWords()) {
			lw.write((String) pd.l.getNumberer().getObjectWithId(LexiconConstants.LEXWORD, i) + "\t");
			for (Integer j : pd.l.getTagForWord(i))
				lw.write((String) pd.l.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, j) + " ");
			lw.write("\n");
		}
		lw.flush();
		lw.close();
	}

	
}
