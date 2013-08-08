/*******************************************************************************
 * File CYKChart.java
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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The chart for the CYK parser. It's just an HashMap extended with some convenience methods.
 * 
 * @author wmaier
 */
public class CYKChart extends HashMap<Integer, Map<BitSet, CYKItem>> {

    // just because of the hashmap, this one does not get serialized.
    private static final long serialVersionUID = 1L;

    private Logger            logger;

    /**
     * Constructor which just gets a logger.
     */
    public CYKChart() {
        logger = Logger.getLogger(CYKChart.class.getPackage().getName());
    }

    /**
     * Add an item to the chart.
     * 
     * @param it
     *            The item.
     */
    public void add(CYKItem it) {
        if (!super.containsKey(it.pl))
            super.put(it.pl, new HashMap<BitSet, CYKItem>());
        if (super.get(it.pl).containsKey(it.rvec)) {
            logger.finer("new item: " + it.toString() + "  + " + it.olc + " + "
                    + it.orc);
            CYKItem oit = super.get(it.pl).get(it.rvec);
            logger.finer("old item: " + oit + " + " + oit.olc + " + " + oit.orc);
            throw new IllegalStateException(
                    "Trying to add an item to the chart which is already in there (there's a bug somewhere)");
        }
        super.get(it.pl).put(it.rvec, it);
    }

    /**
     * Ask if there is an inside score in the chart for a label and a range vector
     * 
     * @param lhs
     *            The label
     * @param yp
     *            The range vector
     * @return True iff there is a score for the input values.
     */
    public boolean hasScore(int lhs, BitSet yp) {
        return super.containsKey(lhs) && super.get(lhs).containsKey(yp);
    }

    /**
     * Get the inside score for a label and a range vector
     * 
     * @param lhs
     *            The label
     * @param yp
     *            The range vector
     * @return The score from the chart, -\infty if there is no such score in the chart.
     */
    public double getScore(int lhs, BitSet yp) {
        if (super.containsKey(lhs) && super.get(lhs).containsKey(yp))
            return super.get(lhs).get(yp).iscore;
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * Get the item instance from the chart
     * 
     * @param it
     *            The item to get
     * @return The item, {@code null} if it is not in the chart.
     */
    public CYKItem getItem(CYKItem it) {
        if (super.containsKey(it.pl) && super.get(it.pl).containsKey(it.rvec))
            return super.get(it.pl).get(it.rvec);
        return null;
    }

}
