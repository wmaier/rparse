/*******************************************************************************
 * File ExportTreeWriter.java
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
import java.util.Arrays;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.Tree;

public class ExportTreeWriter implements SentenceWriter<Tree> {
	
	protected String format;
	
	public ExportTreeWriter(String format) {
		this.format = format;
	}
	
	@Override
    public void write(Tree t, Writer writer) throws TreebankException {
		// write output in format 4 (with lemma column)
		
		// update number in bstring // sentence number must come from ID field, remove bstring/estring and introduce string for annotator info
		// t.setBstring("#BOS " + t.getId());
		
		try {
			writer.write(t.getBstring().concat("\n"));
			if (!t.hasExportNumbering())
				t.calcExportNumbering();
			Object[] nodenums = t.getNodes().keySet().toArray();
			Arrays.sort(nodenums);
			for (Object nn : nodenums) {
				int ii = Integer.valueOf((Integer) nn);
				if (ii > 0) {
					Node n = t.getNodes().get(ii);
					writer.write(n.getLabel().getWord());
					writer.write("\t");
					int tabnum = ((24 - n.getLabel().getWord().length()) / 8) + 1;
					for (int i = 0; i < tabnum - 1; ++i)
						writer.write("\t");
					writer.write("\t");
					writer.write(n.getLabel().getTag());
					writer.write("\t");
					if (format.equals("export-lemma")) {
						writer.write("\t");
						writer.write(n.getLabel().getLemma());
						writer.write("\t");
					}
					writer.write("\t");
					writer.write(n.getLabel().getMorph());
					writer.write("\t\t");
					writer.write("\t");
					writer.write(n.getLabel().getEdge());
					writer.write("\t\t");
					writer.write(n.getLabel().getParent());
					writer.write("\t");
					if (!n.getLabel().getSecedge().equals("")) {
						writer.write(n.getLabel().getSecedge());
						writer.write("\t");
					}
					if (!n.getLabel().getComment().equals("")) {
						writer.write(n.getLabel().getComment());
					}
					writer.write("\n");
				}
			}
			writer.write(t.getEstring().concat("\n"));
			writer.flush();
		} catch (IOException e) {
			throw new TreebankException("Could not write treebank: "
					+ e.getMessage());
		}
		
	}
	
}
