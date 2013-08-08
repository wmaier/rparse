/*******************************************************************************
 * File DependencyForestNodeLabel.java
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

import java.util.Set;

/**
 * Represents a token as specified in the CoNLL-X data format ({@link http
 * ://nextens.uvt.nl/~conll/#dataformat}), without any graph relations. The
 * fields FORM, LEMMA, CPOSTAG, POSTAG, FEATS are represented and can be
 * accessed and modified via constructors, getter, and setter methods. The LEMMA
 * and FEATS fields may be unset ({@code null}). The other fields should never
 * be set to {@code null} values.
 * 
 * @author ke
 * 
 */
public class DependencyForestNodeLabel {
	
	private String form;
	
	private String lemma;
	
	private String cpostag;
	
	private String postag;
	
	private Set<String> feats;
	
	/**
	 * Creates a new {@link DependencyForestNodeLabel} with the specified
	 * values.
	 * 
	 * @param form
	 * @param lemma
	 * @param cpostag
	 * @param postag
	 * @param feats
	 */
	public DependencyForestNodeLabel(String form, String lemma, String cpostag,
			String postag, Set<String> feats) {
		setForm(form);
		setLemma(lemma);
		setCpostag(cpostag);
		setPostag(postag);
		setFeats(feats);
	}
	
	/**
	 * Creates a new {@link DependencyForestNodeLabel} with the specified FORM,
	 * CPOSTAG, and POSTAG values. All other values will be <code>null</code>
	 * 
	 * @param form
	 * @param cpostag
	 * @param postag
	 */
	public DependencyForestNodeLabel(String form, String cpostag, String postag) {
		setForm(form);
		setCpostag(cpostag);
		setPostag(postag);
	}
	
	/**
	 * Creates a new {@link CoNNLXToken} with the specified FORM and CPOSTAG
	 * values. The POSTAG will be set to the same value as CPOSTAG. All other
	 * values will be <code>null</code>.
	 * 
	 * @param form
	 * @param cpostag
	 */
	public DependencyForestNodeLabel(String form, String cpostag) {
		setForm(form);
		setCpostag(cpostag);
		setPostag(cpostag);
	}
	
	public void setForm(String form) {
		this.form = form;
	}
	
	public String getForm() {
		return form;
	}
	
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	
	public String getLemma() {
		return lemma;
	}
	
	public boolean isLemmaSet() {
		return lemma != null;
	}
	
	public void setCpostag(String cpostag) {
		this.cpostag = cpostag;
	}
	
	public String getCpostag() {
		return cpostag;
	}
	
	public void setPostag(String postag) {
		this.postag = postag;
	}
	
	public String getPostag() {
		return postag;
	}
	
	public void setFeats(Set<String> feats) {
		this.feats = feats;
	}
	
	public Set<String> getFeats() {
		return feats;
	}
	
	public boolean isFeatsSet() {
		return feats != null;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hashCode(form);
		result = prime * result + hashCode(lemma);
		result = prime * result + hashCode(cpostag);
		result = prime * result + hashCode(postag);
		result = prime * result + hashCode(form);
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
		
		if (!(o instanceof DependencyForestNodeLabel)) {
			return false;
		}
		
		DependencyForestNodeLabel that = (DependencyForestNodeLabel) o;
		return equals(form, that.form) && equals(lemma, that.lemma)
				&& equals(cpostag, that.cpostag) && equals(postag, that.postag)
				&& equals(feats, that.feats);
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
	public String toString() {
		return form;
	}
	
}
