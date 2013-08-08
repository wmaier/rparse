/*******************************************************************************
 * File RCGWriterRparse.java
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
package de.tuebingen.rparse.grammar.write;

import java.io.IOException;
import java.io.Writer;

import de.tuebingen.rparse.grammar.Clause;
import de.tuebingen.rparse.grammar.RCG;
import de.tuebingen.rparse.misc.ClassParameters;
import de.tuebingen.rparse.misc.HasParameters;
import de.tuebingen.rparse.misc.ParameterException;

/**
 * Write out an RCG with several diagnostic fields in our in-house RCG format
 * 
 * @author wmaier
 */
public class RCGWriterRparse implements GrammarWriter<RCG>, HasParameters {

    private ClassParameters params;

    private boolean         writeHead;
    private boolean         writeDiag;
    private boolean         writeStat;

    /**
     * Constructor
     * 
     * @param options
     * @throws ParameterException
     */
    public RCGWriterRparse(String options) throws ParameterException {
        params = new ClassParameters();
        params.add("writehead", "Write marked heads of the RCG (must be there)");
        params.add("writediag",
                "Write diagnostics (origins and gorn addresses)");
        params.add("writestat", "Write statistics (count and score)");
        params.parse(options);
        writeHead = params.check("writehead");
        writeDiag = params.check("writediag");
        writeStat = params.check("writestat");
    }

    @Override
    public ClassParameters getParameters() throws ParameterException {
        return params;
    }

    @Override
    public void write(RCG g, Writer w) throws IOException {// File d, String pref, String encoding) throws IOException {
        for (Integer labeln : g.getClausesByLhsLabel().keySet()) {
            for (Clause c : g.getClausesByLhsLabel().get(labeln)) {
                if (writeHead) {
                    w.write("H:" + c.getHeadPos());
                    w.write(" ");
                }
                if (writeDiag) {
                    w.write("O:[");
                    for (int i = 0; i < c.getOrigins().size(); ++i) {
                        w.write(String.valueOf(c.getOrigins().get(i)));
                        if (i < c.getOrigins().size() - 1)
                            w.write(",");
                    }
                    w.write("] ");

                    w.write("G:[");
                    /*
                     * for (int i = 0; i < c.gorn.size(); ++i) { w.write((String)nb.getObjectWithId(RCG.GORN,
                     * c.gorn.get(i))); if (i < c.gorn.size() - 1) w.write(","); }
                     */
                    w.write("] ");
                }
                if (writeStat)
                    w.write("C:" + g.getClauseOccurrenceCount(c) + " S:"
                            + c.getScore() + " ");
                w.write(c.print(g.getNumberer()) + "\n");
            }
        }
        // write(pd, w);
        w.flush();
        w.close();
    }

}
