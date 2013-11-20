/*******************************************************************************
 * File BinaryRCGWriterRparse.java
 * 
 * Authors:
 *    Peter Ljunglöf
 *    
 * Copyright:
 *    Peter Ljunglöf, 2013
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
import java.util.HashSet;
import java.util.Set;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;

/**
 * Writes out a binary RCG in our standard format for binary grammars. This is now modularized too, so if somebody wants
 * to have another format (and has time)...
 * 
 * @author wmaier
 */
public class BinaryRCGWriterGF implements GrammarWriter<BinaryRCG> {

    private String escape(String s) {
        s = s.replace("$", "");
        s = s.replace("(", "_LBR_");
        s = s.replace(")", "_RBR_");
        s = s.replace(".", "_PUNCT_");
        s = s.replace(",", "_COMMA_");
        s = s.replace("--", "_MDASH_");
        s = s.replace("-", "_DASH_");
        s = s.replace("/", "_SLASH_");
        s = s.replace("\\", "_BACKSLASH_");
        s = s.replace("\"", "_DQ_");
        s = s.replace("\'", "_SQ_");
        s = s.replace("@", "_AT_");
        s = s.replace("^", "_HAT_");
        return s;
    }

    private String germanEscape(String s) {
        s = s.replace("ç", "_ccedil_");
        s = s.replace("é", "_eacute_");
        s = s.replace("è", "_egrave_");
        s = s.replace("ã", "_atilde_");
        s = s.replace("á", "_aacute_");
        s = s.replace("à", "_agrave_");
        s = s.replace("í", "_iacute_");
        s = s.replace("ì", "_igrave_");
        s = s.replace("ü", "_ue_");
        s = s.replace("ä", "_ae_");
        s = s.replace("ö", "_oe_");
        s = s.replace("ô", "_ocirc_");
        s = s.replace("î", "_icirc_");
        s = s.replace("â", "_acirc_");
        s = s.replace("Ü", "_UE_");
        s = s.replace("Ä", "_AE_");
        s = s.replace("Ö", "_OE_");
        s = s.replace("ß", "_SZET_");
        s = s.replace("!", "_EXCLAMATION_");
        s = s.replace("?", "_QUESTION_");
        s = s.replace(":", "_COLON_");
        s = s.replace(";", "_SEMICOLON_");
        s = s.replace("·", "_CDOT_");
        s = s.replace("1", "_ONE_");
        s = s.replace("2", "_TWO_");
        s = s.replace("3", "_THREE_");
        s = s.replace("4", "_FOUR_");
        s = s.replace("5", "_FIVE_");
        s = s.replace("6", "_SIX_");
        s = s.replace("7", "_SEVEN_");
        s = s.replace("8", "_EIGHT_");
        s = s.replace("9", "_NINE_");
        s = s.replace("0", "_NULL_");
        s = s.replace("ë", "_EDIA_");
        s = s.replace("%", "_PERCENT_");
        s = s.replace("*", "_ASTERISK_");
        s = s.replace("&", "_AMPERSAND_");
        s = s.replace("\\", "_BACKSLASH_");
        s = s.replace("/", "_SLASH_");
        s = s.replace("§", "_PARAGRAPH_");
        s = s.replace("=", "_EQUALS_");
        s = s.replace("+", "_PLUS_");
        s = s.replace("-", "_DASH_");
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
    public void write(BinaryRCG g, Lexicon lex, String path, String encoding) throws IOException {
        Set<String> declared = new HashSet<>();

        Writer probw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".probs"), encoding));

        Writer lexaw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "lexabstract.gf"), encoding));
        lexaw.write("abstract bingrammargflexabstract = { \n\n");

        Writer lexw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "lexconcrete.gf"), encoding));
        lexw.write("concrete bingrammargflexconcrete of bingrammargflexabstract = {\n\n");

        Writer abstractw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "abstract.gf"), encoding));
        abstractw.write("abstract bingrammargfabstract = bingrammargflexabstract ** {\n\n");

        Writer concw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "concrete.gf"), encoding));
        concw.write("concrete bingrammargfconcrete of bingrammargfabstract = bingrammargflexconcrete ** {\n\n");

        int namecnt = 0;

        abstractw.write("flags \n");
        abstractw.write("startcat=VROOT1; \n\n");
	

        for (BinaryClause c : g.clauses) {
            String funName = "fun" + String.valueOf(namecnt++);
            String lhs = (String) c.n.getObjectWithId(GrammarConstants.PREDLABEL, c.lhs);
            lhs = escape(lhs);

            int[] rhsnames = {c.lc, c.rc};
            boolean[] rhspterms = {c.lcPt, c.rcPt};
            int rhslength = c.rc == -1 ? 1 : 2;

            // probability
            probw.write(funName + " " + c.score + "\n");

            // abstract cat and concrete lincat
            if (!declared.contains(lhs)) {
                declared.add(lhs);
                abstractw.write("cat " + lhs + " ;\n");
                concw.write("lincat " + lhs + " = " + buildTuple(lhs, c.yf.length) + " ;\n");
            } 

            // concrete lin
            int[] rhsarity = {0, 0};
            String lin = funName;
            for (int i = 0; i < rhslength; ++i) {
                lin += " rhs" + String.valueOf(i + 1);
            }
            lin += " = { ";
            for (int i = 0; i < c.yf.length; ++i) {
                if (i > 0) {
                    lin += " ; ";
                }
                lin += "p" + String.valueOf(i + 1) + " = ";
                for (int j = 0; j < c.yf[i].length; ++j) {
                    int k = c.yf[i][j] ? 1 : 0;
                    int l = rhsarity[k]++;
                    if (j > 0) {
                        lin += " ++ ";
                    }
                    lin += "rhs" + String.valueOf(k + 1) + ".p" + String.valueOf(l + 1);
                }
            }
            lin += "}";
            concw.write("lin " + lin + " ;\n");

            // abstract fun; and abstract/concrete lexicon
            String fun = "";
            for (int i = 0; i < rhslength; i++) {
                String urhsel = (String) c.n.getObjectWithId(GrammarConstants.PREDLABEL, rhsnames[i]);
                String rhsel = escape(urhsel);
                fun += rhsel + " -> ";
                if (!declared.contains(rhsel)) {
                    declared.add(rhsel);
                    if (rhspterms[i]) {
                        Integer tagId = lex.getNumberer().getIntWithId(GrammarConstants.PREDLABEL, urhsel);
                        lexaw.write("cat " + rhsel + " ;\n");
                        lexw.write("lincat " + rhsel + " = " + buildTuple(rhsel, rhsarity[i]) + " ;\n");
                        for (int wordId : lex.getWordsForTag(tagId)) {
                            String word = (String) lex.getNumberer().getObjectWithId(LexiconConstants.LEXWORD, wordId); 
                            word = escape(word);
                            String lexFunName = germanEscape(word) + "_" + rhsel;
                            lexaw.write("fun " + lexFunName + " : " + rhsel + ";\n");
                            lexw.write("lin " + lexFunName + " = { p1=\"" + word + "\" };\n");
                            probw.write(lexFunName + " " + lex.getScore(wordId, tagId) + "\n");
                        }
                    } else {
                        abstractw.write("cat " + rhsel + " ;\n");
                        concw.write("lincat " + rhsel + " = " + buildTuple(rhsel, rhsarity[i]) + " ;\n");
                    }
                } 
            }
            fun += lhs;
            abstractw.write("fun " + funName + " : " + fun + " ; \n");
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
