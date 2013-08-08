/*******************************************************************************
 * File IncrementalExportProcessor.java
 * 
 * Authors:
 *    Wolfgang Maier, Kilian Evang
 *    
 * Copyright:
 *    Wolfgang Maier, Kilian Evang, 2011
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
package de.tuebingen.rparse.treebank.constituent.process;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeLabel;
import de.tuebingen.rparse.treebank.constituent.Tree;

public class IncrementalExportProcessor extends
		IncrementalTreebankProcessor<Tree> {

	private Scanner scanner;

	private Numberer nb;

	public IncrementalExportProcessor(Numberer nb) {
		this.nb = nb;
	}

	@Override
	public void skipNextSentence() {
		while (scanner.hasNextLine()) {
			if (scanner.nextLine().trim().startsWith("#EOS")) {
				return;
			}
		}

		throw new NoSuchElementException();
	}

	@Override
	public Tree getNextSentence() {
		List<String> sentence = new ArrayList<String>();

		// go to #BOS
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (line.startsWith("#BOS")) {
				sentence.add(line);
				break;
			}
		}

		// go to #EOS
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			if (!line.equals("")) {
				sentence.add(line);

				if (line.startsWith("#EOS")) {
					return parseExportTree(sentence, nb);
				}
			}
		}

		// no next sentence
		return null;
	}

	@Override
	public void doInitialize(Reader reader) {
		scanner = new Scanner(reader);
	}

	@Override
	public int getLength(Tree sentence) {
		return sentence.getOrderedTerminals().size();
	}

	public static Tree parseExportTree(List<String> intree, Numberer nb) {
		Tree ret = new Tree(nb);
		ret.setBstring(intree.get(0));
		intree.remove(0);
		ret.setEstring(intree.get(intree.size() - 1));
		intree.remove(intree.size() - 1);
		ret.setId(Integer.valueOf(ret.getEstring().substring(5)));
		if (intree.size() == 0)
			return ret;
		// parse all nodes
		List<Node> lnodes = new ArrayList<Node>();
		for (String nodeline : intree) {
			lnodes.add(parseExportNode(nodeline));
		}
		int termnum = 1;
		// introduce terminal numbering
		for (Node n : lnodes) {
			if (n.getLabel().getNum() == -1) {
				n.getLabel().setNum(termnum);
				++termnum;
			}
		}
		// hash them by their label
		Map<Integer, Node> hnodes = new HashMap<Integer, Node>();
		for (Node n : lnodes)
			hnodes.put(n.getLabel().getNum(), n);
		// add VROOT
		Node vroot = new Node(new NodeLabel(0, "#0", "VROOT", "", "--", "-",
				"-1", "", ""));
		hnodes.put(0, vroot);

		Map<Integer, List<Integer>> children = new HashMap<Integer, List<Integer>>();
		Map<Integer, Integer> leftmost = new HashMap<Integer, Integer>();

		// terminals
		for (int i = 1; i < termnum; ++i) {
			// parent
			int paint = Integer.valueOf(hnodes.get(i).getLabel().getParent());
			hnodes.get(i).setPa(hnodes.get(paint));
			// rs
			if (i > 1
					&& hnodes.get(i).getLabel().getParent() == hnodes
							.get(i - 1).getLabel().getParent())
				hnodes.get(i - 1).setRs(hnodes.get(i));
			if (hnodes.get(i).getPa() != null) {
				// record children (for determining ID/LP)
				int panum = hnodes.get(i).getPa().getLabel().getNum();
				if (!children.containsKey(panum))
					children.put(panum, new ArrayList<Integer>());
				children.get(panum).add(i);
				leftmost.put(i, i);
				if (!leftmost.containsKey(panum))
					leftmost.put(panum, i);
				else
					leftmost.put(panum,
							leftmost.get(panum) < i ? leftmost.get(panum) : i);
			}
		}

		// nonterminals
		for (int i = 500; i < 500 + hnodes.size() - termnum; ++i) {
			// parent
			int paint = Integer.valueOf(hnodes.get(i).getLabel().getParent());
			hnodes.get(i).setPa(hnodes.get(paint));
			if (hnodes.get(i).getPa() != null) {
				// record children (for determining ID/LP)
				int panum = hnodes.get(i).getPa().getLabel().getNum();
				if (!children.containsKey(panum))
					children.put(panum, new ArrayList<Integer>());
				children.get(panum).add(i);

				if (!leftmost.containsKey(panum)) {
					leftmost.put(panum, leftmost.get(i));
				} else {
					leftmost.put(
							panum,
							leftmost.get(panum) < leftmost.get(i) ? leftmost
									.get(panum) : leftmost.get(i));
				}
			}
		}

		// sort children and create ID/LP links
		for (int i = 500; i < 500 + hnodes.size() - termnum; ++i) {
			// sort the children
			Collections.sort(children.get(i), new Leftmostcomp(leftmost));
			// LP link (right sibling)
			hnodes.get(i).setLc(hnodes.get(children.get(i).get(0)));
			for (int j = 0; j < children.get(i).size() - 1; ++j)
				hnodes.get(children.get(i).get(j)).setRs(
						hnodes.get(children.get(i).get(j + 1)));
		}
		Collections.sort(children.get(0), new Leftmostcomp(leftmost));
		// LP link (right sibling)
		hnodes.get(0).setLc(hnodes.get(children.get(0).get(0)));
		for (int j = 0; j < children.get(0).size() - 1; ++j)
			hnodes.get(children.get(0).get(j)).setRs(
					hnodes.get(children.get(0).get(j + 1)));

		ret.setRoot(hnodes.get(0));
		ret.setNodes(hnodes);
		List<Node> termlist = new ArrayList<Node>();
		for (Integer n : hnodes.keySet()) {
			if (n < 500 && n > 0)
				termlist.add(hnodes.get(n));
		}
		ret.setTerminals(termlist);
		ret.setLastterm(termnum);
		ret.setLastnont(500 + hnodes.size() - termnum);

		return ret;
	}

	/*
	 * Compare nodes by the leftmost terminal they dominate. Used for
	 * non-terminal ordering.
	 */
	private static class Leftmostcomp implements Comparator<Integer> {

		private Map<Integer, Integer> leftmost;

		public Leftmostcomp(Map<Integer, Integer> leftmost) {
			this.leftmost = leftmost;
		}

		public int compare(Integer arg0, Integer arg1) {
			if (!leftmost.containsKey(arg0) || !leftmost.containsKey(arg1))
				return 0;
			if (leftmost.get(arg0) < leftmost.get(arg1))
				return -1;
			if (leftmost.get(arg0) > leftmost.get(arg1))
				return 1;
			return 0;
		}
	}

	public static Node parseExportNode(String line) {
		Node ret = new Node();
		NodeLabel l = new NodeLabel();
		ret.setLabel(l);
		String[] spline = line.split("\\s+");
		// try to detect if we have a lemma field
		boolean oldformat = true;
		for (int i = 0; i < spline[4].length(); ++i) {
			oldformat &= Character.isDigit(spline[4].charAt(i));
		}
		int num = -1;
		if (spline[0].startsWith("#") && !spline[0].equals("#"))
			num = Integer.valueOf(spline[0].substring(1));
		l.setWord(spline[0]);
		l.setLemma("");
		if (!oldformat) {
			l.setLemma(new String(spline[1]));
			for (int i = 1; i < spline.length - 1; ++i)
				spline[i] = spline[i + 1];
		}
		l.setTag(spline[1]);
		l.setMorph(spline[2]);
		l.setEdge(spline[3]);
		l.setParent(spline[4]);
		l.setNum(num);
		return ret;
	}

}
