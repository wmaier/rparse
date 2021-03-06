/*******************************************************************************
 * File WellnestedYFComposer.java
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

import java.util.BitSet;
import java.util.logging.Logger;

import de.tuebingen.rparse.misc.IntegerContainer;

/**
 * Disallows illnested constituents during parsing. This handles illnestedness in the binarized grammar, which is why it
 * is probably not that interesting.
 * 
 * @author wmaier
 */
public class WellnestedYFComposer extends YieldFunctionComposer {

    private int    gapblockcnt;

    private Logger logger;

    public WellnestedYFComposer() {
        gapblockcnt = 0;
        logger = Logger.getLogger(WellnestedYFComposer.class.getPackage()
                .getName());
    }

    @Override
    public String stats() {
        String ret = super.stats();
        ret += " -- " + gapblockcnt + " items blocked";
        return ret;
    }

    @Override
    public BitSet doComposition(CYKItem lit, CYKItem rit, boolean[][] yf,
            IntegerContainer s, IntegerContainer e) {

        IllnestedDetector illnestedDetector = new IllnestedDetector();

        BitSet lvec = lit.rvec;
        BitSet rvec = rit.rvec;

        // positions with the first set bits in both vectors
        int lpos = lvec.nextSetBit(0);
        int rpos = rvec.nextSetBit(0);
        // loop through yield function
        for (int arg = 0; arg < yf.length; ++arg) {
            for (int argc = 0; argc < yf[arg].length; ++argc) {
                // next argument comes from the left
                if (!yf[arg][argc]) {
                    // we can stop here if one of the following holds:
                    // 1. no set bit left in the left vector (lpos is -1 in this case)
                    // 2. there is a bit left in the right vector which occurs at a position <= the new left position
                    if (lpos == -1 || (rpos != -1 && rpos <= lpos))
                        return null;
                    // we're still here: jump to next hole
                    lpos = lvec.nextClearBit(lpos);
                    // stop here if right vec has element on the left of the left vec next element (overlapping)
                    if (rpos != -1 && rpos < lpos)
                        return null;
                    // if this is the last element of the current argument,
                    // make sure there is a gap (both vectors have unset bit next)
                    // if it's not the last argument, make sure there is NO gap
                    if (argc == yf[arg].length - 1) {
                        if (rvec.get(lpos))
                            return null;
                    } else {
                        if (!rvec.get(lpos))
                            return null;
                    }
                    // we're still here: jump to start of next argument (= next set bit)
                    lpos = lvec.nextSetBit(lpos);
                    // if the fact that this argument comes from the left RHS predicate makes
                    // it illnested, we're done
                    if (illnestedDetector.add(false)) {
                        logger.finest("Blocking composition of "
                                + lvec.toString() + " and " + rvec.toString());
                        gapblockcnt++;
                        return null;
                    }
                }
                // next argument comes from the right
                else {
                    // same as above, the other way round
                    if (rpos == -1 || (lpos != -1 && lpos <= rpos))
                        return null;
                    rpos = rvec.nextClearBit(rpos);
                    if (lpos != -1 && lpos < rpos)
                        return null;
                    if (argc == yf[arg].length - 1) {
                        if (lvec.get(rpos))
                            return null;
                    } else {
                        if (!lvec.get(rpos))
                            return null;
                    }
                    rpos = rvec.nextSetBit(rpos);
                    if (illnestedDetector.add(true)) {
                        logger.finest("Blocking composition of "
                                + lvec.toString() + " and " + rvec.toString());
                        gapblockcnt++;
                        return null;
                    }
                }
            }
        }
        // if there are leftover elements on the left side or the right side, at least one index is not -1
        if (lpos != -1 || rpos != -1) {
            return null;
        }

        // everything ok: return xored vectors
        BitSet yp = (BitSet) lvec.clone();
        yp.xor(rvec);
        return yp;
    }

    private class IllnestedDetector {

        private int     status;
        private boolean lastlr;

        public IllnestedDetector() {
            status = 0;
            lastlr = false;
        }

        // false: from the left, true: from the right
        public boolean add(boolean lr) {
            if (status == 0) {
                status++;
            } else if ((!lastlr && lr) || (lastlr && !lr)) {
                status++;
            }
            lastlr = lr;
            return status >= 4;
        }

    }

    private static final long serialVersionUID = 1888552253859813996L;

}
