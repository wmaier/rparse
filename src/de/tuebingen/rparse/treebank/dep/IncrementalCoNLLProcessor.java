/*******************************************************************************
 * File IncrementalCoNLLProcessor.java
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.TreebankException;

/**
 * An incremental processor for the CoNLL format
 * @author wmaier
 *
 */
public class IncrementalCoNLLProcessor extends IncrementalTreebankProcessor<DependencyForest<DependencyForestNodeLabel, String>> {

    private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern
            .compile("\\x02");
    
    private Matcher matcher;

    private static final String DUMMY_VALUE = "_";

    private boolean projective;
    
    private int sentenceNumber = 0;
    
    private Scanner scanner;
    
    public IncrementalCoNLLProcessor(boolean projective) {
        this.projective = projective;
    }
    
    
    @Override
    public int getLength(
            DependencyForest<DependencyForestNodeLabel, String> sentence) {
        return sentence.size();
    }

    @Override
    protected void doInitialize(Reader reader) {
        scanner = new Scanner(reader);
        matcher = CONTROL_CHARACTER_PATTERN.matcher("");
    }

    @Override

    protected DependencyForest<DependencyForestNodeLabel, String> getNextSentence() {
        
        if (!scanner.hasNextLine()) {
            return null;
        }

        List<String> sentence = new ArrayList<String>();
        sentenceNumber++;
        while (scanner.hasNextLine()) {
            matcher.reset(scanner.nextLine());
            String line = matcher.replaceAll("");

            if (line.equals("")) {
                break;
            } else {
                sentence.add(line);
            }
        }

        DependencyForest<DependencyForestNodeLabel, String> graph = new DependencyForest<DependencyForestNodeLabel, String>();
        int expectedID = 0;

        for (String sline : sentence) {                
            expectedID++;
            try {
                processToken(sline, graph, expectedID);
            } catch (TreebankException x) {
                logger.warning("could not read token: " + x.getMessage());
            }
        }
        
        graph.id = sentenceNumber;
        
        return graph;
    }
    
    private void processToken(String line,
            DependencyForest<DependencyForestNodeLabel, String> graph, int expectedID)
            throws TreebankException {
        String[] fields = line.split("\\s+");

        if (fields.length != 10) {
            throw new TreebankException("wrong number of fields ("
                    + fields.length + ")");
        }

        // ID:
        try {
            if (Integer.parseInt(fields[0]) != expectedID) {
                throw new TreebankException("unexpected ID");
            }
        } catch (NumberFormatException x) {
            throw new TreebankException("could not parse ID field");
        }

        // LEMMA:
        if (fields[2].equals(DUMMY_VALUE)) {
            fields[2] = null;
        }

        // FEATS:
        Set<String> feats;

        if (fields[5].equals(DUMMY_VALUE)) {
            feats = null;
        } else {
            feats = new HashSet<String>();
            String[] featStrings = fields[5].split("\\|");

            for (String featString : featStrings) {
                feats.add(featString);
            }
        }

        // Create token (FORM, LEMMA, CPOSTAG, POSTAG, FEATS):
        graph.addNode(new DependencyForestNodeLabel(fields[1], fields[2], fields[3],
                fields[4], feats));

        // HEAD and DEPREL resp. PHEAD and PDEPREL:
        int headField;
        int deprelField;

        if (projective) {
            headField = 8;
            deprelField = 9;
        } else {
            headField = 6;
            deprelField = 7;
        }

        if (fields[headField].equals(DUMMY_VALUE)) {
            throw new TreebankException(headFieldName()
                    + " field not available");
        }

        try {
            int head = Integer.parseInt(fields[headField]);

            if (head != 0) {
                graph.addEdge(expectedID, head, fields[deprelField]);
            }
        } catch (NumberFormatException x) {
            throw new TreebankException("could not parse " + headFieldName()
                    + " field", x);
        }
    }

    private String headFieldName() {
        if (projective) {
            return "PHEAD";
        } else {
            return "HEAD";
        }
    }


}
