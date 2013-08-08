/*******************************************************************************
 * File BinaryClauseTwo.java
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
package de.tuebingen.rparse.grammar.fanouttwo;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.misc.Numberer;

/**
 * An efficient representation for a clause of a (2,2)-SRCG
 * 
 * @author wmaier
 * 
 */
public class BinaryClauseTwo {

	public double score;

	public int type;

	public int lhs;

	public int lc;

	public int rc;

	public BinaryClause orig;

	public Numberer n;

	private BinaryClauseTwo(BinaryClause c, Numberer n) throws GrammarException {
		orig = c;
		this.n = n;
	}

	public static BinaryClauseTwo constructClause(BinaryClause bc)
			throws GrammarException {
		BinaryClauseTwo ret = new BinaryClauseTwo(bc, bc.n);
		// set the labels
		ret.lhs = bc.lhs;
		ret.lc = bc.lc;
		ret.rc = bc.rc;
		// set the score
		ret.score = bc.score;
		// determine the type from yield function
		/*
		 * 1 A(X) -> B(X) 3 A(XY) -> B(X) C(Y) 5 A(XYZ) -> B(X,Z) C(Y)
		 * 
		 * 2 A(X,Y) -> B(X,Y) 4 A(X,Y) -> B(X) C(Y) 6 A(X,YZ) -> B(X,Y) C(Z) 7
		 * A(X,YZ) -> B(X,Z) C(Y) 12 A(X,YZU) -> B(X,Z) C(Y,U)
		 * 
		 * 8 A(XY,Z) -> B(X,Z) C(Y) 9 A(XY,Z) -> B(X) C(Y,Z) 10 A(XY,ZU) ->
		 * B(X,Z) C(Y,U) 11 A(XY,ZU) -> B(X,U) C(Y,Z)
		 * 
		 * 13 A(XYZ,U) -> B(X,Z) C(Y,U)
		 * 
		 * 14 A(XYZU) -> B(X,Z) C(Y,U)
		 */
		if (bc.yf.length == 1) {
			if (bc.yf[0].length == 1) {
				ret.type = 1;
			} else if (bc.yf[0].length == 2) {
				ret.type = 3;
			} else if (bc.yf[0].length == 3) {
				ret.type = 5;
			} else if (bc.yf[0].length == 4) {
				ret.type = 14;
			} else {
				throw new GrammarException(
						"Cannot construct fanout-two clause: more than 4 vars in arg 1 of lhs");
			}
		} else if (bc.yf.length == 2) {
			if (bc.yf[0].length == 1) {
				if (bc.yf[1].length == 1) {
					/* 2 A(X,Y) -> B(X,Y) */
					/* 4 A(X,Y) -> B(X) C(Y) */
					if (bc.rc == -1) {
						ret.type = 2;
					} else {
						ret.type = 4;
					}
				} else if (bc.yf[1].length == 2) {
					/* 6 A(X,YZ) -> B(X,Y) C(Z) */
					/* 7 A(X,YZ) -> B(X,Z) C(Y) */
					if (bc.yf[1][1]) { // true: z from rc
						ret.type = 6;
					} else {
						ret.type = 7;
					}
				} else if (bc.yf[1].length == 3) {
					ret.type = 12;
				} else {
					throw new GrammarException(
							"Cannot construct fanout-two clause");
				}
			} else if (bc.yf[0].length == 2) {
				if (bc.yf[1].length == 1) {
					/* 8 A(XY,Z) -> B(X,Z) C(Y) */
					/* 9 A(XY,Z) -> B(X) C(Y,Z) */
					if (bc.yf[1][0]) { // true: Z coming from rc
						ret.type = 9;
					} else {
						ret.type = 8;
					}
				} else if (bc.yf[1].length == 2) {
					/* 10 A(XY,ZU) -> B(X,Z) C(Y,U) */
					/* 11 A(XY,ZU) -> B(X,U) C(Y,Z) */
					if (bc.yf[1][1]) { // true: U coming from rc
						ret.type = 10;
					} else {
						ret.type = 11;
					}
				}
			} else if (bc.yf[0].length == 3) {
				ret.type = 13;
			} else {
				throw new GrammarException(
						"Cannot construct fanout-two clause: more than 3 vars in arg 1 of lhs");
			}
		} else {
			throw new GrammarException(
					"Cannot construct fanout-two clause from clause with fanout > 2");
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lc;
		result = prime * result + lhs;
		result = prime * result + rc;
		result = prime * result + type;
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
		BinaryClauseTwo other = (BinaryClauseTwo) obj;
		if (lc != other.lc)
			return false;
		if (lhs != other.lhs)
			return false;
		if (rc != other.rc)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

}
