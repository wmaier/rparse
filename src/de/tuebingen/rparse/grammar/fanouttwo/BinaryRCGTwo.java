/*******************************************************************************
 * File BinaryRCGTwo.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tuebingen.rparse.grammar.BinaryClause;
import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.grammar.binarize.Binarizer;

/**
 * A 2-RCG with fanout two, supports illnestedness
 * 
 * @author wmaier
 */
public class BinaryRCGTwo extends BinaryRCG {

    private static final long                  serialVersionUID = 3056798171199391224L;

    public List<BinaryClauseTwo>               twoCl;
    public Map<Integer, List<BinaryClauseTwo>> twoClByLc;
    public Map<Integer, List<BinaryClauseTwo>> twoClByRc;

    private Map<Integer, Integer>              typeCount;

    private BinaryRCGTwo(RCG g, Class<? extends Binarizer> binarizationType) {
        super(g, binarizationType);
        twoClByLc = new HashMap<Integer, List<BinaryClauseTwo>>();
        twoClByRc = new HashMap<Integer, List<BinaryClauseTwo>>();
        twoCl = new ArrayList<BinaryClauseTwo>();
    }

    /**
     * Compile a (2,2)-RCG into a more efficient representation
     * 
     * @param bg
     *            The binary RCG.
     * @return The more efficient Representation.
     * @throws GrammarException
     *             If something goes wrong during the conversion
     */
    public static BinaryRCGTwo buildFromBinaryRCG(BinaryRCG bg)
            throws GrammarException {
        Map<Integer, Integer> typeCount = new HashMap<Integer, Integer>();
        if (bg.getArity() > 2) {
            throw new GrammarException(
                    "A BinaryRCGTwo can only be built if the source grammar has fan-out two as well");
        }
        BinaryRCGTwo ret = new BinaryRCGTwo(bg.getGrammar(),
                bg.getBinarizerType());
        for (BinaryClause bc : bg.clauses) {
            BinaryClauseTwo bct;
            try {
                bct = BinaryClauseTwo.constructClause(bc);
            } catch (GrammarException e) {
                throw new GrammarException(e.getMessage());
            }
            if (!typeCount.containsKey(bct.type)) {
                typeCount.put(bct.type, 0);
            }
            typeCount.put(bct.type, typeCount.get(bct.type) + 1);
            if (!(ret.twoCl.contains(bct))) {
                ret.twoCl.add(bct);
                if (!ret.twoClByLc.containsKey(bct.lc))
                    ret.twoClByLc.put(bct.lc, new ArrayList<BinaryClauseTwo>());
                if (!ret.twoClByRc.containsKey(bct.rc))
                    ret.twoClByRc.put(bct.rc, new ArrayList<BinaryClauseTwo>());
                ret.twoClByLc.get(bct.lc).add(bct);
                ret.twoClByRc.get(bct.rc).add(bct);
            }
        }
        ret.typeCount = typeCount;
        return ret;
    }

    public String printStats() {
        String ret = "";
        for (int type : typeCount.keySet()) {
            ret += type + ": " + typeCount.get(type) + " times\n";
        }
        return ret;
    }

}
