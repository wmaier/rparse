/*******************************************************************************
 * File GapfilterYFComposer.java
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

import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.HasParameters;
import de.tuebingen.rparse.misc.IntegerContainer;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * Same as the "fast" composer, but refrains from creating items which contain gaps exceeding a certain length
 * 
 * @author wmaier
 */
public class GapfilterYFComposer extends YieldFunctionComposer
        implements
            HasParameters {

    private static final long serialVersionUID = 5476813693800303007L;

    private ClassParameters   params;

    protected int             gapmax;

    protected int             gapblockcnt;

    /**
     * Needs parameter "gapmax" with integer value, specifying how long are the gaps to be blocked.
     * 
     * @param paramstring
     *            The parameter string.
     * @throws ParameterException
     *             If there is an error in the parameter string.
     */
    public GapfilterYFComposer(String paramstring) throws ParameterException {
        params = new ClassParameters();
        params.add("gapmax",
                "Block items which have gaps larger than this threshold");
        params.parse(paramstring);
        gapmax = Integer.MAX_VALUE;
        if (params.check("gapmax")) {
            try {
                gapmax = Integer.valueOf(params.getVal("gapmax"));
            } catch (NumberFormatException e) {
            }
        }
        if (gapmax != Integer.MAX_VALUE) {
            logger.info("Will block all items with gaps longer than " + gapmax);
        }
        gapblockcnt = 0;
    }

    @Override
    public String stats() {
        String ret = super.stats();
        ret += " -- " + gapblockcnt + " items blocked";
        return ret;
    }

    @Override
    public void reset() {
        super.reset();
        gapblockcnt = 0;
    }

    @Override
    public BitSet doComposition(CYKItem lit, CYKItem rit, boolean[][] yf,
            IntegerContainer s, IntegerContainer e) {
        boolean doblock = false;
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
                    // additionally, track distance to next argument and return null if exceeding maximum
                    int newlpos = lvec.nextSetBit(lpos);
                    if (newlpos != -1 && newlpos - lpos > gapmax) {
                        gapblockcnt++;
                        return null;
                        // doblock = true;
                    }
                    lpos = newlpos;
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
                    int newrpos = rvec.nextSetBit(rpos);
                    if (newrpos != -1 && newrpos - rpos > gapmax) {
                        gapblockcnt++;
                        return null;
                        // doblock = true;
                    }
                    rpos = newrpos;
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

        if (doblock) {
            // System.err.println("blocked: " + lvec + "-" + rvec);
            return null;
        }

        return yp;
    }

    @Override
    public ClassParameters getParameters() throws ParameterException {
        return params;
    }

}
