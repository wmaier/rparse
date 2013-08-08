/*******************************************************************************
 * File BinaryClause.java
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
import java.util.Arrays;

import de.tuebingen.rparse.misc.Numberer;

/**
 * A more efficient representation of a binary clause, optimized for parsing. The hash code of this class is cached and
 * fields are nevertheless public for efficiency. So watch out, tampering with the fields of an instance of this clause
 * after its creation renders the whole thing unusable.
 * 
 * @author wmaier
 */
public class BinaryClause implements Serializable {

    // for serializing
    private static final long serialVersionUID = -2454347456042697960L;

    /**
     * A numberer for this clause
     */
    public Numberer           n;

    // parsing ****************

    /**
     * The yield function of this clause
     */
    public final boolean[][]  yf;

    /**
     * true if arity is one
     */
    public boolean            iscf;

    /**
     * LHS name
     */
    public final int          lhs;

    /**
     * left RHS name
     */
    public final int          lc;

    /**
     * True if left RHS label is preterminal
     */
    public boolean            lcPt             = false;

    /**
     * Right RHS name
     */
    public final int          rc;

    /**
     * True if right RHS label is preterminal
     */
    public boolean            rcPt             = false;

    /**
     * The probability of this clause
     */
    public double             score;

    /**
     * This is the count of the original clause that this binary clause was created from. Once this binary clause is
     * added to a binary grammar, this field is not meaningful anymore.
     */
    public int                cnt              = 0;

    // grammar ******************

    /**
     * Arity of LHS predicate, in order to sort clauses by arity in the grammar (only used for statistics)
     */
    public int                lhsfanout        = -1;

    /**
     * Grammar projection estimate "F" emulation (for deterministic binariziation only): In the deterministic case,
     * where a unique correspondence exists between binarized clauses and the original clauses, this is the
     * {@link rhspterm} field of the original unbinarized clause
     */
    public boolean[]          unbinarizedTopRhspterm;

    /**
     * Grammar projection estimate "F" emulation (for deterministic binariziation only): In the deterministic case,
     * where a unique correspondence exists between binarized clauses and the original clauses, this is the
     * {@link rhsname} field of the original unbinarized clause
     */
    public int[]              unbinarizedTopRhsnames;

    /*
     * Construct a new BinaryClause from a Clause instance which is binary.
     * 
     * @param clause A Clause instance which is binary.
     * 
     * @param n The numberer for the clause.
     */
    private BinaryClause(Clause clause, Numberer n) throws GrammarException {
        // names of RHS and LHS
        this.lhs = clause.lhsname;
        int lctemp = clause.rhsnames[0];
        this.lcPt = clause.getRhspterm()[0];
        if (clause.rhsnames.length > 2) {
            throw new GrammarException(
                    "Cannot create a binary clause from a clause with |RHS| > 2");
        }
        int rctemp;
        if (clause.rhsnames.length == 2) {
            rctemp = clause.rhsnames[1];
            this.rcPt = clause.getRhspterm()[1];
        } else {
            rctemp = -1;
            this.rcPt = false;
        }
        // check if the arity is one and set CF fields
        this.iscf = clause.lhsargs.length == 1;
        for (int i = 0; i < clause.rhsargs.length && this.iscf; ++i) {
            this.iscf &= clause.rhsargs[i].length == 1;
        }
        this.lhsfanout = clause.lhsargs.length;
        this.unbinarizedTopRhspterm = clause.getOrigRhspterm();
        this.unbinarizedTopRhsnames = clause.getOrigRhsnames();
        // compute yield function for CYK parsing from the variables
        // in the arguments
        yf = new boolean[clause.lhsargs.length][];
        for (int i = 0; i < clause.lhsargs.length; ++i) {
            yf[i] = new boolean[clause.lhsargs[i].length];
            for (int j = 0; j < clause.lhsargs[i].length; ++j) {
                int var = clause.lhsargs[i][j];
                for (int k = 0; k < clause.rhsargs.length; ++k) {
                    for (int l = 0; l < clause.rhsargs[k].length; ++l) {
                        if (clause.rhsargs[k][l] == var) {
                            yf[i][j] = k == 1;
                        }
                    }
                }
            }
        }
        // A --> B C [[true,false]]: swap RHS
        if (yf[0][0] && rctemp != -1) {
            // swap lc and rc
            lc = rctemp;
            rc = lctemp;
            // swap lcpt and rcpt
            boolean tempb = rcPt;
            rcPt = lcPt;
            lcPt = tempb;
            // swap yf
            for (int i = 0; i < yf.length; ++i) {
                for (int j = 0; j < yf[i].length; ++j) {
                    yf[i][j] = !yf[i][j];
                }
            }
        } else {
            lc = lctemp;
            rc = rctemp;
        }
        this.score = clause.getScore();
        // the numberer
        this.n = n;
    }

    /**
     * Get a new clause, to be constructed from a {@link Clause} instance.
     * 
     * @param c
     *            The clause instance to construct this binary clause from
     * @param nb
     *            The numberer of the original clause instance.
     * @return The {@link BinaryClause}.
     * @throws GrammarException
     *             If we cannot create a BinaryClause from the given input
     */
    public static BinaryClause constructClause(Clause c, Numberer nb)
            throws GrammarException {
        return new BinaryClause(c, nb);
    }

    /**
     * Hash code will be cashed since we assume that fields relevant for identity are never changed after the creation
     * of an instance. This is only done by convention - we leave all the fields public anyway to have faster access
     * to them during parsing. So be careful.
     */
    public int hc = -1;

    /**
     * Compute a hash code which will be cached. See comment on {@link hc}.
     */
    @Override
    public int hashCode() {
        if (hc == -1) {
            hc = 5381;
            hc = ((hc << 5) + hc) + lhs;
            hc = ((hc << 5) + hc) + lc;
            hc = ((hc << 5) + hc) + rc;
            hc = ((hc << 5) + hc) + Arrays.deepHashCode(yf);
        }
        return hc;
    }

    @Override
    public boolean equals(Object o) {
        boolean ret = o instanceof BinaryClause;
        if (!ret)
            return false;
        BinaryClause bc = (BinaryClause) o;
        ret = ret && bc.lhs == this.lhs;
        ret = ret && bc.lc == this.lc;
        ret = ret && bc.rc == this.rc;
        ret = ret && Arrays.deepEquals(this.yf, bc.yf);
        return ret;
    }

    @Override
    public String toString() {
        String ret = "";
        String pn = (String) n.getObjectWithId(GrammarConstants.PREDLABEL, lhs);
        String lcn = (String) n.getObjectWithId(GrammarConstants.PREDLABEL, lc);
        String rcn = "";
        if (rc != -1) {
            rcn = (String) n.getObjectWithId(GrammarConstants.PREDLABEL, rc);
        }
        ret = score + ":" + pn + " " + GrammarConstants.LHSRHS_SEP + " " + lcn
                + " " + rcn + " [" + Arrays.deepToString(yf) + "]";
        return ret;
    }

}
