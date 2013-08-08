/*******************************************************************************
 * File CollectionMapper.java
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

import java.util.Collection;
import java.util.List;

/**
 * Allows to emulate python-style mapping
 * 
 * @author ke
 * @param <F>
 *            The type of elements to be mapped
 * @param <T>
 *            The type of elements resulting from the mapping
 */
public abstract class CollectionMapper<F, T> {

    /**
     * The mapping function for a single element
     * 
     * @param element
     *            The element.
     * @return The result from the mapping.
     */
    public abstract T map(F element);

    /**
     * Map an entire collection to a list
     * 
     * @param collection
     *            The collection.
     * @return The list.
     */
    public List<T> map(Collection<F> collection) {
        List<T> result = createResultList(collection);

        for (F element : collection) {
            result.add(map(element));
        }

        return result;
    }

    /**
     * Build a list of the resulting type
     * 
     * @param collection
     *            The list we start from
     * @return The list where the result of the mapping is to be stored.
     */
    protected abstract List<T> createResultList(Collection<F> collection);

}
