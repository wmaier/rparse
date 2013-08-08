/*******************************************************************************
 * File PriorityAgendaTwo.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2013
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
package de.tuebingen.rparse.parser.fanouttwo;

/**
 * An interface for an agenda to be used for weighted deductive parsing. Behavior is undefined if keys are used which
 * are < 0.
 * 
 * @author wmaier
 */
public interface PriorityAgendaTwo {

    /**
     * Gets the best item from the agenda
     * 
     * @return The corresponding item
     */
    public CYKItemTwo poll();

    /**
     * Pushes a new item, does update if approriate.
     * 
     * @param it
     *            The item to push
     */
    public void push(CYKItemTwo it);

    /**
     * Returns stats about the usage of the agenda.
     * 
     * @return The statistics as a string.
     */
    public String getStats();

    /**
     * The current size of the agenda.
     * 
     * @return The corresponding value.
     */
    public int size();

    /**
     * True if the agenda is empty.
     * 
     * @return
     */
    public boolean isEmpty();

}
