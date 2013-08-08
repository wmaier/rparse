/*******************************************************************************
 * File EvalConnector.java
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
package de.tuebingen.rparse.eval;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.Constants;
import de.tuebingen.rparse.misc.HasParameters;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.misc.Ranges;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.MultiTask;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.UnknownFormatException;
import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.constituent.process.IncrementalConstituentProcessorFactory;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.dep.IncrementalDependencyTreebankProcessorFactory;
import de.tuebingen.rparse.ui.Rparse;

/**
 * The interface for treebank comparison. As for the parameters in the parameter string, they are only to be defined
 * here.
 * 
 * @author wmaier, ke
 */
public class EvalConnector implements HasParameters {

    private ClassParameters evalParams;

    /**
     * Constructor, sets up the parameters
     */
    public EvalConnector() {
        evalParams = new ClassParameters();
        evalParams
                .add("highGapBlock",
                        "Block items from evaluation which have more gaps than this threshold");
        evalParams
                .add("lowGapBlock",
                        "Block items from evaluation which have less gaps than this threshold");
        evalParams
                .add("gf",
                        "Evaluate with grammatical functions (not available for all measures)");
    }

    @Override
    public ClassParameters getParameters() throws ParameterException {
        return evalParams;
    }

    /**
     * Evalute constituency parses.
     * 
     * @param key
     *            Gold data file
     * @param keyformat
     *            Gold data file format
     * @param keyEncoding
     *            Gold data file encoding
     * @param keyTask
     *            Gold data file preprocessing task
     * @param answer
     *            Parser output file
     * @param ansformat
     *            Parser output file format
     * @param answerEncoding
     *            Parser output file encoding
     * @param answerTask
     *            Parser output file preprocessing
     * @param maxlen
     *            Maximum sentence length to be considered
     * @param ignoreMissing
     *            Ignore missing sentences or make them influence the result
     * @param model
     *            Evaluation metric
     * @param highGapBlock
     *            Exclude sentences with more than {@code highGapBlock} gaps
     * @param lowGapBlock
     *            Exclude sentences with less than {@code lowGapBlock} gaps
     * @param gf
     *            Include grammatical functions (edge labels)
     * @param ranges
     *            Sentences to be considered in range notation (cf. {@link Ranges.java})
     * @throws EvalException
     *             Thrown if there is an error with the evaluation itself.
     * @throws UnknownFormatException
     *             If the treebank formats of key or answer are not known.
     * @throws FileNotFoundException
     *             If key or answer files are not found.
     * @throws UnsupportedEncodingException
     *             If the encoding specified for key or answer is not supported.
     * @throws TreebankException
     *             If something goes wrong with processing key or answer.
     * @throws UnknownTaskException
     *             If an unknown metric is requested.
     */
    public static void evaluateIncrementallyConstituents(String key,
            String keyformat, String keyEncoding, ProcessingTask<Tree> keyTask,
            String answer, String ansformat, String answerEncoding,
            ProcessingTask<Tree> answerTask, int maxlen, boolean ignoreMissing,
            String model, int highGapBlock, int lowGapBlock, boolean gf,
            Ranges ranges) throws EvalException, UnknownFormatException,
            UnsupportedEncodingException, FileNotFoundException,
            TreebankException, UnknownTaskException {

        IncrementalTreebankProcessor<Tree> answerProcessor = null;
        IncrementalTreebankProcessor<Tree> keyProcessor = null;
        IncrementalComparator<Tree> comparator = null;
        Numberer nb = new Numberer();

        comparator = IncrementalComparatorFactory.getConstituentComparator(
                model, ignoreMissing, highGapBlock, lowGapBlock, gf, nb);

        keyProcessor = IncrementalConstituentProcessorFactory
                .getTreebankProcessor(keyformat, nb);

        answerProcessor = IncrementalConstituentProcessorFactory
                .getTreebankProcessor(ansformat, nb);

        keyProcessor.initialize(new BufferedReader(new InputStreamReader(
                new FileInputStream(key), keyEncoding)));

        answerProcessor.initialize(new BufferedReader(new InputStreamReader(
                new FileInputStream(answer), answerEncoding)));

        comparator.evaluate(keyProcessor, keyTask, answerProcessor, answerTask,
                ranges, maxlen);
    }

    /**
     * Evaluate dependency parses.
     * 
     * @param key
     *            The gold data file
     * @param keyformat
     *            Format of the gold data file
     * @param keyEncoding
     *            Encoding of the gold data file
     * @param keyTask
     *            Preprocessing task for the gold data file
     * @param answer
     *            Parser output file
     * @param ansformat
     *            Parser output file format
     * @param answerEncoding
     *            Parser output file encoding
     * @param answerTask
     *            Parser output file preprocessing task
     * @param maxlen
     *            Maximum sentence length to be considered
     * @param ignoreMissing
     *            Ignore missing sentences in parser output or make them influence the result
     * @param highGapBlock
     *            Block sentences with more than {@code highGapBlock} gaps
     * @param lowGapBlock
     *            Block sentence with less than {@code lowGapBlock} gaps
     * @param ranges
     *            Sentences to be considered in range notation (cf. {@link Range.java})
     * @throws EvalException
     *             Thrown if there is an error with the evaluation itself. Anything else is fatal anyway and caught
     *             directly here.
     * @throws TreebankException
     *             If there is a problem processing key or answer.
     * @throws UnknownFormatException
     *             If the format specified for key or answer is not valid.
     * @throws FileNotFoundException
     *             If key or answer files are not found.
     * @throws UnsupportedEncodingException
     *             If the encoding specified for key/answer is invalid.
     */
    public static void evaluateIncrementallyDependencies(
            String key,
            String keyformat,
            String keyEncoding,
            ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> keyTask,
            String answer,
            String ansformat,
            String answerEncoding,
            ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>> answerTask,
            int maxlen, int highGapBlock, int lowGapBlock, Ranges ranges)
            throws EvalException, TreebankException, UnknownFormatException,
            UnsupportedEncodingException, FileNotFoundException {

        IncrementalTreebankProcessor<DependencyForest<DependencyForestNodeLabel, String>> answerProcessor = null;
        IncrementalTreebankProcessor<DependencyForest<DependencyForestNodeLabel, String>> keyProcessor = null;
        IncrementalComparator<DependencyForest<DependencyForestNodeLabel, String>> comparator = null;

        Numberer nb = new Numberer();

        comparator = IncrementalComparatorFactory.getDependencyComparator(
                highGapBlock, lowGapBlock);

        keyProcessor = IncrementalDependencyTreebankProcessorFactory
                .getTreebankProcessor(keyformat, nb);

        answerProcessor = IncrementalDependencyTreebankProcessorFactory
                .getTreebankProcessor(ansformat, nb);

        keyProcessor.initialize(new BufferedReader(new InputStreamReader(
                new FileInputStream(key), keyEncoding)));

        answerProcessor.initialize(new BufferedReader(new InputStreamReader(
                new FileInputStream(answer), answerEncoding)));

        comparator.evaluate(keyProcessor, keyTask, answerProcessor, answerTask,
                ranges, maxlen);
    }

    /**
     * Interface method for evaluation
     * 
     * @param key
     *            Gold data file
     * @param keyformat
     *            Gold data file format
     * @param keyEncoding
     *            Gold data file encoding
     * @param keyPreprocessors
     *            Preprocessors for gold data
     * @param answer
     *            Parser output file
     * @param answerformat
     *            Parser output file format
     * @param answerEncoding
     *            Parser output file encoding
     * @param answerPreprocessors
     *            Parser output preprocessors
     * @param maxlen
     *            Maximum sentence length to be considered for evaluation
     * @param ignoreMissing
     *            Ignore missing sentences or make them influence the result
     * @param metric
     *            Evaluation metric
     * @param mode
     *            Constituents or dependencies
     * @param params
     *            Additional parameters
     * @param ranges
     *            Sentences to be considered in range notation (cf. {@link Ranges.java})
     * @param nb
     *            The numberer with all information from the parser
     * @throws EvalException
     *             If something goes wrong with the evaluation
     * @throws ParameterException
     *             If something is wrong with the evaluation parameter string
     * @throws TreebankException
     *             If there is a problem processing key or answer.
     * @throws UnknownFormatException
     *             If the format specified for key or answer is not valid.
     * @throws FileNotFoundException
     *             If key or answer files are not found.
     * @throws UnsupportedEncodingException
     *             If the encoding specified for key/answer is invalid.
     * @throws UnknownTaskException
     *             If an unknown metric is requested.
     */
    // http://www-pu.informatik.uni-tuebingen.de/users/klaeren/epigrams.html --> no. 11
    public void doEval(String key, String keyformat, String keyEncoding,
            String keyPreprocessors, String answer, String answerformat,
            String answerEncoding, String answerPreprocessors, int maxlen,
            boolean ignoreMissing, String metric, String mode, String params,
            Ranges ranges, Numberer nb) throws EvalException,
            ParameterException, UnsupportedEncodingException,
            FileNotFoundException, TreebankException, UnknownFormatException,
            UnknownTaskException {

        Logger logger = Logger.getLogger(EvalConnector.class.getPackage()
                .getName());

        evalParams.parse(params);
        int highGapblock = Integer.MAX_VALUE;
        if (evalParams.check("highGapBlock")) {
            try {
                highGapblock = Integer.parseInt(evalParams
                        .getVal("highGapBlock"));
            } catch (NumberFormatException e) {
            }
        }
        int lowGapblock = 0;
        if (evalParams.check("lowGapBlock")) {
            try {
                lowGapblock = Integer
                        .parseInt(evalParams.getVal("lowGapBlock"));
            } catch (NumberFormatException e) {
            }
        }
        boolean gf = false;
        if (evalParams.check("gf")) {
            gf = true;
        }

        if (Constants.DEPENDENCIES.equals(mode)) {
            if (ignoreMissing) {
                throw new EvalException(
                        "Cannot ignore missing answer sentences in dependency mode yet.");
            }

            MultiTask<DependencyForest<DependencyForestNodeLabel, String>> keyTask = null;
            MultiTask<DependencyForest<DependencyForestNodeLabel, String>> answerTask = null;
            try {
                keyTask = Rparse
                        .createDependencyMultiTask(keyPreprocessors, nb);
            } catch (TreebankException e) {
                logger.severe("Error creating key preprocessors. ");
                e.printStackTrace();
                System.exit(-1);
            }
            try {
                answerTask = Rparse.createDependencyMultiTask(
                        answerPreprocessors, nb);
            } catch (TreebankException e) {
                logger.severe("Error creating answer preprocessors.");
                e.printStackTrace();
                System.exit(-1);
            }

            evaluateIncrementallyDependencies(key, keyformat, keyEncoding,
                    keyTask, answer, answerformat, answerEncoding, answerTask,
                    maxlen, highGapblock, lowGapblock, ranges);
        } else {
            ProcessingTask<Tree> keyTask = null;
            ProcessingTask<Tree> answerTask = null;
            try {
                keyTask = Rparse.createConstituentMultiTask(keyPreprocessors,
                        nb);
            } catch (TreebankException e) {
                logger.severe("Error creating key preprocessors.");
                e.printStackTrace();
                System.exit(-1);
            }
            try {
                answerTask = Rparse.createConstituentMultiTask(
                        answerPreprocessors, nb);
            } catch (TreebankException e) {
                logger.severe("Error creating answer preprocessors.");
                e.printStackTrace();
                System.exit(-1);
            }
            evaluateIncrementallyConstituents(key, keyformat, keyEncoding,
                    keyTask, answer, answerformat, answerEncoding, answerTask,
                    maxlen, ignoreMissing, metric, highGapblock, lowGapblock,
                    gf, ranges);
        }
    }
}
