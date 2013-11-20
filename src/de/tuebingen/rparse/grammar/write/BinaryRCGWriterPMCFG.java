/*******************************************************************************
 * File BinaryRCGWriterPMCFG.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2013
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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;

/**
 * Writes out a binary RCG in PMCFG format
 * 
 * @author wmaier
 */
public class BinaryRCGWriterPMCFG implements GrammarWriter<BinaryRCG> {

	private String clauseToString(BinaryClause c, Numberer nb) {
    	// NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1] [2:0 "token" 1:0]
		String result = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lhs);
		result += " <- ";
		result += (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lc);
		result += " ";
		result += (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rc);
		result += " = ";
		int lccnt = 0;
		int rccnt = 0;
		for (int i = 0; i < c.yf.length; ++i) {
			result += "[";
			for (int j = 0; j < c.yf[i].length; ++j) {
				result += (c.yf[i][j] ? 1 : 0)  
						+ ":" 
						+ (c.yf[i][j] ? rccnt++ : lccnt++);
				if (j != c.yf[i].length - 1) {
					result += " ";
				}
			}
			result += "]";
			if (i != c.yf.length - 1) {
				result += " ";
			}
		}
		return result;
	}
	
    @Override
    public void write(BinaryRCG g, Lexicon lex, String grammarPath, String encoding) throws IOException,
    GrammarException {
    	Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grammarPath), encoding));

    	w.write("*** pragmas\n");
    	w.write("\n");
    	w.write(":encoding " + encoding + "\n");
    	w.write(":start " + g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, g.getStartPredLabel()) + "\n");
    	w.write("\n");

    	w.write("*** arity definitions \n");
    	w.write("\n");
    	for (Integer labeln : g.clByParent.keySet()) {
			String labels = (String) g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, labeln);
    		int arity = Utilities.getArity(labels);
        	w.write(labels + " " + arity + "\n");
    	}

    	w.write("\n");
    	w.write("*** lexicon arity definitions \n");
    	w.write("\n");
    	for (Integer tag : lex.getPreterminals()) {
    		String tags = (String) g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, tag);
    		w.write(tags + " : " + 1 + "\n");
    	}
    	w.write("\n");
    	
    	int id = 0;

    	w.write("\n");
    	w.write("*** lexicon\n");
    	w.write("\n");

    	for (Integer word : lex.getWords()) {
    		String words = (String) g.getNumberer().getObjectWithId(LexiconConstants.LEXWORD, word);
    		for (Integer tag : lex.getTagForWord(word)) {
    			String tags = (String) g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, tag);
    			w.write(id++ + " : " + tags + " <- " + "[\"" + words + "\"]\n");
    		}
    	}
    	
    	w.write("\n");
    	w.write("*** rules \n");
    	w.write("\n");
    	
    	for (Integer labeln : g.clByParent.keySet()) {
    		for (BinaryClause c : g.clByParent.get(labeln)) {
                w.write(id++ + " : " + clauseToString(c, g.getNumberer()) + "\n"); 
            }
        }
    	
    	w.flush();
    	w.close();
    }
}
