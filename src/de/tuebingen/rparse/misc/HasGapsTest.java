/*******************************************************************************
 * File HasGapsTest.java
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

import de.tuebingen.rparse.treebank.HasGapDegree;

/**
 * This is a test for classes which implement the HasGapDegree interface. It basically reflects what their GetGapDegree
 * method returns.
 * 
 * @author wmaier
 * @param <S>
 */
public class HasGapsTest<S extends HasGapDegree> implements Test<S> {

    private boolean hasGaps;

    public HasGapsTest(boolean hasGaps) {
        this.hasGaps = hasGaps;
    }

    @Override
    public boolean test(S object) {
        return hasGaps ? object.getGapDegree() > 0 : object.getGapDegree() == 0;
    }

}
