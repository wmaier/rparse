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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Writes out a binary RCG in PMCFG format
 * 
 * @author wmaier
 */
public class BinaryRCGWriterPMCFG implements GrammarWriter<BinaryRCG> {
	
	private class Rule {
		
		String lhs;
		
		String[] rhs;
		
		int[] lin;
		
	}

	private int linid;
	
	private Rule parseClause(BinaryClause c, Numberer nb) {
    	// NP.sg <- Det.sg CN.sg = [0:2 "token" 1:1] [2:0 "token" 1:0]
		Rule r = new Rule();
		r.lhs = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lhs);
		r.rhs = c.rc == -1 ? new String[1] : new String[2];
		r.rhs[0] = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.lc);
		if (c.rc != -1) {
			r.rhs[1] = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rc);
		}
		int lccnt = 0;
		int rccnt = 0;
		r.lin = new int[c.yf.length];
		String lin = "";
		for (int i = 0; i < c.yf.length; ++i) {
			for (int j = 0; j < c.yf[i].length; ++j) {
				lin += (c.yf[i][j] ? 1 : 0)  
						+ ":" 
						+ (c.yf[i][j] ? rccnt++ : lccnt++);
				if (j != c.yf[i].length - 1) {
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
			lin = "";
		}
		return r;
	}
	
	private Map<String,Integer> lins2id;
	private Map<Integer,String> id2lins;
	
	public BinaryRCGWriterPMCFG() {
		lins2id = new HashMap<String,Integer>();
		id2lins = new HashMap<Integer,String>();
		linid = 0;
	}
	
    @Override
    public void write(BinaryRCG g, Lexicon lex, String grammarPath, String encoding) throws IOException,
    GrammarException {
    	Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(grammarPath), encoding));

    	w.write("-- pragmas\n");
    	w.write("\n");
    	w.write(":encoding " + encoding + "\n");
    	w.write(":start " + g.getNumberer().getObjectWithId(GrammarConstants.PREDLABEL, g.getStartPredLabel()) + "\n");
    	w.write("\n");
    	int id = 0;
    	w.write("\n");

    	w.write("-- rules \n");
    	w.write("\n");
    	HashMap<String,Rule> id2rule = new HashMap<String,Rule>();
    	for (Integer labeln : g.clByParent.keySet()) {
    		for (BinaryClause c : g.clByParent.get(labeln)) {
    			id2rule.put("f" + String.valueOf(id++), parseClause(c, g.getNumberer()));
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
    		Rule r = id2rule.get(key);
    		w.write(key + " : " + r.lhs + " <- ");
    		for (int i = 0; i < r.rhs.length; ++i) {
    			w.write(r.rhs[i]);
    			if (i != r.rhs.length - 1) {
    				w.write(" ");
    			}
    		}
			w.write("\n");
    	}
		w.write("\n");
		w.write("\n");
		
    	w.write("-- linearization \n");
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
		w.write("\n");
		w.write("\n");

		w.write("-- linearization definitions\n");
		for (Integer linid : id2lins.keySet()) {
			w.write("s" + linid + " => " + id2lins.get(linid) + "\n");
		}
		w.write("\n");

    	w.flush();
    	w.close();
    }
}
