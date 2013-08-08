/*******************************************************************************
 * File ConstituentTestsFactory.java
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

import de.tuebingen.rparse.misc.HasGapsTest;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.PassTest;
import de.tuebingen.rparse.misc.Test;
import de.tuebingen.rparse.treebank.SyntacticStructureTests;
import de.tuebingen.rparse.treebank.UnknownTaskException;

/**
 * A factory from which one can get tests that are applicable on constituency trees.
 * 
 * @author wmaier
 */
public class ConstituentTestsFactory {

    /**
     * Get a test for a constituency structure.
     * 
     * @param test
     *            The test type
     * @param nb
     *            A numberer for the tree labels
     * @return The corresponding test.
     * @throws UnknownTaskException
     *             If the test is not known.
     */
    public static final Test<Tree> getTest(String test, Numberer nb)
            throws UnknownTaskException {

        if (SyntacticStructureTests.ALL.equals(test)) {
            return new PassTest<Tree>();
        }

        if (SyntacticStructureTests.HAS_GAPS.equals(test)) {
            return new HasGapsTest<Tree>(true);
        }

        if (SyntacticStructureTests.HAS_NO_GAPS.equals(test)) {
            return new HasGapsTest<Tree>(false);
        }

        throw new UnknownTaskException(test + ": no such test.");

    }

}
