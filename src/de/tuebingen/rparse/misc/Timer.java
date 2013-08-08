/*******************************************************************************
 * File Timer.java
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
package de.tuebingen.rparse.misc;

/**
 * A simple timer for measuring parsing times and such.
 * 
 * @author wmaier
 */
public class Timer {

    private long starttime;

    /**
     * Start to measure time by remembering the current system time.
     */
    public void start() {
        starttime = System.nanoTime();
    }

    /**
     * Reset the timer by setting the remembered system time to 0.
     */
    public void reset() {
        starttime = 0;
    }

    /**
     * Get the time which has elapsed between calling start and the current system time. No sense calling this before
     * calling start, of course.
     * 
     * @return The corresponding value.
     */
    public String time() {
        long ptime = System.nanoTime() - starttime;
        return String.valueOf((ptime) / (Math.pow(10, 9))) + " sec.";
    }

}
