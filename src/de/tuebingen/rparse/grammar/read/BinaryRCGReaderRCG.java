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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;

public class BinaryRCGReaderRCG extends GrammarReader<BinaryRCG> {

	private String startPred = "VROOT1";
	private Numberer nb = null;

	public BinaryRCGReaderRCG(File f, Numberer nb)
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
		String line = "";
		while ((line = super.readLine()) != null) {
			line = line.trim();
			String[] sp = line.split(":");

			String prefix = sp[0];
			String prod = sp[1];
			String[] spProd = prod.split(" --> ");
			String lhs = spProd[0];
			String rhs = spProd[1].substring(0, spProd[1].indexOf('['));
			String[] spRhs = rhs.split("\\s+");
			String yf = spProd[1].substring(spProd[1].indexOf('['));
			yf = yf.substring(3, yf.length() - 3);
			
			Clause c = new Clause(spRhs.length);
			c.lhsname = nb.number(GrammarConstants.PREDLABEL, lhs);
			c.rhsnames = new int[spRhs.length];
			for (int i = 0; i < spRhs.length; ++i) {
				c.rhsnames[i] = nb.number(GrammarConstants.PREDLABEL, spRhs[i]);
			}

			int varcnt = 0;
			ArrayList<ArrayList<Integer>> arglist = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> l = new ArrayList<>();
			ArrayList<Integer> r = new ArrayList<>();
			for (String arg : yf.split("\\], \\[")) {
				arglist.add(new ArrayList<Integer>());
				for (String argel : arg.split(", ")) {
					arglist.get(arglist.size() - 1).add(varcnt);
					if (!Boolean.valueOf(argel)) {
						l.add(varcnt);
					} else {
						r.add(varcnt);
					}
					varcnt++;
				}
			}
			c.lhsargs = new int[arglist.size()][];
			for (int i = 0; i < arglist.size(); ++i) {
				c.lhsargs[i] = new int[arglist.get(i).size()];
				for (int j = 0; j < arglist.get(i).size(); ++j) {
					c.lhsargs[i][j] = arglist.get(i).get(j);
				}
			}
			c.rhsargs = new int[2][];
			c.rhsargs[0] = new int[l.size()];
			for (int i = 0; i < l.size(); ++i) {
				c.rhsargs[0][i] = l.get(i);
			}
			c.rhsargs[1] = new int[r.size()];
			for (int i = 0; i < r.size(); ++i) {
				c.rhsargs[1][i] = r.get(i);
			}
			
			BinaryClause bc = BinaryClause.constructClause(c, nb);
			int count = Integer.valueOf(prefix);
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

		grammar = "1:VROOT1 --> S1 $.1 [[[false, true]]]\n1:S1 --> VP2 VMFIN1 [[[false, true, false]]]\n1:VP2 --> VP2 VAINF1 [[[false], [false, true]]]\n";
		
		File temp = File.createTempFile("temp", ".txt");
		temp.deleteOnExit();
		BufferedWriter w = new BufferedWriter(new FileWriter(temp));
		w.write(grammar);
		w.close();
		BinaryRCGReaderRCG r = new BinaryRCGReaderRCG(temp, new Numberer());
		BinaryRCG res = r.getRCG();
		for (BinaryClause bc : res.clauses) {
			System.err.println(bc.toString());
		}
		r.close();
	}

}
