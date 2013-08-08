/*******************************************************************************
 * File BracketSquarer.java
 * 
 * Authors:
 *    Kilian Evang
 *    
 * Copyright:
 *    Kilian Evang, 2011
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
package de.tuebingen.rparse.treebank.lex;

import java.util.HashMap;
import java.util.Map;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Maps parenthesis to square brackets in the parser input. This is important if one wants to produce parser output in
 * PTB mrg format or something, where parenthesis are used for bracketing of constituents.
 * 
 * @author wmaier
 */
public class BracketSquarer extends WordTagTranslator {

    private static final Map<String, String> MAP = new HashMap<String, String>();

    static {
        MAP.put("(", "[");
        MAP.put(")", "]");
    }

    public BracketSquarer(Numberer nb) {
        super(nb);
    }

    @Override
    protected Map<String, String> getMap() {
        return MAP;
    }

}
