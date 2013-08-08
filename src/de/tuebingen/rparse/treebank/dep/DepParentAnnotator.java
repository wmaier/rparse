/*******************************************************************************
 * File DepParentAnnotator.java
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Utilities;
import de.tuebingen.rparse.treebank.VerticalMarkovizer;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * Performs parent annotation for dependencies.
 * @author wmaier
 *
 */
public class DepParentAnnotator extends
		ProcessingTask<DependencyForest<DependencyForestNodeLabel, String>>
		implements VerticalMarkovizer, Serializable {

	private static final long serialVersionUID = 3378280549864084537L;

	// TODO: Move those somewhere else and share them with the consituency stuff
	public final static String VERTICALSEP = "^";
	public final static String HORIZONTALSEP = "-";
	public final static String DEPROOT = "TOP";

	private int v;
	
	private boolean markovNoArities;

	protected Numberer nb;

	public DepParentAnnotator(int v, boolean markovNoArities, Numberer nb) {
		this.v = v;
		this.markovNoArities = markovNoArities;
		this.nb = nb;
	}

	@Override
	public void done() throws TreebankException {
	}

	/*
	 * watch out: vertical history has to have an upper limit since negra conversion from
	 * Daum et al. produces cycles
	 */
	@Override
	public void processSentence(
			DependencyForest<DependencyForestNodeLabel, String> t)
			throws TreebankException {
		t.setVerticalDepth(v);
		for (int i = 1; i <= t.getNodeCount(); ++i) {
			// System.err.println("Processing node " + t.getNode(i).getToken());
			List<Integer> verticalHistory = new ArrayList<Integer>();
			// get vertical history for the node
			DependencyForestNode<DependencyForestNodeLabel, String> myNode = t
					.getNode(i);
			int cnt = 0;
			do {
				verticalHistory.add(myNode.getID());
				myNode = myNode.getHead();
				cnt++;
			} while (cnt <= v && myNode != null);
			int endpoint = v > verticalHistory.size() ? verticalHistory.size()
					: v;
			verticalHistory = verticalHistory.subList(0, endpoint);
			String labelverticalHistory = "";
			for (Integer id : verticalHistory) {
				int arity = 1;
				if (t.getHeadID(id) != 0) {
					int head = t.getHeadID(id);
					List<List<Integer>> dom = Utilities.splitContinuous(t
							.getNode(head).projectionAsInt());
					arity = dom.size();
					arity = t.getGapDegree() + 1;
				}
				String tag = t.getRelation(id);
				tag = tag == null ? DEPROOT : tag;
				labelverticalHistory += VERTICALSEP + tag;
				if (!markovNoArities) {
					labelverticalHistory += String.valueOf(arity);
				}
			}
			t.setVertical(i, labelverticalHistory);
		}
	}

}
