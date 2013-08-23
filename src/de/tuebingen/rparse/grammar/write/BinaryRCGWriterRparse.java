/*******************************************************************************
 * File BinaryRCGWriterRparse.java
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
package de.tuebingen.rparse.grammar.write;

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Writes out a binary RCG in our standard format for binary grammars. This is now modularized too, so if somebody wants
 * to have another format (and has time)...
 * 
 * @author wmaier
 */
public class BinaryRCGWriterRparse implements GrammarWriter<BinaryRCG> {

    @Override
    public void write(BinaryRCG g, Lexicon lex, String grammarPath, String encoding) throws IOException,
    GrammarException {
    	Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grammarPath), encoding));
    	for (Integer labeln : g.clByParent.keySet()) {
    		for (BinaryClause c : g.clByParent.get(labeln)) {
    			w.write(g.cnt.get(c) + " " + c + "\n");
    		}
    	}
    	w.close();
    }
}
