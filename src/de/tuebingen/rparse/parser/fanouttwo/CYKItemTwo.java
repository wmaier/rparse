/*******************************************************************************
 * File CYKItemTwo.java
 * 
 * Authors:
 *    Wolfgang Maier
 *    
 * Copyright:
 *    Wolfgang Maier, 2012
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
package de.tuebingen.rparse.parser.fanouttwo;

import de.tuebingen.rparse.grammar.GrammarConstants;
import de.tuebingen.rparse.misc.Numberer;


public class CYKItemTwo {

    public int   label;

    public int ll;
     
    public int lr;
    
    public int rl;
        
    public int rr;
    
    public double iscore;
    
    public double oscore;
    
    public String st;
    
    /**
     * backpointer left
     */
    public CYKItemTwo olc;
    
    /**
     * backpointer right
     */
    public CYKItemTwo orc;
    
    public final int onetwo;
    
    public CYKItemTwo(int label, int onetwo, double iscore, CYKItemTwo olc, CYKItemTwo orc, 
            int ll, int lr, int rl, int rr, String st) {
        this.label = label;
        this.ll = ll;
        this.lr = lr;
        this.rl = rl;
        this.rr = rr;
        this.olc = olc;
        this.orc = orc;
        this.iscore = iscore;
        this.oscore = 0.0;
        this.onetwo = onetwo;
        this.st = st;
    }

    public CYKItemTwo(int lhs, int onetwo, double iscore, CYKItemTwo olc, CYKItemTwo orc,
            CYKItemTwo i, String st) {
        this(lhs, onetwo, iscore, olc, orc, i.ll, i.lr, i.rl, i.rr, st);
    }
    
    public boolean isOne() {
        return this.onetwo == 1;
    }

    public String print(Numberer nb) {
        if (this.onetwo == 1) {
            return "[" + onetwo + "#" + (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, label)
                    + ", " + ll + ", " + lr 
                    + ", s" + st + "]:" + iscore;
        } else if (this.onetwo == 2) {
            return "[" + onetwo + "#" + (String) nb.getObjectWithId(GrammarConstants.PREDLABEL, label)
                    + ", " + ll + ", " + lr + " | " + rl + ", " + rr  
                    + ", s" + st + "]:" + iscore;
        } else {
            return "error";
        }
    }

    public boolean isSane() {
        if (onetwo == 1) {
            return rl == -1 && rr == -1 && ll != -1 && lr != -1 && ll < lr && label != -1;
        } else if (onetwo == 2) {
            return ll != -1 && lr != -1 && rl != -1 && rr != -1 && ll < lr && ll < rl 
                    && ll < rr && lr < rl && lr < rr && rl < rr;
        } else {
            return false;
        }
    }

}
