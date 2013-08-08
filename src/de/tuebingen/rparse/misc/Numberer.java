/*******************************************************************************
 * File Numberer.java
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A numberer which maps objects to integers. Useful everywhere.
 * 
 * @author wmaier
 */
public class Numberer implements Serializable {

    private static final long     serialVersionUID = -437863810328700734L;

    private Map<String, Numberer> nmap;

    private Map<Object, Integer>  objmap;

    private Map<Integer, Object>  intmap;

    private int                   tnum             = 0;

    /**
     * Constructs a new Numberer
     */
    public Numberer() {
        nmap = new HashMap<String, Numberer>();
        objmap = new HashMap<Object, Integer>();
        intmap = new HashMap<Integer, Object>();
        tnum = 0;
    }

    /**
     * Get the numberer for a certain category
     * 
     * @param id
     *            The category ID string
     * @return The numberer for the ID string.
     */
    public Numberer getNumberer(String id) {
        if (nmap.get(id) == null)
            nmap.put(id, new Numberer());
        return nmap.get(id);
    }

    /**
     * Get the object with a certain ID in a certain category.
     * 
     * @param id
     *            The category ID string
     * @param num
     *            The object number
     * @return The object
     */
    public Object getObjectWithId(String id, int num) {
        if (nmap.containsKey(id))
            return nmap.get(id).getObject(num);
        return null;
    }

    /**
     * Get the ID of an object within a certain category
     * 
     * @param id
     *            The ID string of the object
     * @param obj
     *            The object
     * @return The id of the object
     */
    public Integer getIntWithId(String id, Object obj) {
        if (nmap.containsKey(id))
            return nmap.get(id).getInt(obj);
        return null;
    }

    private Object getObject(int num) {
        if (intmap.containsKey(num))
            return intmap.get(num);
        return null;
    }

    private Integer getInt(Object obj) {
        if (objmap.containsKey(obj))
            return objmap.get(obj);
        return null;
    }

    /**
     * The size of the numberer
     * 
     * @return The corresponding value
     */
    public int size() {
        return objmap.size();
    }

    /**
     * Get a number for an object in a certain category.
     * 
     * @param id
     *            The category ID string
     * @param obj
     *            The object
     * @return A new ID number for the object, or the existing object ID if applicable
     */
    public Integer number(String id, Object obj) {
        Numberer n = getNumberer(id);
        Integer ret = n.getInt(obj);
        if (ret == null) {
            n.objmap.put(obj, tnum);
            n.intmap.put(tnum, obj);
            tnum++;
            ret = number(id, obj);
        }
        return ret;
    }

    /**
     * Return a human-readable representation of the numberer
     */
    @Override
    public String toString() {
        String ret = "";
        for (String id : nmap.keySet()) {
            ret += "type: " + id + "\n";
            Numberer nb = getNumberer(id);
            for (Integer i : nb.intmap.keySet())
                ret += "    " + i + " -> " + getObjectWithId(id, i) + "\n";
            ret += "\n";
        }
        return ret;
    }

}
