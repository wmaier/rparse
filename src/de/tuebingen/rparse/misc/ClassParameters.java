/*******************************************************************************
 * File ClassParameters.java
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * A class which is supposed to make the task of passing multiple parameters to a class within a single string less of a
 * mess. The format for the string is: Different parameters are separated by {@linkplain OPTION_SEPARATOR}. If
 * parameters have a key and a value, then both are separated by a {@linkplain KV_SEPARATOR}.
 * 
 * @author wmaier
 */
public class ClassParameters {

    public final static String                      OPTION_SEPARATOR = ":";
    public final static String                      KV_SEPARATOR     = "=";

    private List<ClassParameterOption>              options;
    private Hashtable<String, ClassParameterOption> optionsByKey;

    public class ClassParameterOption {

        String key;
        String description;
        String value;

        public ClassParameterOption(String key, String description) {
            this.key = key;
            this.description = description;
            this.value = null;
        }

        public void setValue(String v) throws ParameterException {
            if (v == null) {
                throw new ParameterException("Could not set value");
            }
            value = v;
        }

        public boolean hasValue() {
            return value != null;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

    }

    public ClassParameters() {
        this.options = new ArrayList<ClassParameterOption>();
        this.optionsByKey = new Hashtable<String, ClassParameterOption>();
    }

    public void add(String key, String descr) {
        ClassParameterOption o = new ClassParameterOption(key, descr);
        options.add(o);
        optionsByKey.put(key, o);
    }

    public void parse(String line) throws ParameterException {
        if (line == null || line.isEmpty()) {
            return;
        }
        for (String param : line.split(OPTION_SEPARATOR)) {
            String[] kv = param.split(KV_SEPARATOR);
            if (optionsByKey.containsKey(kv[0])) {
                ClassParameterOption o = optionsByKey.get(kv[0]);
                if (kv.length > 1) {
                    o.setValue(kv[1]);
                } else {
                    o.setValue("");
                }
            } else {
                throw new ParameterException("Unknown parameter: " + param);
            }
        }
    }

    public boolean check(String key) {
        return hasVal(key);
    }

    public boolean hasVal(String key) {
        return optionsByKey.get(key).hasValue();
    }

    public String getVal(String key) {
        return hasVal(key) ? optionsByKey.get(key).getValue() : null;
    }

    @Override
    public String toString() {
        String res = "";
        for (String k : optionsByKey.keySet()) {
            if (!optionsByKey.get(k).hasValue())
                res += k + "\n";
            else
                res += k + " -> " + optionsByKey.get(k).getValue() + "\n";
        }
        return res;
    }

}
