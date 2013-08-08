/*******************************************************************************
 * File GrammarException.java
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

/**
 * Exception for something related with grammar processing.
 * 
 * @author wmaier
 */
public class GrammarException extends Exception {

    public GrammarException() {
    }

    public GrammarException(String arg0) {
        super(arg0);
    }

    public GrammarException(Throwable arg0) {
        super(arg0);
    }

    public GrammarException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    private static final long serialVersionUID = 1L;

}
