/*******************************************************************************
 * File AbstractBinarizer.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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
package de.tuebingen.rparse.grammar.binarize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.ClauseOccurrence;
import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.HasParameters;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * Offers a simple left-to-right binarization without any optimization. It is parametrizeable if one wants to have
 * binary or unary top, resp. bottom clauses. Subclasses of this abstract class must provide the method
 * {@code getNextName} for getting the name of the respectively next binarization nonterminal. Furthermore,
 * {@code preprocessClause} can be overridden to perform a clause specific pre- processing before binarization, such as
 * a re-ordering of the RHS.
 * 
 * @author ke, wmaier
 */
abstract public class AbstractBinarizer implements Binarizer, HasParameters {

    protected boolean       binaryTop;

    protected boolean       binaryBottom;

    private Logger          logger;

    private ClassParameters params;

    /**
     * Constructor only allows parametrization for binary/unary top/bottom clauses.
     * 
     * @param paramstring
     *            If it contains binaryTop, then a binary top clause will be created, if it contains binaryBottom, a
     *            binary bottom clause will be created.
     * @param nb
     *            The numberer of the grammar
     * @throws ParameterException
     *             If there is a problem with the parameter string.
     */
    protected AbstractBinarizer(String paramstring) throws ParameterException {
        logger = Logger.getLogger(AbstractBinarizer.class.getPackage()
                .getName());
        params = new ClassParameters();
        params.add("binaryTop", "Use binary top clause for binarization");
        params.add("binaryBottom", "Use binary bottom clause for binarization");
        params.parse(paramstring);
        this.binaryTop = params.check("binaryTop");
        this.binaryBottom = params.check("binaryBottom");
        logger.info("Binarizer parameters: " + paramstring);
    }

    @Override
    public ClassParameters getParameters() throws ParameterException {
        return params;
    }

    /**
     * Perform a simple left-to-right binarization. An optimization (such as variable number or arity minimization) can
     * be provided in subclasses either by overriding this method, or by changing the order of the RHS of clauses in the
     * preprocessing method.
     * 
     * @throws GrammarException
     *             If there is a problem with the binarization.
     */
    @Override
    public final BinaryRCG binarize(RCG grammar) throws GrammarException {
        Numberer numberer = grammar.getNumberer();
        BinaryRCG result = new BinaryRCG(grammar, this.getClass());

        Integer startpredLabel = grammar.getStartPredLabel();
        if (startpredLabel != null) {
            result.setStartPredLabel(startpredLabel);
        }

        for (Clause clause : grammar.getClauses()) {
            // vertical is occurrence-related
            // information, so binarize the clause once per occurrence (or
            // rather, once with count n for each vertical string with which
            // it occurs n times).
            Map<String, Integer> verticalCounts = new HashMap<String, Integer>();
            for (ClauseOccurrence occurrence : grammar.getOccurrences(clause)) {
                String vertical = occurrence.getVertical();
                
                if (!verticalCounts.containsKey(vertical)) {
                    verticalCounts.put(vertical, 1);
                } else {
                    verticalCounts.put(vertical,
                            verticalCounts.get(vertical) + 1);
                }
            }

            for (String vertical : verticalCounts.keySet()) {
                clause.setVertical(vertical);
                int count = verticalCounts.get(vertical);
                
                for (BinaryClause binarizedClause : binarizeClause(clause, count, 
                        numberer)) {
                    try {
                        logger.finest("binarized clause: " + binarizedClause);
                        result.addClause(binarizedClause);
                    } catch (GrammarException e) {
                        logger.severe("Could not add clause!");
                    }
                }
            }
        }

        String stats = getStats();
        if (stats != null && !stats.isEmpty()) {
            logger.info("AbstractBinarizer stats: " + getStats());
        }

        return result;
    }

    /**
     * Binarize a single clause.
     * 
     * @param clause
     *            The clause to be binarized.
     * @param numberer
     *            The numberer for the original unbinarized Clause instance.
     * @return A list of BinaryClause instances.
     * @throws GrammarException
     *             If something goes wrong during binarization.
     */
    public List<BinaryClause> binarizeClause(Clause clause, int count, Numberer numberer)
            throws GrammarException {
        int rhsLength = clause.rhsnames.length;
        // Already (at most) binary?
        if (rhsLength <= 2) {
            BinaryClause nbc = BinaryClause.constructClause(clause, numberer);
            nbc.cnt = count;
            return Collections.singletonList(nbc);
        }

        // more than binary --> binarize!
        List<BinaryClause> result = new ArrayList<BinaryClause>(rhsLength - 1);
        // this may be overriden by a subclass to change the order of the corresponding RHS
        // (for instance)

        int[][] oldLhsArgs = clause.lhsargs;

        clause = preprocessClause(clause, numberer);

        // Pick name for first LHS
        // if we want a binary top clause, picking the lhs name of the top clause is easy
        int lhsName = clause.lhsname;
        // if we want a unary top clause, we create the first binarization label already here
        // since it will be used in the RHS of the top clause
        String newNameString = null;
        int newName = -1;
        if (!binaryTop) {
            // then we pick here the RHS of the top unary clause, which will
            // be the same as the LHS of the first binary clause
            newNameString = getNextName(clause, "",
                    (String) numberer.getObjectWithId(
                            GrammarConstants.PREDLABEL, clause.lhsname), -1,
                    clause.lhsargs.length, numberer);
            newName = numberer
                    .number(GrammarConstants.PREDLABEL, newNameString);
            lhsName = newName;
        }

        // Determine the highest variable number used. Since this is a
        // simple RCG, it's enough to go through the variables on one
        // side. We need this for splitting variables.
        int maxVar = clause.getVarnum();

        // we need to keep two RHS predicates for the final clause if we want a binary
        // bottom clause, otherwise one
        int lastRhs = binaryBottom ? rhsLength - 2 : rhsLength - 1;

        // The clauses to split the non-binary clause into:
        for (int h = 0; h < lastRhs; h++) {
            List<List<Integer>> newLhsArgs = new ArrayList<List<Integer>>();
            List<Integer> currentLhsarg;
            List<List<Integer>> reduced = new ArrayList<List<Integer>>();
            List<Integer> currentReduced = new ArrayList<Integer>();
            reduced.add(currentReduced);
            List<Integer> reducedReplaced = new ArrayList<Integer>();
            int reductorIndex = 0;
            List<Integer> buffer = new ArrayList<Integer>();

            // For each LHS argument:
            for (int i = 0; i < oldLhsArgs.length; i++) {
                // Start an argument:
                currentLhsarg = new ArrayList<Integer>();
                newLhsArgs.add(currentLhsarg);

                if (!currentReduced.isEmpty()) {
                    currentReduced = new ArrayList<Integer>();
                    reduced.add(currentReduced);
                }

                // For each variable in the argument:
                for (int j = 0; j < oldLhsArgs[i].length; j++) {
                    // To reduce or not to reduce: That is the question!
                    if (reductorIndex < clause.rhsargs[h].length
                            && clause.rhsargs[h][reductorIndex] == oldLhsArgs[i][j]) {
                        reductorIndex++;

                        // Process buffered variables:
                        if (buffer.size() > 0) {
                            if (buffer.size() > 1) {
                                // More than one variable? Must introduce one
                                // new variable instead:
                                currentLhsarg.add(++maxVar);
                                reducedReplaced.add(maxVar);
                            } else {
                                // Just one variable? That's okay.
                                int var = buffer.get(0);
                                currentLhsarg.add(var);
                                reducedReplaced.add(var);
                            }

                            buffer.clear();
                        }

                        currentLhsarg.add(oldLhsArgs[i][j]);

                        // Start an argument:
                        if (!currentReduced.isEmpty()) {
                            currentReduced = new ArrayList<Integer>();
                            reduced.add(currentReduced);
                        }
                    } else {
                        buffer.add(oldLhsArgs[i][j]);
                        currentReduced.add(oldLhsArgs[i][j]);
                    }
                }

                // Process buffered variables:
                if (buffer.size() > 0) {
                    if (buffer.size() > 1) {
                        // More than one variable? Must introduce one
                        // new variable instead:
                        currentLhsarg.add(++maxVar);
                        reducedReplaced.add(maxVar);
                    } else {
                        // Just one variable? That's okay.
                        int var = buffer.get(0);
                        currentLhsarg.add(var);
                        reducedReplaced.add(var);
                    }

                    buffer.clear();
                }
            }

            // If nothing has gone into the last argument, remove it:
            if (currentReduced.isEmpty()) {
                reduced.remove(reduced.size() - 1);
            }

            // Get the next name for this RHS (= next LHS)
            newNameString = getNextName(clause, "",
                    (String) numberer.getObjectWithId(
                            GrammarConstants.PREDLABEL, clause.lhsname), h,
                    reducedReplaced.size(), numberer);
            newName = numberer
                    .number(GrammarConstants.PREDLABEL, newNameString);

            // Create clause
            Clause newClause = new Clause(2);
            newClause.setScore(1.0);
            newClause.lhsname = lhsName;
            newClause.lhsargs = toArray(newLhsArgs);
            newClause.rhsnames = new int[]{clause.rhsnames[h], newName};
            newClause.rhsargs = new int[][]{clause.rhsargs[h],
                    toArraySimple(reducedReplaced)};
            newClause.getRhspterm()[0] = clause.getRhspterm()[h];
            newClause.setOrigins(clause.getOrigins());
            // for preterminal filtering in deterministic parsing
            newClause.setOrigRhsnames(newClause.rhsnames);
            newClause.setOrigRhspterm(newClause.getRhspterm());

            // if we are at the top clause
            if (h == 0) {
                if (!binaryTop) {
                    // If top clause is unary, we add a unary clause on top, here called
                    // startingClause. The RHS label of this clause is the one picked earlier
                    Clause unaryTopClause = new Clause(1);
                    unaryTopClause.setScore(clause.getScore());
                    unaryTopClause.setOrigins(clause.getOrigins());
                    unaryTopClause.setOrigRhsnames(clause.rhsnames);
                    unaryTopClause.setOrigRhspterm(clause.getRhspterm());

                    int[] rhsarg = new int[newClause.lhsargs.length];
                    unaryTopClause.lhsargs = new int[rhsarg.length][];
                    for (int i = 0; i < rhsarg.length; i++) {
                        rhsarg[i] = i;
                        unaryTopClause.lhsargs[i] = new int[]{i};
                    }
                    unaryTopClause.rhsargs = new int[][]{rhsarg};
                    unaryTopClause.rhsnames[0] = newClause.lhsname;
                    unaryTopClause.lhsname = clause.lhsname;
                    unaryTopClause.getRhspterm()[0] = false;
                    BinaryClause binUnaryTopClause = BinaryClause
                            .constructClause(unaryTopClause, numberer);
                    binUnaryTopClause.cnt = count;
                    result.add(binUnaryTopClause);
                } else {
                    // If binary top, then the right deterministic probability is set here
                    newClause.setScore(clause.getScore());
                }
            }

            // Add clause:
            BinaryClause newBinaryClause = BinaryClause.constructClause(
                    newClause, numberer);
            newBinaryClause.cnt = count;
            result.add(newBinaryClause);

            // Prepare for next round:
            oldLhsArgs = toArray(reduced);
            lhsName = newName;
        }

        // The final clause:
        Clause finalClause = null;
        if (binaryBottom) {
            finalClause = new Clause(2);
        } else {
            finalClause = new Clause(1);
        }
        finalClause.setOrigins(clause.getOrigins());
        finalClause.setOrigRhspterm(clause.getRhspterm());
        finalClause.setOrigRhsnames(clause.rhsnames);
        finalClause.setScore(1.0);
        finalClause.lhsname = lhsName;
        finalClause.lhsargs = oldLhsArgs;
        if (binaryBottom) {
            finalClause.rhsnames = new int[]{clause.rhsnames[rhsLength - 2],
                    clause.rhsnames[rhsLength - 1]};
            finalClause.rhsargs = new int[][]{clause.rhsargs[rhsLength - 2],
                    clause.rhsargs[rhsLength - 1]};
            finalClause.getRhspterm()[0] = clause.getRhspterm()[rhsLength - 2];
            finalClause.getRhspterm()[1] = clause.getRhspterm()[rhsLength - 1];
        } else {
            finalClause.rhsnames = new int[]{clause.rhsnames[rhsLength - 1]};
            finalClause.rhsargs = new int[][]{clause.rhsargs[rhsLength - 1]};
            finalClause.getRhspterm()[0] = clause.getRhspterm()[rhsLength - 1];
        }
        BinaryClause finalBinaryClause = BinaryClause.constructClause(
                finalClause, numberer);
        finalBinaryClause.cnt = count;
        result.add(finalBinaryClause);

        return result;
    }

    /**
     * Destructively (!) preprocess the clause which is to be binarized. Watch out, the result of binarizing a clause
     * instance is not defined if this clause instance has already been binarized before. The default implementation
     * does nothing.
     * 
     * @param clause
     *            The clause instance to be preprocessed for binarization.
     * @param nb
     *            The corresponding numberer
     * @return The same clause instance, preprocessed.
     */
    protected Clause preprocessClause(Clause clause, Numberer nb) {
        return clause;
    }

    /**
     * Gets the name of the next binarization non-terminal out of the given information.
     * 
     * @param clause
     *            The clause.
     * @param basename
     *            The base name of the binarization non-terminal.
     * @param infix
     *            An infix.
     * @param rhspos
     *            The current position on the RHS of the unbinarized clause.
     * @param arity
     *            The arity of the binarization non-terminal.
     * @param nb
     *            The numberer of the unbinarized grammar.
     * @return The name for the next binarization non-terminal.
     */
    protected abstract String getNextName(Clause clause, String basename,
            String infix, int rhspos, int arity, Numberer nb);

    /**
     * This method is called after the end of the binarization and can be overriden by subclasses.
     * 
     * @return A string containing statistics of the binarization.
     */
    public String getStats() {
        return "";
    }

    private static int[] toArraySimple(List<Integer> list) {
        int[] result = new int[list.size()];
        int index = 0;

        for (int element : list) {
            result[index++] = element;
        }

        return result;
    }

    private static int[][] toArray(List<List<Integer>> list) {
        int[][] result = new int[list.size()][];
        int index = 0;

        for (List<Integer> element : list) {
            result[index++] = toArraySimple(element);
        }

        return result;
    }

}
