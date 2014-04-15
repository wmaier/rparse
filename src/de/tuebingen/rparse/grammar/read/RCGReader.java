/*******************************************************************************
 * File RCGReader.java
 * 
 * Authors:
 *    Wolfgang Maier, Kilian Evang
 *    
 * Copyright:
 *    Wolfgang Maier, Kilian Evang 2011
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.ClauseOccurrence;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.lex.Lexicon;

/**
 * Read an RCG from a file. Reader assumes as input the output from RCG.toString() without the probability and counts
 * columns, e.g. something like O1:[] ... On:[] S1([1][2][3]) --> VP2([1],[3]) VMFIN([2])
 * 
 * @author wmaier, ke
 */
public class RCGReader extends GrammarReader<RCG> {

    /**
     * Pattern for splitting the data columns before the clause
     */
    public static final Pattern FIELD     = Pattern.compile("(\\w+):(\\S+) ");

    // holds the start predicate
    private String              startPred = "";

    // holds the preterminals
    private Set<Integer>        preterminals;

    // a numberer
    private Numberer            nb;

    /**
     * Constructor
     * 
     * @param f
     *            File to read the grammar from
     * @param nb
     *            A numberer
     * @throws FileNotFoundException
     *             If there is no such file
     */
    public RCGReader(File f, Numberer nb) throws FileNotFoundException {
        this(f, GrammarConstants.DEFAULTSTART, null, nb);
    }

    /**
     * Constructor
     * 
     * @param f
     *            File to read the grammar from
     * @param lexicon
     *            A lexicon to fill
     * @param nb
     *            A numberer
     * @throws FileNotFoundException
     *             If there is no such file
     */
    public RCGReader(File f, Lexicon lexicon, Numberer nb)
            throws FileNotFoundException {
        this(f, GrammarConstants.DEFAULTSTART, lexicon, nb);
    }

    /**
     * Constructor
     * 
     * @param f
     *            File to read the grammar from
     * @param sp
     *            A start predicate name
     * @param lexicon
     *            A lexicon to fill
     * @param nb
     *            A numberer
     * @throws FileNotFoundException
     *             if there is no such file
     */
    public RCGReader(File f, String sp, Lexicon lexicon, Numberer nb)
            throws FileNotFoundException {
        super(new FileReader(f));
        this.startPred = sp;
        if (lexicon == null) {
            preterminals = null;
        } else {
            preterminals = lexicon.getPreterminals();
        }
        this.nb = nb;
    }

    /**
     * Interpret a string as an argument of a predicate. Required format: [0]..[n], where [i], 0 <= i <= n, is a
     * variable.
     * 
     * @param argument
     *            The string to parse.
     * @return An array of integers corresponding to the variable.
     * @throws GrammarException
     *             If something goes wrong.
     */
    public int[] stringToArgument(String argument) throws GrammarException {
        List<Integer> args = new ArrayList<Integer>();
        int argcontindl = 0;
        int argcontindr = argument.indexOf(']');
        while ((argcontindr + 1) > 0) {
            String curarg = argument.substring(argcontindl + 1, argcontindr);
            if (curarg.length() < 1)
                throw new GrammarException("Couldn't parse argument");
            args.add(Integer.parseInt(curarg));
            argcontindl = argcontindr + 1;
            argcontindr = argument.indexOf(']', argcontindl);
        }
        int[] ret = new int[args.size()];
        for (int i = 0; i < args.size(); ++i)
            ret[i] = args.get(i);
        return ret;
    }

    private class Predicate {

        public int     label;
        public int[][] args;
    }

    /**
     * Interpret a string as a predicate. Syntax: NAME(arg,..,arg), where arg is as in {@code stringToArgument}.
     * 
     * @param pred
     *            The input string.
     * @return A predicate (has fields: int label and int[][] args).
     * @throws GrammarException
     *             If something goes wrong.
     */
    public Predicate stringToPredicate(String pred) throws GrammarException {
        Predicate ret = new Predicate();
        List<int[]> aux = new ArrayList<int[]>();
        int oparind = pred.indexOf('(');
        if (oparind > -1) {
            String head = pred.substring(0, oparind);
            if (head.endsWith(GrammarConstants.HEAD_MARKER))
                head = pred.substring(0, oparind - 1);
            pred = pred.substring(oparind);
            if (pred.charAt(0) == '(' && pred.charAt(pred.length() - 1) == ')') {
                pred = pred.substring(1, pred.length() - 1);
                if (pred.indexOf('(') == -1 && pred.indexOf('(') == -1) {
                    ret.label = nb.number(GrammarConstants.PREDLABEL, head);
                    int argindl = 0;
                    int argindr = pred.indexOf(GrammarConstants.ARG_SEP) == -1
                            ? pred.length()
                            : pred.indexOf(GrammarConstants.ARG_SEP);
                    while ((argindr + 1) > 0) {
                        aux.add(stringToArgument(pred.substring(argindl,
                                argindr).trim()));
                        argindl = argindr + 1;
                        argindr = pred.indexOf(GrammarConstants.ARG_SEP,
                                argindl);
                        if (argindr == -1 && argindl <= pred.length())
                            argindr = pred.length();
                    }
                    ret.args = new int[aux.size()][];
                    for (int i = 0; i < aux.size(); ++i) {
                        ret.args[i] = aux.get(i);
                    }
                }
            }
        }
        return ret;
    }

    private Integer[] stringToIntArray(String list) throws GrammarException {
        if (!(list.startsWith("[") && list.endsWith("]"))) {
            throw new GrammarException("invalid integer list format: " + list);
        }

        if (list.length() == 2) {
            return new Integer[0];
        }

        String[] elements = list.substring(1, list.length() - 1).split(",");
        Integer[] result = new Integer[elements.length];

        for (int i = 0; i < elements.length; i++) {
            result[i] = Integer.parseInt(elements[i]);
        }

        return result;
    }

    /**
     * Helper class, contains a clause and a count.
     */
    public class ClauseWithCount {

        public Clause clause;
        public int    count;

        public ClauseWithCount() {
            clause = null;
            count = 0;
        }
    }

    /**
     * Interpret a string as a clause. See upper comment for details on format.
     * 
     * @param line
     *            The input string
     * @return A clause instance with a count
     * @throws GrammarException
     */
    public ClauseWithCount stringToClause(String line) throws GrammarException {
        List<Integer> sorigin = new ArrayList<Integer>();
        List<Integer> gorn = new ArrayList<Integer>();
        int count = 0;
        double score = Double.NEGATIVE_INFINITY;
        Matcher matcher = FIELD.matcher(line);
        int end = 0;
        int headpos = -1;

        while (matcher.find()) {
            String key = matcher.group(1);

            if ("O".equals(key)) {
                sorigin = Arrays.asList(stringToIntArray(matcher.group(2)));
            } else if ("G".equals(key)) {
                gorn = Arrays.asList(stringToIntArray(matcher.group(2)));
            } else if ("C".equals(key)) {
                count = Integer.parseInt(matcher.group(2));
            } else if ("S".equals(key)) {
                score = Double.parseDouble(matcher.group(2));
            } else if ("H".equals(key)) {
                headpos = Integer.parseInt(matcher.group(2));
            }

            end = matcher.end();
        }

        line = line.substring(end);
        int sepind = line.indexOf(GrammarConstants.LHSRHS_SEP);
        if (sepind > 1) {
            int lhsstart = line.lastIndexOf(' ', sepind - 2) + 1;
            int lhsend = line.indexOf(')', lhsstart) + 1;
            if (lhsend > 0) {
                String lhs = line.substring(lhsstart, lhsend).trim();
                int rhsstart = sepind + GrammarConstants.LHSRHS_SEP.length();
                String rhs = line.substring(rhsstart).trim();
                Predicate lhsp = stringToPredicate(lhs);
                if (lhsp != null) {
                    // lhs
                    List<Predicate> preds = new ArrayList<Predicate>();
                    preds.add(lhsp);
                    // rhs
                    Predicate rhsp = null;
                    int rhsindl = 0;
                    int rhsindr = rhs.indexOf(')') + 1;
                    while (rhsindr > 0) {
                        String rhscurrent = rhs.substring(rhsindl, rhsindr)
                                .trim();
                        if (rhscurrent.length() < 6)
                            throw new GrammarException(
                                    "Could not parse "
                                            + line
                                            + ": This does not seem to be a predicate: "
                                            + rhscurrent);
                        rhsp = stringToPredicate(rhscurrent);
                        if (rhsp == null)
                            throw new GrammarException("Could not parse "
                                    + line + ": Could not read rhs predicate "
                                    + rhscurrent);
                        else
                            preds.add(rhsp);
                        rhsindl = rhsindr;
                        rhsindr = rhs.indexOf(')', rhsindl) + 1;
                    }

                    Predicate lhshelper = preds.get(0);
                    preds.remove(0);
                    int rhslen = preds.size();
                    int[] rhsnames = new int[rhslen];
                    int[][] rhsargs = new int[rhslen][];

                    for (int i = 0; i < preds.size(); ++i) {
                        Predicate rhshelper = preds.get(i);
                        rhsnames[i] = rhshelper.label;
                        rhsargs[i] = new int[rhshelper.args.length];

                        for (int j = 0; j < rhshelper.args.length; ++j) {
                            rhsargs[i][j] = rhshelper.args[j][0];
                        }
                    }

                    ClauseWithCount result = new ClauseWithCount();
                    result.clause = new Clause(lhshelper.label, lhshelper.args,
                            rhsnames, rhsargs, headpos, score, sorigin, gorn,
                            null, null, null, -1);
                    result.count = count;

                    if (preterminals != null) {
                        result.clause.setPreterms(preterminals);
                    }

                    if (headpos != -1) {
                        result.clause.setHeadPos(headpos);
                    }

                    return result;
                } else {
                    throw new GrammarException("Could not parse " + line
                            + ": Could not read lhs predicate " + lhs);
                }
            } else {
                throw new GrammarException(
                        "Could not parse "
                                + line
                                + ": Could not read lhs predicate, no closing parenthesis.");
            }
        } else {
            throw new GrammarException("Could not parse " + line
                    + ": Separator '" + GrammarConstants.LHSRHS_SEP
                    + "' not found.");
        }
    }

    /**
     * Get the RCG from the file to which this reader has been initialized
     * 
     * @return The RCG
     * @throws IOException
     *             If there is unexpected I/O
     * @throws GrammarException
     *             If something else is wrong.
     */
    public RCG getRCG() throws IOException, GrammarException {
        RCG ret = new RCG(nb);
        String line = "";
        while ((line = super.readLine()) != null) {
            if (line.length() < 2)
                continue;
            line = line.trim();
            // comment field support
            if (line.charAt(0) == '#') {
                line = line.substring(1).trim();
                if (line.indexOf("start") == 0) {
                    line = line.substring(5).trim();
                    if (line.length() > 0)
                        ret.setStartPredLabel(nb.number(
                                GrammarConstants.PREDLABEL, line));
                }
            } else if (line.charAt(0) != '%') {
                ClauseWithCount cwc = stringToClause(line);
                if (cwc != null && cwc.clause != null) {
                    for (int i = 0; i < cwc.count; ++i) {
                        ret.addClause(cwc.clause, ClauseOccurrence.create(""));
                    }
                }
            }
        }
        if (!ret.startPredicateDefined())
            ret.setStartPredLabel(nb.number(GrammarConstants.PREDLABEL,
                    startPred));
        if (ret.getClausesForLabel(nb.number(GrammarConstants.PREDLABEL,
                startPred)) == null)
            throw new GrammarException(
                    "Cannot read RCG: No predicate with start predicate label found.");
        return ret;
    }

}
