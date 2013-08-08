/*******************************************************************************
 * File ExportConsecutiveNumbersTreeWriter.java
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
package de.tuebingen.rparse.treebank.constituent.write;

import java.io.Writer;

import de.tuebingen.rparse.treebank.SentenceWriter;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Tree;


public class ExportConsecutiveNumbersTreeWriter extends ExportTreeWriter
        implements
            SentenceWriter<Tree> {

    private int cnt;
    
    public ExportConsecutiveNumbersTreeWriter(String format) {
        super(format);
        cnt = 0;
    }
    
    public void write(Tree t, Writer writer) throws TreebankException {
        cnt += 1;
        String bos = t.getBstring().replaceFirst("\\d+", String.valueOf(cnt));
        t.setBstring(bos);
        String eos = t.getEstring().replaceFirst("\\d+", String.valueOf(cnt));
        t.setEstring(eos);
        super.write(t, writer);
    }

}
