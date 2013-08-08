/*******************************************************************************
 * File Node.java
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
package de.tuebingen.rparse.treebank.constituent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.misc.Test;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Represent a labeled node in a constituent treebank tree.
 * 
 * @author wmaier, ke
 */
public class Node {

	// test for terminals
	private static final Test<Node> DEFAULT_TERMINAL_TEST = new Test<Node>() {

		@Override
		public boolean test(Node object) {
			return object.isLeaf();
		}

	};

	// parent node
	private Node pa;
	// left child
	private Node lc;
	// right sibling
	private Node rs;
	// annotation
	private NodeLabel label;
	// node properties to keep it flexible
	private Map<String, Object> properties;

	/**
	 * Creates a node with no label
	 */
	public Node() {
		setLabel(null);
		properties = new HashMap<String, Object>();
	}

	/**
	 * Creates a labeled node
	 * @param label
	 */
	public Node(NodeLabel label) {
		setLabel(label);
		properties = new HashMap<String, Object>();
	}

	/**
	 * Copy constructor
	 * @param n label gets set on copy, properties do not get copied
	 */
	public Node(Node n) {
		setLabel(new NodeLabel(n.getLabel()));
		properties = new HashMap<String, Object>();
	}


	public void setProperty(String id, Object prop) {
		properties.put(id, prop);
	}

	public Object getProperty(String id) {
		return properties.get(id);
	}
	
	public boolean hasProperty(String id) {
		return properties.containsKey(id);
	}

	public NodeLabel getLabel() {
		return label;
	}

	public void setLabel(NodeLabel label) {
		this.label = label;
	}

	public Node getPa() {
		return pa;
	}

	public void setPa(Node pa) {
		this.pa = pa;
	}

	public Node getLc() {
		return lc;
	}

	public void setLc(Node lc) {
		this.lc = lc;
	}

	public Node getRs() {
		return rs;
	}

	public void setRs(Node rs) {
		this.rs = rs;
	}

	/**
	 * Get the left sibling of this node.
	 * 
	 * @return Left sibling or null if node is the first child or if node is root.
	 */
	public Node getLeftSibling() {
		if (pa != null)
			for (Node child : pa.getChildren())
				if (child.getRs() != null && child.getRs().equals(this))
					return child;
		return null;
	}

	/**
	 * Return n if this is the nth child of this.pa, -1 if this is root.
	 * 
	 * @return
	 */
	public int getChildIndex() {
		int ret = -1;
		if (pa != null) {
			ret = 0;
			for (Node pac : pa.getChildren()) {
				if (pac.getLabel().getNum() == getLabel().getNum())
					break;
				ret++;
			}
		}
		return ret;
	}

	/**
	 * Check if there are children
	 * @return boolean indicating if this node has children
	 */
	public boolean hasChildren() {
		return lc != null;
	}

	/**
	 * Get all children of this node.
	 * 
	 * @return List<Node> containing all children of this node 
	 */
	public List<Node> getChildren() {
		ArrayList<Node> ret = new ArrayList<Node>();
		if (lc != null) {
			ret.add(lc);
			Node tmprs = lc.rs;
			while (tmprs != null) {
				ret.add(tmprs);
				tmprs = tmprs.rs;
			}
		}
		return ret;
	}
	
    static final Comparator<Node> HEAD_ORDER_COMPARATOR = new Comparator<Node>() {
        public int compare(Node n1, Node n2) { 
            try {
                return n1.lexicalHead().getLabel().getNum() - n2.lexicalHead().getLabel().getNum();
            } catch (TreebankException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("Failed to compare two nodes");
        }
    };
	
	public List<Node> getHeadOrderedChildren() {
	    List<Node> ret = getChildren();
	    if (ret.size() > 0) {
	        Collections.sort(ret, HEAD_ORDER_COMPARATOR);
	    }
	    return ret;
	}

	/**
	 * Add a child to the right of the rightmost child of this node.
	 * 
	 * @param child
	 */
	public void appendChild(Node child) {
		child.setPa(this);
		if (lc == null) {
			lc = child;
		} else {
			Node ap = lc;
			while (ap.getRs() != null)
				ap = ap.getRs();
			ap.setRs(child);
		}
	}

	public void addChildInOrder(Node child) {
		int terminalNumber = Collections.min(child.calcTermdom());
		child.setPa(this);
		Node ls = null;
		Node rs = lc;

		while (rs != null && Collections.min(rs.calcTermdom()) < terminalNumber) {
			ls = rs;
			rs = rs.rs;
		}

		if (ls == null) {
			lc = child;
		} else {
			ls.setRs(child);
		}

		child.setRs(rs);
	}

	/**
	 * Get the rightmost child
	 * 
	 * @return the rightmost child
	 */
	public Node getLastChild() {
		LinkedList<Node> children = new LinkedList<Node>(getChildren());
		if (children.size() > 0)
			return children.getLast();
		return null;
	}

	/**
	 * Reset nonterminal numbering
	 */
	public void numResetNonterminals(int num) {
		if (label != null && label.getNum() > 499 || label.getNum() == 0) {
			getLabel().setNum(-1);
		}
		if (lc != null)
			lc.numResetNonterminals(num);
		if (rs != null)
			rs.numResetNonterminals(num);
	}

	/**
	 * Utility method export numbering calculation (see {@link Tree}): Assign
	 * new numbers to this node and all nodes dominated by it, the dominated
	 * nodes getting lower numbers.
	 */
	public int numAssign(int num) {
		if (label.getNum() == -1) {
			label.setNum(num);
			--num;
		}
		if (lc != null)
			num = lc.numAssign(num);
		if (rs != null)
			num = rs.numAssign(num);
		return num;
	}

	/**
	 * Renumber nodes following translation map.
	 * 
	 * @param dic
	 */
	public void numTranslate(Map<Integer, Integer> dic) {
		if (dic.containsKey(label.getNum())) {
			label.setNum(dic.get(label.getNum()));
			if (label.getNum() > 499)
				label.setWord("#" + String.valueOf(label.getNum()));
		}
		if (pa != null)
			label.setParent(String.valueOf(pa.label.getNum()));
		if (lc != null)
			lc.numTranslate(dic);
		if (rs != null)
			rs.numTranslate(dic);
		return;
	}

	/**
	 * Translate tag and word fields on all nodes.
	 */
	public void textTranslateWordAndTag(Map<String, String> dic) {
		for (String key : dic.keySet()) {
			label.setWord(label.getWord().replace(key, dic.get(key)));
			label.setTag(label.getTag().replace(key, dic.get(key)));
		}
		if (lc != null)
			lc.textTranslateWordAndTag(dic);
		if (rs != null)
			rs.textTranslateWordAndTag(dic);
		return;
	}

	/**
	 * Translate tag and edge on all nodes
	 * 
	 * @param dic
	 */
	public void textTranslateTagAndEdge(Map<String, String> dic) {
		for (String key : dic.keySet()) {
			label.setEdge(label.getEdge().replace(key, dic.get(key)));
			label.setTag(label.getTag().replace(key, dic.get(key)));
		}
		if (lc != null)
			lc.textTranslateTagAndEdge(dic);
		if (rs != null)
			rs.textTranslateTagAndEdge(dic);
		return;
	}

	public List<Node> getNodes() {
		return getNodes(new ArrayList<Node>());
	}

	/**
	 * Get all nodes dominated by this one in depth-first order.
	 * 
	 * @param nodes
	 * @return
	 */
	public List<Node> getNodes(List<Node> nodes) {
		nodes.add(this);
		if (lc != null)
			nodes = lc.getNodes(nodes);
		if (rs != null)
			nodes = rs.getNodes(nodes);
		return nodes;
	}

	/**
	 * Add a gorn address to this node and all nodes dominated by it.
	 */
	public void addGorn(String start) {
		setProperty(ConstituentConstants.GORN, start);
		List<Node> children = getChildren();
		for (int i = 0; i < children.size(); ++i) {
			String gorn = start + "." + String.valueOf(i + 1);
			// System.err.println(gorn);
			children.get(i).addGorn(gorn);
		}
	}

	/**
	 * Find a certain node below this one given a number.
	 * 
	 * @param num
	 * @return
	 */
	public Node findNode(int num) {
		Node ret = null;
		if (getLabel().getNum() == num)
			ret = this;
		if (ret == null && lc != null)
			ret = lc.findNode(num);
		if (ret == null && rs != null)
			ret = rs.findNode(num);
		return ret;
	}

	public List<Integer> calcTermdom() {
		return calcTermdom(new ArrayList<Integer>());
	}

	public List<Integer> calcTermdom(Test<Node> isTerminal) {
		return calcTermdom(new ArrayList<Integer>(), isTerminal);
	}

	public List<Integer> calcTermdom(List<Integer> terms) {
		return calcTermdom(terms, DEFAULT_TERMINAL_TEST);
	}

	/**
	 * Calculate a list of all terminals dominated by this one.
	 * 
	 * @param terms
	 * @return
	 */
	public List<Integer> calcTermdom(List<Integer> terms, Test<Node> isTerminal) {
		if (isTerminal.test(this)) {
			terms.add(getLabel().getNum());
		}

		for (Node n : getChildren()) {
			terms = n.calcTermdom(terms, isTerminal);
		}

		return terms;
	}

	public boolean[] calcTermdom(boolean[] rv) {
		if (lc == null)
			rv[getLabel().getNum() - 1] = true;
		for (Node n : getChildren())
			rv = n.calcTermdom(rv);
		return rv;
	}

	/**
	 * true if the node itself AND all nodes dominated by it only cover
	 * continuous spans.
	 */
	public boolean isContinuous() {
		// System.err.println("Calling isContinuous for " + getLabel());
		boolean ret = true;
		List<Integer> termdom = calcTermdom(new ArrayList<Integer>());
		Collections.sort(termdom);
		// System.err.println(termdom);
		for (int i = 0; i < termdom.size() - 1 && ret; ++i)
			ret &= termdom.get(i) + 1 == termdom.get(i + 1);
		if (ret)
			for (Node child : getChildren()) {
				ret &= child.isContinuous();
				if (!ret)
					return false;
			}
		return ret;
	}

	/**
	 * Return all children of this node split into sublists, each of which is
	 * guaranteed to have a continuous terminal yields.
	 */
	public List<List<Node>> getContinuousChildren() {
		LinkedList<List<Node>> ret = new LinkedList<List<Node>>();
		ret.add(new ArrayList<Node>());
		int lastmax = 0;
		for (Node child : getChildren()) {
			List<Integer> ctd = child.calcTermdom(new ArrayList<Integer>());
			if (ret.getLast().size() > 0 && Collections.min(ctd) - 1 != lastmax) {
				ret.add(new ArrayList<Node>());
			}
			ret.getLast().add(child);
			lastmax = Collections.max(ctd);
		}
		return ret;
	}

	/**
	 * Returns all children of this node split into sublists, each of which
	 * represents a continuous terminal span dominated by this node assuming
	 * that each child is already continuous.
	 * 
	 * @param ignore
	 *            A test that returns {@code true} for terminals that should be
	 *            ignored when they occur as gaps in checking for continuity.
	 * @return
	 */
	public List<List<Node>> getOrderedChildrenBySpan(
			List<Node> orderedTerminals, Test<Node> ignore) {
		Map<Integer, Node> childByTerminal = new HashMap<Integer, Node>();
		int min = Integer.MAX_VALUE;
		int max = 0;

		// determine which child dominates each terminal
		for (Node child : getChildren()) {
			for (Integer terminal : child.calcTermdom()) {
				childByTerminal.put(terminal, child);

				if (terminal < min) {
					min = terminal;
				}

				if (terminal > max) {
					max = terminal;
				}
			}
		}

		List<List<Node>> result = new LinkedList<List<Node>>();
		List<Node> currentSpan = new LinkedList<Node>();
		Set<Node> alreadyInResult = new HashSet<Node>();

		// go through all terminals between the leftmost and the rightmost
		// terminal dominated by this node
		for (int i = min; i <= max; i++) {
			if (childByTerminal.containsKey(i)) {
				Node child = childByTerminal.get(i);

				if (!alreadyInResult.contains(child)) {
					// add child dominating this terminal to current span
					alreadyInResult.add(child);
					currentSpan.add(child);
				}
			} else if (!currentSpan.isEmpty()
					&& !ignore.test(orderedTerminals.get(i - 1))) {
				// encountered gap - open new span
				result.add(currentSpan);
				currentSpan = new LinkedList<Node>();
			}
		}

		if (!currentSpan.isEmpty()) {
			result.add(currentSpan);
		}

		return result;
	}

	public boolean dominates(Node dominated) {
		while (dominated != null) {
			if (dominated == this) {
				return true;
			}

			dominated = dominated.getPa();
		}

		return false;
	}

	/**
	 * Check for dominates relation.
	 * 
	 * @param dominated
	 * @param considerRoot
	 *            Whether the (virtual) root should be considered to dominate
	 *            the other nodes in the tree.
	 * @return
	 */
	public boolean dominates(Node dominated, boolean considerRoot) {
		List<Integer> domlist = dominated.domintlist(true, considerRoot);
		return domlist.contains(getLabel().getNum());
	}

	/**
	 * Return a list of all nodes which dominate this one
	 * 
	 * @param includeself
	 * @param vroot
	 * @return
	 */
	public List<Node> domlist(boolean includeself, boolean vroot) {
		List<Node> ret = new ArrayList<Node>();
		if (includeself)
			ret.add(this);
		Node d = pa;
		while (d.pa != null) {
			ret.add(d);
			d = d.pa;
		}
		if (vroot) {
			ret.add(d);
		}
		return ret;
	}

	/**
	 * Return a list of all numbers of all nodes which dominate this one.
	 * 
	 * @param includeself
	 * @param vroot
	 * @return
	 */
	public List<Integer> domintlist(boolean includeself, boolean vroot) {
		List<Integer> ret = new ArrayList<Integer>();
		if (includeself)
			ret.add(0);
		Node d = pa;
		while (d != null && d.pa != null) {
			ret.add(d.getLabel().getNum());
			d = d.pa;
		}
		if (vroot)
			ret.add(1000);
		return ret;
	}

	/**
	 * Unlink this node and relink it as the new last child of target.
	 * 
	 * @param target
	 * @throws TreebankException
	 */
	public void moveAsChild(Node target) throws TreebankException {
		moveAsChild(target, -1);
	}

	/**
	 * Unlink this node and relink it as the nth child of target. Appending can
	 * be triggered by n == -1.
	 */
	public void moveAsChild(Node target, int n) throws TreebankException {
		Node ls = getLeftSibling();
		Node oldpa = pa;
		Node oldrs = rs;

		// unlink:
		if (ls != null)
			ls.setRs(oldrs);
		else if (oldpa != null) {
			oldpa.setLc(oldrs);
		}

		pa = target;
		List<Node> tkids = target.getChildren();

		if (n == -1) {
			n = tkids.size();
		}

		if (n > tkids.size() || n < 0) {
			throw new TreebankException("Cannot move node");
		}

		rs = (n < tkids.size()) ? tkids.get(n) : null;

		// link:
		if (n > 0) {
			tkids.get(n - 1).setRs(this);
		} else {
			target.setLc(this);
		}
	}

	/**
	 * Unlinks this node and relinks it as a child of {@link target}, trying to
	 * avoid crossing branches.
	 * 
	 * @param target
	 * @throws TreebankException
	 */
	public void moveAsChildInOrder(Node target) throws TreebankException {
		List<Node> children = target.getChildren();
		int size = children.size();
		int position = 0;
		int terminalNumber = Collections.min(calcTermdom());

		while (position < size
				&& Collections.min(children.get(position).calcTermdom()) < terminalNumber) {
			position++;
		}

		moveAsChild(target, position);
	}

	/**
	 * Remove the nth child of this node. Caution, you have to update the
	 * terminal list yourself.
	 */
	public void removeChild(int n) throws TreebankException {
		List<Node> tkids = getChildren();
		if (n >= tkids.size() || n < 0)
			throw new TreebankException("Cannot move node");
		// n == 0
		if (n == 0) {
			lc = lc.rs;
		} else {
			tkids.get(n - 1).setRs(tkids.get(n).getRs());
		}
	}

	/**
	 * remove this node
	 */
	public void unlink() {
		Node ls = getLeftSibling();
		if (ls == null) {
			pa.lc = rs;
		} else {
			ls.rs = rs;
		}
		ls = null;
		rs = null;
		pa = null;
	}

	public String print() {
		if (label == null)
			return super.toString();
		return label.getTag();
	}

	public String toString() {
		return print();
	}

	private boolean isHead;

	public void setIsHead(boolean b) {
		isHead = b;
	}

	public boolean isHead() {
		return isHead;
	}

	public Node getHeadChild() throws TreebankException {		
		for (Node child : getChildren()) {
			if (child.isHead) {
				return child;
			}
		}
		
		throw new TreebankException("No head child marked on " + this);
	}

	public int getHeadChildIndex() {
		List<Node> children = getChildren();
		if (children != null && children.size() > 0) {
			for (int i = 0; i < children.size(); ++i)
				if (children.get(i).isHead())
					return i;
		}
		return -1;
	}
	
	public Node getLexicalHead() throws TreebankException {
		if (isLeaf()) {
			return this;
		}
		
		return getHeadChild().getLexicalHead();
	}

	public boolean isLeaf() {
		return lc == null;
	}

	public void reorderSelfAndAncestors() throws TreebankException {
		Node parent = getPa();

		if (parent != null) {
			unlink();
			parent.addChildInOrder(this);
			parent.reorderSelfAndAncestors();
		}
	}

	public Node lowestCommonAncestor(Node other) {
		Set<Node> ownAncestors = new HashSet<Node>();
		Set<Node> otherAncestors = new HashSet<Node>();
		Node ownAncestor = this;
		Node otherAncestor = other;

		while (true) {
			ownAncestors.add(ownAncestor);

			if (ownAncestor != null) {
				ownAncestor = ownAncestor.getPa();
			}

			if (ownAncestors.contains(otherAncestor)) {
				return otherAncestor;
			}

			otherAncestors.add(otherAncestor);

			if (otherAncestor != null) {
				otherAncestor = otherAncestor.getPa();
			}

			if (otherAncestors.contains(ownAncestor)) {
				return ownAncestor;
			}
		}
	}

	public static Node lowestCommonAncestor(Collection<Node> nodes) {
		int size = nodes.size();
		Iterator<Node> iterator = nodes.iterator();

		if (size < 2) {
			return nodes.iterator().next();
		}

		Node candidate = iterator.next().lowestCommonAncestor(iterator.next());

		while (iterator.hasNext()) {
			if (candidate == null) {
				return null;
			}

			candidate = candidate.lowestCommonAncestor(iterator.next());
		}

		return candidate;
	}

	public int calcGapDegree(List<Node> orderedTerminals,
            Test<Node> ignoreTerminal) {
        return calcGapDegree(orderedTerminals, ignoreTerminal,
                DEFAULT_TERMINAL_TEST);
    }

	public int calcGapDegree(List<Node> orderedTerminals,
			Test<Node> ignoreTerminal, Test<Node> isTerminal) {
		List<Integer> termdom = calcTermdom(isTerminal);
		Collections.sort(termdom);
		int lastTerminalNumber = Integer.MAX_VALUE;
		int result = 0;

		for (Integer terminalNumber : termdom) {
			if (!ignoreTerminal.test(orderedTerminals.get(terminalNumber - 1))) {
				if (terminalNumber - 1 > lastTerminalNumber
						&& !allTerminalsInBetweenPassTest(orderedTerminals,
								lastTerminalNumber, terminalNumber,
								ignoreTerminal)) {
					result++;
				}

				lastTerminalNumber = terminalNumber;
			}
		}

		return result;
	}

	public List<Integer> calcGapLengths(List<Node> orderedTerminals, Test<Node> ignoreTerminal) {
	    return calcGapLengths(orderedTerminals, ignoreTerminal, DEFAULT_TERMINAL_TEST);
	}

	public List<Integer> calcGapLengths(List<Node> orderedTerminals, Test<Node> ignoreTerminal, Test<Node> isTerminal) {
        List<Integer> termdom = calcTermdom(isTerminal);
        Collections.sort(termdom);
        int lastTerminalNumber = Integer.MAX_VALUE;
        List<Integer> result = new ArrayList<Integer>();
        for (Integer terminalNumber : termdom) {
            if (!ignoreTerminal.test(orderedTerminals.get(terminalNumber - 1))) {
                if (terminalNumber - 1 > lastTerminalNumber
                        && !allTerminalsInBetweenPassTest(orderedTerminals, lastTerminalNumber, terminalNumber, ignoreTerminal)) {
                    result.add(terminalNumber - 1 - lastTerminalNumber);
                    //System.err.println("gap: " + lastTerminalNumber + "-" + (terminalNumber - 1));
                }
                lastTerminalNumber = terminalNumber;
            }
        }
        return result;
	}
	
    private boolean allTerminalsInBetweenPassTest(List<Node> orderedTerminals,
			int before, Integer after, Test<Node> test) {
		for (int i = before + 1; i < after; i++) {
			if (!test.test(orderedTerminals.get(i - 1))) {
				return false;
			}
		}

		return true;
	}

	public List<Node> getAncestors() {
		List<Node> result = new ArrayList<Node>();
		getAncestors(result);
		return result;
	}

	public List<Node> getDominatingNodes() {
		List<Node> result = new ArrayList<Node>();
		getDominatingNodes(result);
		return result;
	}

	public void getAncestors(List<Node> ancestors) {
		Node node = getPa();

		while (node != null) {
			ancestors.add(node);
			node = node.getPa();
		}
	}

	public void getDominatingNodes(List<Node> dominatingNodes) {
		dominatingNodes.add(this);
		getAncestors(dominatingNodes);
	}

	/**
	 * If this node is a leaf, then calling this method and removes it, then
	 * calling this method recursively on the parent.
	 */
	public void removeEmptyNonTerminals() {
		Node parent = getPa();

		if (isLeaf()) {
			unlink();
			parent.removeEmptyNonTerminals();
		}
	}

	public Node lexicalHead() throws TreebankException {
		if (isLeaf()) {
			return this;
		}
		
		for (Node child : getChildren()) {
			if (child.isHead()) {
				return child.lexicalHead();
			}
		}
		
		throw new TreebankException("No head marked on " + this);
	}

	public int terminalNumber(List<Node> orderedTerminals) throws TreebankException {
		int size = orderedTerminals.size();
		
		for (int i = 0; i < size; i++) {
			if (orderedTerminals.get(i) == this) {
				return i;
			}
		}
		
		throw new TreebankException("Node " + this + " not found among given terminals.");
	}

}
