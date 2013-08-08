/*******************************************************************************
 * File NegraProcessor.java
 * 
 * Authors:
 *    Wolfgang Maier, Kilian Evang
 *    
 * Copyright:
 *    Wolfgang Maier, Kilian Evang, 2011
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

import java.io.IOException;
import java.io.Reader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuebingen.rparse.misc.Ranges;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;


public class NegraProcessor extends DependencyTreebankProcessor {

    private static final Pattern CONTROL_CHARACTER_PATTERN = Pattern
    .compile("\\x02");

    private static final String COMMENT = "//";
    
    private static final Pattern TOKEN_LINE = Pattern.compile("\\s*(\\d+)\\s+(\\d+)\\s+(?:'((?:\\.|[^'])*)'|(\\S*))\\s+(\\S+)\\s+->\\s+(\\S+)\\s+->\\s+(\\d+).*");
    
    @Override
    public void process(
            Reader treebankReader,
            ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task,
            Ranges ranges, Integer maxlen) throws IOException, TreebankException {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(treebankReader);
        Matcher matcher = CONTROL_CHARACTER_PATTERN.matcher("");
        int sentenceNumber = 0;
        int lineNumber = 0;

        while (!isInRange(sentenceNumber + 1, ranges)
                && scanner.hasNextLine()) {
            matcher.reset(scanner.nextLine());
            String line = matcher.replaceAll("");
            lineNumber++;
//            sentenceNumber++;
            boolean hasTokens = false;
            
            while (!line.equals("")) {
                if (!line.startsWith(COMMENT)) { 
                    lineNumber++;
                    hasTokens = true;
                }
                scanner.nextLine();
            }
            if (!hasTokens) {
                sentenceNumber--;
            }
        }

        while (isInRange(sentenceNumber + 1, ranges) && scanner.hasNextLine()) {
            matcher.reset(scanner.nextLine());
            String line = matcher.replaceAll("");
            lineNumber++;
            if (line.startsWith(COMMENT)) 
                continue;

            DependencyForest<DependencyForestNodeLabel, String> graph = new DependencyForest<DependencyForestNodeLabel, String>();
            int expectedID = 0;

            while (!line.equals("")) {
                expectedID++;

                try {
                    processToken(line, graph, expectedID);
                } catch (TreebankException x) {
                    throw new TreebankException("could not read token on line "
                            + lineNumber + ": " + x.getMessage(), x);
                }

                if (!scanner.hasNextLine()) {
                    break;
                }

                line = scanner.nextLine();
                lineNumber++;
            }

            
            if (graph.getNodeCount() > 0) {
            	//System.err.println(graph.getTerminalsAsString());
                sentenceNumber++;
                if (graph.getNodeCount() <= maxlen)
                    task.processSentence(graph);
            }
        }
        task.done();
    }

    private void processToken(String line,
            DependencyForest<DependencyForestNodeLabel, String> graph,
            int expectedID) throws TreebankException {
        
        if (line.indexOf("<->") == -1) {

            Matcher matcher = TOKEN_LINE.matcher(line);
            
            if (matcher.matches()) {
                int groupCount = matcher.groupCount();
                
                if (groupCount < 7) {
                    throw new TreebankException("can't parse line: " + line);
                }
                
                int modifierID = Integer.valueOf(matcher.group(2));
                String word = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                word = word.replace("\\", "");
                String elabel = matcher.group(6);
                int headID = Integer.valueOf(matcher.group(7));
                
                graph.addNode(new DependencyForestNodeLabel(word, "", "", "", null));
                graph.addEdge(modifierID, headID, elabel);
            } else {
                throw new TreebankException("can't parse line: " + line);
            }
            

        } 
        

    }
    
}
