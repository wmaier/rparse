/*******************************************************************************
 * File NodeLabel.java
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
package de.tuebingen.rparse.treebank.constituent;

public class NodeLabel {

    // annotation
    private int    num;
    private String word;
    private String tag;
    private String lemma;
    private String morph;
    private String edge;
    private String parent;
    private String secedge;
    private String comment;
    
    // markovization
    private String leftsiblings;
    private String rightsiblings;
    private int siblingdepth;
    private String vertical;
    private int verticaldepth;
    private String verticalNoArities;
    
    
    public NodeLabel() {
        this(-1);
    }

    public NodeLabel(int num) {
        this(num, "", "", "", "", "", "-1", "", "");
    }

    public NodeLabel(int num, String word, String tag, String lemma, String morph,
            String edge, String parent, String secedge, String comment) {
        this.num = num;
        this.word = word;
        this.tag = tag;
        this.lemma = lemma;
        this.morph = morph;
        this.edge = edge;
        this.parent = parent;
        this.secedge = secedge;
        this.comment = comment;
    }
    
    public String getLeftsiblings() {
		return leftsiblings;
	}

	public void setLeftsiblings(String leftsiblings) {
		this.leftsiblings = leftsiblings;
	}

	public String getRightsiblings() {
		return rightsiblings;
	}

	public void setRightsiblings(String rightsiblings) {
		this.rightsiblings = rightsiblings;
	}

	public int getSiblingdepth() {
		return siblingdepth;
	}

	public void setSiblingdepth(int siblingdepth) {
		this.siblingdepth = siblingdepth;
	}

	public void setVerticalDepth(int d) {
		verticaldepth = d;
	}
	
	public int getVerticalDepth() {
		return verticaldepth;
	}
	
	public void setVertical(String vertical) {
		this.vertical = vertical;
	}
	
	public String getVertical() {
		return vertical;
	}
	
	public void setVerticalNoArities(String verticalNoArities) {
	    this.verticalNoArities = verticalNoArities;
	}
	
	public String getVerticalNoArities() {
	    return verticalNoArities;
	}

	public NodeLabel(NodeLabel l) {
    	this.num = l.num;
    	this.word = l.word;
    	this.tag = l.tag;
    	this.lemma = l.lemma;
    	this.morph = l.morph;
    	this.edge = l.edge;
    	this.parent = l.parent;
    	this.secedge = l.secedge;
    	this.comment = l.comment;
    }

    public boolean edgeEmpty() {
    	return edge == null || edge.equals("") || edge.equals("-") || edge.equals("--");
	}

	public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getMorph() {
        return morph;
    }

    public void setMorph(String morph) {
        this.morph = morph;
    }

    public String getEdge() {
        return edge;
    }

    public void setEdge(String edge) {
        this.edge = edge;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getSecedge() {
        return secedge;
    }

    public void setSecedge(String secedge) {
        this.secedge = secedge;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPunct() {
        return tag.startsWith("$");
    }

    public boolean isTerm() {
        return num > 0 && num < 500;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((edge == null) ? 0 : edge.hashCode());
        result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
        result = prime * result + ((morph == null) ? 0 : morph.hashCode());
        result = prime * result + num;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((secedge == null) ? 0 : secedge.hashCode());
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + ((word == null) ? 0 : word.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeLabel other = (NodeLabel) obj;
        if (comment == null) {
            if (other.comment != null)
                return false;
        } else if (!comment.equals(other.comment))
            return false;
        if (edge == null) {
            if (other.edge != null)
                return false;
        } else if (!edge.equals(other.edge))
            return false;
        if (lemma == null) {
            if (other.lemma != null)
                return false;
        } else if (!lemma.equals(other.lemma))
            return false;
        if (morph == null) {
            if (other.morph != null)
                return false;
        } else if (!morph.equals(other.morph))
            return false;
        if (num != other.num)
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (secedge == null) {
            if (other.secedge != null)
                return false;
        } else if (!secedge.equals(other.secedge))
            return false;
        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        if (word == null) {
            if (other.word != null)
                return false;
        } else if (!word.equals(other.word))
            return false;
        return true;
    }

    public String toString() {
        return getTag();
    }

}
