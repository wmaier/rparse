/*******************************************************************************
 * File GrammarFormalisms.java
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
package de.tuebingen.rparse.grammar.read;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;

public class BinaryRCGReaderPMCFG extends BufferedReader {

	private static Pattern COMMENT = Pattern.compile("^\\s*[#/*-]");
	private static Pattern IDENTIFIER = Pattern
			.compile("[a-zA-Z0-9_][^ \\t\\n\\r\\f\\v]+");
	private static Pattern DIGIT = Pattern.compile("\\d+"); // only allow counts
															// for now
	private static String FUN = ":";
	private static String LIN = "=";
	private static String LINDEF = "->";

	private String startPred = "VROOT1";
	private Numberer nb = null;

	public BinaryRCGReaderPMCFG(File f, Numberer nb)
			throws FileNotFoundException {
		super(new FileReader(f));
		this.startPred = GrammarConstants.DEFAULTSTART;
		this.nb = nb;
	}

	public BinaryRCG getRCG() throws IOException, GrammarException {
		RCG rcg = new RCG(this.nb);
		int startPredNum = nb.number(GrammarConstants.PREDLABEL, startPred);
		rcg.setStartPredLabel(startPredNum);
		BinaryRCG res = new BinaryRCG(rcg, null);
		HashMap<String, String[]> funcs = new HashMap<>();
		HashMap<String, String[]> lindefs = new HashMap<>();
		HashMap<String, String[]> lin = new HashMap<>();
		HashMap<String, String> score = new HashMap<>();
		String line = "";
		Matcher m = IDENTIFIER.matcher("");
		Matcher m_digit = DIGIT.matcher("");
		Matcher m_comment = COMMENT.matcher("");
		while ((line = super.readLine()) != null) {
			ArrayList<String> identifiers = new ArrayList<>();
			line = line.trim();
			String[] sp = line.split("\\s+");
			int pos = 0;
			while (m.reset(sp[pos]).matches() && pos < sp.length - 1) {
				identifiers.add(sp[pos]);
				pos += 1;
			}
			String kw = sp[pos];
			String[] def = Arrays.copyOfRange(sp, pos + 1, sp.length);
			if (FUN.equals(kw)) {
				for (String identifier : identifiers) {
					funcs.put(identifier, def);
				}
			} else if (LIN.equals(kw)) {
				for (String identifier : identifiers) {
					lin.put(identifier, def);
				}
			} else if (LINDEF.equals(kw)) {
				if (identifiers.size() != 1) {
					throw new GrammarException(
							"can only define linearization component for one id: "
									+ line);
				}
				lindefs.put(identifiers.get(0), def);
			} else if (m_digit.reset(kw).matches()) {
				if (identifiers.size() != 1) {
					throw new GrammarException(
							"can only define score/count for one id: " + line);
				}
				if (sp.length != 2) {
					throw new GrammarException(
							"does not look like a score or count, but should be one: "
									+ line);
				}
				// must be score
				score.put(identifiers.get(0), sp[1]);
			} else {
				if (m_comment.reset(line).matches()) {
					// do nothing
				} else {
					// also do nothing
				}
			}
		}

		for (String funId : funcs.keySet()) {
			String[] func = funcs.get(funId);
			String[] linearization = lin.get(funId); // s1 s2
			String count = score.get(funId);
			// number all labels
			int lhs = nb.number(GrammarConstants.PREDLABEL,
					func[0] + String.valueOf(linearization.length));
			int[] rhs = new int[func.length - 2];
			if (rhs.length > 2) {
				throw new GrammarException("Only binary and unary production allowed");
			}
			String[] rhsnames = new String[func.length - 2];
			for (int i = 0; i < rhs.length; ++i) {
				rhsnames[i] = func[i + 2];
			}
			Clause c = new Clause(rhs.length);
			c.lhsname = lhs;

			int varcnt = 1;
			int lhsargpos = 0;
			int[][] lhsargs = new int[linearization.length][];
			Vector<Vector<Integer>> rhsargs = new Vector<Vector<Integer>>();
			for (int i = 0; i < rhs.length; ++i) {
				rhsargs.add(new Vector<Integer>());
			}
			for (int i = 0; i < linearization.length; ++i) {
				int lhsargvarpos = 0;
				String linid = linearization[i]; // s1
				String[] lindef = lindefs.get(linid); // s1 = 1:2 3:0
				lhsargs[lhsargpos] = new int[lindef.length];

				for (String varRef : lindef) {
					String[] rhspossplit = varRef.split(":");
					int rhspos = Integer.valueOf(rhspossplit[0]);
					int rhsargpos = Integer.valueOf(rhspossplit[1]);
					lhsargs[lhsargpos][lhsargvarpos++] = varcnt;
					Vector<Integer> rhsargarray = rhsargs.get(rhspos);
					if (rhsargarray.size() < rhsargpos) {
						for (int j = rhsargarray.size(); j < rhsargpos; ++j) {
							rhsargarray.add(-1);
						}
					}
					rhsargs.get(rhspos).insertElementAt(varcnt, rhsargpos);
					varcnt++;
				}
				lhsargpos++;
			}
			c.lhsargs = lhsargs;
			c.rhsargs = new int[rhsargs.size()][];
			for (int i = 0; i < rhsargs.size(); ++i) {
				c.rhsargs[i] = new int[rhsargs.get(i).size()];
				for (int j = 0; j < rhsargs.get(i).size(); ++j) {
					c.rhsargs[i][j] = rhsargs.get(i).get(j);
				}
			}
			for (int i = 0; i < rhsnames.length; ++i) {
				rhs[i] = nb.number(GrammarConstants.PREDLABEL, rhsnames[i]
						+ String.valueOf(c.rhsargs[i].length));
			}
			c.rhsnames = rhs;
			BinaryClause bc = BinaryClause.constructClause(c, nb);
			bc.cnt = Integer.valueOf(count);
			res.addClause(bc);
		}
		return res;
	}

	public static void main(String[] args) throws Exception {

		String grammar = "fun1 : S <- VP VMFIN\n" + "fun1 = s1\n" + "fun1 1\n"
				+ "fun2 : VROOT <- S $.\n" + "fun2 = s2\n" + "fun2 1\n"
				+ "fun3 : VP <- PROAV VVPP\n" + "fun3 = s3 s4\n" + "fun3 1\n"
				+ "fun4 : VP <- VP VAINF\n" + "fun4 = s3 s5\n" + "fun4 1\n"
				+ "s1 -> 0:0 1:0 0:1\n" + "s2 -> 0:0 1:0\n" + "s3 -> 0:0\n"
				+ "s4 -> 1:0\n" + "s5 -> 0:1 1:0\n";

		File temp = File.createTempFile("temp", ".txt");
		temp.deleteOnExit();
		BufferedWriter w = new BufferedWriter(new FileWriter(temp));
		w.write(grammar);
		w.close();
		BinaryRCGReaderPMCFG r = new BinaryRCGReaderPMCFG(temp, new Numberer());
		BinaryRCG res = r.getRCG();
		for (BinaryClause bc : res.clauses) {
			System.err.println(bc.toString());
		}
		r.close();
	}

}
