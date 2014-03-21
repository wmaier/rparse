/*******************************************************************************
 * File ParserData.java
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
package de.tuebingen.rparse.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.TrainingMethod;
import de.tuebingen.rparse.grammar.TrainingMethodFactory;
import de.tuebingen.rparse.grammar.TrainingMethods;
import de.tuebingen.rparse.grammar.estimates.Estimate;
import de.tuebingen.rparse.grammar.read.BinaryRCGReaderPMCFG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * A package which avoids to confuse instances and which obliterates the need to
 * have methods with 1000 parameters, and which allows for easy serialization.
 * 
 * @author wmaier
 */
public class ParserData implements Serializable {

	/**
	 * The unbinarized grammar
	 */
	public RCG g;

	/**
	 * The binarized grammar
	 */
	public BinaryRCG bg;

	/**
	 * The lexicon
	 */
	public Lexicon l;

	/**
	 * A yield function composer for the parser
	 */
	public YieldFunctionComposer yfcomp;

	/**
	 * An context summary estimate
	 */
	public Estimate est;

	/**
	 * A numberer
	 */
	public Numberer nb;

	/**
	 * Indicates if we should do deterministic filtering (K&M 03)
	 */
	public boolean doFilter;

	/**
	 * Construct a ParserData
	 * 
	 * @param g
	 *            The grammar
	 * @param l
	 *            The lexicon
	 * @param n
	 *            The numberer
	 */
	public ParserData(RCG g, Lexicon l, Numberer n) {
		if (g == null || l == null || n == null)
			throw new NoSuchElementException("Cannot create parser data.");
		this.g = g;
		this.bg = null;
		this.l = l;
		this.est = null;
		this.yfcomp = null;
		this.nb = n;
		this.doFilter = false;
	}

	/**
	 * Construct a empty new parser data
	 */
	public ParserData() {
		this.nb = new Numberer();
		this.g = new RCG(nb);
		this.bg = null;
		this.l = new Lexicon(nb);
		this.yfcomp = null;
		this.est = null;
		this.doFilter = false;
	}

	/**
	 * Compute the log probabilities of all clauses. Should only be called once
	 * of course.
	 */
	public void computeLogprobs() {
		if (bg != null)
			for (BinaryClause bc : bg.clauses) {
				bc.score = Math.abs(Math.log(bc.score));
			}
	}

	private static final long serialVersionUID = 9087498304785086841L;

	/**
	 * Write serialized model to file
	 * 
	 * @param filename
	 *            The filename to serialize this thing to
	 * @throws IOException
	 *             Unexpected I/O during serialization
	 */
	public void serializeModel(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		GZIPOutputStream zipout = new GZIPOutputStream(fos);
		ObjectOutputStream out = new ObjectOutputStream(zipout);
		out.writeObject(this);
		out.flush();
		out.close();
	}

	/**
	 * Deserialize a model from file
	 * 
	 * @param filename
	 *            The model file
	 * @return The ParserData instance
	 * @throws IOException
	 *             If there is unexpected I/O during deserialization
	 * @throws ClassNotFoundException
	 *             If the parser has changed between serialization and
	 *             de-serialization
	 */
	public static ParserData unserializeModel(String filename)
			throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(filename);
		GZIPInputStream zipin = new GZIPInputStream(fis);
		ObjectInputStream in = new ObjectInputStream(zipin);
		ParserData ret = (ParserData) in.readObject();
		in.close();
		// get some loggers, since they don't get serialized.
		ret.est.setLogger(Logger.getLogger(Estimate.class.getPackage()
				.getName()));
		return ret;
	}

	public static ParserData buildFromBinaryGrammar(String filename)
			throws IOException, GrammarException, UnknownTaskException, ParameterException {
		ParserData pd = new ParserData();
		Numberer nb = new Numberer();
		pd.nb = nb;
		BinaryRCGReaderPMCFG r = 
					new BinaryRCGReaderPMCFG(new File(filename), nb);
		pd.bg = r.getRCG();
		TrainingMethod t = TrainingMethodFactory.getTrainingMethod(TrainingMethods.MLE, pd.g, pd.bg, pd.l, pd.nb, "");
		t.setDoBinarized(true);
		pd.doFilter = false;
		t.process();
		pd.yfcomp = YieldFunctionComposerFactory.getYieldFunctionComposer(YieldFunctionComposerTypes.FAST, "");
		r.close();
		return pd;
	}

}
