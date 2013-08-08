/*******************************************************************************
 * File Ranges.java
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
package de.tuebingen.rparse.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This provides a way of getting an iterator over integers out of a simple range notation as it is used, e.g., in
 * "print file" dialogs.
 * 
 * @author wmaier
 */
public class Ranges implements Iterable<Integer>, Iterator<Integer> {

    private int             val;

    private int             pos;

    /**
     * Lowest bound of ranges (1)
     */
    public static final int LOWERBOUND = 1;

    private String          rangestr;

    private List<Integer>   leftBounds;

    private List<Integer>   rightBounds;

    public Ranges(String rangestr) {
        if (rangestr == null) {
            rangestr = "";
        }

        this.rangestr = rangestr;
        leftBounds = new ArrayList<Integer>();
        rightBounds = new ArrayList<Integer>();

        // parse ranges string and check for plausibility
        if ("".equals(rangestr)) {
            leftBounds.add(0);
            rightBounds.add(Integer.MAX_VALUE);
        } else {
            String[] ranges = rangestr.split(",");

            for (int i = 0; i < ranges.length; ++i) {
                int left = LOWERBOUND;
                int right = Integer.MAX_VALUE;
                int dashpos = ranges[i].indexOf('-');

                try {
                    if (dashpos == -1) {
                        left = right = Integer.parseInt(ranges[i]);
                    } else if (dashpos == 0) {
                        right = Integer.parseInt(ranges[i]
                                .substring(dashpos + 1));
                    } else if (dashpos == ranges[i].length() - 1) {
                        left = Integer
                                .parseInt(ranges[i].substring(0, dashpos));
                    } else {
                        left = Integer
                                .parseInt(ranges[i].substring(0, dashpos));
                        right = Integer.parseInt(ranges[i]
                                .substring(dashpos + 1));
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Cannot interpret range string '" + ranges[i] + "'");
                }

                if (left < 0 || right < 0 || left > right) {
                    throw new IllegalArgumentException(
                            "Cannot interpret range string '" + ranges[i] + "'");
                }

                leftBounds.add(left);
                rightBounds.add(right);
            }
        }
        Collections.sort(leftBounds);
        Collections.sort(rightBounds);
        val = leftBounds.get(0);
    }

    /**
     * True if the range is not restricting (includes everything between {@code LOWERBOUND} and infinity).
     * 
     * @return The corresponding boolean value.
     */
    public boolean isUnbounded() {
        return "1-".equals(getRangeString());
    }

    @Override
    public Iterator<Integer> iterator() {
        pos = 0;
        val = leftBounds.get(pos);
        return this;
    }

    @Override
    public boolean hasNext() {
        return pos < leftBounds.size();
    }

    @Override
    public Integer next() {
        int ret;

        if (leftBounds.get(pos) <= val && val < rightBounds.get(pos)) {
            ret = val;
            val++;
        } else if (val == rightBounds.get(pos)) {
            ret = val;
            pos++;
            if (pos < leftBounds.size())
                val = leftBounds.get(pos) > val ? leftBounds.get(pos) : val + 1;
        } else {
            throw new NoSuchElementException(val + ": No such element.");
        }

        return ret;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Removing elements on "
                + getClass().toString() + " is not supported");
    }

    @Override
    public String toString() {
        return rangestr;
    }

    /**
     * The original string passed to the instance in the constructor.
     * 
     * @return The corresponding string.
     */
    public String getRangeString() {
        return rangestr;
    }

}
