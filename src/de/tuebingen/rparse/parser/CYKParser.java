/*******************************************************************************
 * File CYKParser.java
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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeoutException;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.binarize.Debinarizer;
import de.tuebingen.rparse.grammar.binarize.DeterministicBinarizer;
import de.tuebingen.rparse.misc.IntegerContainer;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;
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
 * The CYK parser from Seki et al. (1991) to be used as Weighted Deductive Parser (Nederhof, 2003).
 * 
 * @author wmaier
 */
public class CYKParser implements RCGParser {

    // parser data which contains all necessary information for parsing
    private ParserData                                                          pd;

    // indicates if the binary grammar has been deterministically binarized
    private boolean                                                             deterministicBinarization;

    // the chart
    private CYKChart                                                            chart;

    // the agenda
    private PriorityAgenda                                                      agenda;

    // our goal item
    private CYKItem                                                             goal;

    // the input words, mapped to integers
    private int[]                                                               words;

    // the input terminals, as nodes
    private Node[]                                                              terminals;

    // write result (constituents)
    private SentenceWriter<Tree>                                                sw;

    // write result (dependencies)
    private SentenceWriter<DependencyForest<DependencyForestNodeLabel, String>> dw;

    // the numberer
    private final Numberer                                                      nb;

    // agenda type to get (fibonacci, etc.)
    private String                                                              agendaType;

    // our logger
    private Logger                                                              logger;

    // log level. this is needed because we need to ask it in order to not do certain computations
    // which are only necessary for the output of a finer log level.
    private Level                                                               logLevel;

    /**
     * Constructor
     * 
     * @param pd
     *            A parser data object
     * @param agendaType
     *            The agenda type for data-driven parsing
     * @param nb
     *            The numberer for the parser data
     * @throws NoSuchElementException
     *             If any essential part of the parser data is not provided, i.e., is null.
     */
    public CYKParser(ParserData pd, String agendaType, Numberer nb)
            throws NoSuchElementException {
        // get the logger and the log level
        logger = Logger.getLogger(CYKParser.class.getPackage().getName());
        Logger getLevelLogger = logger;
        logLevel = getLevelLogger.getLevel();
        while (logLevel == null) {
            getLevelLogger = getLevelLogger.getParent();
            logLevel = getLevelLogger.getLevel();
        }
        this.nb = nb;
        if (pd.bg == null)
            throw new NoSuchElementException(
                    "Cannot run CYK parser with un-binarized grammar");
        if (pd.yfcomp == null)
            throw new NoSuchElementException(
                    "Cannot run CYK parser without a yield function composer");
        this.pd = pd;
        this.deterministicBinarization = pd.bg.getBinarizerType().equals(
                DeterministicBinarizer.class);
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
        this.agendaType = agendaType;
        try {
            agenda = PriorityAgendaFactory.getPriorityAgenda(agendaType, pd.nb);
        } catch (UnknownFormatException e) {
            throw new NoSuchElementException(
                    "Could not get a priority agenda of type " + agendaType);
        }
        chart = new CYKChart();
        goal = null;
    }

    @Override
    public void reset() {
        // agenda.clear();
        // chart.clear();
        try {
            agenda = PriorityAgendaFactory.getPriorityAgenda(agendaType, pd.nb);
        } catch (UnknownFormatException e) {
            throw new NoSuchElementException(
                    "Could not get a priority agenda of type " + agendaType);
        }
        agenda = new PriorityAgendaFibonacci(pd.nb);
        chart = new CYKChart();
        goal = null;
        pd.yfcomp.reset();
        // getting new instances and calling the garbage collector is faster than clearing.
        System.gc();
    }

    @Override
    public String getStats() {
        return agenda.getStats() + "\n" + "Chart size: " + chart.size() + "\n"
                + "Composer stats: " + pd.yfcomp.stats();
    }

    @Override
    public Tree getResult() {
        if (goal == null)
            throw new NoSuchElementException("No goal item present");
        Tree ret = new Tree(nb);
        terminals = new Node[goal.rvec.length()];
        ret.setRoot(buildTree(goal));
        ret.setLastterm(words.length);
        ret.setTerminals(Arrays.asList(terminals));
        Debinarizer.debinarize(ret);
        return ret;
    }

    /*
     * Build a tree from the goal item.
     */
    private Node buildTree(CYKItem it) {
        NodeLabel plabel = new NodeLabel();
        String tag = (String) pd.nb.getObjectWithId(GrammarConstants.PREDLABEL,
                it.pl);
        plabel.setTag(Utilities.removeArity(tag));
        Node ret = new Node(plabel);
        if (it.olc != null) {
            Node lcn = buildTree(it.olc);
            ret.appendChild(lcn);
        } else {
            // determine term position
            int i = 0;
            for (; i < words.length && !it.rvec.get(i); ++i);
            plabel.setWord((String) pd.nb.getObjectWithId(LexiconConstants.INPUTWORD,
                    words[i]));
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
    public boolean parse(ParserInput pi) {
        try {
            return parseWithTimeout(pi, 0);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
    public boolean parseWithTimeout(ParserInput pi, int timeout) throws TimeoutException {
        this.words = pi.getWords();
        try {
            if (deterministicBinarization && pd.doFilter)
                doParseDeterministicFilters(words, pi.getTags(), pi.tagsAsSet(), timeout);
            else
                doParse(words, pi.getTags(), timeout);
        } finally {
            if (goal != null)
                System.err.println(goal);
            System.err.println("chart size: " + chart.values().size());
        }
        return goal != null;
    }

    /**
     * Do weighted deductive parsing
     * @param words input words
     * @param tags input tags
     * @param timeout timeout in seconds, parse indefinitely if timeout == 0
     * @return true if there is a parse. the goal item is stored in the corresponding field.
     */
    public boolean doParse(int[] words, int[] tags, int timeout) throws TimeoutException {

        CYKItem item;
        for (int i = 0; i < tags.length; ++i) {
            BitSet rv = new BitSet();
            rv.set(0, tags.length, false);
            rv.set(i);
            item = new CYKItem(tags[i], 0.0, rv, null, null, words.length,
                    true, i, i);
            item.oscore = pd.est.get(words.length, tags[i], item.rvec, tags);
            agenda.push(item);
        }

        BitSet yp = null;
        ArrayList<CYKItem> transport = new ArrayList<CYKItem>();
        IntegerContainer start = new IntegerContainer(-1);
        IntegerContainer end = new IntegerContainer(-1);
        CYKItem nit = null;

		// boolean cont = false;
        long starttime = System.nanoTime();
        long nanotimeout = 1000000000L * timeout;
		while (!agenda.isEmpty()) {
            if (nanotimeout > 0 && System.nanoTime() - starttime > nanotimeout) {
                throw new TimeoutException();
            }

            item = agenda.poll();
            chart.add(item);

            if (logLevel.equals(Level.FINEST)) {
                String lstring = "";
                if (item.olc != null) {
                    lstring = item.olc.print(pd.nb);
                }
                String rstring = "";
                if (item.orc != null) {
                    rstring = item.orc.print(pd.nb);
                }
                logger.finest("item: " + item.print(pd.nb) + " # " + lstring
                        + "|" + rstring);
            }

            if (item.pl == pd.bg.startSymbol
                    && item.rvec.cardinality() == item.length
                    && item.length == words.length) {
                goal = item;
                break;
            }

            logger.fine("Processing " + item.print(pd.nb));

            // item is left child
            if (pd.bg.clByLc.containsKey(item.pl)) {
                for (BinaryClause bc : pd.bg.clByLc.get(item.pl)) {

                    // do we have unary clause?
                    if (bc.rc == -1) {
                        nit = new CYKItem(bc.lhs, item.iscore + bc.score,
                                (BitSet) item.rvec.clone(), item, null,
                                words.length, item.iscf, item.start, item.end);
                        transport.add(nit);
                    } else {
                        if (chart.containsKey(bc.rc)) {
                            for (BitSet candvec : chart.get(bc.rc).keySet()) {
                                CYKItem candit = chart.get(bc.rc).get(candvec);
                                if (bc.iscf && item.iscf && candit.iscf) {
                                    if (item.end + 1 == candit.start) {
                                        yp = (BitSet) item.rvec.clone();
                                        yp.xor(candit.rvec);
                                        nit = new CYKItem(bc.lhs, item.iscore
                                                + candit.iscore + bc.score, yp,
                                                item, candit, words.length,
                                                true, item.start, candit.end);
                                        transport.add(nit);
                                    }
                                } else {
                                    yp = pd.yfcomp.composeYields(item, candit,
                                            bc.yf, start, end);
                                    if (yp != null) {
                                        // start/end fields
                                        nit = new CYKItem(bc.lhs, item.iscore
                                                + candit.iscore + bc.score, yp,
                                                item, candit, words.length,
                                                false, start.i, end.i);
                                        transport.add(nit);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // item is right child
            if (pd.bg.clByRc.containsKey(item.pl)) {
                for (BinaryClause bc : pd.bg.clByRc.get(item.pl)) {

                    if (chart.containsKey(bc.lc)) {
                        for (BitSet candvec : chart.get(bc.lc).keySet()) {
                            CYKItem candit = chart.get(bc.lc).get(candvec);
                            if (bc.iscf && item.iscf && candit.iscf) {
                                if (candit.end + 1 == item.start) {
                                    yp = (BitSet) item.rvec.clone();
                                    yp.xor(candit.rvec);
                                    nit = new CYKItem(bc.lhs, item.iscore
                                            + candit.iscore + bc.score, yp,
                                            candit, item, words.length, true,
                                            candit.start, item.end);
                                    transport.add(nit);
                                }
                            } else {
                                yp = pd.yfcomp.composeYields(candit, item,
                                        bc.yf, start, end);
                                if (yp != null) {
                                    nit = new CYKItem(bc.lhs, item.iscore
                                            + candit.iscore + bc.score, yp,
                                            candit, item, words.length, false,
                                            start.i, end.i);
                                    transport.add(nit);
                                }
                            }
                        }
                    }
                }
            }

            for (CYKItem it : transport) {
                if (!chart.hasScore(it.pl, it.rvec)) {
                    it.oscore = pd.est.get(words.length, it.pl, it.rvec, tags);
                    // if (it.oscore > Double.NEGATIVE_INFINITY) {
                    if (logLevel.equals(Level.FINEST)) {
                        String lstring = "";
                        if (item.olc != null)
                            lstring = item.olc.print(pd.nb);
                        String rstring = "";
                        if (item.orc != null)
                            rstring = item.orc.print(pd.nb);

                        logger.finest("--> agenda item: " + it.print(pd.nb)
                                + " # " + lstring + "|" + rstring);
                    }
                    agenda.push(it);
                    // }
                }
            }
            transport = new ArrayList<CYKItem>();

        }

        return goal != null;
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

    /**
     * Parse a sentence.
     * 
     * @param words
     *            The input words as integers, backed by the numberer in the ParserData.
     * @param tags
     *            The input tags as integers, backed by the numberer in the ParserData.
     * @param tagset
     *            The complete set of POS tags from the training set.
     * @param timeout 
     *            The timeout in seconds. Parse indefinitely if timeout == 0.
     * @return true if there is a parse.
     */
    public boolean doParseDeterministicFilters(int[] words, int[] tags, Set<Integer> tagset, int timeout) 
        throws TimeoutException {

        CYKItem item;
        // scan: One item for every input word, resp. tag.
        for (int i = 0; i < tags.length; ++i) {
            BitSet rv = new BitSet();
            rv.set(0, tags.length, false);
            rv.set(i);
            item = new CYKItem(tags[i], 0.0, rv, null, null, words.length,
                    true, i, i);
            item.oscore = pd.est.get(words.length, tags[i], item.rvec, tags);
            agenda.push(item);
        }

        // Holds the resulting range vector after applying a deduction rule
        BitSet yp = null;
        // Holds the resulting item
        CYKItem nit = null;
        // Holds all items which can be created from another one. After all of them
        // have been created, the get added in bulk to the agenda.
        ArrayList<CYKItem> transport = new ArrayList<CYKItem>();
        // Shortcuts for items which are continuous constituents
        IntegerContainer start = new IntegerContainer(-1);
        IntegerContainer end = new IntegerContainer(-1);
        boolean cont = false;

        // Weighted Deductive Parsing
        long starttime = System.nanoTime();
        long nanotimeout = 1000000000L * timeout;
		while (!agenda.isEmpty()) {
            if (nanotimeout > 0 && System.nanoTime() - starttime > nanotimeout) {
                throw new TimeoutException();
            }

            // get an item and put in the agenda.
            item = agenda.poll();
            chart.add(item);

            if (logLevel.equals(Level.FINEST)) {
                String lstring = "";
                if (item.olc != null)
                    lstring = item.olc.print(pd.nb);
                String rstring = "";
                if (item.orc != null)
                    rstring = item.orc.print(pd.nb);

                logger.finest("item: " + item.print(pd.nb) + " # " + lstring
                        + "|" + rstring);
            }

            if (item.pl == pd.bg.startSymbol
                    && item.rvec.cardinality() == item.length
                    && item.length == words.length) {
                goal = item;
                break;
            }

            logger.fine("Processing " + item.print(pd.nb));

            // item is left child
            if (pd.bg.clByLc.containsKey(item.pl)) {
                for (BinaryClause bc : pd.bg.clByLc.get(item.pl)) {

                    // filter: this only works with deterministic binarization
                    cont = false;
                    if (bc.unbinarizedTopRhspterm != null) {
                        for (int i = 0; i < bc.unbinarizedTopRhspterm.length
                                && !cont; ++i) {
                            if (bc.unbinarizedTopRhspterm[i]) {
                                cont = !(tagset
                                        .contains(bc.unbinarizedTopRhsnames[i]));
                            }
                        }
                    }
                    if (cont)
                        continue;

                    // do we have unary clause?
                    if (bc.rc == -1) {
                        nit = new CYKItem(bc.lhs, item.iscore + bc.score,
                                (BitSet) item.rvec.clone(), item, null,
                                words.length, item.iscf, item.start, item.end);
                        transport.add(nit);
                    } else {
                        if (chart.containsKey(bc.rc)) {
                            for (BitSet candvec : chart.get(bc.rc).keySet()) {
                                CYKItem candit = chart.get(bc.rc).get(candvec);
                                if (bc.iscf && item.iscf && candit.iscf) {
                                    if (item.end + 1 == candit.start) {
                                        yp = (BitSet) item.rvec.clone();
                                        yp.xor(candit.rvec);
                                        nit = new CYKItem(bc.lhs, item.iscore
                                                + candit.iscore + bc.score, yp,
                                                item, candit, words.length,
                                                true, item.start, candit.end);
                                        transport.add(nit);
                                    }
                                } else {
                                    yp = pd.yfcomp.composeYields(item, candit,
                                            bc.yf, start, end);
                                    if (yp != null) {
                                        // start/end fields
                                        nit = new CYKItem(bc.lhs, item.iscore
                                                + candit.iscore + bc.score, yp,
                                                item, candit, words.length,
                                                false, start.i, end.i);
                                        transport.add(nit);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // item is right child
            if (pd.bg.clByRc.containsKey(item.pl)) {
                for (BinaryClause bc : pd.bg.clByRc.get(item.pl)) {

                    // filter
                    cont = false;
                    if (bc.unbinarizedTopRhspterm != null) {
                        for (int i = 0; i < bc.unbinarizedTopRhspterm.length
                                && !cont; ++i) {
                            if (bc.unbinarizedTopRhspterm[i])
                                cont = !(tagset
                                        .contains(bc.unbinarizedTopRhsnames[i]));
                        }
                    }
                    if (cont)
                        continue;

                    if (chart.containsKey(bc.lc)) {
                        for (BitSet candvec : chart.get(bc.lc).keySet()) {
                            CYKItem candit = chart.get(bc.lc).get(candvec);
                            if (bc.iscf && item.iscf && candit.iscf) {
                                if (candit.end + 1 == item.start) {
                                    yp = (BitSet) item.rvec.clone();
                                    yp.xor(candit.rvec);
                                    nit = new CYKItem(bc.lhs, item.iscore
                                            + candit.iscore + bc.score, yp,
                                            candit, item, words.length, true,
                                            candit.start, item.end);
                                    transport.add(nit);
                                }
                            } else {
                                yp = pd.yfcomp.composeYields(candit, item,
                                        bc.yf, start, end);
                                if (yp != null) {
                                    nit = new CYKItem(bc.lhs, item.iscore
                                            + candit.iscore + bc.score, yp,
                                            candit, item, words.length, false,
                                            start.i, end.i);
                                    transport.add(nit);
                                }
                            }
                        }
                    }
                }
            }

            for (CYKItem it : transport) {
                if (!chart.hasScore(it.pl, it.rvec)) {
                    it.oscore = pd.est.get(words.length, it.pl, it.rvec, tags);
                    if (logLevel.equals(Level.FINEST)) {
                        String lstring = "";
                        if (item.olc != null)
                            lstring = item.olc.print(pd.nb);
                        String rstring = "";
                        if (item.orc != null)
                            rstring = item.orc.print(pd.nb);

                        logger.finest("--> agenda item: " + it.print(pd.nb)
                                + " # " + lstring + "|" + rstring);
                    }
                    agenda.push(it);
                }
            }
            transport = new ArrayList<CYKItem>();

        }

        return goal != null;
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
