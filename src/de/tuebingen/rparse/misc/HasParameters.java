/*******************************************************************************
 * File HasParameters.java
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
 * An interface which makes passing parameters to a class less of a mess. The convention is to define the parameters in
 * {@code setupParameters} and to parse them. The {@code getParameters()} method can mainly be used for
 * self-documentation, i.e., usage information can pull information about available options from there. This way no
 * documentation has to be updated when updating options. The good thing is that we can see through sub-classes what it
 * is happening with the parameter string.
 * 
 * @author wmaier
 */
public interface HasParameters {

    /**
     * Get the underlying Parameters instance
     * 
     * @return the parameters
     * @throws ParameterException
     */
    public ClassParameters getParameters() throws ParameterException;

}
