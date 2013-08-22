/*******************************************************************************
 * File Lexicon.java
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
package de.tuebingen.rparse.treebank.lex;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Word/tag lexcion, openclass stuff is for LoPar and BitPar.
 * @author wmaier
 *
 */
public class Lexicon implements Serializable {

    private static final long serialVersionUID = 5942328918616505247L;

    private Map<Integer, Map<Integer, Integer>> wordToTag;

    private Map<Integer, Set<Integer>> tagToWord;
	
    private Map<Integer, Integer> tagCounts;
    
    private Map<Integer, Map<Integer,Integer>> wordTagCounts;

    private Map<Integer, Integer> wordCounts;
    
    private Set<Integer> preterminals;
    
    private Map<Integer,Integer> openClassLower;
    
    private Map<Integer,Integer> openClassUpper;
    
    private Map<Integer,Integer> openClassAll;

    private Numberer nb;

    /**
     * Construct a new lexicon.
     * @param nb Numberer which must hold the number/label mappings.
     */
    public Lexicon(Numberer nb) {
        wordToTag = new HashMap<Integer, Map<Integer,Integer>>();
	tagToWord = new HashMap<Integer, Set<Integer>>();
        tagCounts = new HashMap<Integer, Integer>();
        wordTagCounts = new HashMap<Integer, Map<Integer,Integer>>();
	wordCounts = new HashMap<Integer, Integer>();
        preterminals = new HashSet<Integer>();
        openClassLower = new HashMap<Integer,Integer>();
        openClassUpper = new HashMap<Integer,Integer>();
        openClassAll = new HashMap<Integer,Integer>();
        this.nb = nb;
    }

    /**
     * Add a new word/tag pair to the lexicon.
     * @param word The word.
     * @param tag The tag.
     */
    public void addPair(String word, String tag) {
        int wnum = nb.number(LexiconConstants.LEXWORD, word);
        int tnum = tag.endsWith("1") ? nb.number(GrammarConstants.PREDLABEL, tag) : nb.number(GrammarConstants.PREDLABEL, tag + "1");
        if (!tagCounts.containsKey(tnum))
            tagCounts.put(tnum, 0);
        tagCounts.put(tnum, tagCounts.get(tnum) + 1);

	if (!wordCounts.containsKey(wnum))
	    wordCounts.put(wnum, 0);
	wordCounts.put(wnum, wordCounts.get(wnum) + 1);

	if (!wordTagCounts.containsKey(wnum)) {
	    wordTagCounts.put(wnum, new HashMap<Integer,Integer>());
	}
	if (!wordTagCounts.get(wnum).containsKey(tnum)) {
	    wordTagCounts.get(wnum).put(tnum, 0);
	}
	wordTagCounts.get(wnum).put(tnum, wordTagCounts.get(wnum).get(tnum) + 1);

        if (!wordToTag.containsKey(wnum))
            wordToTag.put(wnum, new HashMap<Integer,Integer>());
        if (!wordToTag.get(wnum).containsKey(tnum))
	    wordToTag.get(wnum).put(tnum, 0);
        wordToTag.get(wnum).put(tnum, wordToTag.get(wnum).get(tnum) + 1);

	if (!tagToWord.containsKey(tnum))
	    tagToWord.put(tnum, new HashSet<Integer>());
	tagToWord.get(tnum).add(wnum);

        if (Character.isUpperCase(word.charAt(0))) {
	    if (!openClassUpper.containsKey(tnum)) 
		openClassUpper.put(tnum, 0);
	    openClassUpper.put(tnum, openClassUpper.get(tnum) + 1);
        } else {
	    if (!openClassLower.containsKey(tnum))
		openClassLower.put(tnum, 0);
	    openClassLower.put(tnum, openClassLower.get(tnum) + 1);
        }
    	if (!openClassAll.containsKey(tnum))
	    openClassAll.put(tnum, 0);
    	openClassAll.put(tnum, openClassAll.get(tnum) + 1);
        preterminals.add(tnum);
    }
    
    /**
     * Get the numbers of all POS tags in this lexicon.
     * @return An unmodifiable set of integers.
     */
    public Set<Integer> getPreterminals() {
    	return Collections.unmodifiableSet(preterminals);
    }

    @Override
	public String toString() {
        String ret = "";
        for (int i : wordToTag.keySet()) {
            ret += (String) nb.getObjectWithId(LexiconConstants.LEXWORD, i) + "\t";
            for (Integer j : wordToTag.keySet())
                ret += (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, j) + " ";
            ret += "\n";
        }
        return ret;
    }

    /**
     * Get the numberer of this lexicon which hold the word/tag mappings.
     * @return The numberer
     */
    public Numberer getNumberer() {
	return nb;
    }

    /**
     * Get the numbers of all words in this lexicon.
     * @return An unmodifiable set of integers.
     */
    public Set<Integer> getWords() {
	return Collections.unmodifiableSet(wordToTag.keySet());
    }

    /**
     * Get all tags (as integers) for a word (given as an integer).
     * @param i The number of the word.
     * @return The set of integers corresponding to all the POS tags of this word.
     */
    public Set<Integer> getTagForWord(Integer i) {
	return Collections.unmodifiableSet(wordToTag.get(i).keySet());
    }

    public Set<Integer> getWordsForTag(int i) {
	return Collections.unmodifiableSet(tagToWord.get(i));
    }

    public Integer getTagCounter(Integer word, Integer tag) {
	if (wordToTag.containsKey(word)) {
	    if (wordToTag.get(word).containsKey(tag)) {
		return wordToTag.get(word).get(tag);
	    }
	}
	return -1;
    }

    public int getWordCounter(int word) {
	int result = -1;
	if (wordToTag.containsKey(word)) {
	    for (int tag : wordToTag.get(word).keySet()) {
		result += wordToTag.get(word).get(tag);
	    }
	}
	return result;
    }

    public Double getScore(int word, int tag) {
	return wordTagCounts.get(word).get(tag) / new Double(wordCounts.get(word));
    }

    public Set<Integer> getOcLowerLabels() {
	return Collections.unmodifiableSet(openClassLower.keySet());
    }

    public Set<Integer> getOcUpperLabels() {
	return Collections.unmodifiableSet(openClassUpper.keySet());
    }

    public Set<Integer> getOcAllLabels() {
	return Collections.unmodifiableSet(openClassAll.keySet());
    }

    public Integer getOcLowerLabelCounter(Integer label) {
	if (openClassLower.containsKey(label)) {
	    return openClassLower.get(label);
	}
	return -1;
    }

    public Integer getOcUpperLabelCounter(Integer label) {
	if (openClassUpper.containsKey(label)) {
	    return openClassUpper.get(label);
	}
	return -1;
    }

    public Integer getOcAllLabelCounter(Integer label) {
	if (openClassAll.containsKey(label)) {
	    return openClassAll.get(label);
	}
	return -1;
    }
	
    public void printTagCounts() {
	System.out.println("Tag counts:");
	for (int tnum : tagCounts.keySet()) {
	    String tag = (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, tnum);
	    System.out.println(tag + " " + tagCounts.get(tnum));
	}
    }

}
