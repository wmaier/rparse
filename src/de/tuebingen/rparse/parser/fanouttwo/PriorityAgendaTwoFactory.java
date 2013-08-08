/*******************************************************************************
 * File PriorityAgendaTwoFactory.java
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

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.parser.PriorityAgendaTypes;
import de.tuebingen.rparse.treebank.UnknownFormatException;

/**
 * A factory for priority agendas.
 * 
 * @author wmaier
 */
public class PriorityAgendaTwoFactory {

    /**
     * Get a priority agenda.
     * 
     * @param type
     *            The type of it.
     * @param nb
     *            A numberer.
     * @return The agenda.
     * @throws UnknownFormatException
     */
    public static PriorityAgendaTwo getPriorityAgenda(String type, Numberer nb)
            throws UnknownFormatException {

        if (PriorityAgendaTypes.FIBONACCI.equals(type)) {
            return new PriorityAgendaTwoFibonacci(nb);
        }

        throw new UnknownFormatException(type);

    }

}
