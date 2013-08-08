/*******************************************************************************
 * File RCG.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Implements a Range Concatenation Grammar (see Boullier (2000))
 * 
 * @author wmaier
 */
public class RCG implements Serializable {

    // for serializing
    private static final long                         serialVersionUID    = -3594564519442795412L;

    // Map clauses on occurrences thereof
    private final Map<Clause, List<ClauseOccurrence>> occurrencesByClause = new HashMap<Clause, List<ClauseOccurrence>>();

    // The numberer of this grammar
    private final Numberer                            nb;

    // The list of clauses
    private final List<Clause>                        clauses;

    // Clauses by LHS label
    private final Map<Integer, Set<Clause>>           clausesByLhsLabel;

    // lhslabel -> arity
    private final Map<Integer, Short>                 stateToArity;

    // start predicate label
    private Integer                                   startPredicateLabel;

    // counts unique clauses
    private int                                       clausecnt;

    // The arity of this grammar
    private int                                       arity;

    // The highest number of variables in the grammar
    private int                                       maxvarnum;

    /**
     * Constructor.
     * 
     * @param nb
     *            A numberer for the predicate names.
     */
    public RCG(Numberer nb) {
        this.nb = nb;
        clauses = new ArrayList<Clause>();
        clausesByLhsLabel = new HashMap<Integer, Set<Clause>>();
        stateToArity = new HashMap<Integer, Short>();
        startPredicateLabel = null;
        clausecnt = 0;
        maxvarnum = 0;
        arity = -1;
    }

    /**
     * Returns an RCG instance which is equal to this one except that all productions have been removed which occur <=
     * times than the given threshold. We do this in place because for some reason doing it with a copy of the grammar
     * uses too much memory.
     * 
     * @param threshold
     *            The threshold
     * @throws GrammarException
     *             In case there is a problem with adding clauses to the resulting grammar
     */
    public void cutoff(int threshold) throws GrammarException {
        List<Clause> removalCandidates = new ArrayList<Clause>();
        for (Clause c : occurrencesByClause.keySet()) {
            if (occurrencesByClause.get(c).size() <= threshold) {
                removalCandidates.add(c);
            }
        }
        for (Clause c : removalCandidates) {
            occurrencesByClause.remove(c);
            clauses.remove(c);
            clausesByLhsLabel.get(c.lhsname).remove(c);
        }
        stateToArity.clear();
        clausecnt = 0;
        arity = 0;
        for (Clause c : occurrencesByClause.keySet()) {
            if (stateToArity.containsKey(c.lhsname)
                    && stateToArity.get(c.lhsname) != c.lhsargs.length) {
                throw new GrammarException(
                        "Same predicate with different arities");
            }
            stateToArity.put(c.lhsname, (short) c.lhsargs.length);
            clausecnt++;
            if (arity < c.getArity()) {
                arity = c.getArity();
            }
            if (maxvarnum < c.getVarnum()) {
                maxvarnum = c.getVarnum();
            }
        }
    }

    /**
     * Get the list of occurrences of a certain clause. The clause count is the length of this list.
     * 
     * @param clause
     *            The clause.
     * @return The (unmodifiable) list of its occurrences.
     */
    public List<ClauseOccurrence> getOccurrences(Clause clause) {
        return Collections.unmodifiableList(occurrencesByClause.get(clause));
    }

    /**
     * Get the number of occurrences of a clause
     * 
     * @param c
     *            The clause
     * @return Its number of occurrences. 0 if the clause is not in the grammar.
     */
    public int getClauseOccurrenceCount(Clause c) {
        if (occurrencesByClause.containsKey(c)) {
            return getOccurrences(c).size();
        }
        return 0;
    }

    /**
     * Get all clauses for a certain LHS label.
     * 
     * @return The corresponding list of clauses.
     */
    public Map<Integer, Set<Clause>> getClausesByLhsLabel() {
        return clausesByLhsLabel;
    }

    /**
     * Add a clause to the grammar. The counting is only done with the occurrences.
     * 
     * @param c
     *            The clause instance.
     * @param occurrence
     *            The clause occurrence. A {@link ClauseOccurrence} contains all information which is relevant with
     *            respect to a concrete occurrence of the clause in the treebank.
     * @throws GrammarException
     */
    public void addClause(Clause c, ClauseOccurrence occurrence)
            throws GrammarException {
        // occurrences
        if (!occurrencesByClause.containsKey(c)) {
            occurrencesByClause.put(c, new ArrayList<ClauseOccurrence>());
        }
        if (occurrence != null) {
            occurrencesByClause.get(c).add(occurrence);
        }

        // state to arity
        if (stateToArity.containsKey(c.lhsname)
                && stateToArity.get(c.lhsname) != c.lhsargs.length) {
            throw new GrammarException("Same predicate with different arities");
        }
        stateToArity.put(c.lhsname, (short) c.lhsargs.length);

        // maximum arity
        if (arity < c.getArity()) {
            arity = c.getArity();
        }

        // maximum number of variables
        if (maxvarnum < c.getVarnum()) {
            maxvarnum = c.getVarnum();
        }

        // add the clause
        if (!getClausesByLhsLabel().containsKey(c.lhsname))
            getClausesByLhsLabel().put(c.lhsname, new HashSet<Clause>());
        // if clause is new
        if (!getClausesByLhsLabel().get(c.lhsname).contains(c)) {
            // add clause
            clauses.add(c);
            getClausesByLhsLabel().get(c.lhsname).add(c);
            c.setId(clausecnt++);
        } else {
            // not new: update origins and gorn addresses
            for (Clause oc : getClausesByLhsLabel().get(c.lhsname)) {
                if (c.equals(oc)) {
                    oc.getOrigins().addAll(c.getOrigins());
                    oc.getGorn().addAll(c.getGorn());
                    break;
                }
            }
        }

    }

    /**
     * Get the clauses
     * 
     * @return The clauses.
     */
    public List<Clause> getClauses() {
        return clauses;
    }

    /**
     * Get the clauses for a certain LHS label
     * 
     * @param label
     *            The LHS label
     * @return The list of clauses for the label.
     */
    public Set<Clause> getClausesForLabel(Integer label) {
        return getClausesByLhsLabel().get(label);
    }

    /**
     * Get the arity of this grammar.
     * 
     * @return The corresponding value.
     */
    public int getArity() {
        return arity;
    }

    /**
     * Get the arity of a certain non-terminal
     * 
     * @param state
     *            The nonterminal
     * @return The arity. -1 if the symbol is not in the grammar.
     */
    public int getArity(Integer state) {
        if (stateToArity.containsKey(state)) {
            return stateToArity.get(state);
        }
        return -1;
    }

    /**
     * True if the start predicate is defined (not null)
     * 
     * @return The corresponding value.
     */
    public boolean startPredicateDefined() {
        return startPredicateLabel != null;
    }

    /**
     * Get the start predicate.
     * 
     * @return The corresponding value.
     */
    public Integer getStartPredLabel() {
        return startPredicateLabel;
    }

    /**
     * Set the start predicate.
     * 
     * @param startPredicate
     *            The corresponding value.
     */
    public void setStartPredLabel(Integer startPredicate) {
        this.startPredicateLabel = startPredicate.intValue();
    }

    /**
     * Get the numberer of this grammar.
     * 
     * @return The corresponding value.
     */
    public Numberer getNumberer() {
        return nb;
    }

    /**
     * Some human-readable statistics about this grammar.
     * 
     * @return A string containing the statistics.
     */
    public String stats() {
        return "Clauses: " + clausecnt + "\nLabels: "
                + getClausesByLhsLabel().size() + "\nMax arity: " + arity
                + "\nMax varnum: " + maxvarnum + "\n";
    }

    @Override
    public String toString() {
        String ret = arity + "-RCG:\n";
        ret += "Start predicate: "
                + nb.getObjectWithId(GrammarConstants.PREDLABEL,
                        startPredicateLabel) + "\n";
        for (Integer labeln : getClausesByLhsLabel().keySet()) {
            for (Clause c : getClausesByLhsLabel().get(labeln))
                ret += getClauseOccurrenceCount(c) + " " + c.getScore() + " "
                        + c.print(nb) + "\n";
        }
        ret = ret.trim();
        return ret;
    }

}
