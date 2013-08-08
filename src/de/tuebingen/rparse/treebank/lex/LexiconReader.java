/*******************************************************************************
 * File LexiconReader.java
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.tuebingen.rparse.misc.Numberer;

/**
 * Reads a lexicon from a file, as output by Lexicon.toString(). 
 * @author wmaier
 *
 */
public class LexiconReader extends BufferedReader {

    private Numberer nb;
    
    /**
     * Construct the lexicon reader. TODO this should be reader-based and not file-based.
     * @param f The file where to read the lexicon.
     * @param nb The numberer where the number/label mappings are to be stored.
     * @throws FileNotFoundException If the file cannot be found.
     */
    public LexiconReader(File f, Numberer nb) throws FileNotFoundException {
        super(new FileReader(f));
        this.nb = nb;
    }

    /**
     * 
     * @return
     * @throws IOException
     * @throws LexiconException
     */
    public Lexicon getLexicon() throws IOException, LexiconException {
        Lexicon ret = new Lexicon(nb);
        String line = "";
        while ((line = super.readLine()) != null) {
            if (line.length() < 2)
                continue;
            line = line.trim();
            String[] s = line.split("\t");
            if (s.length == 2) {
                ret.addPair(s[0], s[1]);
            }
        }
        return ret;
    }

}
