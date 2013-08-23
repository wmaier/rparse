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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;

/**
 * Write out an RCG without diagnostic fields in Grammatical Framework format
 * (includes abstract and concrete syntax, as well as prob file)
 * 
 * @author wmaier
 */
public class RCGWriterGF implements GrammarWriter<RCG> {

    private String escape(String s) {
	s = s.replace("$", "");
	s = s.replace("(", "LBR");
	s = s.replace(")", "RBR");
	s = s.replace(".", "PUNCT");
	s = s.replace(",", "COMMA");
	s = s.replace("--", "MDASH");
	s = s.replace("-", "DASH");
	s = s.replace("/", "SLASH");
	s = s.replace("\\", "BACKSLASH");
	s = s.replace("\"", "DQ");
	s = s.replace("\'", "SQ");
	return s;
    }

    private String germanEscape(String s) {
	s = s.replace("ç", "_ccedil");
	s = s.replace("é", "_eacute");
	s = s.replace("è", "_egrave");
	s = s.replace("ã", "_atilde");
	s = s.replace("á", "_aacute");
	s = s.replace("à", "_agrave");
	s = s.replace("í", "_iacute");
	s = s.replace("ì", "_igrave");
	s = s.replace("ü", "_ue");
	s = s.replace("ä", "_ae");
	s = s.replace("ö", "_oe");
	s = s.replace("ô", "_ocirc");
	s = s.replace("î", "_icirc");
	s = s.replace("â", "_acirc");
	s = s.replace("Ü", "_UE");
	s = s.replace("Ä", "_AE");
	s = s.replace("Ö", "_OE");
	s = s.replace("ß", "_SZET");
	s = s.replace("!", "_EXCLAMATION");
	s = s.replace("?", "_QUESTION");
	s = s.replace(":", "_COLON");
	s = s.replace(";", "_SEMICOLON");
	s = s.replace("·", "_CDOT");
	s = s.replace("1", "_ONE");
	s = s.replace("2", "_TWO");
	s = s.replace("3", "_THREE");
	s = s.replace("4", "_FOUR");
	s = s.replace("5", "_FIVE");
	s = s.replace("6", "_SIX");
	s = s.replace("7", "_SEVEN");
	s = s.replace("8", "_EIGHT");
	s = s.replace("9", "_NINE");
	s = s.replace("0", "_NULL");
	s = s.replace("ë", "_EDIA");
	s = s.replace("%", "_PERCENT");
	s = s.replace("*", "_ASTERISK");
	s = s.replace("&", "_AMPERSAND");
	s = s.replace("\\", "_BACKSLASH");
	s = s.replace("/", "_SLASH");
	s = s.replace("§", "_PARAGRAPH");
	s = s.replace("=", "_EQUALS");
	s = s.replace("+", "_PLUS");
	s = s.replace("-", "_DASH");
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

	Writer probw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".probs"), encoding));

	Writer lexaw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "lexabstract.gf"), encoding));
	lexaw.write("abstract grammargflexabstract = { \n\n");

	Writer lexw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "lexconcrete.gf"), encoding));
	lexw.write("concrete grammargflexconcrete of grammargflexabstract = {\n\n");

	Writer abstractw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "abstract.gf"), encoding));
	abstractw.write("abstract grammargfabstract = grammargflexabstract ** {\n\n");

	Writer concw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "concrete.gf"), encoding));
	concw.write("concrete grammargfconcrete of grammargfabstract = grammargflexconcrete ** {\n\n");

	int namecnt = 0;
	Numberer nb = g.getNumberer();

	abstractw.write("flags \n");
	abstractw.write("startcat=VROOT1; \n\n");
	
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
			if (c.getRhspterm()[i]) {
			    Integer tagId = lex.getNumberer().getIntWithId(GrammarConstants.PREDLABEL, urhsel);
			    for (int wordId : lex.getWordsForTag(tagId)) {
				String word = (String) lex.getNumberer().getObjectWithId(LexiconConstants.LEXWORD, wordId); 
				word = escape(word);
				String lexFunName = germanEscape(word) + "_" + rhsel;
				lexaw.write("fun " + lexFunName + " : " + rhsel + ";\n");
				lexaw.write("cat " + rhsel + " ;\n");
				probw.write(lexFunName + " " + lex.getScore(wordId, tagId) + "\n");
				//concw.write("lin " + lexFunName + " = { p1=\"" + word + "\" };\n");
				//concw.write("lincat " + rhsel + " = " + buildTuple(rhsel, c.rhsargs[i].length) + " ;\n");
				lexw.write("lin " + lexFunName + " = { p1=\"" + word + "\" };\n");
				lexw.write("lincat " + rhsel + " = " + buildTuple(rhsel, c.rhsargs[i].length) + " ;\n");
			    }
			} else {
			    concw.write("lincat " + rhsel + " = " + buildTuple(rhsel, c.rhsargs[i].length) + " ;\n");
			    abstractw.write("cat " + rhsel + " ;\n");
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
        abstractw.close();
        concw.write("}");
        concw.flush();
        concw.close();
        lexw.write("}");
        lexw.flush();
        lexw.close();
        lexaw.write("}");
        lexaw.flush();
        lexaw.close();
        probw.flush();
        probw.close();
    }

}
