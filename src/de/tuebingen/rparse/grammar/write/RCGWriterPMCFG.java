/*******************************************************************************
 * File RCGWriterPMCFG.java
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

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;

public class RCGWriterPMCFG implements GrammarWriter<RCG> {


    public RCGWriterPMCFG() throws ParameterException {
    }

/*
     
*** pragmas

:encoding utf-8
:start S.1 S.2

:abscat NP.sg NP
:remove @

*** arity definitions

NP.sg 2 
Det.sg 3 

*** example rules

NP.pl <- Det.pl CN.pl
NP.sg <- Det.sg CN.sg

DetCN.sg : NP.sg <- Det.sg CN.sg = [[0:0 "to\u0020ken" 1:0][2:0 "to\"k\\\nen" 1:1]] 

DetCN.sg : NP.sg <- Det.sg CN.sg = [[0:2 "token" 1:1][2:0 "token" 1:0]] 

DetCN.sg : NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1] [2:0 "token" 1:0]
DetCN.sg : NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1][ 2:0 "token" 1:0 ]

DetCN.sg : NP.sg <- Det.sg CN.sg = 
[0:2 "token
ASD" 
1:1]
[2:0 "[" 1:0] asdasd : asdasd <- = []

NP.pl <- Det.pl CN.pl = [0:2 "token" 1:1][2:0 "" 1:0]

A[b] : A = []

Det.pl <- = ["token"]
Det.pl = ["token"]


*** informal grammar

grammar = pragma* arity+ rule+

flag = ":"+ alnum+ "\n"
arity = ident ws integer "\n"

rule = ident ws ":" ws ident ws "<-" ws (ident ws)* "=" ws lin*
lin = "[" (childref | token)* "]"
childref = int ":" int
token = (python/js strings) = "\"" char1+ "\"" | "'" char2+ "'"
char1 = anything except " \ | \" | \' | \\ | \n | \t | \r | uNNNN | xNN
char2 = anything except ' \ | \" | \' | \\ | \n | \t | \r | uNNNN | xNN

ident = [^\"\'\:\<\[\] \t\n\r] non-ws*

ws = [ \t\n\r]


     */
    
    
    private String clauseToString(Clause c, Numberer nb) {
    	// NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1] [2:0 "token" 1:0]
    	String lhs = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lhsname);
    	String result = lhs + " <- ";
    	for (int i = 0; i < c.rhsnames.length; ++i) {
    		result += (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rhsnames[i]) + " ";
    	}
    	result += " = ";
    	for (int i = 0; i < c.lhsargs.length; ++i) {
    		result += "[";
    		for (int j = 0; j < c.lhsargs[i].length; ++j) {
    			int lhsarg = c.lhsargs[i][j];
    			// find on RHS
    			int rhspred = -1;
    			int rhsarg = -1;
    			for (int ri = 0; ri < c.rhsargs.length; ++ri) {
    				for (int rj = 0; rj < c.rhsargs[ri].length; ++rj) {
    					if (c.rhsargs[ri][rj] == lhsarg) {
    						rhspred = ri;
    						rhsarg = rj;
    					}
    				}
    			}
    			result += String.valueOf(rhspred) + ":" + String.valueOf(rhsarg);
    			if (j != c.lhsargs[i].length - 1) {
    				result += " ";
    			}
    		}
			result += "]";
			if (i != c.lhsargs.length - 1) {
				result += " ";
			}
    	}
    	return result;
    }

    @Override
	public void write(RCG g, Lexicon lex, String grammarPath, String encoding) throws IOException {// File d, String pref, String encoding) throws IOException {
    	Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grammarPath), encoding));
    	
    	w.write("*** pragmas\n");
    	w.write("\n");
    	w.write(":encoding " + encoding + "\n");
    	w.write(":start " + g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, g.getStartPredLabel()) + "\n");

    	w.write("\n");
    	w.write("*** arity definitions \n");
    	w.write("\n");
    	for (Integer labeln : g.getClausesByLhsLabel().keySet()) {
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

    	int id = 0;

    	w.write("\n");
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
    	
    	for (Integer labeln : g.getClausesByLhsLabel().keySet()) {
    		for (Clause c : g.getClausesByLhsLabel().get(labeln)) {
                w.write(id++ + " : " + clauseToString(c, g.getNumberer()) + "\n"); 
            }
        }
    	w.flush();
        w.close();
    }

}
