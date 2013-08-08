/*******************************************************************************
 * File BracketsTreeWriter.java
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

public class BracketsTreeWriter implements SentenceWriter<Tree> {
	
	private String format;
	private String indentchar;
	
	public BracketsTreeWriter(String format) {
		this.format = format;
		indentchar = "  ";
		if (format.contains("negraptb"))
			indentchar = "\t";
	}
	
	@Override
	public void write(Tree tree, Writer writer) throws TreebankException,
			IOException {
		if (tree.isContinuous()) {
			writeBracketsNode(tree.getRoot(), writer, format);
		} else {
			writer.write("discontinuous");
		}
		writer.write("\n");
		writer.flush();
	}
	
	private void writeBracketsNode(Node n, Writer w, String format)
			throws IOException {
		// indent level
		int indentlev = 0;
		int indentlev_ind = format.lastIndexOf('-');
		if (indentlev_ind != -1)
			try {
				indentlev = Integer
						.valueOf(format.substring(indentlev_ind + 1));
			} catch (NumberFormatException e) {
			}
		if (indentlev > 0)
			format = format.substring(0, indentlev_ind);
		// printing
		if (format.contains("indent")) {
			w.write("\n");
			for (int i = 0; i < indentlev; ++i)
				w.write(indentchar);
		}
		if (format.contains("gf") && !n.getLabel().getEdge().startsWith("-")) {
			w.write("(");
			w.write(n.getLabel().getTag());
			w.write("-");
			w.write(n.getLabel().getEdge());
		} else {
			w.write("(");
			w.write(n.getLabel().getTag());
		}
		if (n.getLc() != null) {
			writeBracketsNode(n.getLc(), w, format + "-"
					+ String.valueOf(indentlev + 1));
		} else {
			w.write(" ");
			w.write(n.getLabel().getWord());
		}
		if (n.getLc() != null)
			if (format.contains("negraptb")) {
				w.write("\n");
				for (int i = 0; i < indentlev; ++i)
					w.write(indentchar);
			}
		w.write(")");
		if (n.getRs() != null) {
			writeBracketsNode(n.getRs(), w, format + "-"
					+ String.valueOf(indentlev));
		}
	}
	
}
