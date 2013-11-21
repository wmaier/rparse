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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

public class RCGWriterPMCFG implements GrammarWriter<RCG> {

	
	private class Rule {
		
		String lhs;
		String[] rhs;
		int[] lin;
		
	}

    public RCGWriterPMCFG() throws ParameterException {
    	lins2id = new HashMap<String,Integer>();
    	id2lins = new HashMap<Integer,String>();
    	linid = 0;
    }

    
	private Map<String,Integer> lins2id;
	private Map<Integer,String> id2lins;
	
	private String rule2String(Rule r) {
		String rulestr = r.lhs + " <- ";
        for (int i = 0; i < r.rhs.length; ++i) {
        	rulestr += r.rhs[i];
        	if (i != r.rhs.length - 1) {
        		rulestr += " ";
        	}
        }
        return rulestr;
	}
	
	private int linid;
	
	private Rule parseClause(Clause c, Numberer nb) {
    	// NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1] [2:0 "token" 1:0]
		Rule r = new Rule();
		r.lhs = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lhsname);
		r.rhs = new String[c.rhsnames.length];
		for (int i = 0; i < r.rhs.length; ++i) {
			r.rhs[i] = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rhsnames[i]);
		}
		r.lin = new int[c.lhsargs.length];
	
		String lin = "";
		for (int i = 0; i < c.lhsargs.length; ++i) {
			System.err.println("linearizing " + i + "th argument, lin is " + lin);
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
				lin += String.valueOf(rhspred) + ":" + String.valueOf(rhsarg);
				if (j != c.lhsargs[i].length - 1) {
					lin += " ";
				}
			}
			
			int thislinid = linid;
			if (!lins2id.containsKey(lin)) {
				lins2id.put(lin, linid);
				id2lins.put(linid, lin);
				linid++;
			} else {
				thislinid = lins2id.get(lin);
			}
			r.lin[i] = thislinid;
			System.err.println(i + "th linearization for " + c.print(nb) + " is " + id2lins.get(thislinid) + "\n before we had " + lin);
			lin = "";
		}
		
		return r;
	}
	


    @Override
	public void write(RCG g, Lexicon lex, String grammarPath, String encoding) throws IOException {// File d, String pref, String encoding) throws IOException {
    	Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grammarPath), encoding));
    	
    	w.write("-- pragmas\n");
    	w.write("\n");
    	w.write(":encoding " + encoding + "\n");
    	w.write(":start " + g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, g.getStartPredLabel()) + "\n");

    	
    	w.write("\n");
    	w.write("-- rules \n");
    	w.write("\n");
    	
    	Map<String,Rule> id2rule = new HashMap<String,Rule>();
    	int id = 0;
    	for (Integer labeln : g.getClausesByLhsLabel().keySet()) {
    		for (Clause c : g.getClausesByLhsLabel().get(labeln)) {
    			Rule r = parseClause(c, g.getNumberer());
                id2rule.put("f" + String.valueOf(id), r);
                id++;
            }
        }
    	
       	List<String> sortedKeys = new ArrayList<String>();
    	sortedKeys.addAll(id2rule.keySet());
    	Collections.sort(sortedKeys, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				Integer i0 = Integer.valueOf(arg0.substring(1));
				Integer i1 = Integer.valueOf(arg1.substring(1));
				return i0.compareTo(i1);
			}
    		
    	});

    	for (String key : sortedKeys) {
    		w.write(key + " : " + rule2String(id2rule.get(key)) + "\n");
    	}
		w.write("\n\n\n-- linearization \n\n\n");
    	for (String key : sortedKeys) {
    		Rule r = id2rule.get(key);
    		w.write(key + " = ");
    		for (int i = 0; i < r.lin.length; ++i) {
    			w.write("s" + String.valueOf(r.lin[i]));
    			if (i != r.lin.length - 1) {
    				w.write(" ");
    			}
    		}
    		w.write("\n");
    	}    	

    	w.write("\n-- linearization definitions \n\n");
    	for (Integer linid : id2lins.keySet()) {
			w.write("s" + linid + " => " + id2lins.get(linid) + "\n");
		}
    	
    	w.flush();
        w.close();
    }

}
