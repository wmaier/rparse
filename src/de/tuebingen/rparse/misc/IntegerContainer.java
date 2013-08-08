/*******************************************************************************
 * File IntegerContainer.java
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
 * This is used for the obvious task of getting an integer value out of a method parameter after returning from the
 * method.
 * 
 * @author wmaier
 */
public class IntegerContainer {

    /**
     * Initialize the container.
     * 
     * @param i
     */
    public IntegerContainer(int i) {
        this.i = i;
    }

    /**
     * The value of the container.
     */
    public int i;

}