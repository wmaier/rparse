/*******************************************************************************
 * File DependencyTerminalsTreeWriter.java
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
package de.tuebingen.rparse.treebank.dep;

import java.io.IOException;
import java.io.Writer;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * A {@link SentenceWriter} which writes out all terminals of a tree.
 * 
 * @author wmaier
 * 
 */
public class DependencyTerminalsWriter implements SentenceWriter<DependencyForest<DependencyForestNodeLabel, String>> {
	
	public final static String SEPCOM_SLASH = "SEPslash";
	public final static String SEP_SLASH = "/";
	public final static String SEPCOM_TAB = "SEPtab";
	public final static String SEP_TAB = "\t";
	public final static String SEP_SPACE = " "; // default
	
	public final static String MODE_POS = "pos";
	public final static String MODE_ONELINE = "oneline";
	
	protected String format;
	private String sep;
	private boolean pos;
	private boolean oneline;
	
	public DependencyTerminalsWriter(String format) {
		this.format = format;
		if (format.contains(SEPCOM_SLASH))
			sep = SEP_SLASH;
		else if (format.contains(SEPCOM_TAB))
			sep = SEP_TAB;
		else
			sep = SEP_SPACE;
		pos = format.contains(MODE_POS);
		oneline = format.contains(MODE_ONELINE);
	}
	
    @Override
    public void write(
            DependencyForest<DependencyForestNodeLabel, String> t,
            Writer w) throws TreebankException, IOException {
        for (DependencyForestNode<DependencyForestNodeLabel, String> terminal : t.nodes()) {
            w.write(terminal.getToken().getForm());
            if (pos)
                w.write(sep + terminal.getToken().getPostag());
            if (oneline)
                w.write(" ");
            else
                w.write("\n");
        }
        w.write("\n");
        
    }
	
}
