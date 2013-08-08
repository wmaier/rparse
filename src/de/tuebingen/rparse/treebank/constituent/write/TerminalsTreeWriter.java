/*******************************************************************************
 * File TerminalsTreeWriter.java
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
package de.tuebingen.rparse.treebank.constituent.write;

import java.io.IOException;
import java.io.Writer;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * A {@link SentenceWriter} which writes out all terminals of a tree.
 * 
 * @author wmaier
 * 
 */
public class TerminalsTreeWriter implements SentenceWriter<Tree> {
	
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
	
	public TerminalsTreeWriter(String format) {
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
	public void write(Tree t, Writer w) throws TreebankException, IOException {
		for (Node terminal : t.getOrderedTerminals()) {
			w.write(terminal.getLabel().getWord());
			if (pos)
				w.write(sep + terminal.getLabel().getTag());
			if (oneline)
				w.write(" ");
			else
				w.write("\n");
		}
		w.write("\n");
	}
	
}
