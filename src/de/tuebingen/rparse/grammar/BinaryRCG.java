/*******************************************************************************
 * File BinaryRCG.java
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
package de.tuebingen.rparse.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.grammar.binarize.Binarizer;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Simple RCG implementation for efficient indexing of binarized grammars
 * 
 * @author wmaier, ke
 */
public class BinaryRCG extends RCG {

    // for serializing (RCG)
    private static final long               serialVersionUID = 5717794936912200291L;

    /**
     * The start symbol of the grammar
     */
    public int                              startSymbol      = -1;

    /**
     * The clauses in the grammar
     */
    public List<BinaryClause>               clauses;

    /**
     * The clauses, hashed by the LHS
     */
    public Map<Integer, List<BinaryClause>> clByParent;

    /**
     * The clauses, hash by the left predicate of the RHS
     */
    public Map<Integer, List<BinaryClause>> clByLc;

    /**
     * The clauses, hashed by the right predicate of the LHS
     */
    public Map<Integer, List<BinaryClause>> clByRc;

    /**
     * The counts for each clause
     */
    public Map<BinaryClause, Integer>       cnt;

    // The pre-terminals in this grammar
    private Set<Integer>                    preterminals;

    // The arity of labels (states)
    private Map<Integer, Short>             stateToArity;

    // The useful labels of the grammar (everything which occurs on LHSes)
    private Set<Integer>                    labels;

    // The labels of the original grammar, passed through a constructor.
    private Set<Integer>                    labelsBeforeBin;

    // The binarizer type used to create this grammar.
    private Class<? extends Binarizer>      binarizationType;
    
    // The underlying unbinarized grammar
    private RCG                             grammar;

    /**
     * Construct a new BinaryRCG from an underlying RCG instance
     * 
     * @param grammar
     *            The underlying RCG instance
     * @param binarizationType
     *            The kind of binarization used to produce the grammar. It possibly depends on this what you can do with
     *            the grammar during parsing.
     */
    public BinaryRCG(RCG grammar, Class<? extends Binarizer> binarizationType) {
        super(grammar.getNumberer());
        clauses = new ArrayList<BinaryClause>();
        clByParent = new HashMap<Integer, List<BinaryClause>>();
        clByLc = new HashMap<Integer, List<BinaryClause>>();
        clByRc = new HashMap<Integer, List<BinaryClause>>();
        stateToArity = new HashMap<Integer, Short>();
        cnt = new HashMap<BinaryClause, Integer>();
        startSymbol = grammar.getStartPredLabel();
        preterminals = new HashSet<Integer>();
        labels = new HashSet<Integer>();
        labelsBeforeBin = new HashSet<Integer>();
        labelsBeforeBin.addAll(grammar.getClausesByLhsLabel().keySet());
        this.binarizationType = binarizationType;
        this.grammar = grammar;
    }

    // Used only for the bin cutoff, see there
    private BinaryRCG(Numberer numberer, int startSymbol,
            Set<Integer> labelsBeforeBin,
            Class<? extends Binarizer> binarizationType) {
        super(numberer);
        clauses = new ArrayList<BinaryClause>();
        clByParent = new HashMap<Integer, List<BinaryClause>>();
        clByLc = new HashMap<Integer, List<BinaryClause>>();
        clByRc = new HashMap<Integer, List<BinaryClause>>();
        stateToArity = new HashMap<Integer, Short>();
        cnt = new HashMap<BinaryClause, Integer>();
        this.startSymbol = startSymbol;
        preterminals = new HashSet<Integer>();
        labels = new HashSet<Integer>();
        this.labelsBeforeBin = labelsBeforeBin;
        this.binarizationType = binarizationType;
    }

    /**
     * Add a clause to this grammar
     * 
     * @param bc
     *            The clause to add
     * @throws GrammarException
     *             If clause cannot be added
     */
    public void addClause(BinaryClause bc) throws GrammarException {
        // Add clause, indexed by parent
        if (!clByParent.keySet().contains(bc.lhs)) {
            clByParent.put(bc.lhs, new ArrayList<BinaryClause>());
        }
        if (!clByParent.get(bc.lhs).contains(bc)) {
            clByParent.get(bc.lhs).add(bc);
        }

        // Add clause, indexed by left RHS pred
        if (!clByLc.keySet().contains(bc.lc)) {
            clByLc.put(bc.lc, new ArrayList<BinaryClause>());
        }
        if (!clByLc.get(bc.lc).contains(bc)) {
            clByLc.get(bc.lc).add(bc);
        }
        // LHS preterminal?
        if (bc.lcPt) {
            getPreterminals().add(bc.lc);
        }

        // Add clause, indexed by right RHS pred, if not unary
        if (bc.rc != -1) {
            if (!clByRc.keySet().contains(bc.rc)) {
                clByRc.put(bc.rc, new ArrayList<BinaryClause>());
            }
            if (!clByRc.get(bc.rc).contains(bc)) {
                clByRc.get(bc.rc).add(bc);
            }
        }
        // RHS preterminal
        if (bc.rcPt) {
            getPreterminals().add(bc.rc);
        }

        // Add the LHS label
        labels.add(bc.lhs);

        // Add clause, unhashed
        if (!clauses.contains(bc)) {
            clauses.add(bc);
        }

        // Clause count
        if (!cnt.containsKey(bc)) {
            cnt.put(bc, 0);
        }
        cnt.put(bc, cnt.get(bc) + bc.cnt);

        // Add clause arity
        stateToArity.put(bc.lhs, (short) bc.lhsfanout);
    }

    /**
     * Returns a version of this grammar where every clause with a count equal to or lower than the specified cutoff
     * value is removed. Specifying 0 returns an identical grammar.
     * 
     * @param cutoff
     *            The cutoff threshold
     * @return The modified grammar
     * @throws GrammarException
     *             If something goes wrong.
     */
    public BinaryRCG binCutoff(int cutoff) throws GrammarException {
        BinaryRCG result = new BinaryRCG(getNumberer(), startSymbol,
                labelsBeforeBin, binarizationType);
        result.setStartPredLabel(getStartPredLabel());

        for (BinaryClause clause : clauses) {
            int count = cnt.get(clause);
            if (count > cutoff) {
                for (int i = 0; i < count; i++) {
                    result.addClause(clause);
                }
            }
        }

        return result;
    }

    /**
     * Get the arity of a state in the grammar.
     * 
     * @param state
     *            The state to get the arity from
     * @return The arity of the state, or -1 if the state is not in the grammar
     */
    public int getArity(int state) {
        if (stateToArity.containsKey(state))
            return stateToArity.get(state);
        else if (getPreterminals().contains(state))
            return 1;
        else
            return -1;
    }

    /**
     * Get the useful labels of this grammar
     * 
     * @return The useful labels of this grammar in an unmodifiable set
     */
    public Set<Integer> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    /**
     * Get the binarizer type which has been used to create this instance
     * 
     * @return The {@link Binarizer} instance used to create this grammar.
     */
    public Class<? extends Binarizer> getBinarizerType() {
        return binarizationType;
    }
    
    /**
     * Get underlying unbinarized RCG
     * @return The underlying unbinarized RCG
     */
    public RCG getGrammar() {
        return grammar;
    }

    /**
     * Get the preterminals in this grammar. Contains all labels which have occurred as a RHS predicate name which was
     * marked as preterminal during adding the clause
     * 
     * @return A set of integers containing the preterminals.
     */
    public Set<Integer> getPreterminals() {
        return preterminals;
    }

    @Override
    public String stats() {
        int maxArity = (int) Collections.max(stateToArity.values());
        return "Binarized " + Collections.max(stateToArity.values()) + "-RCG"
                + "\nClauses: " + clauses.size() + "\nLabels: "
                + (clByParent.keySet().size() + getPreterminals().size())
                + ", thereof " + getPreterminals().size() + " preterminals"
                + "\nMax arity: " + maxArity + "\n";
    }

}
