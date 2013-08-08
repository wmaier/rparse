/*******************************************************************************
 * File PrefixSuffixFilter.java
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

import java.io.File;
import java.io.FilenameFilter;

/**
 * A FilenameFilter for directory output during treebank processing. Accepts a name if the prefix and suffix of its name
 * match the ones given in the constructor.
 * 
 * @author wmaier
 */
public class PrefixSuffixFilter implements FilenameFilter {

    private final String prefix;

    private final String suffix;

    /**
     * Constructor.
     * 
     * @param prefix
     *            The prefix for later matching.
     * @param suffix
     *            The suffix.
     */
    public PrefixSuffixFilter(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.startsWith(prefix) && name.endsWith(suffix);
    }

}
