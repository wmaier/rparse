/*******************************************************************************
 * File CommandLineOption.java
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
 * A command line option
 * 
 * @author wmaier
 */
public class CommandLineOption {

    private Prefix    prefix;
    private String    key;
    private Separator sep;
    private boolean   needsVal;
    private String    descr;

    CommandLineOption(Prefix prefix, String key, Separator separator,
            boolean value, String descr) {
        this.prefix = prefix;
        this.key = key;
        this.sep = separator;
        this.needsVal = value;
        this.descr = descr;
    }

    /**
     * Get the prefix (dash, ...)
     * 
     * @return
     */
    Prefix getPrefix() {
        return prefix;
    }

    /**
     * Set the prefix (dash, ...)
     * 
     * @param prefix
     */
    void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    /**
     * Get the name of the option
     * 
     * @return
     */
    String getKey() {
        return key;
    }

    /**
     * Set the name of the option
     * 
     * @param key
     */
    void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the key/value separator
     * 
     * @return
     */
    Separator getSep() {
        return sep;
    }

    /**
     * Set the key/value separator
     * 
     * @param sep
     */
    void setSep(Separator sep) {
        this.sep = sep;
    }

    /**
     * Check if the option needs a value
     * 
     * @return
     */
    boolean getNeedsVal() {
        return needsVal;
    }

    /**
     * Set if the option needs a value
     * 
     * @param needsVal
     */
    void setNeedsVal(boolean needsVal) {
        this.needsVal = needsVal;
    }

    /**
     * Get the doc string of the option
     * 
     * @return
     */
    String getDescr() {
        return descr;
    }

    /**
     * Set the doc string of the option
     * 
     * @param descr
     */
    void setDescr(String descr) {
        this.descr = descr;
    }

    @Override
    public String toString() {
        String ret = "     -" + key;
        if (needsVal) {
            ret += " [" + key.toUpperCase() + "]";
        }
        int len = 40 - ret.length();
        for (int i = 0; i < len; ++i)
            ret += " ";
        ret += ": " + descr;
        return ret;
    }

    // Enumerate the different components of a command line

    /**
     * A prefix (default is -)
     */
    public static enum Prefix {
        DASH('-'), SLASH('/');

        private char c;

        private Prefix(char c) {
            this.c = c;
        }

        char getName() {
            return c;
        }
    }

    // Enumerate the different components of a command line

    /**
     * A separator (default is blank)
     */
    public static enum Separator {
        BLANK(' '), COLON(':'), EQUALS('='), NONE('D');

        private char c;

        private Separator(char c) {
            this.c = c;
        }

        char getName() {
            return c;
        }
    }

}
