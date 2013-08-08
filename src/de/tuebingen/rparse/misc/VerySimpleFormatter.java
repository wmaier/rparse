/*******************************************************************************
 * File VerySimpleFormatter.java
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

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Offers a formatter for log information. Essentially does the same thing as {@link SimpleFormatter}, with the
 * difference that below {@link Level.INFO}, no class information is being output.
 * 
 * @author wmaier
 */
public class VerySimpleFormatter extends SimpleFormatter {

    @Override
    public String format(LogRecord r) {
        StringBuffer sb = new StringBuffer();
        if (Level.SEVERE.equals(r.getLevel())) {
            sb.append("SEVERE: ");
        } else if (Level.WARNING.equals(r.getLevel())) {
            sb.append("WARNING: ");
        }
        sb.append(r.getMessage());
        sb.append("\n");
        return sb.toString();
    }

}
