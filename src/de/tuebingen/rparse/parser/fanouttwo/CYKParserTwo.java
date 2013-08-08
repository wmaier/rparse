/*******************************************************************************
 * File CYKParserTwo.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2012, 2013
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
package de.tuebingen.rparse.parser.fanouttwo;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.binarize.Debinarizer;
import de.tuebingen.rparse.grammar.fanouttwo.BinaryClauseTwo;
import de.tuebingen.rparse.grammar.fanouttwo.BinaryRCGTwo;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.parser.CYKParser;
import de.tuebingen.rparse.parser.ParserData;
import de.tuebingen.rparse.parser.RCGParser;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeLabel;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.constituent.process.ConstituentInputFormats;
import de.tuebingen.rparse.treebank.constituent.write.ConstituentSentenceWriterFactory;
import de.tuebingen.rparse.treebank.dep.ConstituentConverter;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.dep.DependencyInputFormats;
import de.tuebingen.rparse.treebank.dep.DependencySentenceWriterFactory;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;
import de.tuebingen.rparse.treebank.lex.ParserInput;

/**
 * A specialized parser for grammars with fanout two. Writing, result building
 * and some other stuff is shared with the unrestricted parser
 * 
 * @author wmaier
 */
public class CYKParserTwo implements RCGParser {

	// parser data which contains all necessary information for parsing
	private ParserData pd;

	// the chart
	private CYKChartTwo chart;

	// the agenda
	private PriorityAgendaTwo agenda;

	// our goal item
	private CYKItemTwo goal;

	// the input words, mapped to integers
	private int[] words;

	// the input terminals, as nodes
	private Node[] terminals;

	// write result (constituents)
	private SentenceWriter<Tree> sw;

	// write result (dependencies)
	private SentenceWriter<DependencyForest<DependencyForestNodeLabel, String>> dw;

	// the numberer
	private final Numberer nb;

	// our logger
	private Logger logger;

	// log level. this is needed because we need to ask it in order to not do
	// certain computations
	// which are only necessary for the output of a finer log level.
	private Level logLevel;

	private String agendaType;

	private BinaryRCGTwo bgg;

	public CYKParserTwo(ParserData pd, String agendaType, Numberer nb) {
		// get the logger and the log level
		logger = Logger.getLogger(CYKParser.class.getPackage().getName());
		Logger getLevelLogger = logger;
		logLevel = getLevelLogger.getLevel();
		while (logLevel == null) {
			getLevelLogger = getLevelLogger.getParent();
			logLevel = getLevelLogger.getLevel();
		}
		logLevel = Level.FINEST;
		logger.setLevel(logLevel);
		this.nb = nb;
		if (pd.bg == null)
			throw new NoSuchElementException(
					"Cannot run CYK parser with un-binarized grammar");
		// Efficiencyfize the fan-out-two-RCG
		try {
			bgg = BinaryRCGTwo.buildFromBinaryRCG(pd.bg);
		} catch (GrammarException e1) {
			throw new IllegalStateException(
					"Could not build special representation for grammar with fan-out two: "
							+ e1.getMessage());
		}
		System.err.println(bgg.printStats());
		this.pd = pd;
		try {
			sw = ConstituentSentenceWriterFactory
					.getSentenceWriter(ConstituentInputFormats.EXPORT);
		} catch (UnknownFormatException e) {
		}
		try {
			dw = DependencySentenceWriterFactory
					.getSentenceWriter(DependencyInputFormats.CONLL);
		} catch (UnknownFormatException e) {
		}
		chart = new CYKChartTwo(bgg);
		goal = null;
		this.agendaType = agendaType;
		try {
			agenda = PriorityAgendaTwoFactory.getPriorityAgenda(agendaType,
					pd.nb);
		} catch (UnknownFormatException e) {
			throw new NoSuchElementException(
					"Could not get a priority agenda of type " + agendaType);
		}
	}

	@Override
	public boolean parse(ParserInput pi) {
		this.words = pi.getWords();
		doParse(words, pi.getTags());
		if (goal != null)
			System.err.println(goal);
		System.err.println("chart size: " + chart.size());
		return goal != null;
	}

	public static int UNSET = -1;

	public boolean doParse(int[] words, int[] tags) {
		CYKItemTwo item;
		for (int i = 0; i < tags.length; ++i) {
			item = new CYKItemTwo(tags[i], 1, 0.0, null, null, i, i + 1, UNSET,
					UNSET, "ax");
			item.oscore = pd.est.get(words.length, tags[i], item.ll, item.lr,
					item.rl, item.rr);
			agenda.push(item);
		}

		ArrayList<CYKItemTwo> transport = new ArrayList<CYKItemTwo>();
		CYKItemTwo nit = null;

		// boolean cont = false;
		while (!agenda.isEmpty()) {
			item = agenda.poll();
			chart.add(item);

			if (logLevel.equals(Level.INFO)) {
				String lstring = "";
				if (item.olc != null) {
					lstring = item.olc.print(pd.nb);
				}
				String rstring = "";
				if (item.orc != null) {
					rstring = item.orc.print(pd.nb);
				}
				logger.info("item: " + item.print(pd.nb) + " # " + lstring
						+ "|" + rstring);
			}

			if (item.label == pd.bg.startSymbol && item.isOne() && item.ll == 0
					&& item.lr == words.length) {
				goal = item;
				break;
			}

			// System.err.println("Processing " + item.print(pd.nb));

			// item is left child
			if (bgg.twoClByLc.containsKey(item.label)) {
				for (BinaryClauseTwo bc : bgg.twoClByLc.get(item.label)) {
					// do we have unary clause?
					if (bc.type == 1 || bc.type == 2) {
						// A(X) -> B(X) or A(X,Y) -> B(X,Y): just copy upwards
						nit = new CYKItemTwo(bc.lhs, item.onetwo, item.iscore
								+ bc.score, item, null, item, item.st);
						transport.add(nit);
					}
					// not unary: we have do get something matching from the
					// chart
					// right child has one span
					if (chart.hasLabelOne(bc.rc)) {
						// chartone: label -> l -> r
						if (bc.type == 3) {
							// 3 A(XY) -> B(X) C(Y)
							// X.r = Y.l
							if (item.isOne()) {
								if (chart.chartone.get(bc.rc).containsKey(
										item.lr)) {
									for (int yr : chart.chartone.get(bc.rc)
											.get(item.lr).keySet()) {
										CYKItemTwo candit = chart.chartone
												.get(bc.rc).get(item.lr)
												.get(yr);
										nit = new CYKItemTwo(bc.lhs, 1,
												item.iscore + candit.iscore
														+ bc.score, item,
												candit, item.ll, candit.lr,
												UNSET, UNSET, "l3");
										transport.add(nit);
									}
								}
							}
						} else if (bc.type == 4) {
							// 4 A(X,Y) -> B(X) C(Y)
							// Y.l > X.r
							if (item.isOne()) {
								for (int yl : chart.chartone.get(bc.rc)
										.keySet()) {
									if (yl > item.lr) {
										for (int yr : chart.chartone.get(bc.rc)
												.get(yl).keySet()) {
											CYKItemTwo candit = chart.chartone
													.get(bc.rc).get(yl).get(yr);
											nit = new CYKItemTwo(bc.lhs, 2,
													item.iscore + candit.iscore
															+ bc.score, item,
													candit, item.ll, item.lr,
													candit.ll, candit.lr, "l4");
											transport.add(nit);
										}
									}
								}
							}
						} else if (bc.type == 5) {
							// 5 A(XYZ) -> B(X,Z) C(Y)
							// Y.l = X.r
							// Y.r = Z.l
							if (!item.isOne()) {
								if (chart.chartone.get(bc.rc).containsKey(
										item.lr)) {
									if (chart.chartone.get(bc.rc).get(item.lr)
											.containsKey(item.rl)) {
										CYKItemTwo candit = chart.chartone
												.get(bc.rc).get(item.lr)
												.get(item.rl);
										nit = new CYKItemTwo(bc.lhs, 1,
												item.iscore + candit.iscore
														+ bc.score, item,
												candit, item.ll, item.rr,
												UNSET, UNSET, "l5");
										transport.add(nit);
									}
								}
							}
						} else if (bc.type == 6) {
							// 6 A(X,YZ) -> B(X,Y) C(Z)
							// Z.l = Y.r
							if (!item.isOne()) {
								if (chart.chartone.get(bc.rc).containsKey(
										item.rr)) {
									for (int zr : chart.chartone.get(bc.rc)
											.get(item.rr).keySet()) {
										CYKItemTwo candit = chart.chartone
												.get(bc.rc).get(item.rr)
												.get(zr);
										nit = new CYKItemTwo(bc.lhs, 2,
												item.iscore + candit.iscore
														+ bc.score, item,
												candit, item.ll, item.lr,
												item.rl, candit.lr, "l6");
										transport.add(nit);
									}
								}
							}
						} else if (bc.type == 7) {
							// 7 A(X,YZ) -> B(X,Z) C(Y)
							// Y.l > X.r
							// Y.r = Z.l
							if (!item.isOne()) {
								for (int yl : chart.chartone.get(bc.rc)
										.keySet()) {
									if (yl > item.lr) {
										if (chart.chartone.get(bc.rc).get(yl)
												.containsKey(item.rl)) {
											CYKItemTwo candit = chart.chartone
													.get(bc.rc).get(yl)
													.get(item.rl);
											nit = new CYKItemTwo(bc.lhs, 2,
													item.iscore + candit.iscore
															+ bc.score, item,
													candit, item.ll, item.lr,
													candit.ll, item.rr, "l7");
											transport.add(nit);
										}
									}
								}
							}
						} else if (bc.type == 8) {
							// 8 A(XY,Z) -> B(X,Z) C(Y)
							// Y.l = X.r
							// Y.r < Z.l
							if (!item.isOne()) {
								if (chart.chartone.get(bc.rc).containsKey(
										item.lr)) {
									for (int yr : chart.chartone.get(bc.rc)
											.get(item.lr).keySet()) {
										if (yr < item.rl) {
											CYKItemTwo candit = chart.chartone
													.get(bc.rc).get(item.lr)
													.get(yr);
											nit = new CYKItemTwo(bc.lhs, 2,
													item.iscore + candit.iscore
															+ bc.score, item,
													candit, item.ll, candit.lr,
													item.rl, item.rr, "l8");
											transport.add(nit);
										}
									}
								}
							}
						}
					}
					// right child has two spans
					if (chart.hasLabelTwo(bc.rc)) {
						if (bc.type == 9) {
							// 9 A(XY,Z) -> B(X) C(Y,Z)
							// Y.l = X.r
							if (item.isOne()) {
								if (chart.charttwo.get(bc.rc).containsKey(
										item.lr)) {
									for (int yr : chart.charttwo.get(bc.rc)
											.get(item.lr).keySet()) {
										for (int zl : chart.charttwo.get(bc.rc)
												.get(item.lr).get(yr).keySet()) {
											for (int zr : chart.charttwo
													.get(bc.rc).get(item.lr)
													.get(yr).get(zl).keySet()) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.rc)
														.get(item.lr).get(yr)
														.get(zl).get(zr);
												nit = new CYKItemTwo(bc.lhs, 2,
														item.iscore
																+ candit.iscore
																+ bc.score,
														item, candit, item.ll,
														candit.lr, candit.rl,
														candit.rr, "l9");
												transport.add(nit);
											}
										}
									}
								}
							}
						} else if (bc.type == 10) {
							// 10 A(XY,ZU) -> B(X,Z) C(Y,U)
							// ill-nested
							// Y.l = X.r (item.lr)
							// Y.r < Z.l (item.rl)
							// U.l = Z.r (item.rr)
							if (!item.isOne()) {
								if (chart.charttwo.get(bc.rc).containsKey(
										item.lr)) {
									for (int yl : chart.charttwo.get(bc.rc)
											.get(item.lr).keySet()) {
										if (yl < item.rl) {
											if (chart.charttwo.get(bc.rc)
													.get(item.lr).get(yl)
													.containsKey(item.rr)) {
												for (int ur : chart.charttwo
														.get(bc.rc)
														.get(item.lr).get(yl)
														.get(item.rr).keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.rc)
															.get(item.lr)
															.get(yl)
															.get(item.rr)
															.get(ur);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															item, candit,
															item.ll, candit.lr,
															item.rl, candit.rr,
															"l10");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 11) {
							// 11 A(XY,ZU) -> B(X,U) C(Y,Z)
							// Y.l = X.r (item.lr)
							// Z.r = U.l (item.rl)
							if (!item.isOne()) {
								if (chart.charttwo.get(bc.rc).containsKey(
										item.lr)) {
									for (int yr : chart.charttwo.get(bc.rc)
											.get(item.lr).keySet()) {
										for (int zl : chart.charttwo.get(bc.rc)
												.get(item.lr).get(yr).keySet()) {
											if (chart.charttwo.get(bc.rc)
													.get(item.lr).get(yr)
													.get(zl)
													.containsKey(item.rl)) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.rc)
														.get(item.lr).get(yr)
														.get(zl).get(item.rl);
												nit = new CYKItemTwo(bc.lhs, 2,
														item.iscore
																+ candit.iscore
																+ bc.score,
														item, candit, item.ll,
														candit.lr, candit.rl,
														item.rr, "l11");
												transport.add(nit);
											}
										}
									}
								}
							}
						} else if (bc.type == 12) {
							// 12 A(X,YZU) -> B(X,Z) C(Y,U)
							// Y.l > X.r
							// Y.r = Z.l
							// U.l = Z.r
							if (!item.isOne()) {
								for (int yl : chart.charttwo.get(bc.rc)
										.keySet()) {
									if (yl > item.lr) {
										if (chart.charttwo.get(bc.rc).get(yl)
												.containsKey(item.rl)) {
											if (chart.charttwo.get(bc.rc)
													.get(yl).get(item.rl)
													.containsKey(item.rr)) {
												for (int ur : chart.charttwo
														.get(bc.rc).get(yl)
														.get(item.rl)
														.get(item.rr).keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.rc).get(yl)
															.get(item.rl)
															.get(item.rr)
															.get(ur);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															item, candit,
															item.ll, item.lr,
															candit.rl,
															candit.rr, "l12");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 13) {
							// 13 A(XYZ,U) -> B(X,Z) C(Y,U)
							// Y.l = X.r
							// Y.r = Z.l
							// U.l > Z.r
							if (!item.isOne()) {
								if (chart.charttwo.get(bc.rc).containsKey(
										item.lr)) {
									if (chart.charttwo.get(bc.rc).get(item.lr)
											.containsKey(item.rl)) {
										for (int ul : chart.charttwo.get(bc.rc)
												.get(item.lr).get(item.rl)
												.keySet()) {
											if (ul > item.rr) {
												for (int ur : chart.charttwo
														.get(bc.rc)
														.get(item.lr)
														.get(item.rl).get(ul)
														.keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.rc)
															.get(item.lr)
															.get(item.rl)
															.get(ul).get(ur);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															item, candit,
															item.ll, item.rr,
															candit.rl,
															candit.rr, "l13");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 14) {
							// 14 A(XYZU) -> B(X,Z) C(Y,U)
							if (!item.isOne()) {
								if (chart.charttwo.get(bc.rc).containsKey(
										item.lr)) {
									if (chart.charttwo.get(bc.rc).get(item.lr)
											.containsKey(item.rl)) {
										if (chart.charttwo.get(bc.rc)
												.get(item.lr).get(item.rl)
												.containsKey(item.rr)) {
											for (int ur : chart.charttwo
													.get(bc.rc).get(item.lr)
													.get(item.rl).get(item.rr)
													.keySet()) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.rc)
														.get(item.lr)
														.get(item.rl)
														.get(item.rr).get(ur);
												nit = new CYKItemTwo(bc.lhs, 1,
														item.iscore
																+ candit.iscore
																+ bc.score,
														item, candit, item.ll,
														candit.rr, UNSET,
														UNSET, "l14");
												transport.add(nit);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			// item is right child
			if (bgg.twoClByRc.containsKey(item.label)) {
				for (BinaryClauseTwo bc : bgg.twoClByRc.get(item.label)) {
					// not unary: we have do get something matching from the
					// chart
					// left child has one span
					if (chart.hasLabelOne(bc.lc)) {
						// chartone: label -> l -> r
						if (bc.type == 3) {
							// 3 A(XY) -> B(X) C(Y)
							// X.r = Y.l
							if (item.isOne()) {
								for (int xl : chart.chartone.get(bc.lc)
										.keySet()) {
									if (chart.chartone.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										CYKItemTwo candit = chart.chartone
												.get(bc.lc).get(xl)
												.get(item.ll);
										nit = new CYKItemTwo(bc.lhs, 1,
												item.iscore + candit.iscore
														+ bc.score, candit,
												item, candit.ll, item.lr,
												UNSET, UNSET, "r3");
										transport.add(nit);
									}
								}
							}
						} else if (bc.type == 4) {
							// 4 A(X,Y) -> B(X) C(Y)
							// X.r < Y.l
							if (item.isOne()) {
								for (int xl : chart.chartone.get(bc.lc)
										.keySet()) {
									for (int xr : chart.chartone.get(bc.lc)
											.get(xl).keySet()) {
										int yl = item.ll;
										if (xr < yl) {
											CYKItemTwo candit = chart.chartone
													.get(bc.lc).get(xl).get(xr);
											nit = new CYKItemTwo(bc.lhs, 2,
													item.iscore + candit.iscore
															+ bc.score, candit,
													item, candit.ll, candit.lr,
													item.ll, item.lr, "r4");
											transport.add(nit);
										}
									}
								}
							}
						} else if (bc.type == 9) {
							// 9 A(XY,Z) -> B(X) C(Y,Z)
							// Y.l = X.r
							if (!item.isOne()) {
								for (int xl : chart.chartone.get(bc.lc)
										.keySet()) {
									if (chart.chartone.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										CYKItemTwo candit = chart.chartone
												.get(bc.lc).get(xl)
												.get(item.ll);
										nit = new CYKItemTwo(bc.lhs, 2,
												item.iscore + candit.iscore
														+ bc.score, candit,
												item, candit.ll, item.lr,
												item.rl, item.rr, "r9");
										transport.add(nit);
									}
								}
							}
						}
					}
					// left child has two spans
					if (chart.hasLabelTwo(bc.lc)) {
						if (bc.type == 5) {
							// 5 A(XYZ) -> B(X,Z) C(Y)
							// X.r = Y.l
							// Z.l = Y.r
							if (item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										if (chart.charttwo.get(bc.lc).get(xl)
												.get(item.ll)
												.containsKey(item.lr)) {
											for (int zr : chart.charttwo
													.get(bc.lc).get(xl)
													.get(item.ll).get(item.lr)
													.keySet()) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.lc).get(xl)
														.get(item.ll)
														.get(item.lr).get(zr);
												nit = new CYKItemTwo(bc.lhs, 1,
														item.iscore
																+ candit.iscore
																+ bc.score,
														candit, item,
														candit.ll, candit.rr,
														UNSET, UNSET, "r5");
												transport.add(nit);
											}
										}
									}
								}
							}
						} else if (bc.type == 6) {
							// 6 A(X,YZ) -> B(X,Y) C(Z)
							// Y.r = Z.l
							if (item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									for (int xr : chart.charttwo.get(bc.lc)
											.get(xl).keySet()) {
										for (int yl : chart.charttwo.get(bc.lc)
												.get(xl).get(xr).keySet()) {
											if (chart.charttwo.get(bc.lc)
													.get(xl).get(xr).get(yl)
													.containsKey(item.ll)) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.lc).get(xl)
														.get(xr).get(yl)
														.get(item.ll);
												nit = new CYKItemTwo(bc.lhs, 2,
														item.iscore
																+ candit.iscore
																+ bc.score,
														candit, item,
														candit.ll, candit.lr,
														candit.rl, item.lr,
														"r6");
												transport.add(nit);
											}
										}
									}
								}
							}
						} else if (bc.type == 7) {
							// 7 A(X,YZ) -> B(X,Z) C(Y)
							// Y.l > X.r
							// Y.r = Z.l
							if (item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									for (int xr : chart.charttwo.get(bc.lc)
											.get(xl).keySet()) {
										if (xr < item.ll) {
											if (chart.charttwo.get(bc.lc)
													.get(xl).get(xr)
													.containsKey(item.lr)) {
												for (int zr : chart.charttwo
														.get(bc.lc).get(xl)
														.get(xr).get(item.lr)
														.keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(xr)
															.get(item.lr)
															.get(zr);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll,
															candit.lr, item.ll,
															candit.rr, "r7");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 8) {
							// 8 A(XY,Z) -> B(X,Z) C(Y)
							// Y.l = X.r
							// Y.r < Z.l
							if (item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										for (int zl : chart.charttwo.get(bc.lc)
												.get(xl).get(item.ll).keySet()) {
											if (zl > item.lr) {
												for (int zr : chart.charttwo
														.get(bc.lc).get(xl)
														.get(item.ll).get(zl)
														.keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(item.ll)
															.get(zl).get(zr);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll, item.lr,
															candit.rl,
															candit.rr, "r8");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 10) {
							// 10 A(XY,ZU) -> B(X,Z) C(Y,U)
							// ill-nested
							// Y.l = X.r (item.lr)
							// Y.r < Z.l (item.rl)
							// U.l = Z.r (item.rr)
							if (!item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										int xr = item.ll;
										for (int zl : chart.charttwo.get(bc.lc)
												.get(xl).get(xr).keySet()) {
											if (zl > item.lr) {
												int zr = item.rl;
												if (chart.charttwo.get(bc.lc)
														.get(xl).get(xr)
														.get(zl)
														.containsKey(zr)) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(xr).get(zl)
															.get(zr);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll, item.lr,
															candit.rl, item.rr,
															"r10");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 11) {
							// 11 A(XY,ZU) -> B(X,U) C(Y,Z)
							// X.r = Y.l
							// U.l = Z.r
							if (!item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										for (int zl : chart.charttwo.get(bc.lc)
												.get(xl).get(item.ll).keySet()) {
											if (chart.charttwo.get(bc.lc)
													.get(xl).get(item.ll)
													.get(zl)
													.containsKey(item.rl)) {
												for (int zr : chart.charttwo
														.get(bc.lc).get(xl)
														.get(item.ll)
														.get(item.rl).keySet()) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(item.ll)
															.get(item.rl)
															.get(zr);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll, item.lr,
															item.rl, candit.rr,
															"r11");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 12) {
							// 12 A(X,YZU) -> B(X,Z) C(Y,U)
							// Y.l > X.r
							// Y.r = Z.l
							// U.l = Z.r
							if (!item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									for (int xr : chart.charttwo.get(bc.lc)
											.get(xl).keySet()) {
										if (xr < item.ll) {
											if (chart.charttwo.get(bc.lc)
													.get(xl).get(xr)
													.containsKey(item.lr)) {
												if (chart.charttwo.get(bc.lc)
														.get(xl).get(xr)
														.get(item.lr)
														.containsKey(item.rl)) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(xr)
															.get(item.lr)
															.get(item.rl);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll,
															candit.lr, item.ll,
															item.rr, "r12");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 13) {
							// 13 A(XYZ,U) -> B(X,Z) C(Y,U)
							// X.r = Y.l
							// Z.l = Y.r
							// Z.r < U.l
							if (!item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										if (chart.charttwo.get(bc.lc).get(xl)
												.get(item.ll)
												.containsKey(item.lr)) {
											for (int zr : chart.charttwo
													.get(bc.lc).get(xl)
													.get(item.ll).get(item.lr)
													.keySet()) {
												if (zr < item.rl) {
													CYKItemTwo candit = chart.charttwo
															.get(bc.lc).get(xl)
															.get(item.ll)
															.get(item.lr)
															.get(zr);
													nit = new CYKItemTwo(
															bc.lhs,
															2,
															item.iscore
																	+ candit.iscore
																	+ bc.score,
															candit, item,
															candit.ll,
															candit.lr, item.rl,
															item.rr, "r13");
													transport.add(nit);
												}
											}
										}
									}
								}
							}
						} else if (bc.type == 14) {
							// 14 A(XYZU) -> B(X,Z) C(Y,U)
							// item is C
							if (!item.isOne()) {
								for (int xl : chart.charttwo.get(bc.lc)
										.keySet()) {
									if (chart.charttwo.get(bc.lc).get(xl)
											.containsKey(item.ll)) {
										if (chart.charttwo.get(bc.lc).get(xl)
												.get(item.ll)
												.containsKey(item.lr)) {
											if (chart.charttwo.get(bc.lc)
													.get(xl).get(item.ll)
													.get(item.lr)
													.containsKey(item.rl)) {
												CYKItemTwo candit = chart.charttwo
														.get(bc.lc).get(xl)
														.get(item.ll)
														.get(item.lr)
														.get(item.rl);
												nit = new CYKItemTwo(bc.lhs, 1,
														item.iscore
																+ candit.iscore
																+ bc.score,
														candit, item,
														candit.ll, item.rr,
														UNSET, UNSET, "r14");
												transport.add(nit);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			for (CYKItemTwo it : transport) {
				if (!chart.hasScore(it)) {
					it.oscore = pd.est.get(words.length, it.label, it.ll,
							it.lr, it.rl, it.rr);
					if (logLevel.equals(Level.INFO)) {
						String lstring = "";
						if (item.olc != null)
							lstring = item.olc.print(pd.nb);
						String rstring = "";
						if (item.orc != null)
							rstring = item.orc.print(pd.nb);

						logger.info("--> agenda item: " + it.print(pd.nb)
								+ " # " + lstring + "|" + rstring);
					}
					if (!(it.isSane())) {
						System.err.println("ill-formed item: " + it.print(nb));
						System.exit(1);
					} else {
						agenda.push(it);
					}
				}
			}
			transport = new ArrayList<CYKItemTwo>();

		}

		return goal != null;
	}

	@Override
	public void reset() {
		try {
			agenda = PriorityAgendaTwoFactory.getPriorityAgenda(agendaType,
					pd.nb);
		} catch (UnknownFormatException e) {
			throw new NoSuchElementException(
					"Could not get a priority agenda of type " + agendaType);
		}
		chart = new CYKChartTwo(bgg);
		goal = null;

		// getting new instances and calling the garbage collector is faster
		// than clearing.
		System.gc();
	}

	@Override
	public Tree getResult() {
		if (goal == null)
			throw new NoSuchElementException("No goal item present");
		Tree ret = new Tree(nb);
		terminals = new Node[words.length];
		System.err.println("goal: " + goal.print(nb));
		ret.setRoot(buildTree(goal));
		ret.setLastterm(words.length);
		ret.setTerminals(Arrays.asList(terminals));
		Debinarizer.debinarize(ret);
		return ret;
	}

	/*
	 * Build a tree from the goal item.
	 */
	private Node buildTree(CYKItemTwo it) {
		NodeLabel plabel = new NodeLabel();
		String tag = (String) pd.nb.getObjectWithId(GrammarConstants.PREDLABEL,
				it.label);
		plabel.setTag(Utilities.removeArity(tag));
		Node ret = new Node(plabel);
		if (it.olc != null) {
			Node lcn = buildTree(it.olc);
			ret.appendChild(lcn);
		} else {
			// determine term position
			// int i = 0;
			// for (; i < words.length && !it.rvec.get(i); ++i);
			int i = it.lr - 1;
			plabel.setWord((String) pd.nb.getObjectWithId(
					LexiconConstants.INPUTWORD, words[i]));
			terminals[i] = ret;
			plabel.setNum(i + 1);
		}
		if (it.orc != null) {
			Node rcn = buildTree(it.orc);
			ret.appendChild(rcn);
		}
		plabel.setEdge("--");
		plabel.setMorph("--");
		return ret;
	}

	@Override
	public void writeResult(Writer w, int scnt, ProcessingTask<Tree> task)
			throws IOException, TreebankException {
		Tree result = getResult();
		if (task != null) {
			task.processSentence(result);
		}
		result.setId(scnt);
		try {
			sw.write(result, w);
		} catch (TreebankException e) {
			throw new IOException(e.getMessage());
		}
		w.flush();
	}

	@Override
	public String getStats() {
		return agenda.getStats() + "\n" + "Chart size: " + chart.size() + "\n"
				+ "Composer stats: " + pd.yfcomp.stats();
	}

	@Override
	public void writeDependencyResult(
			Writer w,
			int scnt,
			ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task)
			throws IOException, TreebankException {

		Tree result = getResult();
		result.setId(scnt);
		DependencyForest<DependencyForestNodeLabel, String> g = ConstituentConverter
				.convert(result);
		if (task != null) {
			task.processSentence(g);
		}
		try {
			dw.write(g, w);
		} catch (TreebankException e) {
			throw new IOException(e.getMessage());
		}
		w.flush();

	}

}
