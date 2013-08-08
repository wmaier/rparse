/*******************************************************************************
 * File ArrayListMapper.java
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
package de.tuebingen.rparse.misc;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Python-style mapping, for ArrayLists. See doc of the superclass.
 * 
 * @author ke
 * @param <F>
 *            The type of elements to be mapped.
 * @param <T>
 *            The type of elements of the result of the mapping.
 */
public abstract class ArrayListMapper<F, T> extends CollectionMapper<F, T> {

    @Override
    protected ArrayList<T> createResultList(Collection<F> collection) {
        return new ArrayList<T>(collection.size());
    }

}
