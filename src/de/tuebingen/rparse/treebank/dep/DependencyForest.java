/*******************************************************************************
 * File DependencyForest.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tuebingen.rparse.treebank.HasGapDegree;
import de.tuebingen.rparse.treebank.HasID;
import de.tuebingen.rparse.treebank.HasSize;

/**
 * Represents a dependency graph
 * 
 * @author ke, wmaier
 * 
 * @param <T>
 *            The type of tokens
 * @param <R>
 *            The type of relations
 */
public class DependencyForest<T, R> implements HasSize, HasID, HasGapDegree {

	private int nodeCount;

	private Map<Integer, T> tokenByID;

	private Map<Integer, Integer> headByModifierID;

	private Map<Integer, R> relationByModifierID;

	private Map<Integer, DependencyForestNode<T, R>> nodeByID;

	private Map<Integer, Set<Integer>> modifierIDsByHeadID;

	private Map<Integer, String> verticalHistoryByID;

	private int verticalDepth;

	// necessary for parser output comparison
	public int id;

	/**
	 * Creates an empty {@link DependencyForest}.
	 */
	public DependencyForest() {
		nodeCount = 0;
		nodeByID = new HashMap<Integer, DependencyForestNode<T, R>>();
		tokenByID = new HashMap<Integer, T>();
		headByModifierID = new HashMap<Integer, Integer>();
		relationByModifierID = new HashMap<Integer, R>();
		modifierIDsByHeadID = new HashMap<Integer, Set<Integer>>();
		verticalHistoryByID = new HashMap<Integer, String>();
		verticalDepth = 0;
	}

	/**
	 * Adds a node associated with the specified token to the graph. Nodes must
	 * be added to the graph through this method in the order of their
	 * occurrence in the sentence.
	 * 
	 * @param token
	 */
	public void addNode(T token) {
		nodeCount++;
		nodeByID.put(nodeCount, new DependencyForestNode<T, R>(this, nodeCount));
		tokenByID.put(nodeCount, token);
	}

	/**
	 * Adds an edge between a head node and a modifier node to the graph,
	 * labeled with the specified relation. The ID of a node is its order of
	 * occurrence in the sentence, starting from 1.
	 * 
	 * @param modifierID
	 * @param headID
	 * @param relation
	 */
	public void addEdge(int modifierID, int headID, R relation) {
		headByModifierID.put(modifierID, headID);
		relationByModifierID.put(modifierID, relation);
		ensureModifierSetExists(headID);
		modifierIDsByHeadID.get(headID).add(modifierID);
	}

	@Override
	public int size() {
		return tokenByID.size();
	}

	/**
	 * @return The number of nodes in the graph.
	 */
	public int getNodeCount() {
		return nodeCount;
	}

	/**
	 * @return The number of edges in the graph.
	 */
	public int edgeCount() {
		return headByModifierID.size();
	}

	public Set<DependencyForestNode<T, R>> roots() {
		Set<DependencyForestNode<T, R>> result = new HashSet<DependencyForestNode<T, R>>();

		for (int i = 0; i < nodeCount; i++) {
			if (isRoot(i)) {
				result.add(nodeByID.get(i));
			}
		}

		return Collections.unmodifiableSet(result);
	}

	/**
	 * @return A list of the nodes of the graph, in their order of occurrence.
	 */
	public List<DependencyForestNode<T, R>> nodes() {
		List<DependencyForestNode<T, R>> nodes = new ArrayList<DependencyForestNode<T, R>>(
				nodeCount);

		for (int i = 1; i <= nodeCount; i++) {
			nodes.add(nodeByID.get(i));
		}

		return Collections.unmodifiableList(nodes);
	}

	public DependencyForestNode<T, R> getNode(int id) {
		return nodeByID.get(id);
	}

	boolean isRoot(int id) {
		return headByModifierID.get(id) == null;
	}

	T getToken(int id) {
		return tokenByID.get(id);
	}

	DependencyForestNode<T, R> getHead(int modifierID) {
		return nodeByID.get(headByModifierID.get(modifierID));
	}

	int getHeadID(int modifierID) {
		Integer result = headByModifierID.get(modifierID);

		if (result == null) {
			return 0;
		}

		return result;
	}

	R getRelation(int modifierID) {
		return relationByModifierID.get(modifierID);
	}

	void setRelation(int modifierID, R relation) {
		relationByModifierID.put(modifierID, relation);
	}

	Set<DependencyForestNode<T, R>> getModifiers(int headID) {
		ensureModifierSetExists(headID);
		Set<DependencyForestNode<T, R>> modifiers = new HashSet<DependencyForestNode<T, R>>();

		for (Integer modifierID : modifierIDsByHeadID.get(headID)) {
			modifiers.add(nodeByID.get(modifierID));
		}

		return Collections.unmodifiableSet(modifiers);
	}

	Set<Integer> getModifierIDs(int headID) {
		return Collections.unmodifiableSet(modifierIDsByHeadID.get(headID));
	}

	/**
	 * @param id
	 *            ID of a node
	 * @return an array {@code projection} where for every node {@code n} in the
	 *         yield of node {@code id}, {@code projection[n - 1] == true}.
	 */
	boolean[] projection(int id) {
		boolean[] inProjection = new boolean[nodeCount];
		inProjection[id - 1] = true;
		project(id, inProjection);
		return inProjection;
	}

	/**
	 * adds the yield of the specified node to the boolean array
	 * 
	 * @param id
	 *            ID of a node
	 * @param inProjection
	 *            boolean array, it is assumed that for every node marked
	 *            {@code true}, all of its yield is also already marked
	 *            {@code true}
	 */
	void project(int id, boolean[] inProjection) {
		ensureModifierSetExists(id);
		Set<Integer> modifierIDs = modifierIDsByHeadID.get(id);

		for (int modifierID : modifierIDs) {
			if (!inProjection[modifierID - 1]) {
				inProjection[modifierID - 1] = true;
				project(modifierID, inProjection);
			}
		}
	}

/**
     * same as {@code project(int, boolean[]), but only marks and follows nodes
     * in a specified range of IDs (inclusive)
     * @param id
     * @param from
     * @param to
     * @param inProjection
     */
	void project(int id, int from, int to, boolean[] inProjection) {
		ensureModifierSetExists(id);
		Set<Integer> modifierIDs = modifierIDsByHeadID.get(id);

		for (int modifierID : modifierIDs) {
			if (from <= modifierID && modifierID <= to
					&& !inProjection[modifierID - 1]) {
				inProjection[modifierID - 1] = true;
				project(modifierID, from, to, inProjection);
			}
		}
	}

	/**
	 * Analyze wellnestedness (Bodirsky et al., 2005)
	 * 
	 * @return True if graph is wellnested.
	 */
	public boolean isWellnested() {
		int nc = getNodeCount();
		for (int i = 1; i <= nc; ++i) {
			List<DependencyForestNode<T, R>> iproj = getNode(i).projection();
			for (int j = 1; j <= nc; ++j) {
				if (i == j)
					continue;
				List<DependencyForestNode<T, R>> jproj = getNode(j)
						.projection();

				// check if iproj and jproj are disjoint
				List<DependencyForestNode<T, R>> disjoint = new ArrayList<DependencyForestNode<T, R>>(
						iproj);
				disjoint.retainAll(jproj);
				if (disjoint.size() > 0)
					continue;
				for (int l1 = 0; l1 < iproj.size(); ++l1) {
					int l1id = iproj.get(l1).getID();
					for (int l2 = 0; l2 < jproj.size(); ++l2) {
						int l2id = jproj.get(l2).getID();
						for (int r1 = 0; r1 < iproj.size(); ++r1) {
							int r1id = iproj.get(r1).getID();
							for (int r2 = 0; r2 < jproj.size(); ++r2) {
								int r2id = jproj.get(r2).getID();

								// true if subtrees interleave, i.e. if not
								// wellnested
								// System.err.println(l1id + " " + l2id + " " +
								// r1id + " " + r2id);
								if ((l1id < l2id) && (l2id < r1id)
										&& (r1id < r2id)) {
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Analyze degree of illnestedness (Bodirsky et al., 2005, Maier & Lichte,
	 * 2009)
	 * 
	 * @return the degree of illnestedness
	 */
	public int getIllnestednessDegree() {
		int ret = 0;
		int nc = getNodeCount();
		for (int i = 1; i <= nc; ++i) {
			List<DependencyForestNode<T, R>> iproj = getNode(i).projection();
			List<DependencyForestNode<T, R>> crossingprojs = new ArrayList<DependencyForestNode<T, R>>();
			for (int j = 1; j <= nc; ++j) {
				if (i == j)
					continue;
				List<DependencyForestNode<T, R>> jproj = getNode(j)
						.projection();
				// check if iproj and jproj are disjoint
				List<DependencyForestNode<T, R>> disjoint = new ArrayList<DependencyForestNode<T, R>>(
						iproj);
				disjoint.retainAll(jproj);
				if (disjoint.size() > 0)
					continue;
				boolean localcross = false;
				for (int l1 = 0; l1 < iproj.size(); ++l1) {
					int l1id = iproj.get(l1).getID();
					for (int l2 = 0; l2 < jproj.size(); ++l2) {
						int l2id = jproj.get(l2).getID();
						for (int r1 = 0; r1 < iproj.size(); ++r1) {
							int r1id = iproj.get(r1).getID();
							for (int r2 = 0; r2 < jproj.size(); ++r2) {
								int r2id = jproj.get(r2).getID();
								// true if subtrees interleave, i.e. if not
								// wellnested
								// System.err.println(l1id + " " + l2id + " " +
								// r1id + " " + r2id);
								if ((l1id < l2id) && (l2id < r1id)
										&& (r1id < r2id))
									localcross = true;
							}
						}
					}
				}
				if (localcross) {
					crossingprojs.add(getNode(j));
				}

			}

			Set<DependencyForestNode<T, R>> exclude = new HashSet<DependencyForestNode<T, R>>(
					crossingprojs.size());
			for (int p = 0; p < crossingprojs.size(); ++p) {
				DependencyForestNode<T, R> pnode = crossingprojs.get(p);
				List<DependencyForestNode<T, R>> py = pnode.projection();
				for (int q = 0; q < crossingprojs.size(); ++q) {
					DependencyForestNode<T, R> qnode = crossingprojs.get(q);
					List<DependencyForestNode<T, R>> qy = qnode.projection();
					List<DependencyForestNode<T, R>> disjoint = new ArrayList<DependencyForestNode<T, R>>(
							py);
					disjoint.retainAll(qy);
					if (p == q || disjoint.isEmpty())
						continue;
					if (py.size() > qy.size())
						exclude.add(qnode);
					else
						exclude.add(pnode);
				}

			}
			// holds the number of nodes with yields crossing into the current
			// one
			int local = crossingprojs.size() - exclude.size();

			ret = local > ret ? local : ret;
		}
		if (ret > 1)
			System.err.println(ret + "-illnested: "
					+ this.getTerminalsAsString());
		return ret;
	}

	public int calcGapDegree() {
		int gapDegree = 0;
		for (int i = 1; i <= getNodeCount(); i++) {
			gapDegree = Math.max(gapDegree, getNode(i).gapDegree());
		}
		return gapDegree;
	}

	@Override
	public int getGapDegree() {
		return 0;
	}

	public String getTerminalsAsString() {
		String ret = "";
		for (int i = 1; i <= getNodeCount(); ++i) {
			ret += getNode(i).getToken().toString() + " ";
		}
		return ret;
	}

	public String getVertical(int i) {
		return verticalHistoryByID.get(i);
	}

	public void setVertical(int i, String horizontalHistory) {
		verticalHistoryByID.put(i, horizontalHistory);
	}

	public boolean hasVerticalHistory(int i) {
		return verticalHistoryByID.containsKey(i);
	}

	public void setVerticalDepth(int depth) {
		this.verticalDepth = depth;
	}

	public int getVerticalDepth() {
		return verticalDepth;
	}

	private void ensureModifierSetExists(int headID) {
		if (!modifierIDsByHeadID.containsKey(headID)) {
			modifierIDsByHeadID.put(headID, new HashSet<Integer>());
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode(tokenByID);
		result = prime * result + hashCode(headByModifierID);
		result = prime * result + hashCode(relationByModifierID);
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

		if (!(o instanceof DependencyForest<?, ?>)) {
			return false;
		}

		DependencyForest<?, ?> that = (DependencyForest<?, ?>) o;
		return equals(tokenByID, that.tokenByID)
				&& equals(headByModifierID, that.headByModifierID)
				&& equals(relationByModifierID, that.relationByModifierID);
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

	@Override
	public int getId() {
		return id;
	}

}
