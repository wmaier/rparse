/*******************************************************************************
 * File Estimate.java
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
package de.tuebingen.rparse.grammar.estimates;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.tuebingen.rparse.grammar.BinaryRCG;
import de.tuebingen.rparse.grammar.GrammarProcessingTask;
import de.tuebingen.rparse.misc.Numberer;

/**
 * Baseclass for outside estimates. The subclasses must implement {@code get} (at least) which provides the actual
 * estimate. Here, labels are provided and a mapping from labels to unique numbers to be used in charts.
 * 
 * @author wmaier
 */
public abstract class Estimate implements GrammarProcessingTask, Serializable {

    // for serialization
    private static final long       serialVersionUID = 1L;

    /**
     * binarized grammar (we don't want to serialize it)
     */
    transient protected BinaryRCG   bg;

    /**
     * maximum sentence length up to which the estimate is computed
     */
    protected int                   maxlen;

    /**
     * the numberer (for the classes below to use)
     */
    protected Numberer              nb;

    /**
     * maps labels to unique numbers so we can use them to index our charts
     */
    protected Map<Integer, Integer> labelToCnt;

    /**
     * inverse mapping for {@code labelToCnt}.
     */
    protected Map<Integer, Integer> cntToLabel;

    // just the non-terminals
    private Set<Integer>            nonterminalsOnly;

    // just the pre-terminals
    private Set<Integer>            preterminals;

    // both (we need to check sizes)
    private Set<Integer>            nonterminalsAndPreterminals;

    /**
     * A logger (for the classes below to take)
     */
    transient protected Logger      logger;

    /**
     * Constructor.
     * 
     * @param bg
     *            A binary grammar on which we compute the estimate
     * @param nb
     *            The numberer of the grammar
     * @param sentlen
     *            The maximum sentence length up to which to compute the estimate.
     */
    public Estimate(BinaryRCG bg, Numberer nb, int sentlen) {
        logger = Logger.getLogger(Estimate.class.getPackage().getName());

        this.bg = bg;
        this.nb = nb;

        this.maxlen = sentlen;

        nonterminalsOnly = new HashSet<Integer>();
        nonterminalsOnly.addAll(bg.getLabels());

        preterminals = new HashSet<Integer>();
        preterminals.addAll(bg.getPreterminals());

        nonterminalsAndPreterminals = new HashSet<Integer>();
        nonterminalsAndPreterminals.addAll(nonterminalsOnly);
        nonterminalsAndPreterminals.addAll(preterminals);

        int allLabelsSize = nonterminalsAndPreterminals.size();
        int nonterminalsSize = nonterminalsOnly.size();
        int preterminalsSize = preterminals.size();

        // there can be duplicate labels, we accept that
        if (allLabelsSize != nonterminalsSize + preterminalsSize) {
            logger.warning("The total number of labels (" + allLabelsSize
                    + ") is not the sum ("
                    + (preterminalsSize + nonterminalsSize)
                    + ") of the number of preterminals (" + preterminalsSize
                    + ") and true non-terminals (" + nonterminalsSize + ").");
        }

        // do the mapping
        cntToLabel = new HashMap<Integer, Integer>();
        labelToCnt = new HashMap<Integer, Integer>();
        int labelcnt = 0;
        for (int pos : preterminals) {
            cntToLabel.put(labelcnt, pos);
            labelToCnt.put(pos, labelcnt);
            labelcnt++;
        }
        for (int label : nonterminalsOnly) {
            cntToLabel.put(labelcnt, label);
            labelToCnt.put(label, labelcnt);
            labelcnt++;
        }

    }

    /**
     * Get a label for a chart index number.
     * 
     * @param index
     * @return The corresponding value.
     */
    public int labelForIndex(int count) {
        return cntToLabel.get(count);
    }

    /**
     * Get the chart index number for a label.
     * 
     * @param label
     * @return The corresponding value. An {@code IllegalStateException} gets thrown if the label was not in the map,
     *         since this should not happen.
     */
    public int countForLabel(int label) {
        if (labelToCnt == null) {
            throw new IllegalStateException("labelToCnt is null!");
        }
        return labelToCnt.get(label);
    }

    /**
     * Get all labels
     * 
     * @return
     */
    public Set<Integer> nonterminalsAndPreterminals() {
        return Collections.unmodifiableSet(nonterminalsAndPreterminals);
    }

    /**
     * Get the preterminals
     * 
     * @return
     */
    public Set<Integer> preterminals() {
        return Collections.unmodifiableSet(preterminals);
    }

    /**
     * Get the true non-terminals without the preterminals
     * 
     * @return
     */
    public Set<Integer> nonterminalsOnly() {
        return Collections.unmodifiableSet(nonterminalsOnly);
    }

    /**
     * After computing the estimate, with this method subclasses should provide statistics about the computation.
     * 
     * @return A human-readable string containing the statistics.
     */
    abstract public String getStats();

    /**
     * Get the outside estimate for an item, represented by the values passed as parameters.
     * 
     * @param slen
     *            The sentence length of the sentence which is currently parsed
     * @param state
     *            The label for which to get the estimate.
     * @param vechc
     *            The range vector, represented as BitSet (on bits are covered terminals)
     * @param tags
     *            The set of tags in the sentence. Just in case.
     * @return The outside estimate for the provided data
     */
    abstract public double get(int slen, int state, BitSet vechc, int[] tags);

    abstract public double get(int slen, int state, int ll, int lr, int rl, int rr);
    
    
    /**
     * Serialize this estimate.
     * 
     * @param filename
     *            The location
     * @throws IOException
     *             In case of unexpected I/O
     */
    public void write(String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(this);
        out.close();
    }

    /**
     * Read a serialized estimate
     * 
     * @param filename
     *            The location
     * @return The estimate
     * @throws IOException
     *             In case of unexpected I/O
     * @throws ClassNotFoundException
     *             In case of programming flukes
     */
    public static Estimate read(String filename) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(fis);
        Estimate ret = (Estimate) in.readObject();
        in.close();
        // loggers can't be serialized, so we have to get a new one here
        ret.logger = Logger.getLogger(Estimate.class.getPackage().getName());
        return ret;
    }

    /**
     * Set a logger from outside. Needed in order to set a logger after deserialization.
     * 
     * @param logger
     *            The logger.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
