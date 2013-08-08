/*******************************************************************************
 * File TreebankGetter.java
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
package de.tuebingen.rparse.treebank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple processing task which gets all sentences from a treebank.
 * 
 * @author wmaier
 * @param <S>
 *            The type of structures in the treebank.
 */
public class TreebankGetter<S> extends ProcessingTask<S> {

    protected List<S> sentences;

    public TreebankGetter() {
        sentences = new ArrayList<S>();
    }

    @Override
    public void processSentence(S sentence) {
        sentences.add(sentence);
    }

    public List<S> getSentences() {
        return Collections.unmodifiableList(sentences);
    }

    @Override
    public void done() {

    }

}
