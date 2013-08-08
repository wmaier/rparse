/*******************************************************************************
 * File RCGExtractor.java
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
package de.tuebingen.rparse.grammar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.HasParameters;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.parser.ParserData;
import de.tuebingen.rparse.parser.ParserDataWriter;
import de.tuebingen.rparse.treebank.HasID;
import de.tuebingen.rparse.treebank.HasSize;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.ConstituentConstants;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;
import de.tuebingen.rparse.treebank.dep.DepParentAnnotator;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNode;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Extracts simple RCGs from treebanks, both constituency and dependency treebanks.
 * 
 * @author wmaier
 * @param <T>
 *            Specifies if we extract from constituents or dependencies
 */
public class RCGExtractor<T extends HasID & HasSize>
        extends
            GrammarExtractionTask<T> implements HasParameters {

    // the class parameters
    private ClassParameters  params;

    // holds the extracted grammar + lexicon + etc
    private ParserData       pd;

    // writes the extracted grammars
    private ParserDataWriter parserDataWriter;

    // output location for extracted parser data
    private String           outputDirectory;

    // output encoding for extracted parser data
    private String           outputEncoding;

    // file name prefix within the output directory
    private String           outputPrefix;

    // split output: one file per sentence
    private boolean          split;

    // holds the start predicate
    private int              startPredicate;

    // extract with grammatical functions
    private boolean          gfmode;

    // extract lexicon in format pos,pos instead words,pos
    private boolean          poslex;

    // holds the sentence number
    private int              origin;

    /**
     * Get a grammar and don't write it anywhere
     * 
     * @param pd
     * @throws IOException
     * @throws ParameterException
     */
    public RCGExtractor(ParserData pd) throws IOException, ParameterException {
        this(pd, "");
    }

    /**
     * Get a grammar and don't write it anywhere, same with options
     * 
     * @param pd
     * @throws IOException
     * @throws ParameterException
     */
    public RCGExtractor(ParserData pd, String options) throws IOException,
            ParameterException {
        this(pd, null, null, null, null, false, options);
    }

    /**
     * Get a grammar (one single output file (collection)) and write it out
     * 
     * @param pd
     * @param outputDir
     * @throws IOException
     * @throws ParameterException
     */
    public RCGExtractor(ParserData pd, ParserDataWriter parserDataWriter,
            String outputDirectory, String outputEncoding, String outputPrefix,
            boolean split, String options) throws IOException,
            ParameterException {
        if (pd != null) {
            this.pd = pd;
        } else {
            Numberer nb = new Numberer();
            RCG g = new RCG(nb);
            Lexicon l = new Lexicon(nb);
            this.pd = new ParserData(g, l, nb);
        }
        this.split = split;
        this.parserDataWriter = parserDataWriter;
        this.outputDirectory = outputDirectory;
        this.outputEncoding = outputEncoding;
        this.outputPrefix = outputPrefix;
        params = new ClassParameters();
        params.add("gf", "Add grammatical functions (edge labels) to labels");
        params.add("poslex",
                "Create lexicon in format pos,pos instead word,pos");
        params.parse(options);
        gfmode = params.check("gf");
        poslex = params.check("poslex");
        startPredicate = -1;
    }

    /**
     * Extracts a simple RCG from a treebank tree as described in Maier&Sogaard (2008).
     * 
     * @param t
     *            The tree
     * @throws GrammarException
     *             If something goes wrong.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void extract(T graph) throws GrammarException {
        if (graph == null || graph.size() == 0) {
            throw new GrammarException("Cannot extract grammar from empty tree");
        }
        // extract from constituents or dependencies?
        if (graph instanceof Tree) {
            // constituents
            Tree t = (Tree) graph;
            if (startPredicate == -1) {
                startPredicate = pd.nb.number(GrammarConstants.PREDLABEL, t
                        .getRoot().getLabel().getTag()
                        + "1");
            }
            if (split) {
                Numberer nb = new Numberer();
                RCG g = new RCG(nb);
                Lexicon l = new Lexicon(nb);
                pd = new ParserData(g, l, nb);
            }
            origin = t.getId();
            t.addGorn();
            extractFromConstituentNode(t.getRoot());
            if (split) {
                pd.g.setStartPredLabel(startPredicate);
                try {
                    parserDataWriter.write(pd, new File(outputDirectory),
                            outputPrefix + "-" + t.getId(), outputEncoding);
                } catch (IOException e) {
                    throw new GrammarException(
                            "Could not write split grammar no. " + origin);
                }
            }
        } else if (graph instanceof DependencyForest) {
            // dependencies
            DependencyForest<DependencyForestNodeLabel, String> t = (DependencyForest<DependencyForestNodeLabel, String>) graph;
            if (split) {
                Numberer nb = new Numberer();
                RCG g = new RCG(nb);
                Lexicon l = new Lexicon(nb);
                pd = new ParserData(g, l, nb);
            }
            origin = t.getId();
            extractFromDep(t);
            if (split) {
                pd.g.setStartPredLabel(startPredicate);
                try {
                    parserDataWriter.write(pd, new File(outputDirectory),
                            outputPrefix + "-" + t.getId(), outputEncoding);
                } catch (IOException e) {
                    throw new GrammarException(
                            "Could not write split grammar no. " + origin);
                }
            }
        } else {
            throw new GrammarException(
                    "Cannot extract grammar from something other than constituents or dependencies");
        }
    }

    /**
     * Recursive extraction from a node.
     * 
     * @param n
     *            The node.
     * @return The top LHS label (as numberer-mapped integer).
     * @throws GrammarException
     *             If something goes wrong.
     */
    public int extractFromConstituentNode(Node n) throws GrammarException {
        int resultingLhsLabel = -1;
        if (n == null) {
            throw new GrammarException("Cannot extract grammar from null tree");
        }
        if (!n.hasChildren()) {
            // this is a terminal
            String word = n.getLabel().getWord();
            String tag = n.getLabel().getTag();
            // ensure that there are no dashes in labels
            tag.replace('-', 'X');
            if (gfmode && !n.getLabel().edgeEmpty()) {
                tag += "-" + n.getLabel().getEdge();
            }
            if (!poslex) {
                pd.l.addPair(word, tag);
            } else {
                pd.l.addPair(tag, tag);
            }
            tag += "1";
            resultingLhsLabel = pd.nb.number(GrammarConstants.PREDLABEL, tag);
        } else {
            // this is an internal node
            Clause cl = extractFromInternalConstituentNode(n);
            resultingLhsLabel = cl.lhsname;
            pd.g.addClause(cl, ClauseOccurrence.create(n));
        }
        return resultingLhsLabel;
    }

    /**
     * Recursive extraction from a non-child-node
     * 
     * @param n
     *            The node
     * @return The production
     * @throws GrammarException
     */
    public Clause extractFromInternalConstituentNode(Node n)
            throws GrammarException {
        // for continuous naming of variables
        Map<Integer, Integer> varmap = new HashMap<Integer, Integer>();
        // holds terminals/variables dominated by lhs of this clause
        List<Integer> lhsTermdom = new ArrayList<Integer>();
        // holds terminals/variables which are to be removed from the lhs
        List<Integer> lhsTermdomReduce = new ArrayList<Integer>();
        Clause clause = new Clause(n.getChildren().size());
        clause.addOrigin(origin);
        for (int i = 0; i < n.getChildren().size(); ++i) {
            Node child = n.getChildren().get(i);
            // head information
            if (child.isHead()) {
                clause.setHeadPos(i);
            }
            clause.rhsnames[i] = extractFromConstituentNode(child);
            // build args for the i+1 th rhs predicate
            // terminals dominated by this child
            List<Integer> rhsTermdom = child
                    .calcTermdom(new ArrayList<Integer>());
            Collections.sort(rhsTermdom);
            // remove double variables: only leave first terminal of every yield block
            List<Integer> rhsTermdomReduce = new ArrayList<Integer>();
            int last = rhsTermdom.get(0);
            for (int j = 1; j < rhsTermdom.size(); ++j) {
                if (last + 1 == rhsTermdom.get(j)) {
                    rhsTermdomReduce.add(rhsTermdom.get(j));
                }
                last = rhsTermdom.get(j);
            }
            // build lhs arguments
            lhsTermdom.addAll(rhsTermdom);
            lhsTermdomReduce.addAll(rhsTermdomReduce);
            // shorten all rhs arguments to length 1
            rhsTermdom.removeAll(rhsTermdomReduce);
            // build the argument
            clause.rhsargs[i] = new int[rhsTermdom.size()];
            for (int j = 0; j < rhsTermdom.size(); ++j) {
                clause.rhsargs[i][j] = rhsTermdom.get(j);
            }
        }
        // sort terminals dominated by n in continuous sections
        Collections.sort(lhsTermdom);
        ArrayList<ArrayList<Integer>> lhsArguments = new ArrayList<ArrayList<Integer>>();
        lhsArguments.add(new ArrayList<Integer>());
        lhsArguments.get(0).add(lhsTermdom.get(0));
        for (int i = 0; i < lhsTermdom.size() - 1; ++i) {
            if (lhsTermdom.get(i) + 1 != lhsTermdom.get(i + 1)) {
                lhsArguments.add(new ArrayList<Integer>());
            }
            lhsArguments.get(lhsArguments.size() - 1)
                    .add(lhsTermdom.get(i + 1));
        }
        // build the lhs arguments, build map for continuous renaming of variables
        clause.lhsargs = new int[lhsArguments.size()][];
        for (int i = 0; i < lhsArguments.size(); ++i) {
            lhsArguments.get(i).removeAll(lhsTermdomReduce);
            clause.lhsargs[i] = new int[lhsArguments.get(i).size()];
            for (int j = 0; j < lhsArguments.get(i).size(); ++j) {
                int var;
                if (varmap.containsKey(lhsArguments.get(i).get(j))) {
                    var = varmap.get(lhsArguments.get(i).get(j));
                } else {
                    var = varmap.size();
                    varmap.put(lhsArguments.get(i).get(j), var);
                }
                clause.lhsargs[i][j] = var;
            }
        }
        // rename the rhs continuously
        for (int i = 0; i < clause.rhsargs.length; ++i) {
            for (int j = 0; j < clause.rhsargs[i].length; ++j) {
                clause.rhsargs[i][j] = varmap.get(clause.rhsargs[i][j]);
            }
        }
        String label = n.getLabel().getTag();
        label.replace('-', 'X');
        if (gfmode && !n.getLabel().edgeEmpty())
            label += "-" + n.getLabel().getEdge();

        // mark arity
        if (!label.endsWith("1")) {
            label += lhsArguments.size();
        }

        clause.lhsname = pd.nb.number(GrammarConstants.PREDLABEL, label);
        clause.addGorn(pd.nb.number(GrammarConstants.GORN,
                n.getProperty(ConstituentConstants.GORN)));
        return clause;
    }

    /**
     * Extract a grammar from a dependency structure.
     * 
     * @param g
     *            The dependency structure
     * @throws GrammarException
     *             If something goes wrong.
     */
    public void extractFromDep(
            DependencyForest<DependencyForestNodeLabel, String> g)
            throws GrammarException {

        int rootn = pd.nb.number(GrammarConstants.PREDLABEL,
                DepParentAnnotator.DEPROOT);
        List<Clause> rootClauses = new ArrayList<Clause>();

        for (DependencyForestNode<DependencyForestNodeLabel, String> n : g
                .nodes()) {
            Clause c = new Clause(n.getModifiers().size() + 1);

            // lhs : label of the incoming edge is the lhs
            String lhs = n.getRelation();
            if (lhs == null) {
                lhs = DepParentAnnotator.DEPROOT;
                c.lhsname = rootn;
                rootClauses.add(c);
            } else {
                // strip numbers (labels with numbers at the end allowed in
                // CONLL)
                lhs = Utilities.removeArity(lhs);
                c.lhsname = pd.nb.number(GrammarConstants.PREDLABEL, lhs);
            }

            // rhs : label of outgoing edges and self pos tag
            ArrayList<Integer> rhs = new ArrayList<Integer>();
            for (DependencyForestNode<DependencyForestNodeLabel, String> modifier : n
                    .getModifiers()) {
                // strip numbers (labels with numbers at the end allowed in
                // CONLL)
                String rhsname = modifier.getRelation();
                rhsname = Utilities.removeArity(rhsname);
                rhs.add(pd.nb.number(GrammarConstants.PREDLABEL, rhsname));
            }
            int posnum = pd.nb.number(GrammarConstants.PREDLABEL, n.getToken()
                    .getPostag());
            rhs.add(posnum);
            c.rhsnames = new int[rhs.size()];
            for (int i = 0; i < rhs.size(); ++i)
                c.rhsnames[i] = rhs.get(i);

            // get all nodes (terminals) dominated by the current one
            List<List<Integer>> lhsDom = Utilities.splitContinuous(n
                    .projectionAsInt());

            // get all nodes (terminals) dominated by each of the modifiers +
            // self
            List<List<List<Integer>>> rhsDom = new ArrayList<List<List<Integer>>>();
            for (DependencyForestNode<DependencyForestNodeLabel, String> modifier : n
                    .getModifiers())
                rhsDom.add(Utilities.splitContinuous(modifier.projectionAsInt()));
            rhsDom.add(Utilities.splitContinuous(Collections.singletonList(n
                    .getID())));

            // remove duplicate variables (= collapse) on the rhs
            Set<Integer> rhsArgsReduce = new HashSet<Integer>();
            for (List<List<Integer>> rhsArgs : rhsDom)
                for (List<Integer> rhspredarg : rhsArgs)
                    if (rhspredarg.size() > 1)
                        rhsArgsReduce.addAll(rhspredarg.subList(1,
                                rhspredarg.size()));
            // remove duplicates on the left
            for (int i = 0; i < lhsDom.size(); ++i) {
                List<Integer> newLhsArg = new ArrayList<Integer>(lhsDom.get(i));
                newLhsArg.removeAll(rhsArgsReduce);
                lhsDom.set(i, newLhsArg);
            }

            // reduce lower list level on rhs
            List<List<Integer>> flatRhsDom = new ArrayList<List<Integer>>();
            for (List<List<Integer>> rhsArgs : rhsDom) {
                List<Integer> arg = new ArrayList<Integer>();
                for (List<Integer> rhspredarg : rhsArgs)
                    arg.add(rhspredarg.get(0));
                flatRhsDom.add(arg);
            }

            // make array
            c.lhsargs = Utilities.asIntArrayArray(lhsDom);
            c.rhsargs = Utilities.asIntArrayArray(flatRhsDom);

            // build dic and rename lhs, fill lhsnmap
            Map<Integer, Integer> dic = new HashMap<Integer, Integer>();
            int count = 0;
            for (int i = 0; i < c.lhsargs.length; ++i) {
                for (int j = 0; j < c.lhsargs[i].length; ++j) {
                    ++count;
                    dic.put(c.lhsargs[i][j], count);
                    c.lhsargs[i][j] = count;
                }
            }

            // rename rhs using dic
            for (int i = 0; i < c.rhsargs.length; ++i)
                for (int j = 0; j < c.rhsargs[i].length; ++j)
                    c.rhsargs[i][j] = dic.get(c.rhsargs[i][j]);

            // sort rhs by leftmost dominated terminal (this is automatically
            // the case for constituents)
            Set<Integer> todo = new HashSet<Integer>();
            List<Integer> order = new ArrayList<Integer>();
            for (int i = 0; i < c.rhsargs.length; ++i)
                todo.add(i);
            int minind = 500;
            int minval = 500;
            while (todo.size() > 0) {
                // get the right order
                for (int i = 0; i < c.rhsargs.length; ++i)
                    if (todo.contains(i)) {
                        minind = c.rhsargs[i][0] < minval ? i : minind;
                        minval = Math.min(minval, c.rhsargs[i][0]);
                    }
                order.add(minind);
                todo.remove(minind);
                minind = 500;
                minval = 500;
            }
            // sort names and arguments
            int[] newrhsnames = new int[c.rhsnames.length];
            int[][] newrhspreds = new int[c.rhsargs.length][];
            for (int i = 0; i < order.size(); ++i) {
                newrhsnames[i] = c.rhsnames[order.get(i)];
                int[] rhspred = new int[c.rhsargs[order.get(i)].length];
                for (int j = 0; j < c.rhsargs[order.get(i)].length; ++j)
                    rhspred[j] = c.rhsargs[order.get(i)][j];
                newrhspreds[i] = rhspred;
            }
            c.rhsnames = newrhsnames;
            c.rhsargs = newrhspreds;

            // now add arities to all names
            String newName = ((String) pd.nb.getObjectWithId(
                    GrammarConstants.PREDLABEL, c.lhsname)) + c.lhsargs.length;
            c.lhsname = pd.nb.number(GrammarConstants.PREDLABEL, newName);
            for (int i = 0; i < c.rhsnames.length; ++i) {
                String oldName = ((String) pd.nb.getObjectWithId(
                        GrammarConstants.PREDLABEL, c.rhsnames[i]));
                newName = oldName + c.rhsargs[i].length;
                int newNameInt = pd.nb.number(GrammarConstants.PREDLABEL,
                        newName);
                if (c.rhsnames[i] == posnum)
                    posnum = newNameInt;
                c.rhsnames[i] = newNameInt;
            }
            c.setRhspterm(new boolean[c.rhsnames.length]);

            // lexicon
            pd.l.addPair(n.getToken().getForm(), n.getToken().getPostag() + "1");

            // identify POS tag on the RHS as lexical head
            for (int i = 0; i < c.rhsnames.length; ++i) {
                if (c.rhsnames[i] == posnum) {
                    c.setHeadPos(i);
                }
            }

            pd.g.addClause(c, ClauseOccurrence.create(n));
        }

        // add a TOP node
        Clause c = new Clause(rootClauses.size());

        // lhs : new top label
        c.lhsname = pd.nb.number(GrammarConstants.PREDLABEL,
                GrammarConstants.DEFAULTSTART);
        startPredicate = c.lhsname;

        // rhs : label of outgoing edges and self pos tag
        List<List<List<Integer>>> rhsDom = new ArrayList<List<List<Integer>>>();
        int modcnt = 0;
        for (DependencyForestNode<DependencyForestNodeLabel, String> modifier : g
                .nodes()) {
            if (modifier.getHead() == null) {
                c.rhsnames[modcnt] = rootn;
                modcnt++;
                // get all nodes (terminals) dominated by each of the root nodes
                // + self
                rhsDom.add(Utilities.splitContinuous(modifier.projectionAsInt()));

            }
        }

        // get all nodes (terminals) dominated by the current one
        List<Integer> all = new ArrayList<Integer>();
        for (int i = 1; i <= g.nodes().size(); ++i)
            all.add(i);
        List<List<Integer>> lhsDom = new ArrayList<List<Integer>>();
        lhsDom.add(all);

        // remove duplicate variables (= collapse) on the rhs
        Set<Integer> reduce = new HashSet<Integer>();
        for (List<List<Integer>> rhsArgs : rhsDom)
            for (List<Integer> rhsArg : rhsArgs)
                if (rhsArg.size() > 1)
                    reduce.addAll(rhsArg.subList(1, rhsArg.size()));
        // remove duplicates on the left
        for (int i = 0; i < lhsDom.size(); ++i) {
            List<Integer> newArg = new ArrayList<Integer>(lhsDom.get(i));
            newArg.removeAll(reduce);
            lhsDom.set(i, newArg);
        }

        // reduce lower list level on rhs
        List<List<Integer>> flatRhsArgs = new ArrayList<List<Integer>>();
        for (List<List<Integer>> rhsArgs : rhsDom) {
            List<Integer> arg = new ArrayList<Integer>();
            for (List<Integer> rhspredarg : rhsArgs)
                arg.add(rhspredarg.get(0));
            flatRhsArgs.add(arg);
        }

        // make array
        c.lhsargs = Utilities.asIntArrayArray(lhsDom);
        c.rhsargs = Utilities.asIntArrayArray(flatRhsArgs);

        // build dic and rename lhs
        Map<Integer, Integer> dic = new HashMap<Integer, Integer>();
        int count = 0;
        for (int i = 0; i < c.lhsargs.length; ++i) {
            for (int j = 0; j < c.lhsargs[i].length; ++j) {
                ++count;
                dic.put(c.lhsargs[i][j], count);
                c.lhsargs[i][j] = count;
            }
        }

        // rename rhs using dic
        for (int i = 0; i < c.rhsargs.length; ++i) {
            for (int j = 0; j < c.rhsargs[i].length; ++j) {
                c.rhsargs[i][j] = dic.get(c.rhsargs[i][j]);
            }
        }

        // sort rhs by leftmost dominated terminal (this is automatically the
        // case for constituents)
        Set<Integer> todo = new HashSet<Integer>();
        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < c.rhsargs.length; ++i)
            todo.add(i);
        int minind = 500;
        int minval = 500;
        while (todo.size() > 0) {
            // get the right order
            for (int i = 0; i < c.rhsargs.length; ++i)
                if (todo.contains(i)) {
                    minind = c.rhsargs[i][0] < minval ? i : minind;
                    minval = Math.min(minval, c.rhsargs[i][0]);
                }
            order.add(minind);
            todo.remove(minind);
            minind = 500;
            minval = 500;
        }
        // sort names and arguments and vertical history fields
        int[] newRhsNames = new int[c.rhsnames.length];
        int[][] newRhsPreds = new int[c.rhsargs.length][];
        for (int i = 0; i < order.size(); ++i) {
            newRhsNames[i] = c.rhsnames[order.get(i)];
            int[] rhsPred = new int[c.rhsargs[order.get(i)].length];
            for (int j = 0; j < c.rhsargs[order.get(i)].length; ++j)
                rhsPred[j] = c.rhsargs[order.get(i)][j];
            newRhsPreds[i] = rhsPred;
        }
        c.rhsnames = newRhsNames;
        c.rhsargs = newRhsPreds;

        // now add arities to all names except top label (already has arity)
        for (int i = 0; i < c.rhsnames.length; ++i) {
            String oldName = ((String) pd.nb.getObjectWithId(
                    GrammarConstants.PREDLABEL, c.rhsnames[i]));
            String newName = oldName + c.rhsargs[i].length;
            int newNameInt = pd.nb.number(GrammarConstants.PREDLABEL, newName);
            c.rhsnames[i] = newNameInt;
        }
        c.setRhspterm(new boolean[c.rhsnames.length]);
        // identify POS tag on the RHS as lexical head
        c.setHeadPos(0);
        pd.g.addClause(
                c,
                ClauseOccurrence.create(g.getVerticalDepth() > 0 ? "^"
                        + GrammarConstants.DEFAULTSTART : ""));
        rootClauses.clear();
    }

    @Override
    public void done() throws TreebankException {
        for (Clause c : pd.g.getClauses()) {
            c.setPreterms(pd.l.getPreterminals());
        }
        pd.g.setStartPredLabel(startPredicate);
        if (!split && outputDirectory != null) {
            try {
                parserDataWriter.write(pd, new File(outputDirectory),
                        outputPrefix, outputEncoding);
            } catch (IOException e) {
                throw new TreebankException(e);
            }
        }
    }

    @Override
    public ClassParameters getParameters() {
        return params;
    }

}
