/*******************************************************************************
 * File CoNLLGraphWriter.java
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
package de.tuebingen.rparse.treebank.dep;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;

import de.tuebingen.rparse.treebank.SentenceWriter;

public class CoNLLGraphWriter implements
		SentenceWriter<DependencyForest<DependencyForestNodeLabel, String>> {

	private static final String DUMMY_VALUE = "_";

	@Override
	public void write(DependencyForest<DependencyForestNodeLabel, String> sentence,
			Writer writer) throws IOException {
		for (DependencyForestNode<DependencyForestNodeLabel, String> node : sentence.nodes()) {
			DependencyForestNodeLabel token = node.getToken();
			writer.write(Integer.toString(node.getID()));
			writer.write("\t");
			writer.write(token.getForm());
			writer.write("\t");
			writer.write(nullToDummy(token.getLemma()));
			writer.write("\t");
			writer.write(token.getCpostag());
			writer.write("\t");
			writer.write(token.getPostag());
			writer.write("\t");
			writer.write(featsViz(token.getFeats()));
			writer.write("\t");
			DependencyForestNode<DependencyForestNodeLabel, String> head = node.getHead();

			if (head == null) {
				writer.write("0");
			} else {
				writer.write(Integer.toString(head.getID()));
			}

			writer.write("\t");

			if (node.isRoot()) {
				writer.write("ROOT");
			} else {
				writer.write(nullToDummy(node.getRelation()));
			}

			writer.write("\t_\t_");
			writer.write(System.getProperty("line.separator"));
		}

		writer.write(System.getProperty("line.separator"));
	}

	private String nullToDummy(String string) {
		if (string == null) {
			return DUMMY_VALUE;
		}

		return string;
	}

	private String featsViz(Set<String> feats) {
		if (feats == null) {
			return DUMMY_VALUE;
		}

		Iterator<String> it = feats.iterator();

		if (it.hasNext()) {
			StringBuilder result = new StringBuilder();
			result.append(it.next());

			while (it.hasNext()) {
				result.append("|");
				result.append(it.next());
			}

			return result.toString();
		}

		return "";
	}

}
