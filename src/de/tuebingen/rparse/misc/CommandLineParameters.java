/*******************************************************************************
 * File CommandLineParameters.java
 * 
 * Authors:
 *    Wolfgang Maier, Yannick Parmentier
 *    
 * Copyright:
 *    Wolfgang Maier, Yannick Parmentier 2009-2011
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

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.tuebingen.rparse.misc.CommandLineOption.Prefix;
import de.tuebingen.rparse.misc.CommandLineOption.Separator;

/**
 * Command line options processing class. Freely inspired by : Dr. Matthias Laux
 * (http://www.javaworld.com/javaworld/jw-08-2004/jw-0816-command.html), also used in the TuLiPA system.
 */
public class CommandLineParameters {

    private List<CommandLineOption>              options;
    private Hashtable<String, Pattern>           patterns;
    private Hashtable<String, String>            values;
    private Hashtable<String, CommandLineOption> optionmap;

    public CommandLineParameters() {
        options = new LinkedList<CommandLineOption>();
        patterns = new Hashtable<String, Pattern>();
        values = new Hashtable<String, String>();
        optionmap = new Hashtable<String, CommandLineOption>();
    }

    /**
     * add a new option
     * 
     * @param prefix
     * @param key
     * @param separator
     * @param value
     * @param descr
     */
    public void add(Prefix prefix, String key, Separator separator,
            boolean value, String descr) {
        CommandLineOption o = new CommandLineOption(prefix, key, separator,
                value, descr);
        options.add(o);
        optionmap.put(key, o);
    }

    /**
     * return a read only view on the command line options
     * 
     * @return
     */
    public List<CommandLineOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    /**
     * Prepare the parsers for each possible option
     */
    public void prepare() {
        String accents = "\u00e9\u00e8\u00ea\u00f9\u00fb\u00fc\u00f4\u00ee\u00ef\u00e2\u00c0\u00f6\u00e4";

        for (int i = 0; i < options.size(); i++) {
            CommandLineOption o = options.get(i);
            Prefix prefix = o.getPrefix();
            String key = o.getKey();
            Separator sep = o.getSep();
            boolean needsVal = o.getNeedsVal();
            Pattern p;
            if (needsVal) {
                p = java.util.regex.Pattern.compile("\"" + prefix.getName()
                        + key + "\"" + sep.getName() + "([\\p{Punct}\"a-zA-Z"
                        + accents + "0-9\\.\\@_\\" + File.separator + "~-]+)");
            } else {
                p = java.util.regex.Pattern.compile("\"" + prefix.getName()
                        + key + "\"()");
            }
            patterns.put(key, p);
        }
    }

    /**
     * Process the command line to find the options
     */
    public void parse(String line) throws ParameterException {
        Set<String> keys = patterns.keySet();
        Iterator<String> i = keys.iterator();
        while (i.hasNext()) {
            String k = i.next();
            Pattern p = patterns.get(k);
            // try this option on the command line
            try {
                Matcher m = p.matcher(line);
                boolean a = m.find();
                if (a) {
                    // System.err.println("-- "+k+": "+m.group(1));
                    values.put(k, m.group(1));
                }
                // else { System.err.println("Line : "+line);
                // System.err.println("Pattern not found : "+p.pattern()); }
            } catch (PatternSyntaxException pse) {
                throw new ParameterException(pse.getDescription());
            } catch (IllegalStateException ise) {
                throw new ParameterException(ise.toString());
            }
            // next option
        }
    }

    /**
     * Get all option keys
     * 
     * @return
     */
    public Enumeration<String> getKeys() {
        return values.keys();
    }

    /**
     * Get all option values.
     * 
     * @return
     */
    public Hashtable<String, String> getValues() {
        return values;
    }

    /**
     * Get the value of a command line option
     * 
     * @param key
     * @return
     */
    public String getVal(String key) {
        if (values.containsKey(key)) {
            String res = values.get(key);
            // System.out.println(res);
            if (res.length() > 0) {
                res = res.replace("---", " ");
                // we remove the ""
                return res.substring(1, (res.length() - 1));
            } else
                return res;
        } else {
            return null;
        }
    }

    /**
     * Get the description of a command line option.
     * 
     * @param key
     * @return
     */
    public String getDescr(String key) {
        String ret = "";
        if (values.containsKey(key))
            ret = optionmap.get(key).getDescr();
        return ret;
    }

    /**
     * Empty the command line option object.
     */
    public void removeAll() {
        values.clear();
    }

    /**
     * Remove a command line option
     * 
     * @param key
     */
    public void removeVal(String key) {
        values.remove(key);
    }

    /**
     * Set the value of a command line option in escaped form. (Allows for spaces in the value)
     * 
     * @param key
     * @param value
     */
    public void setOurVal(String key, String value) {
        values.put(key, "\"" + value + "\"");
    }

    /**
     * Set the value of a command line option in unescaped form.
     * 
     * @param key
     * @param value
     */
    public void setVal(String key, String value) {
        values.put(key, value);
    }

    /**
     * Check for the presence of an option.
     * 
     * @param key
     * @return
     */
    public boolean check(String key) {
        return (values.containsKey(key));
    }

    @Override
    public String toString() {
        String res = "";
        for (String k : values.keySet()) {
            if (values.get(k).equals(""))
                res += k + "\n";
            else
                res += k + " -> " + values.get(k) + "\n";
        }
        return res;
    }

}
