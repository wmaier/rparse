/*******************************************************************************
 * File ConstituentInputFormats.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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
package de.tuebingen.rparse.treebank.constituent.process;

/**
 * A collection of format constants.
 * 
 * @author ke, wmaier
 *
 */
public class ConstituentInputFormats {
	
    /**
     * NeGra export format (see Skut et al. (1997))
     */
	public static final String EXPORT = "export";

    /**
     * Any bracketed format like Penn Treebank MRG, >= 1 lines per sentences
     */
	public static final String MRG = "mrg";

	/**
	 * TreeTagger output format
	 */
	public static final String TREETAGGER = "treetagger";


}
