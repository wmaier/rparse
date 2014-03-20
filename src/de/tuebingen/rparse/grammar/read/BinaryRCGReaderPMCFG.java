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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;

public class BinaryRCGReaderPMCFG extends BufferedReader {

	private String startPred = "";
	private Numberer nb = null;

	public BinaryRCGReaderPMCFG(File f, Numberer nb)
			throws FileNotFoundException {
		super(new FileReader(f));
		this.startPred = GrammarConstants.DEFAULTSTART;
		this.nb = nb;
	}

	public BinaryRCG getRCG() throws IOException, GrammarException {
		RCG rcg = new RCG(this.nb);
		BinaryRCG res = new BinaryRCG(new RCG(this.nb), null);
		HashMap<String, String> funcs = new HashMap<String, String>();
		HashMap<String, String> lindef = new HashMap<String, String>();
		HashMap<String, String> lin = new HashMap<String, String>();
		String line = "";
		while ((line = super.readLine()) != null) {
			System.err.println(line);
		}
		return res;
	}

	public static void main(String[] args) throws Exception {
		BinaryRCGReaderPMCFG r = new BinaryRCGReaderPMCFG(new File(args[0]),
				new Numberer());
		BinaryRCG res = r.getRCG();
	}

}
