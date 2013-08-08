/*******************************************************************************
 * File Clause.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;

/**
 * Implements a single clause of a simple RCG.
 * 
 * @author wmaier
 */
public class Clause implements Serializable {

    /*
     * Policy: Only those fields are public which are essential for parsing (performance issue)
     */

    // for serializing
    private static final long serialVersionUID = 2688111807112101449L;

    // I. ESSENTIAL FIELDS ******************************************

    /**
     * LHS predicate name
     */
    public int                lhsname;

    /**
     * LHS arguments, variables. Position i,j contains the jth variable of the ith argument. No terminals in the
     * argument (assumption about treebank parsing), integers used for variables.
     */
    public int[][]            lhsargs;

    /**
     * Names of the RHS predicates
     */
    public int[]              rhsnames;

    /**
     * Arguments of the RHS predicates. Since every argument can only be one variable long, position i,j can represent
     * the jth argument of the ith RHS predicate.
     */
    public int[][]            rhsargs;

    // The head of the RHS
    private int               headpos          = -1;

    // The probability of the clause. In reality, we don't really need this field for parsing, since
    // we only use the binary grammar. Still, it's nice to have.
    private double            score;

    // Origins of this clause (sentence numbers)
    private List<Integer>     origins;

    // Gorn addresses indicating depths where this clause occurrs
    private List<Integer>     gorn;

    // II. CALCULATED FIELDS **************************************

    // true for all RHS predicates which are preterminals
    private boolean[]         rhspterm;

    // III. BINARIZATION-MANAGED FIELDS ****************************

    // this contains the {@code rhspterm} of the original non-binary clause
    private boolean[]         origRhspterm;

    // the {@code rhsnames} of the original non-binary clause
    private int[]             origRhsnames;

    // the vertical context for markovization (set dynamically)
    private String            verticalContext;

    // IV. OTHER FIELDS *******************************************

    // If this field is provided by the grammar, it is used as a hash code, because
    // it is supposed to be unique
    private int               id;

    /**
     * The regular constructor
     * 
     * @param rank
     *            Clause rank
     */
    public Clause(int rank) {
        lhsname = Integer.MIN_VALUE;
        lhsargs = null;
        rhsnames = new int[rank];
        rhsargs = new int[rank][];
        headpos = -1;
        score = Double.NEGATIVE_INFINITY;
        origins = new ArrayList<Integer>();
        gorn = new ArrayList<Integer>();
        rhspterm = new boolean[rank];
        origRhspterm = null;
        origRhsnames = null;
        verticalContext = "";
        id = -1;
    }

    /**
     * Constructor with parameters for all fields. Does not complain if null is passed for any field, watch out.
     * 
     * @param lhsname
     *            The name of the LHS
     * @param lhsargs
     *            The arguments of the LHS
     * @param rhsnames
     *            The names of the RHS predicates
     * @param rhsargs
     *            The arguments of the RHS predicates
     * @param headpos
     *            The head child index
     * @param score
     *            The score of the clause
     * @param sorigin
     *            A list of integers denoting the sentence numbers where this clause has been found
     * @param gorn
     *            A list of integers denoting the gorn addresses denoting the depths in which this clause has been found
     * @param rhspterm
     *            A boolean array where position i is true iff the ith RHS predicate is a preterminal
     * @param origRhspterm
     *            Binarized clause: rhspterm array of the unbinarized clause
     * @param origRhsnames
     *            Binarized clause: rhsnames of the unbinarized clause
     * @param id
     *            A unique id of the clause, default id -1
     */
    public Clause(int lhsname, int[][] lhsargs, int[] rhsnames,
            int[][] rhsargs, int headpos, double score, List<Integer> sorigin,
            List<Integer> gorn, boolean[] rhspterm, boolean[] origRhspterm,
            int[] origRhsnames, int id) {
        this.lhsname = lhsname;
        this.lhsargs = lhsargs == null ? null : Utilities
                .arrayDeepCopy(lhsargs);
        this.rhsnames = rhsnames == null ? null : Arrays.copyOf(rhsnames,
                rhsnames.length);
        this.rhsargs = rhsargs == null ? null : Utilities
                .arrayDeepCopy(rhsargs);
        this.headpos = headpos;
        this.score = score;
        this.origins = origins == null ? null : new ArrayList<Integer>(sorigin);
        this.gorn = gorn == null ? null : new ArrayList<Integer>(gorn);
        this.rhspterm = rhspterm == null ? null : Arrays.copyOf(rhspterm,
                rhspterm.length);
        this.origRhspterm = origRhspterm == null ? null : Arrays.copyOf(
                origRhspterm, origRhspterm.length);
        this.origRhsnames = origRhsnames == null ? null : Arrays.copyOf(
                origRhsnames, origRhsnames.length);
        verticalContext = "";
        this.id = id;
    }

    /**
     * A proper copy constructor
     * 
     * @param c
     *            The clause to copy from
     */
    public Clause(Clause c) {
        this(c.lhsname, c.lhsargs, c.rhsnames, c.rhsargs, c.headpos, c.score,
                c.origins, c.gorn, c.getRhspterm(), c.getOrigRhspterm(), c
                        .getOrigRhsnames(), c.id);
    }

    /**
     * Set the id of this clause
     * 
     * @param id
     *            The corresponding value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the number of variables on the LHS of this clause
     * 
     * @return The corresponding value
     */
    public int getVarnum() {
        int lhsvarnum = 0;
        for (int i = 0; i < lhsargs.length; ++i) {
            lhsvarnum += lhsargs[i].length;
        }
        return lhsvarnum;
    }

    /**
     * Get the arity of this clause
     * 
     * @return The corresponding value
     */
    public int getArity() {
        return lhsargs.length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(lhsargs);
        result = prime * result + lhsname;
        result = prime * result + Arrays.deepHashCode(rhsargs);
        result = prime * result + Arrays.hashCode(rhsnames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Clause other = (Clause) obj;
        if (!Arrays.deepEquals(lhsargs, other.lhsargs))
            return false;
        if (lhsname != other.lhsname)
            return false;
        if (!Arrays.deepEquals(rhsargs, other.rhsargs))
            return false;
        if (!Arrays.equals(rhsnames, other.rhsnames))
            return false;
        return true;
    }

    /**
     * A Q&D representation of this clause without proper predicate names
     */
    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append(lhsname);
        ret.append("(");
        for (int i = 0; i < lhsargs.length; ++i) {
            for (int j = 0; j < lhsargs[i].length; ++j) {
                ret.append("[");
                ret.append(lhsargs[i][j]);
                ret.append("]");
            }
            if (i < lhsargs.length - 1)
                ret.append(GrammarConstants.ARG_SEP);
        }
        ret.append(") " + GrammarConstants.LHSRHS_SEP);
        for (int i = 0; i < rhsargs.length; ++i) {
            ret.append(rhsnames[i]);
            ret.append("(");
            for (int j = 0; j < rhsargs[i].length; ++j) {
                ret.append("[");
                ret.append(rhsargs[i][j]);
                ret.append("]");
                if (j < rhsargs[i].length - 1)
                    ret.append(GrammarConstants.ARG_SEP);
            }
            ret.append(")");
        }
        return ret.toString();

    }

    /**
     * Return a human-readable representation of this clause
     * 
     * @param nb
     *            A numberer which contains the predicate names
     * @return The corresponding string
     */
    public String print(Numberer nb) {
        String ret = nb.getObjectWithId(GrammarConstants.PREDLABEL, lhsname)
                .toString();
        ret += "(";
        for (int i = 0; i < lhsargs.length; ++i) {
            for (int j = 0; j < lhsargs[i].length; ++j) {
                ret += "[" + lhsargs[i][j] + "]";
            }
            if (i < lhsargs.length - 1)
                ret += GrammarConstants.ARG_SEP;
        }
        ret += ") " + GrammarConstants.LHSRHS_SEP;
        for (int i = 0; i < rhsargs.length; ++i) {
            ret += " ";
            ret += nb.getObjectWithId(GrammarConstants.PREDLABEL, rhsnames[i]);
            if (headpos == i)
                ret += GrammarConstants.HEAD_MARKER;
            ret += "(";
            for (int j = 0; j < rhsargs[i].length; ++j) {
                ret += "[" + rhsargs[i][j] + "]";
                if (j < rhsargs[i].length - 1)
                    ret += GrammarConstants.ARG_SEP;
            }
            ret += ")";
        }
        return ret;
    }

    /**
     * Get the head daughter
     * 
     * @return The corresponding value
     * @throws IllegalStateException
     *             if no head daughter is marked
     */
    public int getHeadPos() {
        if (headpos == -1)
            throw new IllegalStateException(
                    "Cannot get head daugther: No head marked");
        return headpos;
    }

    /**
     * Set the head daughter
     * 
     * @param hp
     *            The corresponding value
     */
    public void setHeadPos(int hp) {
        headpos = hp;
    }

    /**
     * Set the vertical context of this clause
     * 
     * @param vertical
     *            The corresponding value
     */
    public void setVertical(String vertical) {
        verticalContext = vertical;
    }

    /**
     * Get the vertical context of this clause
     * 
     * @return The corresponding value
     */
    public String getVerticalContext() {
        return verticalContext;
    }

    /**
     * Given the set of preterminal predicate names in the whole grammar, sets this clause's {@link rhspterm} fields.
     * 
     * @param preterms
     *            The list of preterminals from the grammar
     */
    public void setPreterms(Set<Integer> preterms) {
        rhspterm = new boolean[rhsnames.length];
        for (int i = 0; i < rhsnames.length; i++) {
            if (preterms.contains(rhsnames[i])) {
                rhspterm[i] = true;
            }
        }
    }

    /**
     * Get the rhspterm array, in which position i is true iff the ith RHS predicate is a preterminal
     * 
     * @return the corresponding value
     */
    public boolean[] getRhspterm() {
        return rhspterm;
    }

    /**
     * Set the rhspterm array, in which position i is true iff the ith RHS predicate is a preterminal.
     * 
     * @param rhspterm
     *            the corresponding value
     */
    public void setRhspterm(boolean[] rhspterm) {
        this.rhspterm = rhspterm;
    }

    /**
     * Add a gorn address where this clause has been found.
     * 
     * @param address
     *            The corresponding value
     */
    public void addGorn(int address) {
        gorn.add(address);
    }

    /**
     * Get all gorn addresses where this clause has been found.
     * 
     * @return The list of gorn addresses. Integers, to be mapped back using a suitable numberer.
     */
    public List<Integer> getGorn() {
        return gorn;
    }

    /**
     * Add a sentence number for a sentence in which this clause has been found
     * 
     * @param origin
     *            The corresponding value
     */
    public void addOrigin(int origin) {
        origins.add(origin);
    }

    /**
     * Get the numbers of the sentences in which this clause has been found.
     * 
     * @return The corresponding value
     */
    public List<Integer> getOrigins() {
        return origins;
    }

    /**
     * Set the numbers of the sentences in which this clause has been found.
     * 
     * @param origins
     *            The correponding list.
     */
    public void setOrigins(List<Integer> origins) {
        this.origins = origins;
    }

    /**
     * Get the score of the clause.
     * 
     * @return The corresponding value
     */
    public double getScore() {
        return score;
    }

    /**
     * Set the score of the clause
     * 
     * @param score
     *            The corresponding value.
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * Binarized clause: Get the {@code rhspterm} array of the original unbinarized clause.
     * 
     * @return The corresponding value.
     */
    public boolean[] getOrigRhspterm() {
        return origRhspterm;
    }

    /**
     * Binarized clause: Set the {@code rhspterm} array of the original unbinarized clause.
     * 
     * @param origRhspterm
     *            The corresponding value.
     */
    public void setOrigRhspterm(boolean[] origRhspterm) {
        this.origRhspterm = origRhspterm;
    }

    /**
     * Binarized clause: Get the {@code rhsnames} of the original unbinarized clause.
     * 
     * @return The corresponding value.
     */
    public int[] getOrigRhsnames() {
        return origRhsnames;
    }

    /**
     * Binarized clause: Set the {@code rhsnames} of the original unbinarized clause.
     * 
     * @param origRhsnames
     *            The corresponding value.
     */
    public void setOrigRhsnames(int[] origRhsnames) {
        this.origRhsnames = origRhsnames;
    }

}
