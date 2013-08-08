/*******************************************************************************
 * File PDTProcessor.java
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
package de.tuebingen.rparse.treebank.dep.pdt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.misc.Ranges;
import de.tuebingen.rparse.treebank.ProcessingTask;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;
import de.tuebingen.rparse.treebank.dep.DirectoryDependencyTreebankProcessor;

/**
 * Reader for the Prague Dependency Treebank 2.0. Since the annotation and the
 * sentences are split up in several files, we must read two files in parallel
 * for each sentence group. The convention is to pass the file with the a-layer.
 * 
 * @author wmaier
 * 
 */
public class PDTProcessor extends DirectoryDependencyTreebankProcessor {
	
	protected ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task;
	
	protected Ranges ranges;
	
	protected Integer maxlen;
	
	protected Numberer nb;
	
	Logger logger;
	
	public PDTProcessor(Numberer nb) {
		this.nb = nb;
		this.logger = Logger.getLogger(PDTProcessor.class.getPackage()
				.getName());
		logger.setLevel(Level.FINE);
	}
	
	@Override
	public void process(
			Reader treebankReader,
			ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task,
			Ranges ranges, Integer maxlen) throws IOException,
			TreebankException {
		
		throw new UnsupportedOperationException(
				getClass().toString()
						+ " cannot be used on individual files since PDT has stand-off annotation");
		
	}
	
	@Override
	public void processDirectory(
			File treebankDir, String encoding,
			FileFilter filter,
			ProcessingTask<? super DependencyForest<DependencyForestNodeLabel, String>> task,
			Ranges ranges, Integer maxlen) throws IOException,
			TreebankException {
		PDTMorphReader morphReader = new PDTMorphReader(this);
		PDTAnnotationReader annotReader = new PDTAnnotationReader(this);
		
		treebankDir = treebankDir.getCanonicalFile();
		if (!treebankDir.isDirectory())
			throw new TreebankException(treebankDir + " is not a directory");
		// build a list of base filenames (without extensions)
		File[] allFiles = treebankDir.listFiles(filter);
		Set<String> toProcessSet = new HashSet<String>();
		for (File f : allFiles) {
			toProcessSet.add(f.getAbsolutePath().substring(0,
					f.getAbsolutePath().length() - 5));
		}
		List<String> toProcess = new ArrayList<String>(toProcessSet);
		Collections.sort(toProcess);
		// process all list entries
		for (String fname : toProcess) {
			Map<String, DependencyForest<DependencyForestNodeLabel, String>> graphs = 
				new HashMap<String, DependencyForest<DependencyForestNodeLabel, String>>();
			InputStreamReader ir = null;
			try {
				File morphFile = new File(fname + ".m.gz");
				logger.info("Processing morph file: " + morphFile);
				ir = new InputStreamReader(new BufferedInputStream(
						new GZIPInputStream(new FileInputStream(morphFile))), encoding);
				morphReader.process(new BufferedReader(ir), graphs);
				
				File annotFile = new File(fname + ".a.gz");
				logger.info("Processing annotation file: " + annotFile);
				ir = new InputStreamReader(new BufferedInputStream(
						new GZIPInputStream(new FileInputStream(annotFile))), encoding);
				annotReader.process(new BufferedReader(ir), graphs);
			} catch (FileNotFoundException e) {
				throw new TreebankException(e.getMessage());
			} catch (IOException e) {
				throw new TreebankException(e.getMessage());
			} catch (ParserConfigurationException e) {
				throw new IOException(e.getMessage());
			} catch (SAXException e) {
				throw new IOException(e.getMessage());
			}
			for (DependencyForest<DependencyForestNodeLabel, String> graph : graphs.values()) {
				if (graph.nodes().size() <= maxlen) {
					task.processSentence(graph);
				}
			}
		}
	}
	
}
