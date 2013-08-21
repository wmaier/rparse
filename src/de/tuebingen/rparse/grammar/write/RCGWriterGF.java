/*******************************************************************************
 * File RCGWriterGF.java
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

import java.io.IOException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Write out an RCG without diagnostic fields in Grammatical Framework format
 * (includes abstract and concrete syntax, as well as prob file)
 * 
 * @author wmaier
 */
public class RCGWriterGF implements GrammarWriter<RCG> {

    public final static int LEXLIMIT = 300;

    private String escape(String s) {
	s = s.replace("$", "");
	s = s.replace("(", "LBR");
	s = s.replace(")", "RBR");
	s = s.replace(".", "PUNCT");
	s = s.replace(",", "COMMA");
	s = s.replace("--", "MDASH");
	s = s.replace("-", "DASH");
	s = s.replace("/", "SLASH");
	s = s.replace("\"", "DQ");
	s = s.replace("\'", "SQ");
	return s;
    }

    private String buildTuple(String label, int arity) {
	String lincat = "{";
	for (int i = 0; i < arity; ++i) {
	    lincat += " p" + String.valueOf(i + 1) + " : Str ";
	    if (i != arity - 1) {
		lincat += "; ";
	    }
	}
	return lincat + "}";
    }

    @Override
    public void write(RCG g, Lexicon lex, String path, String encoding) throws IOException {
	List<String> declared = new ArrayList<>();
	Writer abstractw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "abstract.gf"), encoding));
	abstractw.write("abstract grammargfabstract = {\n");
	Writer probw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".probs"), encoding));
	Writer concw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "concrete.gf"), encoding));
	concw.write("concrete grammargfconcrete of grammargfabstract = {\n");
	int namecnt = 0;
	Numberer nb = g.getNumberer();
	
	/*
	NP1([0][1][2][3]) --> NP1'([0]) PP2([1],[3]) PP1([2])
	
	abstract
	fun fun0 : NP1 -> PP2 -> PP1 -> NP1

	concrete
	lincat 
	    NP1 = { p1 : Str };
            PP1 = { p1 : Str };
	    PP2 = { p1 : Str ; p2 : Str };
	lin
	    fun0 rhs1 rhs2 rhs3 = { p1 = rhs1.p1 ++ rhs2.p1 ++ rhs3.p1 ++ rhs.p2 };

	*/

        for (Integer labeln : g.getClausesByLhsLabel().keySet()) {
	    String lhs = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, labeln);
	    lhs = escape(lhs);
            for (Clause c : g.getClausesByLhsLabel().get(labeln)) {

	        // abstract 
		String funName = "fun" + String.valueOf(namecnt++);
		String fun = "";
		for (int i = 0; i < c.rhsargs.length; ++i) {
		    String rhsel = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rhsnames[i]);
		    fun += escape(rhsel) + " -> ";
		}
		fun += lhs;
		abstractw.write("fun " + funName + " : " + fun + " ; \n");

		// prob
                probw.write(funName + " " + c.getScore() + "\n");
		
		// concrete lincat + abstract cat
		String lincat = "";
		if (!declared.contains(lhs)) {
		    declared.add(lhs);
		    abstractw.write("cat " + lhs + " ;\n");
		    concw.write("lincat " + lhs + " = " + buildTuple(lhs, c.lhsargs.length) + " ;\n");
		} 
		for (int i = 0; i < c.rhsargs.length; ++i) {
		    String rhsel = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, c.rhsnames[i]);
		    String urhsel = rhsel;
		    rhsel = escape(rhsel);
		    if (!declared.contains(rhsel)) {
			declared.add(rhsel);
			abstractw.write("cat " + rhsel + " ;\n");
			concw.write("lincat " + rhsel + " = " + buildTuple(rhsel, c.rhsargs[i].length) + " ;\n");
			if (c.getRhspterm()[i]) {
			    abstractw.write("fun fun" + rhsel + " : " + rhsel + ";\n");
			    String res = "lin fun" + rhsel + " = { p1=" ;
			    Integer tagId = lex.getNumberer().getIntWithId(GrammarConstants.PREDLABEL, urhsel);
			    ArrayList<String> wordList = new ArrayList<>();
			    for (int wi : lex.getWordsForTag(tagId)) {
				String word = (String) lex.getNumberer().getObjectWithId(LexiconConstants.LEXWORD, wi); 
				word = escape(word);
				wordList.add(word);
			    }
			    int limit = wordList.size() < LEXLIMIT ? wordList.size() : LEXLIMIT;
			    for (int wi = 0; wi < limit; ++wi) {
				res += "\"" + wordList.get(wi) + "\"";
				if (wi != limit - 1) {
				    res += "|";
				}
			    }
			    concw.write(res + " };\n");
			}
		    } 
		}
		

		// concrete lin
		String lin = funName;
		for (int i = 0; i < c.rhsnames.length; ++i) {
		    lin += " rhs" + String.valueOf(i + 1);
		}
		lin += " = { ";
		for (int i = 0; i < c.getArity(); ++i) {
		    lin += "p" + String.valueOf(i + 1) + " = ";
		    for (int j = 0; j < c.lhsargs[i].length; ++j) {
			int var = c.lhsargs[i][j];
			boolean done = false;
			for (int k = 0; k < c.rhsargs.length && !done; ++k) {
			    for (int l = 0; l < c.rhsargs[k].length && !done; ++l) {
				if (var == c.rhsargs[k][l]) {
				    lin += " rhs" + String.valueOf(k + 1) + ".p" + String.valueOf(l + 1);
				    done = true;
				}
			    }
			}
			if (j != c.lhsargs[i].length - 1) {
			    lin += " ++";
			}
		    }
		    lin += " ; ";
		}
		lin += "}";
                concw.write("lin " + lin + " ;\n");

	    }
        }
        abstractw.write("}");
        abstractw.flush();
        concw.write("}");
        concw.flush();
        probw.flush();
        abstractw.close();
        concw.close();
        probw.close();
    }

}
