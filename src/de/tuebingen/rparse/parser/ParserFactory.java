/*******************************************************************************
 * File ParserFactory.java
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

import de.tuebingen.rparse.grammar.GrammarException;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.parser.fanouttwo.CYKParserTwo;

public class ParserFactory {

    /**
     * Get an RCG parser
     * @param type The type of parser, currently defined by the type of priority agenda used
     * @param pd The parser data (grammars, etc.), see {@link ParserData}
     * @param nb A numberer
     * @return The parser
     * @throws GrammarException If the grammar is not binarized or similar errors occur
     */
	public static RCGParser getParser(String type, ParserData pd, Numberer nb) throws GrammarException {
		
		System.err.println("type is " + type );
		
		// Uses PriorityQueue with remove+insert to simulate decreaseKey
        if (ParsingTypes.RCG_CYK_NAIVE.equals(type)) {
            return new CYKParser(pd, PriorityAgendaTypes.NAIVE, nb);
        }

        // Uses jgrapht FibonacciQueue
        if (ParsingTypes.RCG_CYK_FIBO.equals(type)) {
            return new CYKParser(pd, PriorityAgendaTypes.FIBONACCI, nb);
        }
        
        // Uses the parser for (2,2)-LCFRS
        if (ParsingTypes.RCGTWO_CYK.equals(type)) {
        	return new CYKParserTwo(pd, PriorityAgendaTypes.FIBONACCI, nb);
        }
		
		throw new UnsupportedOperationException(type + " not known");
		
	}
	
}
