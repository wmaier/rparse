/*******************************************************************************
 * File DependencyForestNode.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a node in a {@link DependencyForest}.
 * 
 * @author ke
 * 
 * @param <T>
 * @param <R>
 */
public class DependencyForestNode<T, R> {

	private static final int STATE_BEFORE = 1;

	private static final int STATE_DURING = 2;

	private static final int STATE_GAP_OR_AFTER = 3;

	private DependencyForest<T, R> graph;

	private int id;

	/**
	 * Creates an object representing the node with ID {@code id} of the graph
	 * {@code graph}.
	 * 
	 * @param graph
	 * @param id
	 */
	DependencyForestNode(DependencyForest<T, R> graph, int id) {
		this.graph = graph;
		this.id = id;
	}

	/**
	 * @return This node's ID, i.e. its position in the sentence that the
	 *         dependency graph represents, starting from 1.
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return The token associated with this node.
	 */
	public T getToken() {
		return graph.getToken(id);
	}

	/**
	 * @return This node's head, or {@code null} if it does not have a head.
	 */
	public DependencyForestNode<T, R> getHead() {
		return graph.getHead(id);
	}

	/**
	 * @return The dependency relation in which this node stands with its head,
	 *         or {@code null} if it does not have a head.
	 */
	public R getRelation() {
		return graph.getRelation(id);
	}

	public void setRelation(R relation) {
		graph.setRelation(id, relation);
	}

	/**
	 * @return The set of all of this node's modifiers.
	 */
	public Set<DependencyForestNode<T, R>> getModifiers() {
		return graph.getModifiers(id);
	}

	/**
	 * @return Whether or not this node is marked as the root of the graph.
	 */
	public boolean isRoot() {
		return graph.isRoot(id);
	}

	/**
	 * @return The graph of which this node is a part.
	 */
	public DependencyForest<T, R> getGraph() {
		return graph;
	}

	/**
	 * @return A list of nodes representing the projection of this node, cf.
	 *         {@link http://portal.acm.org/citation.cfm?id=1273139}, section 2.
	 */
	public List<DependencyForestNode<T, R>> projection() {
		int nodeCount = graph.getNodeCount();
		boolean[] inProjection = graph.projection(id);
		List<DependencyForestNode<T, R>> result = new ArrayList<DependencyForestNode<T, R>>();

		for (int i = 0; i < nodeCount; i++) {
			if (inProjection[i]) {
				result.add(graph.getNode(i + 1));
			}
		}

		return result;
	}
	
	/**
	 * @return A list of integers representing the projection of this node (position indices)
	 */
	public List<Integer> projectionAsInt() {
		int nodeCount = graph.getNodeCount();
		boolean[] inProjection = graph.projection(id);
		List<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < nodeCount; i++) {
			if (inProjection[i]) {
				result.add(graph.getNode(i + 1).id);
			}
		}

		return result;		
	}
	

	/** 
	 * @return The size of the largest gap of this node
	 */
	public int gapSize() {
		int n = graph.getNodeCount();
		boolean[] inProjection = graph.projection(id);
		int maxsize = 0;
		int sizecnt = 0;
		int state = STATE_BEFORE;

		for (int i = 0; i < n; i++) {
			switch (state) {
			case STATE_BEFORE:
				if (inProjection[i]) {
					state = STATE_DURING;
				}

				break;
			case STATE_DURING:
				if (!inProjection[i]) {
					state = STATE_GAP_OR_AFTER;
				}

				break;
			case STATE_GAP_OR_AFTER:
				if (inProjection[i]) {
					maxsize = maxsize > sizecnt ? maxsize : sizecnt;
					sizecnt = 0;
					state = STATE_DURING;
				}
				sizecnt++;
			}
		}

		return maxsize;
	}

	/**
	 * @return The gap degree of this node, cf. {@link http
	 *         ://portal.acm.org/citation.cfm?id=1273139}, section 3.
	 */
	public int gapDegree() {
		int n = graph.getNodeCount();
		boolean[] inProjection = graph.projection(id);
		int degree = 0;
		int state = STATE_BEFORE;

		for (int i = 0; i < n; i++) {
			switch (state) {
			case STATE_BEFORE:
				if (inProjection[i]) {
					state = STATE_DURING;
				}

				break;
			case STATE_DURING:
				if (!inProjection[i]) {
					state = STATE_GAP_OR_AFTER;
				}

				break;
			case STATE_GAP_OR_AFTER:
				if (inProjection[i]) {
					degree++;
					state = STATE_DURING;
				}
			}
		}

		return degree;
	}

	/**
	 * @return The edge degree of the incoming edge, or {@code -1} if there is
	 *         no such edge. Cf. {@link http
	 *         ://portal.acm.org/citation.cfm?id=1273139}, section 3.
	 */
	public int edgeDegree() {
		int headID = graph.getHeadID(id);

		if (headID == 0) {
			return -1;
		}

		int from = Math.min(id, headID) + 1;
		int to = Math.max(id, headID) - 1;
		int length = to - from + 1;

		if (length < 1) {
			return 0;
		}

		boolean[] dontCount = new boolean[graph.getNodeCount()];
		// don't count nodes dominated by the head:
		graph.project(headID, dontCount);
		int degree = 0;

		for (int i = from; i <= to; i++) {
			if (!dontCount[i - 1]) {
				degree++;
				// don't count nodes connected to nodes already counted:
				graph.project(i, from, to, dontCount);
			}
		}

		return degree;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode(graph);
		result = prime * result + id;
		return result;
	}

	private int hashCode(Object o) {
		if (o == null) {
			return 0;
		}

		return o.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof DependencyForestNode<?, ?>)) {
			return false;
		}

		DependencyForestNode<?, ?> that = (DependencyForestNode<?, ?>) o;
		return equals(graph, that.graph) && id == that.id;
	}

	private boolean equals(Object one, Object another) {
		if (one == another) {
			return true;
		}

		if (one == null) {
			return false;
		}

		return one.equals(another);
	}

	public String toString() {
		return "(" + id + ")";
	}

}
