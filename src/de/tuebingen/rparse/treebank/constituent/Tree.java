/*******************************************************************************

 * File Tree.java
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.NullTest;
import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Test;
import de.tuebingen.rparse.treebank.HasGapDegree;
import de.tuebingen.rparse.treebank.HasID;
import de.tuebingen.rparse.treebank.HasSize;
import de.tuebingen.rparse.treebank.lex.LexiconConstants;
import de.tuebingen.rparse.treebank.lex.ParserInput;

/**
 * This class represents a tree in a treebank. We support crossing branches. {@TreebankProcessor}s
 * of Trees must respect the following conventions:
 * <ul>
 * <li>{@link #id -1} -1} -1} -1} -1} -1} must contain some value != {@value -1}</li>
 * <li>{@link #root} must contain the root node of the tree</li>
 * <li>{@link #terminals} must contain all terminals of the tree, numbered in ascending order starting with 1</li>
 * <li>{@link #lastterm} must contain the number of the last terminal</li>
 * </ul>
 * Optionally, a start string may be set (e.g., the first and the last line in the export format). {@link #nodes} may
 * optionally contain a mapping of terminal and nonterminal node numbers on the node objects. The convention is to use
 * export format numbering for the nonterminals, i.e., the first nonterminal is numbered 500 and for all nodes {@value
 * n}, it holds that all nodes {@value m_1 ... m_n} dominated by {@value n} have a lower number than {@value n}.
 * {@link #lastnont} may optionally contain the number of the last nonterminal. Also see {@link #calcExportNumbering()}
 * and {@link #hasExportNumbering()}.
 * 
 * @author wmaier
 */
public class Tree extends ParserInput implements HasSize, HasID, HasGapDegree {

    public static final String TOPLABEL = "VROOT";

    private Integer            id;
    private Node               root;
    private Numberer           nb;
    private List<Node>         terminals;
    private int                lastterm;

    private String             bstring;
    private String             estring;
    private Map<Integer, Node> nodes;
    private int                lastnont;

    private String             display;

    /**
     * Create a new Tree.
     */
    public Tree(Numberer nb) {
        this(null, nb);
    }

    /**
     * Create a new tree with a given root.
     * 
     * @param root
     */
    public Tree(Node root, Numberer nb) {
        this(-1, root, nb);
    }

    /**
     * Create a new tree with a given root and id
     * 
     * @param id
     * @param root
     */
    public Tree(Integer id, Node root, Numberer nb) {
        this.id = id;
        this.root = root;
        this.nb = nb;
        this.bstring = "";
        this.estring = "";
        this.nodes = null;
        this.terminals = null;
        this.lastterm = -1;
        this.lastnont = -1;
        this.display = "";
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(Integer integer) {
        this.id = integer;
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public List<Node> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<Node> terminals) {
        this.terminals = terminals;
    }

    public void removeTerminal(Node terminal) {
        terminals.remove(terminal);
    }

    public int getLastterm() {
        return lastterm;
    }

    public void setLastterm(int lastterm) {
        this.lastterm = lastterm;
    }

    /**
     * Find a node in the tree by dfs search
     * 
     * @param num
     * @return
     */
    public Node findNode(int num) {
        return getRoot().findNode(num);
    }

    /**
     * Get a node. Requires nonterminal numbering to be precalculated.
     * 
     * @return
     */
    public Node getNode(int num) {
        if (hasExportNumbering()) {
            return nodes.get(num);
        }
        return null;
    }

    public Map<Integer, Node> getNodes() {
        return nodes;
    }

    public void setNodes(Map<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public int getLastnont() {
        return lastnont;
    }

    public void setLastnont(int lastnont) {
        this.lastnont = lastnont;
    }

    public String getBstring() {
        if (bstring.equals("")) {
            return "#BOS " + String.valueOf(id);
        } else {
            return bstring;
        }
    }

    public void setBstring(String bstring) {
        this.bstring = bstring;
    }

    public String getEstring() {
        if (bstring.equals("")) {
            return "#EOS " + String.valueOf(id);
        } else {
            return estring;
        }
    }

    public void setEstring(String estring) {
        this.estring = estring;
    }

    public boolean isContinuous() {
        return getRoot().isContinuous();
    }

    /*
     * Treedist eval
     */
    public void reduceToRoof() {
        for (Node n : getRoot().getNodes(new ArrayList<Node>()))
            if (!n.hasChildren())
                n.unlink();
    }

    /**
     * True if the tree doesn't have nodes.
     * 
     * @return
     */
    public boolean isEmpty() {
        return (root == null || root.getLc() == null);
    }

    public boolean hasExportNumbering() {
        return getNodes() != null && getLastnont() > -1
                && !getBstring().equals("") && !getEstring().equals("");
    }

    public void calcExportNumbering() {
        getRoot().numResetNonterminals(-1);
        int num = getRoot().numAssign(999) + 1;
        Map<Integer, Integer> trans = new HashMap<Integer, Integer>();
        for (int i = num; i < 999; ++i)
            trans.put(i, 500 + i - num);
        trans.put(999, 0);
        getRoot().numTranslate(trans);
        this.nodes = new HashMap<Integer, Node>();
        for (Node n : getRoot().getNodes(new ArrayList<Node>())) {
            nodes.put(n.getLabel().getNum(), n);
        }
        setBstring("#BOS " + Integer.valueOf(getId()));
        setEstring("#EOS " + Integer.valueOf(getId()));
        setLastnont(500 + 999 - num - 1);
    }

    /**
     * Add a gorn address as property of every node
     * 
     * @author wmaier
     */
    public void addGorn() {
        root.addGorn("");// String.valueOf(id));
    }

    /*
     * A helper class for comparing terminals
     */
    private class Termcomp implements Comparator<Node> {

        public int compare(Node arg0, Node arg1) {
            int n0 = arg0.getLabel().getNum();
            int n1 = arg1.getLabel().getNum();
            if (n0 == n1)
                return 0;
            if (n0 < n1)
                return -1;
            if (n0 > n1)
                return 1;
            return 0;
        }

    }

    /**
     * Return terminals in ascending order
     * 
     * @return
     */
    public List<Node> getOrderedTerminals() {
        List<Node> ret = new ArrayList<Node>(terminals);
        Collections.sort(ret, new Termcomp());
        return Collections.unmodifiableList(ret);
    }

    /**
     * Numbers the terminals from {@code 1} to {@code n}, where {@code n} is the number of terminals, and calls
     * {@code setLastterm(n)}.
     */
    public void calcTermNums() {
        int i = 0;

        for (Node terminal : getOrderedTerminals()) {
            terminal.getLabel().setNum(++i);
        }

        setLastterm(i);
    }

    @Override
    public String toString() {
        if (root != null) {
            return root.toString();
        } else {
            return "";
        }
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Override
    public void setWord(int index, Integer word) {
        getOrderedTerminals().get(index).getLabel()
                .setWord((String) nb.getObjectWithId(LexiconConstants.INPUTWORD, word));
    }

    @Override
    public void setTag(int index, Integer tag) {
        getOrderedTerminals().get(index).getLabel()
                .setTag((String) nb.getObjectWithId(GrammarConstants.PREDLABEL, tag));
    }

    @Override
    public int[] getWords() {
        List<Node> terminals = getOrderedTerminals();
        int[] result = new int[terminals.size()];
        int i = 0;

        for (Node terminal : terminals) {
            result[i++] = nb.number(LexiconConstants.INPUTWORD, terminal.getLabel()
                    .getWord());
        }

        return result;
    }

    @Override
    public int[] getTags() {
        List<Node> terminals = getOrderedTerminals();
        int[] result = new int[terminals.size()];
        int i = 0;

        for (Node terminal : terminals) {
            result[i++] = nb.number(GrammarConstants.PREDLABEL, terminal.getLabel().getTag()
                    + "1");
        }

        return result;
    }

    @Override
    public int size() {
        return this.getOrderedTerminals().size();
    }

    @Override
    public Set<Integer> tagsAsSet() {
        Set<Integer> result = new HashSet<Integer>();

        for (int tag : getTags()) {
            result.add(tag);
        }

        return result;
    }

    public int calcGapDegree(Test<Node> ignoreTerminal) {
        int record = 0;
        List<Node> orderedTerminals = getOrderedTerminals();

        for (Node node : getRoot().getNodes()) {
            int gapDegree = node
                    .calcGapDegree(orderedTerminals, ignoreTerminal);

            if (gapDegree > record) {
                record = gapDegree;
            }
        }

        return record;
    }

    public int calcGapDegree() {
        return calcGapDegree(new NullTest<Node>());
    }
    
    @Override
    public int getGapDegree() {
        return calcGapDegree();
    }
    
    /**
     * Get the gap degrees of all nodes in depth-first order
     * @return The list with the corresponding integers
     */
    public List<Integer> getNodeGapDegrees(Test<Node> ignoreTerminal, boolean withTerminals) {
        List<Integer> ret = new ArrayList<Integer>();
        List<Node> orderedTerminals = getOrderedTerminals();

        for (Node node : getRoot().getNodes()) {
            if (withTerminals) {
                ret.add(node.calcGapDegree(orderedTerminals, ignoreTerminal));
            } else {
                if (!node.isLeaf()) {
                    ret.add(node.calcGapDegree(orderedTerminals, ignoreTerminal));
                }
            }
        }
        
        return ret;
    }
    
    public List<Integer> getNodeGapDegrees(boolean withTerminals) {
        return getNodeGapDegrees(new NullTest<Node>(), withTerminals);
    }
    
    public List<Integer> calcGapLengths() {
        return calcGapLengths(new NullTest<Node>());
    }
    
    public List<Integer> calcGapLengths(Test<Node> ignoreTerminal) {
        List<Integer> ret = new ArrayList<Integer>(); 
        List<Node> orderedTerminals = getOrderedTerminals();

        for (Node node : getRoot().getNodes()) {
            ret.addAll(node.calcGapLengths(orderedTerminals, ignoreTerminal));
        }
        return ret;
    }
    

}
