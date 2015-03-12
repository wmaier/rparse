/*******************************************************************************
 * File Rparse.java
 * 
 * Authors:
 *    Wolfgang Maier, Kilian Evang
 *    
 * Copyright:
 *    Wolfgang Maier, Kilian Evang, 2011
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
package de.tuebingen.rparse.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeoutException;

import de.tuebingen.rparse.eval.EvalConnector;
import de.tuebingen.rparse.eval.EvalException;
import de.tuebingen.rparse.eval.EvalTypes;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.TrainingMethod;
import de.tuebingen.rparse.grammar.TrainingMethodFactory;
import de.tuebingen.rparse.grammar.TrainingMethods;
import de.tuebingen.rparse.grammar.binarize.Binarizer;
import de.tuebingen.rparse.grammar.binarize.BinarizerFactory;
import de.tuebingen.rparse.grammar.binarize.BinarizerTypes;
import de.tuebingen.rparse.grammar.estimates.Estimate;
import de.tuebingen.rparse.grammar.estimates.EstimateTypes;
import de.tuebingen.rparse.grammar.estimates.EstimatesFactory;
import de.tuebingen.rparse.grammar.read.RCGExtractor;
import de.tuebingen.rparse.grammar.read.RCGReader;
import de.tuebingen.rparse.grammar.write.GrammarFormats;
import de.tuebingen.rparse.grammar.write.GrammarWriter;
import de.tuebingen.rparse.grammar.write.GrammarWriterFactory;
import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.CommandLineOption;
import de.tuebingen.rparse.misc.CommandLineParameters;
import de.tuebingen.rparse.misc.Constants;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.misc.PrefixSuffixFilter;
import de.tuebingen.rparse.misc.Ranges;
import de.tuebingen.rparse.misc.SimpleFileFilter;
import de.tuebingen.rparse.misc.Test;
import de.tuebingen.rparse.misc.Timer;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.misc.VerySimpleFormatter;
import de.tuebingen.rparse.parser.ParserData;
import de.tuebingen.rparse.parser.ParserDataFormats;
import de.tuebingen.rparse.parser.ParserDataWriter;
import de.tuebingen.rparse.parser.ParserDataWriterFactory;
import de.tuebingen.rparse.parser.ParserFactory;
import de.tuebingen.rparse.parser.ParsingTypes;
import de.tuebingen.rparse.parser.RCGParser;
import de.tuebingen.rparse.parser.YieldFunctionComposerFactory;
import de.tuebingen.rparse.parser.YieldFunctionComposerTypes;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.MultiTask;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.SplittingExportTask;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.TreebankProcessor;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.WriterExportTask;
import de.tuebingen.rparse.treebank.constituent.ConstituentParentAnnotator;
import de.tuebingen.rparse.treebank.constituent.ConstituentProcessingTaskFactory;
import de.tuebingen.rparse.treebank.constituent.ConstituentTestsFactory;
import de.tuebingen.rparse.treebank.constituent.HeadFinder;
import de.tuebingen.rparse.treebank.constituent.HeadFinderFactory;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.constituent.process.ConstituentInputFormats;
import de.tuebingen.rparse.treebank.constituent.process.ConstituentProcessorFactory;
import de.tuebingen.rparse.treebank.constituent.write.ConstituentSentenceWriterFactory;
import de.tuebingen.rparse.treebank.dep.DepParentAnnotator;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.dep.DependencyInputFormats;
import de.tuebingen.rparse.treebank.dep.DependencyProcessingTaskFactory;
import de.tuebingen.rparse.treebank.dep.DependencySentenceWriterFactory;
import de.tuebingen.rparse.treebank.dep.DependencyTestsFactory;
import de.tuebingen.rparse.treebank.dep.DependencyTreebankProcessorFactory;
import de.tuebingen.rparse.treebank.dep.DirectoryDependencyTreebankProcessor;
import de.tuebingen.rparse.treebank.lex.Lexicon;
import de.tuebingen.rparse.treebank.lex.LexiconException;
import de.tuebingen.rparse.treebank.lex.LexiconReader;
import de.tuebingen.rparse.treebank.lex.LexiconWriter;
import de.tuebingen.rparse.treebank.lex.ParserInput;
import de.tuebingen.rparse.treebank.lex.ParserInputProcessingTaskFactory;
import de.tuebingen.rparse.treebank.lex.ParserInputReaderFactory;
import de.tuebingen.rparse.treebank.lex.RparseLexiconWriter;

/**
 * Entry point for rparse
 * 
 * @author wmaier, ke
 */
public class Rparse {

	public final static String VERSION = "- 2.0 -";

	public final static String APP = "rparse - a parser for statistical simple range concatenation grammar.\n"
			+ "More information and additional documentation at http://www.sfs.uni-tuebingen.de/~wmaier/rparse.\n";

	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * Command line options declarations
	 * 
	 * @param cmdline
	 *            the command line
	 * @return a {@link CommandLineParameters} instance representing the
	 *         processed command line
	 * @throws ParameterException
	 */
	public static CommandLineParameters processCommandLine(String[] cmdline)
			throws ParameterException {
		CommandLineParameters op = new CommandLineParameters();
		op.add(CommandLineOption.Prefix.DASH, "help",
				CommandLineOption.Separator.BLANK, false,
				"Show usage information");
		op.add(CommandLineOption.Prefix.DASH,
				"verbose",
				CommandLineOption.Separator.BLANK,
				true,
				"Set global log level [off|severe|warn|info|config*|fine|finer|finest|all], or see below");

		// Parser mode
		// ***********************************************************
		op.add(CommandLineOption.Prefix.DASH, "dep",
				CommandLineOption.Separator.BLANK, false,
				"Do dependency parsing [true|false*]");

		op.add(CommandLineOption.Prefix.DASH, "parserType",
				CommandLineOption.Separator.BLANK, true,
				"Parser type [cyk*|cyknaive|cyktwo]");

		// Training mode
		// *********************************************************
		op.add(CommandLineOption.Prefix.DASH, "doTrain",
				CommandLineOption.Separator.BLANK, false,
				"Training mode [true|false*]");
		op.add(CommandLineOption.Prefix.DASH, "train",
				CommandLineOption.Separator.BLANK, true, "Training treebank []");
		op.add(CommandLineOption.Prefix.DASH,
				"trainGrammar",
				CommandLineOption.Separator.BLANK,
				true,
				"Extracted grammar/lexicon for retraining (prefix, expecting .gram and .lex extensions) []");
		op.add(CommandLineOption.Prefix.DASH, "trainFromDir",
				CommandLineOption.Separator.BLANK, false,
				"Training data is contained in all files in directory [train] [true|false*]");
		op.add(CommandLineOption.Prefix.DASH, "trainFromDirFilter",
				CommandLineOption.Separator.BLANK, true,
				"Glob filter on filenames given -trainFromDir []");
		op.add(CommandLineOption.Prefix.DASH, "trainFormat",
				CommandLineOption.Separator.BLANK, true,
				"Training treebank format [export*], try -availableTrainFormats");
		op.add(CommandLineOption.Prefix.DASH, "availableTrainFormats",
				CommandLineOption.Separator.BLANK, false,
				"Show available formats and exit");
		op.add(CommandLineOption.Prefix.DASH, "trainEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Training treebank encoding [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH, "trainIntervals",
				CommandLineOption.Separator.BLANK, true,
				"Sentences for training, e.g. -10,20-30,35,40- [1-*]");
		op.add(CommandLineOption.Prefix.DASH, "trainMaxlen",
				CommandLineOption.Separator.BLANK, true,
				"Only train on sentences with a length <= trainMaxlen [30*]");
		op.add(CommandLineOption.Prefix.DASH,
				"trainPreprocessors",
				CommandLineOption.Separator.BLANK,
				true,
				"Processing tasks to run the treebank through before training (see text below). []");
		op.add(CommandLineOption.Prefix.DASH, "trainCutoff",
				CommandLineOption.Separator.BLANK, true,
				"Discard productions which occur <= n times [0*]");
		op.add(CommandLineOption.Prefix.DASH, "trainCutoffSave",
				CommandLineOption.Separator.BLANK, true,
				"Save grammar in directory before applying cutoff []");
		op.add(CommandLineOption.Prefix.DASH, "trainType",
				CommandLineOption.Separator.BLANK, true,
				"Training algorithm [mle*|dtr]");
		op.add(CommandLineOption.Prefix.DASH, "trainParams",
				CommandLineOption.Separator.BLANK, true,
				"Parameter string to training algorithm []");
		op.add(CommandLineOption.Prefix.DASH, "trainSave",
				CommandLineOption.Separator.BLANK, true,
				"Save trained grammar in directory []");
		op.add(CommandLineOption.Prefix.DASH, "trainSaveFormat",
				CommandLineOption.Separator.BLANK, true,
				"Format for saved grammar [gf*|rcg]");
		op.add(CommandLineOption.Prefix.DASH, "trainSaveEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Encoding for saving grammar [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH, "trainExtractOnly",
				CommandLineOption.Separator.BLANK, false,
				"Stop after extraction [false]");
		op.add(CommandLineOption.Prefix.DASH, "binType",
				CommandLineOption.Separator.BLANK, true,
				"Binarization algorithm [headdriven*|km|minarvar|detlr|optimal]");
		op.add(CommandLineOption.Prefix.DASH, "binParams",
				CommandLineOption.Separator.BLANK, true,
				"Parameter string to binarization algorithm");
		op.add(CommandLineOption.Prefix.DASH, "binSave",
				CommandLineOption.Separator.BLANK, true,
				"Save binarized grammar []");
		op.add(CommandLineOption.Prefix.DASH, "binSaveFormat",
				CommandLineOption.Separator.BLANK, true,
				"Format for saved binarized grammar [gf*|rcg]");
		op.add(CommandLineOption.Prefix.DASH, "binSaveEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Encoding for saving binarized grammar [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH, "headFinder",
				CommandLineOption.Separator.BLANK, true, "Head finder []");
		op.add(CommandLineOption.Prefix.DASH, "vMarkov",
				CommandLineOption.Separator.BLANK, true,
				"vertical markovization [1*]");
		op.add(CommandLineOption.Prefix.DASH, "hMarkov",
				CommandLineOption.Separator.BLANK, true,
				"horizontal markovization [2*]");
		op.add(CommandLineOption.Prefix.DASH, "markovNoArities",
				CommandLineOption.Separator.BLANK, false,
				"Don't use arities in markovization labels");
		op.add(CommandLineOption.Prefix.DASH, "binCutoff",
				CommandLineOption.Separator.BLANK, true,
				"Discard productions that occur only n times. [0*]");
		op.add(CommandLineOption.Prefix.DASH, "estType",
				CommandLineOption.Separator.BLANK, true,
				"Outside estimate type [off*]. See de.tuebingen.rparse.grammar.estimates.");
		op.add(CommandLineOption.Prefix.DASH, "estMaxlen",
				CommandLineOption.Separator.BLANK, true,
				"Outside estimate max sentence length [trainMaxlen*]");
		op.add(CommandLineOption.Prefix.DASH, "readEstimate",
				CommandLineOption.Separator.BLANK, true,
				"Read serialized estimate from file []");
		op.add(CommandLineOption.Prefix.DASH, "saveEstimate",
				CommandLineOption.Separator.BLANK, true,
				"Save estimate (serialize) []");
		op.add(CommandLineOption.Prefix.DASH, "saveModel",
				CommandLineOption.Separator.BLANK, true,
				"Save trained model (serialize) []");

		// Parsing mode
		// **********************************************************
		op.add(CommandLineOption.Prefix.DASH, "doParse",
				CommandLineOption.Separator.BLANK, false,
				"Parsing mode [true|false*]");
		op.add(CommandLineOption.Prefix.DASH, "timeout",
				CommandLineOption.Separator.BLANK, true,
				"Timeout in seconds until the parsing thread is killed [0*, 1-...]");
		op.add(CommandLineOption.Prefix.DASH, "yfComp",
				CommandLineOption.Separator.BLANK, true,
				"Yield function composer [classic|fast*|gaps]");
		op.add(CommandLineOption.Prefix.DASH, "yfCompParams",
				CommandLineOption.Separator.BLANK, true,
				"Parameters passed to yield function composer []");
		op.add(CommandLineOption.Prefix.DASH, "readModel",
				CommandLineOption.Separator.BLANK, true,
				"Read trained model (serialized) from file []");
		op.add(CommandLineOption.Prefix.DASH, "readBinary",
				CommandLineOption.Separator.BLANK, true,
				"Read binary grammar from file []");
		op.add(CommandLineOption.Prefix.DASH, "readBinaryFormat",
				CommandLineOption.Separator.BLANK, true,
				"Read binary grammar from file (format) [pmcfg*|rcg]");
		op.add(CommandLineOption.Prefix.DASH, "test",
				CommandLineOption.Separator.BLANK, true, "Testing treebank []");
		op.add(CommandLineOption.Prefix.DASH, "testFormat",
				CommandLineOption.Separator.BLANK, true,
				"Test data format [rparse-tagged*], try -availableTestFormats");
		op.add(CommandLineOption.Prefix.DASH, "availableTestFormats",
				CommandLineOption.Separator.BLANK, false,
				"Show available formats and exit");
		op.add(CommandLineOption.Prefix.DASH, "testEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Test data encoding [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH, "testIntervals",
				CommandLineOption.Separator.BLANK, true,
				"Sentence intervals from testing treebank [1-*]");
		op.add(CommandLineOption.Prefix.DASH, "testMaxlen",
				CommandLineOption.Separator.BLANK, true,
				"Only test on sentences with a length <= testMaxlen [trainMaxlen*]");
		op.add(CommandLineOption.Prefix.DASH, "testMinlen",
				CommandLineOption.Separator.BLANK, true,
				"Only test on sentences with a length >= testMinlen [0*]");
		op.add(CommandLineOption.Prefix.DASH, "testPreprocessors",
				CommandLineOption.Separator.BLANK, true,
				"Processing tasks to run the input through before parsing (see text below). []");
		op.add(CommandLineOption.Prefix.DASH, "goalLabel",
				CommandLineOption.Separator.BLANK, true, "Goal label [VROOT*]");
		op.add(CommandLineOption.Prefix.DASH, "parsePostprocessors",
				CommandLineOption.Separator.BLANK, true,
				"Processing tasks to run the parser output through (see text below). []");
		op.add(CommandLineOption.Prefix.DASH, "saveParses",
				CommandLineOption.Separator.BLANK, true,
				"Save parser output [stdout*]");
		op.add(CommandLineOption.Prefix.DASH, "saveParsesEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Parser output encoding [UTF-8*]");

		// Evaluation mode
		// ****************************************************************
		op.add(CommandLineOption.Prefix.DASH, "doEval",
				CommandLineOption.Separator.BLANK, false,
				"Evaluation mode [true|false*]");
		op.add(CommandLineOption.Prefix.DASH, "availableEvalFormats",
				CommandLineOption.Separator.BLANK, false,
				"Show available formats and exit");
		op.add(CommandLineOption.Prefix.DASH, "evalKey",
				CommandLineOption.Separator.BLANK, true, "Gold data []");
		op.add(CommandLineOption.Prefix.DASH, "evalKeyFormat",
				CommandLineOption.Separator.BLANK, true,
				"Gold data format [export*], try -availableEvalFormats");
		op.add(CommandLineOption.Prefix.DASH, "evalKeyEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Gold data encoding [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH,
				"evalKeyPreprocessors",
				CommandLineOption.Separator.BLANK,
				true,
				"Processing tasks to run the gold data through before comparing (see text below). []");
		op.add(CommandLineOption.Prefix.DASH, "evalIntervals",
				CommandLineOption.Separator.BLANK, true,
				"Intervals to evaluate [1-*]");
		op.add(CommandLineOption.Prefix.DASH, "evalAnswer",
				CommandLineOption.Separator.BLANK, true,
				"Parser output [saveParses if not stdout]");
		op.add(CommandLineOption.Prefix.DASH, "evalAnswerFormat",
				CommandLineOption.Separator.BLANK, true,
				"Parser output format [export*], try -availableEvalFormats");
		op.add(CommandLineOption.Prefix.DASH, "evalAnswerEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Paraser output encoding [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH,
				"evalAnswerPreprocessors",
				CommandLineOption.Separator.BLANK,
				true,
				"Processing tasks to run the parser output through before comparing (see text below). []");
		op.add(CommandLineOption.Prefix.DASH, "evalMaxlen",
				CommandLineOption.Separator.BLANK, true,
				"Maximal sentence length for evaluation [" + Integer.MAX_VALUE
						+ "*]");
		op.add(CommandLineOption.Prefix.DASH,
				"evalIgnoreMissing",
				CommandLineOption.Separator.BLANK,
				false,
				"Ignore missing sentences in the answer [on|*off]\n"
						+ "                                          off by default for evalb, on for other metrics");
		op.add(CommandLineOption.Prefix.DASH, "evalMetric",
				CommandLineOption.Separator.BLANK, true,
				"Evaluation metric [evalb*,treedistwhole,treedistroof,dep]");
		op.add(CommandLineOption.Prefix.DASH, "evalParams",
				CommandLineOption.Separator.BLANK, true,
				"Parameters for evaluation metric []");

		// Treebank processing mode
		// ****************************************************************

		op.add(CommandLineOption.Prefix.DASH, "doProcess",
				CommandLineOption.Separator.BLANK, false,
				"Treebank processing mode [true|false*]");
		op.add(CommandLineOption.Prefix.DASH, "inputTreebank",
				CommandLineOption.Separator.BLANK, true,
				"Input treebank file []");
		op.add(CommandLineOption.Prefix.DASH, "inputTreebankDir",
				CommandLineOption.Separator.BLANK, false,
				"Input treebank is directory, process all files from there");
		op.add(CommandLineOption.Prefix.DASH, "inputTreebankDirFilter",
				CommandLineOption.Separator.BLANK, true,
				"If inputTreebankDir, then this is a glob filter on all files in this dir");
		op.add(CommandLineOption.Prefix.DASH, "inputFormat",
				CommandLineOption.Separator.BLANK, true,
				"Input treebank format [export*]");
		op.add(CommandLineOption.Prefix.DASH, "inputEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Input treebank encoding [UTF-8*]");
		op.add(CommandLineOption.Prefix.DASH, "inputIntervals",
				CommandLineOption.Separator.BLANK, true,
				"Intervals to process [1-*]");
		op.add(CommandLineOption.Prefix.DASH, "inputMaxlen",
				CommandLineOption.Separator.BLANK, true,
				"Only process sentences with a length <= inputMaxlen ["
						+ Integer.MAX_VALUE + "*]");
		op.add(CommandLineOption.Prefix.DASH, "tasks",
				CommandLineOption.Separator.BLANK, true,
				"Processing tasks to perform on input treebank (see text below). []");

		// treebank output
		op.add(CommandLineOption.Prefix.DASH,
				"outputFilters",
				CommandLineOption.Separator.BLANK,
				true,
				"Comma-separated list of filters which all sentences must pass to be output (doesn't work for grammars) []");
		op.add(CommandLineOption.Prefix.DASH, "outputTreebank",
				CommandLineOption.Separator.BLANK, true,
				"Output treebank/grammar file (resp. directory) []");
		op.add(CommandLineOption.Prefix.DASH, "outputTreebankDir",
				CommandLineOption.Separator.BLANK, true,
				"Split output treebank/grammar directory []");
		op.add(CommandLineOption.Prefix.DASH, "outputTreebankDirPrefix",
				CommandLineOption.Separator.BLANK, true,
				"Split output treebank/grammar directory, file name prefix of output files []");
		op.add(CommandLineOption.Prefix.DASH, "outputFormat",
				CommandLineOption.Separator.BLANK, true,
				"Output treebank/grammar format");
		op.add(CommandLineOption.Prefix.DASH, "outputGrammar",
				CommandLineOption.Separator.BLANK, false, "Output a grammar");
		op.add(CommandLineOption.Prefix.DASH, "outputEncoding",
				CommandLineOption.Separator.BLANK, true,
				"Output treebank/grammar encoding [UTF-8*]");

		// Treebank processing mode
		// ****************************************************************

		op.add(CommandLineOption.Prefix.DASH, "inputGrammar",
				CommandLineOption.Separator.BLANK, true,
				"Input grammar file in rparse format []");
		op.add(CommandLineOption.Prefix.DASH, "grammarTasks",
				CommandLineOption.Separator.BLANK, true,
				"Grammar processing tasks to perform on input grammar. []");

		op.prepare();
		String line = "";
		for (int i = 0; i < cmdline.length; i++) {
			String tmp = cmdline[i];
			tmp = tmp.replace(" ", "---");
			// escape quotes
			line += "\"" + tmp + "\" ";
		}
		op.parse(line);
		return op;
	}

	/**
	 * Print usage and exit program.
	 * 
	 * @param op
	 *            A {@link CommandLineParameters} instance. The descriptions
	 *            printed by this method are taken directly from its contents.
	 */
	public static void usage(CommandLineParameters op) {
		String ret = APP + "Version: " + VERSION + "\n\nUsage: \n";
		ret += "java de.tuebingen.rparse.ui.Rparse OPTIONS, where OPTIONS is one or more of the following (defaults marked):\n\n";
		for (CommandLineOption o : op.getOptions()) {
			ret += o.toString() + "\n";
		}
		System.out.println(ret);
		System.out
				.println("Processing tasks are given as comma-separated lists of task specifications. A\n"
						+ "task specification is the identifier string or the fully qualified class name\n"
						+ "of a processing task, optionally followed by a dash and a task-specific \n"
						+ "parameter string.");
		System.out.println();
		System.out
				.println("The -verbose option can also take a comma-separated list of log-level\n"
						+ "specifications. A log-level specification is one of "
						+ "[off|severe|warn|info|config|fine|finer|finest|all], optionally preceded by a\n"
						+ "package name and a colon. Without the latter, the global log level is set.");
		System.exit(0);
	}

	/**
	 * Entry point. See {@link usage()} for usage information
	 * 
	 * @param args
	 *            The command line options
	 * @throws ParameterException
	 */
	public static void main(String[] args) throws ParameterException {
		// process command line, load parameters and default values
		CommandLineParameters op = null;
		try {
			op = processCommandLine(args);
		} catch (ParameterException e1) {
			System.err
					.println("Command line options error: " + e1.getMessage());
			usage(op);
		}
		// options which will make us exit
		if (op.check("help"))
			usage(op);
		if (op.check("availableTrainFormats")) {
			System.out.println("Formats available for training: ");
			System.out.println();
			System.out.println("Constituents: ");
			try {
				Field[] publicFields = ConstituentInputFormats.class
						.getFields();
				for (int i = 0; i < publicFields.length; i++) {
					System.out.println(publicFields[i].get(null));
				}
				System.out.println();
				System.out.println("Dependencies: ");
				publicFields = DependencyInputFormats.class.getFields();
				for (int i = 0; i < publicFields.length; i++) {
					System.out.println(publicFields[i].get(null));
				}
			} catch (IllegalArgumentException e) {
				System.err.println("Cannot read available formats");
				e.printStackTrace();
				System.exit(1);
			} catch (IllegalAccessException e) {
				System.err.println("Cannot read available formats");
				e.printStackTrace();
				System.exit(1);
			}
			System.exit(0);
		}
		if (op.check("availableTestFormats")) {
			System.out
					.println("[rparse-tagged] Accepted unparsed input is one terminal/POS tag combination per line,\n"
							+ "    separated by a slash. The last slash counts.\n"
							+ "[export] NeGra export format (see Skut et al. (1997)).\n"
							+ "[mrg] Any bracketed format like Penn Treebank MRG, >= 1 lines per sentences.\n"
							+ "[treetagger] TreeTagger output format.");
			System.exit(0);
		}
		if (op.check("availableEvalFormats")) {
			System.err
					.println("Tree distance measures only export format, all others see -availableTrainFormats");
			System.exit(0);
		}

		Level verbose = Level.CONFIG;
		if (op.check("verbose")) {
			verbose = parseVerboseOption(op.getVal("verbose"), verbose);
		}

		// set up logging for clean information
		// make sure log output gets written to STDERR by configuring root
		// logger
		Logger globalLogger = Logger.getLogger("");
		for (Handler handler : globalLogger.getHandlers())
			globalLogger.removeHandler(handler);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new VerySimpleFormatter());
		handler.setLevel(verbose);
		globalLogger.addHandler(handler);
		globalLogger.setLevel(verbose);
		// get our package logger
		Logger logger = Logger.getLogger(Rparse.class.getPackage().getName());

		// process the rest of the command line
		String mode = op.check("dep") ? Constants.DEPENDENCIES
				: Constants.CONSTITUENTS;

		boolean doTrain = op.check("doTrain");
		String trainingTreebank = op.getVal("train");
		String trainGrammar = op.getVal("trainGrammar");
		boolean trainFromDir = op.check("trainFromDir");
		String trainFromDirFilter = "";
		if (op.check("trainFromDirFilter"))
			trainFromDirFilter = op.getVal("trainFromDirFilter");
		String trainFormat = ConstituentInputFormats.EXPORT;
		if (op.check("trainFormat"))
			trainFormat = op.getVal("trainFormat");
		String trainEncoding = DEFAULT_ENCODING;
		if (op.check("trainEncoding"))
			trainEncoding = op.getVal("trainEncoding");
		String trainIntervals = "1-";
		if (op.check("trainIntervals"))
			trainIntervals = op.getVal("trainIntervals");
		int trainMaxlen = 30;
		if (op.check("trainMaxlen"))
			trainMaxlen = Integer.parseInt(op.getVal("trainMaxlen"));
		String trainPreprocessors = "";
		if (op.check("trainPreprocessors"))
			trainPreprocessors = op.getVal("trainPreprocessors");
		String trainType = TrainingMethods.MLE;
		int trainCutoff = 0;
		if (op.check("trainCutoff"))
			trainCutoff = Integer.parseInt(op.getVal("trainCutoff"));
		String trainCutoffSave = op.getVal("trainCutoffSave");
		if (op.check("trainType"))
			trainType = op.getVal("trainType");
		String trainSave = op.getVal("trainSave");
		String trainSaveFormat = GrammarFormats.RCG_GF;
		if (op.check("trainSaveFormat"))
			trainSaveFormat = op.getVal("trainSaveFormat");
		if (op.check("trainSave"))
			trainSave = op.getVal("trainSave");
		String trainParams = "";
		if (op.check("trainParams"))
			trainParams = op.getVal("trainParams");
		String trainSaveEncoding = DEFAULT_ENCODING;
		if (op.check("trainSaveEncoding"))
			trainSaveEncoding = op.getVal("trainSaveEncoding");
		boolean trainExtractOnly = op.check("trainExtractOnly");
		String binType = BinarizerTypes.HEADDRIVEN;
		if (op.check("binType"))
			binType = op.getVal("binType");
		String binParams = "";
		if (op.check("binParams"))
			binParams = op.getVal("binParams");
		boolean doBinarization = binType != null && binType.length() > 0;
		String binSave = op.getVal("binSave");
		String binSaveFormat = GrammarFormats.RCG_GF;
		if (op.check("binSaveFormat"))
			binSaveFormat = op.getVal("binSaveFormat");
		String binSaveEncoding = DEFAULT_ENCODING;
		if (op.check("binSaveEncoding"))
			binSaveEncoding = op.getVal("binSaveEncoding");
		String headFinder = op.getVal("headFinder");
		int vMarkov = 2;
		if (op.check("vMarkov"))
			vMarkov = Constants.INFINITY.equals(op.getVal("vMarkov")) ? Integer.MAX_VALUE
					: Integer.valueOf(op.getVal("vMarkov"));
		int hMarkov = 1;
		if (op.check("hMarkov"))
			hMarkov = Constants.INFINITY.equals(op.getVal("hMarkov")) ? Integer.MAX_VALUE
					: Integer.valueOf(op.getVal("hMarkov"));
		if (vMarkov < 1)
			vMarkov = 1;
		if (hMarkov < 0)
			hMarkov = 0;
		boolean markovNoArities = op.check("markovNoArities");
		int binCutoff = 0;
		if (op.check("binCutoff"))
			binCutoff = Integer.parseInt(op.getVal("binCutoff"));
		String estType = EstimateTypes.OFF;
		if (op.check("estType"))
			estType = op.getVal("estType");
		int estMaxlen = trainMaxlen;
		if (op.check("estMaxlen"))
			estMaxlen = Integer.parseInt(op.getVal("estMaxlen"));
		String readEstimate = op.getVal("readEstimate");
		String saveEstimate = op.getVal("saveEstimate");
		String saveModel = op.getVal("saveModel");
		boolean doParse = op.check("doParse");
		int timeout = 0;
		if (op.check("timeout"))
			timeout = Integer.parseInt(op.getVal("timeout"));
		String parserType = ParsingTypes.RCG_CYK_FIBO;
		if (op.check("parserType"))
			parserType = op.getVal("parserType");
		String yfcomp = YieldFunctionComposerTypes.FAST;
		if (op.check("yfComp"))
			yfcomp = op.getVal("yfComp");
		String yfcompparams = "";
		if (op.check("yfCompParams"))
			yfcompparams = op.getVal("yfCompParams");
		String test = op.getVal("test");
		String readModel = op.getVal("readModel");
		String readBinary = op.getVal("readBinary");
		String readBinaryFormat = de.tuebingen.rparse.grammar.read.GrammarFormats.RCG_PMCFG;
		if (op.check("readBinaryFormat")) {
			readBinaryFormat = op.getVal("readBinaryFormat");
		}
		String testFormat = "rparse-tagged";
		if (op.check("testFormat"))
			testFormat = op.getVal("testFormat");
		String testEncoding = DEFAULT_ENCODING;
		if (op.check("testEncoding"))
			testEncoding = op.getVal("testEncoding");
		String testIntervals = "1-";
		if (op.check("testIntervals"))
			testIntervals = op.getVal("testIntervals");
		int testMaxlen = trainMaxlen;
		if (op.check("testMaxlen"))
			testMaxlen = Integer.parseInt(op.getVal("testMaxlen"));
		int testMinlen = 0;
		if (op.check("testMinlen"))
			testMinlen = Integer.parseInt(op.getVal("testMinlen"));
		String testPreprocessors = "";
		if (op.check("testPreprocessors"))
			testPreprocessors = op.getVal("testPreprocessors");
		String goalLabel = GrammarConstants.DEFAULTSTART;
		if (op.check("goalLabel"))
			goalLabel = op.getVal("goalLabel");
		String testPostprocessors = "";
		if (op.check("testPostprocessors"))
			testPostprocessors = op.getVal("testPostprocessors");
		String saveParses = op.getVal("saveParses");
		String saveParsesEncoding = DEFAULT_ENCODING;
		if (op.check("saveParsesEncoding"))
			saveParsesEncoding = op.getVal("saveParsesEncoding");
		boolean doEval = op.check("doEval");
		String evalKey = op.getVal("evalKey");
		String evalKeyFormat = ConstituentInputFormats.EXPORT;
		if (op.check("evalKeyFormat"))
			evalKeyFormat = op.getVal("evalKeyFormat");
		String evalKeyEncoding = DEFAULT_ENCODING;
		if (op.check("evalKeyEncoding"))
			evalKeyEncoding = op.getVal("evalKeyEncoding");
		String evalKeyPreprocessors = "";
		if (op.check("evalKeyPreprocessors"))
			evalKeyPreprocessors = op.getVal("evalKeyPreprocessors");
		String evalIntervals = "1-";
		if (op.check("evalIntervals"))
			evalIntervals = op.getVal("evalIntervals");
		String evalAnswer = saveParses;
		if (op.check("evalAnswer"))
			evalAnswer = op.getVal("evalAnswer");
		String evalAnswerFormat = ConstituentInputFormats.EXPORT;
		if (op.check("evalAnswerFormat"))
			evalAnswerFormat = op.getVal("evalAnswerFormat");
		String evalAnswerEncoding = DEFAULT_ENCODING;
		if (op.check("evalAnswerEncoding"))
			evalAnswerEncoding = op.getVal("evalAnswerEncoding");
		String evalAnswerPreprocessors = "";
		if (op.check("evalAnswerPreprocessors"))
			evalAnswerPreprocessors = op.getVal("evalAnswerPreprocessors");
		int evalMaxlen = Integer.MAX_VALUE;
		if (op.check("evalMaxlen"))
			evalMaxlen = Integer.parseInt(op.getVal("evalMaxlen"));
		String evalMetric = EvalTypes.EVALB;
		if (op.check("evalMetric"))
			evalMetric = op.getVal("evalMetric");
		String evalParams = "";
		if (op.check("evalParams"))
			evalParams = op.getVal("evalParams");
		boolean evalIgnoreMissing = op.check("evalIgnoreMissing");
		boolean doProcess = op.check("doProcess");
		if (!doTrain && !doParse && !doEval && !doProcess) {
			logger.severe("Select at least training, parsing, eval or process mode, or try -help.");
			usage(op);
			System.exit(8);
		}

		// Rule out mode combinations that would only make sense if the
		// output/input of modes could be chained. Which would be nice, in fact.
		// Maybe one day...
		if (doEval) {
			if (doTrain || doParse || doProcess) {
				logger.severe("Evaluation mode is incompatible with all other modes.");
				System.exit(8);
			}
		}

		if (doProcess) {
			if (doTrain || doParse) {
				logger.severe("Processing mode is incompatible with all other modes.");
				System.exit(8);
			}

			/*
			 * if (mode != RCGParser.CONSTITUENTS) { logger
			 * .severe("Processing dependency treebanks is not yet supported.");
			 * System.exit(1); }
			 */

		}

		logger.config("***** M o d e ******************");
		logger.config("  mode            : " + mode);
		if (doTrain) {
			logger.config("***** T r a i n i n g **********");
			logger.config("  train              : " + trainingTreebank);
			logger.config("  trainGrammar       : " + trainGrammar);
			logger.config("  trainFromDir       : " + trainFromDir);
			logger.config("  trainFromDirFilter : " + trainFromDirFilter);
			logger.config("  trainFormat        : " + trainFormat);
			logger.config("  trainEncoding      : " + trainEncoding);
			logger.config("  trainIntervals     : " + trainIntervals);
			logger.config("  trainMaxlen        : " + trainMaxlen);
			logger.config("  trainPreprocessors : " + trainPreprocessors);
			logger.config("  trainCutoff        : " + trainCutoff);
			logger.config("  trainCutoffSave    : " + trainCutoffSave);
			logger.config("  trainType          : " + trainType);
			logger.config("  trainParams        : " + trainParams);
			logger.config("  trainSave          : " + trainSave);
			logger.config("  trainSaveEncoding  : " + trainSaveEncoding);
			logger.config("  trainSaveFormat    : " + trainSaveFormat);
			logger.config("  trainExtractOnly   : " + trainExtractOnly);
			logger.config("  goalLabel          : " + goalLabel);
			logger.config("  binType            : " + binType);
			logger.config("  binParams          : " + binParams);
			logger.config("  binSave            : " + binSave);
			logger.config("  binSaveFormat      : " + binSaveFormat);
			logger.config("  binSaveEncoding    : " + binSaveEncoding);
			logger.config("  headFinder         : " + headFinder);
			logger.config("  vMarkov            : " + vMarkov);
			logger.config("  hMarkov            : " + hMarkov);
			logger.config("  markovNoArities    : " + markovNoArities);
			logger.config("  binCutoff          : " + binCutoff);
			logger.config("  estType            : " + estType);
			logger.config("  estMaxlen          : " + estMaxlen);
			logger.config("  readEstimate       : " + readEstimate);
			logger.config("  saveEstimate       : " + saveEstimate);
			logger.config("  saveModel          : " + saveModel);
		}
		if (doParse) {
			logger.config("***** T e s t i n g ************");
			logger.config("  test            : " + test);
			logger.config("  timeout         : " + timeout);
			logger.config("  readModel       : " + readModel);
			logger.config("  readBinary      : " + readBinary);
			logger.config("  readBinaryFormat: " + readBinaryFormat);
			logger.config("  parserType      : " + parserType);
			logger.config("  yfComp          : " + yfcomp);
			logger.config("  yfCompParams    : " + yfcompparams);
			logger.config("  testMaxlen      : " + testMaxlen);
			logger.config("  testMinlen      : " + testMinlen);
			logger.config("  testPreprocessors: " + testPreprocessors);
			logger.config("  testFormat      : " + testFormat);
			logger.config("  testEncoding    : " + testEncoding);
			logger.config("  testIntervals   : " + testIntervals);
			logger.config("  goalLabel       : " + goalLabel);
			logger.config("  testPostprocessors: " + testPostprocessors);
			logger.config("  saveParses      : " + saveParses);
		}
		if (doEval) {
			logger.config("***** E v a l u a t i o n ******");
			logger.config("  evalKey         : " + evalKey);
			logger.config("  evalKeyFormat   : " + evalKeyFormat);
			logger.config("  evalKeyEncoding : " + evalKeyEncoding);
			logger.config("  evalKeyPreprocessors: " + evalKeyPreprocessors);
			logger.config("  evalIntervals   : " + evalIntervals);
			logger.config("  evalAnswer      : " + evalAnswer);
			logger.config("  evalAnswerFormat: " + evalAnswerFormat);
			logger.config("  evalAnswerEncoding: " + evalAnswerEncoding);
			logger.config("  evalAnswerPreprocessors: "
					+ evalAnswerPreprocessors);
			logger.config("  evalMaxlen      : " + evalMaxlen);
			logger.config("  evalIgnoreMissing: " + evalIgnoreMissing);
			logger.config("  evalMetric      : " + evalMetric);
			logger.config("  evalParams      : " + evalParams);
		}
		// Getting options and logging for doProcessis now done in the doProcess
		// method.

		Timer timer = new Timer();
		Numberer nb = new Numberer();
		ParserData pd = null;

		if (doTrain) {
			pd = new ParserData(new RCG(nb), new Lexicon(nb), nb);
			if (readModel != null || readBinary != null) {
				logger.severe("Either do training, load a pretrained model or load a binary grammar.");
				System.exit(8);
			}
			if (trainingTreebank == null && trainGrammar == null) {
				logger.severe("In training mode, you must supply a treebank or an extracted grammar");
				System.exit(9);
			}
			if (trainingTreebank != null && trainGrammar != null) {
				logger.severe("Either supply a treebank or an extracted grammar, not both");
				System.exit(91);
			}
			if (trainGrammar != null && binType != null
					&& !binType.equals(BinarizerTypes.DETERMINISTIC)) {
				logger.severe("The current grammar file format does not support saving contexts necessary for markovization. ");
				logger.severe("Therefore only deterministic binarization is available when supplying a grammar instead of a treebank.");
				System.exit(92);
			}

			if (trainGrammar == null) {

				try {
					if (Constants.CONSTITUENTS.equals(mode)) {
						logger.info("Reading constituency treebank and extracting grammar...");
						// stuff to do during extraction
						MultiTask<Tree> mt = null;
						try {
							mt = createConstituentMultiTask(trainPreprocessors,
									nb);
						} catch (TreebankException e) {
							logger.severe("Could not create processing tasks for preprocessing: "
									+ e.getMessage());
							e.printStackTrace();
							System.exit(-1);
						}
						HeadFinder<Tree> hf = null;
						if (headFinder != null) {
							hf = HeadFinderFactory.getHeadFinder(headFinder);
							mt.addTask(hf);
						}
						ConstituentParentAnnotator collinizer = new ConstituentParentAnnotator(
								vMarkov, markovNoArities, pd.nb);
						mt.addTask(collinizer);
						mt.addTask(new RCGExtractor<Tree>(pd));
						TreebankProcessor<Tree> tp = ConstituentProcessorFactory
								.getTreebankProcessor(trainFormat, nb);
						timer.start();
						tp.process(new BufferedReader(new InputStreamReader(
								new FileInputStream(trainingTreebank),
								trainEncoding)), mt,
								new Ranges(trainIntervals), trainMaxlen);
					} else if (Constants.DEPENDENCIES.equals(mode)) {
						logger.info("Reading dependency treebank and extracting grammar...");
						// stuff to do during extraction
						MultiTask<DependencyForest<DependencyForestNodeLabel, String>> mt = new MultiTask<DependencyForest<DependencyForestNodeLabel, String>>();
						DepParentAnnotator parentAnnotator = new DepParentAnnotator(
								vMarkov, markovNoArities, pd.nb);
						mt.addTask(parentAnnotator);
						mt.addTask(new RCGExtractor<DependencyForest<DependencyForestNodeLabel, String>>(
								pd));
						if (trainFromDir) {
							DirectoryDependencyTreebankProcessor tp = DependencyTreebankProcessorFactory
									.getDirectoryTreebankProcessor(trainFormat,
											nb);
							timer.start();
							tp.processDirectory(new File(trainingTreebank),
									trainEncoding, new SimpleFileFilter(
											trainFromDirFilter), mt,
									new Ranges(trainIntervals), trainMaxlen);
						} else {
							TreebankProcessor<DependencyForest<DependencyForestNodeLabel, String>> tp = DependencyTreebankProcessorFactory
									.getTreebankProcessor(trainFormat, nb);
							timer.start();
							tp.process(new BufferedReader(
									new InputStreamReader(new FileInputStream(
											trainingTreebank), trainEncoding)),
									mt, new Ranges(trainIntervals), trainMaxlen);
						}
					} else {
						logger.severe("We can process either dependencies or constituents.");
						System.exit(11);
					}
				} catch (FileNotFoundException e) {
					logger.severe("File not found: " + e.getMessage() + "\n");
					System.exit(12);
				} catch (IOException e) {
					logger.severe("IO Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(13);
				} catch (UnknownFormatException e) {
					logger.severe("Unknown format: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(14);
				} catch (TreebankException e) {
					logger.severe("Treebank Exception: " + e.getMessage()
							+ "\n");
					e.printStackTrace();
					System.exit(15);
				} catch (UnsupportedOperationException e) {
					logger.severe("Unsupported Operation: " + e.getMessage()
							+ "\n");
					e.printStackTrace();
					System.exit(16);
				} catch (UnknownTaskException e) {
					logger.severe("Unknown Task: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(17);
				} catch (NumberFormatException e) {
					logger.severe("Ranges Format: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(18);
				}

			} else {
				try {
					LexiconReader lexReader = new LexiconReader(new File(
							trainGrammar + ".lex"), pd.nb);
					pd.l = lexReader.getLexicon();
					RCGReader rcgReader = new RCGReader(new File(trainGrammar
							+ ".gram"), goalLabel, pd.l, nb);
					pd.g = rcgReader.getRCG();
					lexReader.close();
					rcgReader.close();
				} catch (FileNotFoundException e) {
					logger.severe("File not found: " + e.getMessage() + "\n");
					System.exit(181);
				} catch (IOException e) {
					logger.severe("IO Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(182);
				} catch (LexiconException e) {
					logger.severe("Lexicon Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(183);
				} catch (GrammarException e) {
					logger.severe("Grammar Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(184);
				}

			}

			if (pd.g.getClauses().size() == 0) {
				logger.severe("No clauses found in grammar. Maybe wrong format selected?");
				System.exit(18);
			}
			logger.info("finished in " + timer.time());

			// logger.info(pd.nb.toString());

			logger.info(pd.g.stats());

			// pd.l.printTagCounts();

			// apply cutoff
			if (trainCutoff > 0) {
				// write out grammar before removing productions
				if (trainCutoffSave != null) {
					String prefix = "cutoff" + trainSaveFormat;
					logger.info("Writing trained unbinarized grammar and the lexicon to "
							+ trainCutoffSave
							+ File.separator
							+ prefix
							+ "* ...");
					try {
						File trainCutoffSaveDirectory = new File(
								trainCutoffSave);

						if (!trainCutoffSaveDirectory.exists()) {
							trainCutoffSaveDirectory.mkdir();
						}

						GrammarWriter<RCG> gw = GrammarWriterFactory
								.getRCGWriter(trainSaveFormat, "writehead"
										+ ClassParameters.OPTION_SEPARATOR
										+ "writediag"
										+ ClassParameters.OPTION_SEPARATOR
										+ "writestat");
						String grammarPath = trainCutoffSaveDirectory
								.getAbsolutePath() + File.separator + prefix;
						gw.write(pd.g, pd.l, grammarPath, trainSaveEncoding);

						LexiconWriter lexiconWriter = new RparseLexiconWriter(
								"");
						lexiconWriter.write(pd, trainCutoffSaveDirectory,
								prefix, trainSaveEncoding);

					} catch (IOException e) {
						logger.warning("IOException while writing trained grammar: "
								+ e.getMessage());
					} catch (ParameterException e) {
						logger.warning("Could not parse parameter string of grammar writer");
					} catch (GrammarException e) {
						logger.warning("Could not write trained grammar: "
								+ e.getMessage());
					} catch (UnknownFormatException e) {
						logger.warning("Unknown grammar format requested: "
								+ e.getMessage());
					}
					logger.info("finished.");
				}

				logger.info("Applying trainCutoff " + trainCutoff
						+ " to the grammar...");
				try {
					pd.g.cutoff(trainCutoff);
				} catch (GrammarException e) {
					logger.severe("Grammar Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(181);
				}
				logger.info("finished");
			}

			// set start predicate
			System.err.println("Setting " + goalLabel + " as start predicate");
			pd.g.setStartPredLabel(nb.number(GrammarConstants.PREDLABEL,
					goalLabel));

			// train unbinarized grammar
			logger.info("Training grammar using " + trainType + "...");
			timer.start();
			// tm will be used later too for training the binarized grammar
			TrainingMethod tm = null;
			try {
				tm = TrainingMethodFactory.getTrainingMethod(trainType, pd.g,
						null, pd.l, pd.nb, trainParams);
				tm.setDoBinarized(false);
				tm.process();
				pd.g = tm.getGrammar();
				pd.l = tm.getLexicon();
			} catch (GrammarException ge) {
				logger.severe("Grammar Exception: " + ge.getMessage() + "\n");
				ge.printStackTrace();
				System.exit(19);
			} catch (ParameterException pe) {
				logger.severe("Parameter Exception: " + trainParams + "\n");
				pe.printStackTrace();
				System.exit(191);
			} catch (UnknownTaskException ue) {
				logger.severe("Unknown task Exception: " + ue.getMessage()
						+ "\n");
				ue.printStackTrace();
				System.exit(192);
			}
			logger.info("finished in " + timer.time());

			// save the unbinarized grammar
			if (trainSave != null) {
				String prefix = "grammar" + trainSaveFormat;
				logger.info("Writing trained unbinarized grammar and the lexicon to "
						+ trainSave + File.separator + prefix + "* ...");
				try {
					File trainSaveDirectory = new File(trainSave);

					if (!trainSaveDirectory.exists()) {
						trainSaveDirectory.mkdir();
					}

					GrammarWriter<RCG> gw = GrammarWriterFactory.getRCGWriter(
							trainSaveFormat, "writehead"
									+ ClassParameters.OPTION_SEPARATOR
									+ "writediag"
									+ ClassParameters.OPTION_SEPARATOR
									+ "writestat");
					String grammarPath = trainSaveDirectory.getAbsolutePath()
							+ File.separator + prefix;
					gw.write(pd.g, pd.l, grammarPath, trainSaveEncoding);

					LexiconWriter lexiconWriter = new RparseLexiconWriter("");
					lexiconWriter.write(pd, trainSaveDirectory, prefix,
							trainSaveEncoding);

				} catch (IOException e) {
					logger.warning("IOException while writing trained grammar: "
							+ e.getMessage());
				} catch (ParameterException e) {
					logger.warning("Could not parse parameter string of grammar writer.");
				} catch (GrammarException e) {
					logger.warning("Could not write trained grammar: "
							+ e.getMessage());
				} catch (UnknownFormatException e) {
					logger.warning("Unknown grammar format requested: "
							+ e.getMessage());
				}
				logger.info("finished.");
			}

			if (trainExtractOnly) {
				System.exit(0);
			}

			if (doBinarization) {
				// binarization
				logger.info("Binarizing... ");
				timer.start();
				Binarizer binarizer = null;
				try {
					binarizer = BinarizerFactory.getBinarizer(binType, vMarkov,
							hMarkov, markovNoArities, binParams);
					pd.bg = binarizer.binarize(pd.g);

					if (binCutoff > 0) {
						logger.info("Applying binCutoff " + binCutoff
								+ " to binarized grammar...");
						pd.bg = pd.bg.binCutoff(binCutoff);
					}

					pd.nb = pd.bg.getNumberer();
				} catch (UnknownTaskException e) {
					logger.severe("Unknown task exception: " + e.getMessage()
							+ "\n");
					e.printStackTrace();
					System.exit(20);
				} catch (GrammarException e) {
					logger.severe("Grammar Exception: " + e.getMessage() + "\n");
					e.printStackTrace();
					System.exit(21);
				} catch (ParameterException e) {
					logger.severe("Could not parse parameter string for binarization: "
							+ binParams);
					System.exit(21);
				}
				logger.info("finished in " + timer.time());

				// retrain markovized grammar
				if (binarizer.doRetrain()) {
					logger.info("Training binarized grammar using " + trainType
							+ "...");
					timer.start();
					try {
						tm.setDoBinarized(true);
						tm.setBinarizedGrammar(pd.bg);
						tm.process();
						pd.bg = tm.getBinaryGrammar();
						pd.g = tm.getGrammar();
						pd.l = tm.getLexicon();
					} catch (GrammarException e) {
						logger.severe("Error processing grammar: "
								+ e.getMessage());
						e.printStackTrace();
						System.exit(19);
					}
					logger.info("finished in " + timer.time());
				}

				// write binarized grammar
				if (binSave != null) {
					String prefix = "bingrammar" + binSaveFormat;
					logger.info("Writing binarized grammar to " + binSave
							+ File.separator + prefix + "* ...");
					try {
						File binSaveDirectory = new File(binSave);
						if (!binSaveDirectory.exists()) {
							binSaveDirectory.mkdir();
						}
						GrammarWriter<BinaryRCG> gw = GrammarWriterFactory
								.getBinaryRCGWriter(binSaveFormat);
						String grammarPath = binSaveDirectory.getAbsolutePath()
								+ File.separator + prefix;
						gw.write(pd.bg, pd.l, grammarPath, binSaveEncoding);
					} catch (IOException e) {
						logger.warning("IO Exception while writing grammar: "
								+ e.getMessage() + "\n");
					} catch (GrammarException e) {
						logger.warning("Exception while writing grammar: "
								+ e.getMessage() + "\n");
					} catch (UnknownFormatException e) {
						logger.warning("Unknown grammar format requested: "
								+ e.getMessage());
					}
					logger.info("finished.");
				}
				logger.info(pd.bg.stats());
			}

			// take the log of all productions
			pd.computeLogprobs();

			if (doBinarization) {
				// A* context summary estimation
				if (readEstimate != null) {
					logger.info("Trying to load estimate from " + readEstimate
							+ "...");
					try {
						pd.est = Estimate.read(readEstimate);
					} catch (IOException e) {
						pd.est = null;
						logger.warning("IO Exception while reading estimates: "
								+ e.getMessage() + "\n");
					} catch (ClassNotFoundException e) {
						pd.est = null;
						logger.warning("ClassNotFoundException: "
								+ e.getMessage());
					}
					logger.info("Context-summary estimates loaded. Type: "
							+ pd.est.getClass());
				}
				if (pd.est == null) {
					// compute them
					logger.info("Computing estimate type: " + estType
							+ " (max " + estMaxlen + ")...");
					timer.start();
					try {
						pd.est = EstimatesFactory.getEstimates(estType, pd.bg,
								nb, estMaxlen);
						pd.est.process();
					} catch (UnknownTaskException e) {
						logger.severe("Unknown Task Exception: "
								+ e.getMessage());
						e.printStackTrace();
						System.exit(22);
					} catch (GrammarException e) {
						logger.severe("Grammar Exception: " + e.getMessage());
						e.printStackTrace();
						System.exit(23);
					}
					logger.info("finished in " + timer.time());

					if (saveEstimate != null) {
						logger.info("Saving estimates to " + saveEstimate
								+ "...");
						try {
							pd.est.write(saveEstimate);
						} catch (IOException e) {
							logger.warning("IOException:" + e.getMessage());
						}
						logger.info("finished.");
					}
				}
			}

			// write out trained model
			if (saveModel != null) {
				logger.info("Serializing model to " + saveModel + "...");
				try {
					pd.serializeModel(saveModel);
				} catch (IOException e) {
					logger.warning("IOException:" + e.getMessage());
					e.printStackTrace();
				}
				logger.info("finished.");
			}

		} // end train

		if (doParse) {
			if (readModel != null && readBinary != null) {
				logger.severe("Either load pretrained model or load a binary grammar, not both.");
				System.exit(8);
			}
			if (readModel != null) {
				// load model from file
				logger.info("Reading model from " + readModel + "...");
				try {
					pd = ParserData.unserializeModel(readModel);
				} catch (IOException e) {
					logger.severe("IOException: " + e.getMessage());
					e.printStackTrace();
					System.exit(101);
				} catch (ClassNotFoundException e) {
					logger.severe("ClassNotFoundException: " + e.getMessage());
					e.printStackTrace();
					System.exit(102);
				}
				logger.info("finished.");
			}
			if (readBinary != null) {
				logger.info("Reading binary grammar from " + readBinary + "...");
				try {
					pd = ParserData.buildFromBinaryGrammar(readBinary, readBinaryFormat);
				} catch (IOException e) {
					logger.severe("IOException: " + e.getMessage());
					e.printStackTrace();
					System.exit(101);
				} catch (GrammarException e) {
					logger.severe("GrammarException: " + e.getMessage());
					e.printStackTrace();
					System.exit(101);
				} catch (UnknownTaskException e) {
					logger.severe("UnknownTaskException: " + e.getMessage());
					e.printStackTrace();
					System.exit(101);
				} catch (UnknownFormatException e) {
					logger.severe("UnknownFormatException: " + e.getMessage());
					e.printStackTrace();
					System.exit(101);
				}
				logger.info("finished");
			}

			if (pd == null) {
				logger.severe("No model for parsing loaded. Use either training mode or load some model.");
				System.exit(102);
			}

			// get yield function composer only if it has not been read from
			// model
			if (pd.yfcomp == null) {
				try {
					pd.yfcomp = YieldFunctionComposerFactory
							.getYieldFunctionComposer(yfcomp, yfcompparams);
				} catch (UnknownTaskException e) {
					logger.severe("No such yield function composer: " + yfcomp
							+ "\n" + e.getMessage());
					System.exit(1033);
				} catch (ParameterException e) {
					logger.severe("Could not parse parameter string for yield func. composer: "
							+ yfcompparams + "\n" + e.getMessage());
					System.exit(1033);
				}
			}

			RCGParser theParser = null;
			try {
				theParser = ParserFactory.getParser(parserType, pd, nb);
			} catch (GrammarException e) {
				logger.severe("Could not create parser object: "
						+ e.getMessage());
				e.printStackTrace();
				System.exit(104);
			}

			logger.info("We are using a "
					+ theParser.getClass().getSimpleName() + " with a "
					+ pd.yfcomp.getClass().getSimpleName() + ".");

			IncrementalTreebankProcessor<? extends ParserInput> parserInputReader = null;
			try {
				parserInputReader = ParserInputReaderFactory
						.getParserInputReader(testFormat, pd.nb);
				parserInputReader.initialize(new BufferedReader(
						new InputStreamReader(new FileInputStream(test),
								testEncoding)));
			} catch (FileNotFoundException e) {
				logger.severe("Could not open test corpus: " + e.getMessage());
				e.printStackTrace();
				System.exit(105);
			} catch (UnsupportedEncodingException e) {
				logger.severe("Invalid test encoding: " + testEncoding);
				System.exit(1055);
			}
			Ranges parseRanges = null;

			BufferedWriter parseResultWriter = null;
			try {
				parseRanges = new Ranges(testIntervals);
				parseResultWriter = saveParses != null ? new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(saveParses),
								saveParsesEncoding)) : new BufferedWriter(
						new OutputStreamWriter(System.out));
			} catch (IOException e) {
				logger.severe("Could not get writer for parsing result: "
						+ e.getMessage());
				e.printStackTrace();
				System.exit(1056);
			} catch (NumberFormatException e) {
				logger.severe("Range Format: " + e.getMessage());
				e.printStackTrace();
				System.exit(1057);
			}

			theParser.reset();
			logger.info("Ready to parse!");
			int sentenceNumber = 1;

			// Create multitask for parser input preprocessing:
			ProcessingTask<ParserInput> testPreprocessingTasks = null;

			try {
				testPreprocessingTasks = createParserInputMultiTask(
						testPreprocessors, nb);
			} catch (TreebankException e) {
				logger.severe("Could not create processors for parser input preprocessing: "
						+ e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}

			// Create multitask for postprocessing:
			ProcessingTask<Tree> constituentPostprocessingTasks = null;
			ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> dependencyPostProcessingTasks = null;
			if (Constants.DEPENDENCIES.equals(mode)) {
				try {
					dependencyPostProcessingTasks = createDependencyMultiTask(
							testPostprocessors, nb);
				} catch (TreebankException e) {
					logger.severe("Could not create processors for dependency postprocessing: "
							+ e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			} else {
				try {
					constituentPostprocessingTasks = createConstituentMultiTask(
							testPostprocessors, nb);
				} catch (TreebankException e) {
					logger.severe("Could not create processors for constituency postprocessing: "
							+ e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}

			// http://xkcd.com/292/
			ranges: while (parseRanges.hasNext()) {
				int next = parseRanges.next();

				// Skip excluded sentences:
				while (sentenceNumber < next) {
					try {
						// Checking with hasNext() first would construct the
						// next tree, thus defeat the purpose of skipping. So
						// we have to program by exception here.
						try {
							parserInputReader.skipNext();
						} catch (TreebankException e) {
							logger.severe("Error trying to skip sentence: "
									+ e.getMessage());
							e.printStackTrace();
							System.exit(1);
						}
					} catch (NoSuchElementException e) {
						logger.warning("Input ended before specified ranges were complete (no sentence "
								+ sentenceNumber + ")");
						break ranges;
					}
					sentenceNumber++;
				}

				// Here we can check, we are going to use the tree anyway.
				if (!parserInputReader.hasNext()) {
					logger.warning("Input ended before specified ranges were complete (no sentence "
							+ sentenceNumber + ")");
					break ranges;
				}

				// Get sentence:
				ParserInput input = parserInputReader.next();

				try {
					testPreprocessingTasks.processSentence(input);
				} catch (TreebankException e) {
					logger.severe("Error while preprocessing parser input: "
							+ e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}

				// Parse if not too long:
				int size = input.size();

				if (size <= testMaxlen && size >= testMinlen) {
					logger.info("Parsing " + input.parserInputPrint(pd.nb)
							+ "...");
					timer.start();
					try {
						boolean result = false;
						try {
							result = theParser.parseWithTimeout(input, timeout);
						} catch (TimeoutException e) {
							logger.warning(" **** TIMEOUT **** ");
						}

						if (result) {
							try {
								if (Constants.DEPENDENCIES.equals(mode)) {
									theParser.writeDependencyResult(
											parseResultWriter, sentenceNumber,
											dependencyPostProcessingTasks);
								} else {
									theParser.writeResult(parseResultWriter,
											sentenceNumber,
											constituentPostprocessingTasks);
								}
							} catch (TreebankException e) {
								logger.severe("Could not write result due to error in postprocessing: "
										+ e.getMessage());
								e.printStackTrace();
								parseResultWriter.close();
								System.exit(-1);
							}
							logger.info(theParser.getStats());
						} else {
							logger.info(theParser.getStats());
							logger.info("\n ***************** No parse found");
							parseResultWriter.write("\n\n ***************** "
									+ sentenceNumber + ": No parse found \n\n");
						}
					} catch (IOException e) {
						logger.warning("Could not write parsing result for "
								+ sentenceNumber + ": " + e.getMessage());
					}

					logger.info("finished in " + timer.time());
					logger.info("Reset...");
					timer.start();
					theParser.reset();
					logger.info("finished in " + timer.time());
				}

				sentenceNumber++;
			}
		} // end parse

		if (doEval) {
			try {
				EvalConnector con = new EvalConnector();
				con.doEval(evalKey, evalKeyFormat, evalKeyEncoding,
						evalKeyPreprocessors, evalAnswer, evalAnswerFormat,
						evalAnswerEncoding, evalAnswerPreprocessors,
						evalMaxlen, evalIgnoreMissing, evalMetric, mode,
						evalParams, new Ranges(evalIntervals), nb);
			} catch (EvalException e) {
				logger.severe("Could not complete evaluation: "
						+ e.getMessage());
				e.printStackTrace();
				System.exit(666);
			} catch (NumberFormatException e) {
				logger.severe("Ranges Format: " + e.getMessage());
				System.exit(666);
			} catch (ParameterException e) {
				logger.severe("Could not parse evaluation parameter string");
				System.exit(666);
			} catch (UnsupportedEncodingException e) {
				logger.severe("Encoding not supported" + e.getMessage());
				System.exit(666);
			} catch (FileNotFoundException e) {
				logger.severe("File not found: " + e.getMessage());
				System.exit(666);
			} catch (TreebankException e) {
				logger.severe("Error processing treebanks: " + e.getMessage());
				e.printStackTrace();
				System.exit(666);
			} catch (UnknownFormatException e) {
				logger.severe("Unknown format: " + e.getMessage());
				System.exit(666);
			} catch (UnknownTaskException e) {
				logger.severe("Unknown metric: " + e.getMessage());
				System.exit(666);
			}
		} // end eval

		if (doProcess) {
			try {
				doProcess(op, logger);
			} catch (Exception e) {
				logger.severe("Could not complete treebank processing: "
						+ e.getMessage());
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static void doProcess(CommandLineParameters op, Logger logger)
			throws TreebankException, UnknownFormatException, IOException,
			ParameterException, UnknownTaskException {

		File directory = null;
		String prefix = null;
		String suffix = null;

		String mode = op.check("dep") ? Constants.DEPENDENCIES
				: Constants.CONSTITUENTS;
		boolean outputGrammar = op.check("outputGrammar");
		// grammar output goes into a directory by default
		boolean doOutputTreebankDir = op.check("outputTreebankDir")
				|| outputGrammar;
		String outputTreebankDir = "";
		if (doOutputTreebankDir) {
			outputTreebankDir = op.getVal("outputTreebankDir");
		}
		String outputTreebankDirPrefix = "";
		if (doOutputTreebankDir) {
			outputTreebankDirPrefix = op.getVal("outputTreebankDirPrefix");
		}
		boolean doOutputTreebank = op.check("outputTreebank");
		String outputTreebank = op.getVal("outputTreebank");
		String inputTreebank = op.getVal("inputTreebank");
		boolean inputTreebankDir = op.check("inputTreebankDir");
		String inputTreebankDirFilter = "";
		if (op.check("inputTreebankDirFilter"))
			inputTreebankDirFilter = op.getVal("inputTreebankDirFilter");
		String tasks = "";
		if (op.check("tasks"))
			tasks = op.getVal("tasks");
		Numberer nb = new Numberer();
		String inputFormat = mode == Constants.DEPENDENCIES ? DependencyInputFormats.CONLL
				: ConstituentInputFormats.EXPORT;
		if (op.check("inputFormat"))
			inputFormat = op.getVal("inputFormat");
		String inputEncoding = DEFAULT_ENCODING;
		if (op.check("inputEncoding"))
			inputEncoding = op.getVal("inputEncoding");
		String outputEncoding = DEFAULT_ENCODING;
		if (op.check("outputEncoding"))
			outputEncoding = op.getVal("outputEncoding");
		String outputFilters = "";
		if (op.check("outputFilters"))
			outputFilters = op.getVal("outputFilters");
		String inputIntervals = "1-";
		if (op.check("inputIntervals"))
			inputIntervals = op.getVal("inputIntervals");
		int inputMaxlen = Integer.MAX_VALUE;
		if (op.check("inputMaxlen"))
			inputMaxlen = Integer.parseInt(op.getVal("inputMaxlen"));
		String outputFormat = "";
		if (outputGrammar) {
			outputFormat = ParserDataFormats.RPARSE;
		} else {
			outputFormat = mode == Constants.DEPENDENCIES ? DependencyInputFormats.CONLL
					: ConstituentInputFormats.EXPORT;
		}
		if (op.check("outputFormat"))
			outputFormat = op.getVal("outputFormat");

		if (logger != null) {
			logger.config("***** P r o c e s s i n g ******");
			logger.config("  inputTreebank   : " + inputTreebank);
			logger.config("  inputTreebankDir: " + inputTreebankDir);
			logger.config("  inputTreebankDirFilter: " + inputTreebankDirFilter);
			logger.config("  inputFormat     : " + inputFormat);
			logger.config("  inputEncoding   : " + inputEncoding);
			logger.config("  inputIntervals  : " + inputIntervals);
			logger.config("  inputMaxlen     : " + inputMaxlen);
			logger.config("  tasks           : " + tasks);
			logger.config("  outputEncoding  : " + outputEncoding);
			logger.config("  outputFilters   : " + outputFilters);
			logger.config("  outputTreebank  : " + outputTreebank);
			logger.config("  outputTreebankDir: " + outputTreebankDir);
			logger.config("  outputTreebankDirPrefix: "
					+ outputTreebankDirPrefix);
			logger.config("  outputFormat    : " + outputFormat);
			logger.config("  outputGrammar   : " + outputGrammar);
		}

		if (doOutputTreebankDir) {
			directory = new File(outputTreebankDir);
			String name = new File(inputTreebank).getName();
			int index = name.lastIndexOf(".");
			prefix = index == -1 ? name : name.substring(0, index);
			suffix = index == -1 ? "" : inputTreebank.substring(index);

			if (directory.exists()) {
				if (directory.isDirectory()) {
					for (File file : directory
							.listFiles(new PrefixSuffixFilter(prefix, suffix))) {
						Utilities.deleteRecursively(file);
					}
				} else {
					throw new IOException("Specified output directory "
							+ outputTreebankDir
							+ " exists and is not a directory.");
				}
			} else {
				directory.mkdir();
			}
		}

		if (Constants.DEPENDENCIES.equals(mode)) {
			MultiTask<DependencyForest<DependencyForestNodeLabel, String>> multiTask = createDependencyMultiTask(
					tasks, nb);
			// TODO implement the filtering
			List<Test<DependencyForest<DependencyForestNodeLabel, String>>> filterTests = createDependencyFilters(
					outputFilters, nb);
			Writer writer = null;

			if (outputGrammar) {
				// dependency extracted grammar output
				// In any case (also when not splitting), we need a directory
				ParserDataWriter pdw = ParserDataWriterFactory.getWriter(
						outputFormat, outputFormat);
				multiTask
						.addTask(new RCGExtractor<DependencyForest<DependencyForestNodeLabel, String>>(
								new ParserData(), pdw, outputTreebankDir,
								outputEncoding, outputTreebankDirPrefix,
								doOutputTreebankDir, outputFormat));

			} else {
				// dependency treebank output
				if (doOutputTreebank) {
					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputTreebank),
							outputEncoding));
					multiTask
							.addTask(new WriterExportTask<DependencyForest<DependencyForestNodeLabel, String>>(
									DependencySentenceWriterFactory
											.getSentenceWriter(outputFormat),
									writer, filterTests));
				}

				if (doOutputTreebankDir) {
					multiTask
							.addTask(new SplittingExportTask<DependencyForest<DependencyForestNodeLabel, String>>(
									DependencySentenceWriterFactory
											.getSentenceWriter(outputFormat),
									directory, prefix, suffix, filterTests));
				}
			}

			if (!inputTreebankDir) {
				DependencyTreebankProcessorFactory.getTreebankProcessor(
						inputFormat, nb).process(
						new InputStreamReader(
								new FileInputStream(inputTreebank),
								inputEncoding), multiTask,
						new Ranges(inputIntervals), inputMaxlen);
			} else {
				DependencyTreebankProcessorFactory
						.getDirectoryTreebankProcessor(inputFormat, nb)
						.processDirectory(new File(inputTreebank),
								inputEncoding,
								new SimpleFileFilter(inputTreebankDirFilter),
								multiTask, new Ranges(inputIntervals),
								inputMaxlen);
			}

			if (doOutputTreebank && !outputGrammar) {
				writer.close();
			}
		} else {
			MultiTask<Tree> multiTask = createConstituentMultiTask(tasks, nb);
			List<Test<Tree>> filterTests = createConstituentFilters(
					outputFilters, nb);
			Writer writer = null;

			if (outputGrammar) {
				// constituency extracted grammar output
				// in any case (also when not splitting), we need a directory
				ParserDataWriter pdw = ParserDataWriterFactory.getWriter(
						outputFormat, outputFormat);
				multiTask.addTask(new RCGExtractor<Tree>(new ParserData(), pdw,
						outputTreebankDir, outputEncoding,
						outputTreebankDirPrefix, doOutputTreebankDir,
						outputFormat));
				// multiTask.addTask(new RCGExtractor<Tree>(null,
				// outputFormat, outputDir, outputEncoding,
				// outputFormat, doOutputTreebankDir));

			} else {
				// constituency treebank output
				if (doOutputTreebank) {
					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputTreebank),
							outputEncoding));
					multiTask.addTask(new WriterExportTask<Tree>(
							ConstituentSentenceWriterFactory
									.getSentenceWriter(outputFormat), writer,
							filterTests));
				}

				if (doOutputTreebankDir) {
					multiTask.addTask(new SplittingExportTask<Tree>(
							ConstituentSentenceWriterFactory
									.getSentenceWriter(outputFormat),
							directory, prefix, suffix, filterTests));
				}
			}

			if (!inputTreebankDir) {
				ConstituentProcessorFactory.getTreebankProcessor(inputFormat,
						nb).process(
						new InputStreamReader(
								new FileInputStream(inputTreebank),
								inputEncoding), multiTask,
						new Ranges(inputIntervals), inputMaxlen);
			} else {
				ConstituentProcessorFactory.getDirectoryTreebankProcessor(
						inputFormat, nb).processDirectory(
						new File(inputTreebank), inputEncoding,
						new SimpleFileFilter(inputTreebankDirFilter),
						multiTask, new Ranges(inputIntervals), inputMaxlen);

			}

			if (doOutputTreebank && !outputGrammar) {
				writer.close();
			}
		}
	}

	/**
	 * Parses the value of the -verbose option, setting package log levels and
	 * returning the global log level.
	 * 
	 * @param val
	 * @param globalLevel
	 *            The default global log level to return in case it is not
	 *            explicitly specified.
	 * @return
	 */
	private static Level parseVerboseOption(String val, Level globalLevel) {
		String[] specifications = val.split(",");

		for (String specification : specifications) {
			int index = specification.indexOf(':');

			if (index == -1) {
				globalLevel = Level.parse(specification.toUpperCase());
			} else {
				Logger.getLogger(specification.substring(0, index)).setLevel(
						Level.parse(specification.substring(index + 1)
								.toUpperCase()));
			}
		}

		return globalLevel;
	}

	public static List<Test<Tree>> createConstituentFilters(
			String commaSeparatedListOfTestSpecifiers, Numberer nb)
			throws TreebankException {
		List<Test<Tree>> result = new ArrayList<Test<Tree>>();
		for (String taskSpecifier : commaSeparatedListOfTestSpecifiers
				.split(",")) {
			if (!"".equals(taskSpecifier)) {
				try {
					result.add(createConstituentFilter(taskSpecifier, nb));
				} catch (Exception e) {
					throw new TreebankException(e);
				}
			}
		}
		return result;
	}

	public static Test<Tree> createConstituentFilter(String testSpecifier,
			Numberer nb) throws TreebankException {
		try {
			return ConstituentTestsFactory.getTest(testSpecifier, nb);
		} catch (UnknownTaskException e) {
			throw new TreebankException(e);
		}
	}

	public static MultiTask<Tree> createConstituentMultiTask(
			String commaSeparatedListOfTaskSpecifiers, Numberer nb)
			throws TreebankException {
		MultiTask<Tree> result = new MultiTask<Tree>();
		for (String taskSpecifier : commaSeparatedListOfTaskSpecifiers
				.split(",")) {
			if (!"".equals(taskSpecifier)) {
				try {
					result.addTask(createConstituentTask(taskSpecifier, nb));
				} catch (Exception e) {
					throw new TreebankException(e);
				}
			}
		}
		return result;
	}

	private static ProcessingTask<? super Tree> createConstituentTask(
			String taskSpecifier, Numberer nb) throws IOException,
			TreebankException, ClassNotFoundException, SecurityException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		int index = taskSpecifier.indexOf('-');
		String task;
		String params;

		if (index == -1) {
			task = taskSpecifier;
			params = "";
		} else {
			task = taskSpecifier.substring(0, index);
			params = taskSpecifier.substring(index + 1);
		}

		try {
			try {
				return ConstituentProcessingTaskFactory.getProcessingTask(task,
						params, nb);
			} catch (UnknownTaskException e) {
				return ParserInputProcessingTaskFactory.getProcessingTask(task,
						params, nb);
			}
		} catch (UnknownTaskException e) {
			// Okay, then we'll interpret task as a fully qualified class name.
		}

		Class<? extends ProcessingTask<Tree>> clazz = castToTreeProcessingTaskClass(Class
				.forName(task));

		try {
			if ("".equals(params)) {
				try {
					return clazz.getConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					return clazz.getConstructor(String.class).newInstance(
							params);
				}
			} else {
				return clazz.getConstructor(String.class).newInstance(params);
			}
		} catch (NoSuchMethodException e) {
			throw new TreebankException(
					"No suitable constructor found for processing task "
							+ clazz
							+ ". Accepted parameter type lists are () for no parameters and (String) for any number (including 0) of parameters.");
		}
	}

	public static List<Test<DependencyForest<DependencyForestNodeLabel, String>>> createDependencyFilters(
			String commaSeparatedListOfTestSpecifiers, Numberer nb)
			throws TreebankException {
		List<Test<DependencyForest<DependencyForestNodeLabel, String>>> result = new ArrayList<Test<DependencyForest<DependencyForestNodeLabel, String>>>();
		for (String taskSpecifier : commaSeparatedListOfTestSpecifiers
				.split(",")) {
			if (!"".equals(taskSpecifier)) {
				try {
					result.add(createDependencyFilter(taskSpecifier, nb));
				} catch (Exception e) {
					throw new TreebankException(e);
				}
			}
		}
		return result;
	}

	public static Test<DependencyForest<DependencyForestNodeLabel, String>> createDependencyFilter(
			String testSpecifier, Numberer nb) throws TreebankException {
		try {
			return DependencyTestsFactory.getTest(testSpecifier, nb);
		} catch (UnknownTaskException e) {
			throw new TreebankException(e);
		}
	}

	public static MultiTask<DependencyForest<DependencyForestNodeLabel, String>> createDependencyMultiTask(
			String commaSeparatedListOfTaskSpecifiers, Numberer nb)
			throws TreebankException {
		MultiTask<DependencyForest<DependencyForestNodeLabel, String>> result = new MultiTask<DependencyForest<DependencyForestNodeLabel, String>>();
		for (String taskSpecifier : commaSeparatedListOfTaskSpecifiers
				.split(",")) {
			if (!"".equals(taskSpecifier)) {
				try {
					result.addTask(createDependencyTask(taskSpecifier, nb));
				} catch (Exception e) {
					throw new TreebankException(e);
				}
			}
		}
		return result;
	}

	private static ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> createDependencyTask(
			String taskSpecifier, Numberer nb) throws IOException,
			TreebankException, ClassNotFoundException, SecurityException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		int index = taskSpecifier.indexOf('-');
		String task;
		String params;

		if (index == -1) {
			task = taskSpecifier;
			params = "";
		} else {
			task = taskSpecifier.substring(0, index);
			params = taskSpecifier.substring(index + 1);
		}

		try {
			return DependencyProcessingTaskFactory.getProcessingTask(task,
					params);
		} catch (UnknownTaskException e) {
			// Okay, then we'll interpret task as a fully qualified class name.
		}

		Class<? extends ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>>> clazz = castToDependencyProcessingTaskClass(Class
				.forName(task));

		try {
			if ("".equals(params)) {
				try {
					return clazz.getConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					return clazz.getConstructor(String.class).newInstance(
							params);
				}
			} else {
				return clazz.getConstructor(String.class).newInstance(params);
			}
		} catch (NoSuchMethodException e) {
			throw new TreebankException(
					"No suitable constructor found for processing task "
							+ clazz
							+ ". Accepted parameter type lists are () for no parameters and (String) for any number (including 0) of parameters.");
		}
	}

	public static MultiTask<ParserInput> createParserInputMultiTask(
			String commaSeparatedListOfTaskSpecifiers, Numberer nb)
			throws TreebankException {
		MultiTask<ParserInput> result = new MultiTask<ParserInput>();
		for (String taskSpecifier : commaSeparatedListOfTaskSpecifiers
				.split(",")) {
			if (!"".equals(taskSpecifier)) {
				try {
					result.addTask(createParserInputTask(taskSpecifier, nb));
				} catch (Exception e) {
					throw new TreebankException(e);
				}
			}
		}
		return result;
	}

	private static ProcessingTask<ParserInput> createParserInputTask(
			String taskSpecifier, Numberer nb) throws IOException,
			TreebankException, ClassNotFoundException, SecurityException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		int index = taskSpecifier.indexOf('-');
		String task;
		String params;

		if (index == -1) {
			task = taskSpecifier;
			params = "";
		} else {
			task = taskSpecifier.substring(0, index);
			params = taskSpecifier.substring(index + 1);
		}

		try {
			return ParserInputProcessingTaskFactory.getProcessingTask(task,
					params, nb);
		} catch (UnknownTaskException e) {
			// Okay, then we'll interpret task as a fully qualified class name.
		}

		Class<? extends ProcessingTask<ParserInput>> clazz = castToParserInputProcessingTaskClass(Class
				.forName(task));

		try {
			if ("".equals(params)) {
				try {
					return clazz.getConstructor().newInstance();
				} catch (NoSuchMethodException e) {
					return clazz.getConstructor(String.class).newInstance(
							params);
				}
			} else {
				return clazz.getConstructor(String.class).newInstance(params);
			}
		} catch (NoSuchMethodException e) {
			throw new TreebankException(
					"No suitable constructor found for processing task "
							+ clazz
							+ ". Accepted parameter type lists are () for no parameters and (String) for any number (including 0) of parameters.");
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ProcessingTask<Tree>> castToTreeProcessingTaskClass(
			Class<?> clazz) {
		return (Class<? extends ProcessingTask<Tree>>) clazz;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>>> castToDependencyProcessingTaskClass(
			Class<?> clazz) {
		return (Class<? extends ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>>>) clazz;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends ProcessingTask<ParserInput>> castToParserInputProcessingTaskClass(
			Class<?> clazz) {
		return (Class<? extends ProcessingTask<ParserInput>>) clazz;
	}

}
