/*******************************************************************************
 * File HeadFinderFactory.java
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
package de.tuebingen.rparse.treebank.constituent;

import java.io.IOException;

import de.tuebingen.rparse.treebank.UnknownTaskException;
import de.tuebingen.rparse.treebank.constituent.negra.NegraHeadFinder;

public class HeadFinderFactory {

	public static HeadFinder<Tree> getHeadFinder(String params) throws UnknownTaskException, IOException {
		
		if (params.startsWith(HeadFinderTypes.NEGRA)) 
			return new NegraHeadFinder(params);

		if (params.startsWith(HeadFinderTypes.LABEL))
			return new LabelHeadFinder(params);

		throw new UnknownTaskException(params);
		
		
	}
	
}
