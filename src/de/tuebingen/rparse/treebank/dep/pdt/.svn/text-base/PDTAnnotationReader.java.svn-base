/*******************************************************************************
 * File PDTAnnotationReader.java
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.dep.DependencyForest;
import de.tuebingen.rparse.treebank.dep.DependencyForestNodeLabel;

public class PDTAnnotationReader extends LinkedList<Transition> implements
		ContentHandler, ErrorHandler, EntityResolver, URIResolver {
	
	private static final long serialVersionUID = 1L;
	
	private final PDTProcessor processor;
	
	public PDTAnnotationReader(PDTProcessor processor) {
		this.processor = processor;
	}
	
	// global sentence num
	private int cnt;
	// global error list
	private List<Integer> errors;
	// global graph list
	private Map<String, DependencyForest<DependencyForestNodeLabel, String>> graphs;
	// local graph var
	private DependencyForest<DependencyForestNodeLabel, String> graph;
	
	// sentence id
	private boolean isInSentence;
	private String sentenceId;
	
	// for indices
	private boolean isInInd;
	protected Integer currentInd;
	private String currentSInd;
	
	// for label
	private boolean isInLabel;
	private String currentLabel;
	
	// transition
	private Transition trans;
	private Map<Integer, Transition> transitionsMap;
	private LinkedList<Transition> parentQueue;
	
	int lmLevel;
	
	public void process(
			Reader treebankReader,
			Map<String, DependencyForest<DependencyForestNodeLabel, String>> graphs)
			throws TreebankException, ParserConfigurationException,
			SAXException, IOException {
		
		this.graphs = graphs;
		SAXParserFactory spf = null;
		SAXParser sp = null;
		XMLReader xmlreader = null;
		InputSource is = new InputSource();
		spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(false);
		sp = spf.newSAXParser();
		xmlreader = sp.getXMLReader();
		xmlreader.setContentHandler(this);
		xmlreader.setEntityResolver(this);
		is = new InputSource(treebankReader);
		xmlreader.parse(is);
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (isInLabel) {
			for (int i = start; i < start + length; i++) {
				switch (ch[i]) {
					case '\\':
						currentLabel += "\\\\";
						break;
					case '"':
						currentLabel += "\\\"";
						break;
					case '\t':
						currentLabel += "\\t";
						break;
					case '\n':
						currentLabel += " ";
						break;
					default:
						currentLabel += ch[i];
						break;
				}
			}
		}
		if (isInInd) {
			for (int i = start; i < start + length; i++) {
				switch (ch[i]) {
					case '\\':
						currentSInd += "\\\\";
						break;
					case '"':
						currentSInd += "\\\"";
						break;
					case '\t':
						currentSInd += "\\t";
						break;
					case '\n':
						currentSInd += " ";
						break;
					default:
						currentSInd += ch[i];
						break;
				}
			}
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (isInSentence) {
			if ("LM".equals(name)) {
				lmLevel--;
				//System.err.println("lm level is now " + lmLevel);
				if (!isEmpty()) {
					Transition last = pop();
					transitionsMap.put(last.getHead(), last);
					Transition trans = parentQueue.peek();
					if (trans != null) 
						trans.addDep(last.getHead());
				}
				if (lmLevel == 0) {
					//System.err.println("Transitions: ");
					for (Transition t : transitionsMap.values()) {
						for (int dep : t.getDeps()) {
							graph.addEdge(dep, t.getHead(), transitionsMap.get(
									dep).getLabel());
						}
					}
					transitionsMap.clear();
					isInSentence = false;
					isInLabel = false;
					isInInd = false;
				}
			}
			if ("afun".equals(name)) {
				isInLabel = false;
				if (trans != null) {
					trans.setLabel(currentLabel);
				}
			}
			if ("ord".equals(name)) {
				isInInd = false;
				if (trans != null)
					trans.setHead(Integer.parseInt(currentSInd));
			}
			if ("children".equals(name)) {
				parentQueue.pop();
			}
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (!isInSentence) {
			// lowest-level LM starts a sentence
			if ("LM".equals(name)) {
				isInSentence = true;
				lmLevel = 1;
				currentInd = null;
				currentLabel = "";
				transitionsMap = new HashMap<Integer, Transition>();
				parentQueue = new LinkedList<Transition>();
				// get the sentence ID to load the right graph
				sentenceId = atts.getValue(atts.getIndex("id")).substring(2);
				graph = graphs.get(sentenceId);
				if (graph == null)
					throw new SAXException("Graph ID " + sentenceId + " in annotation file has no graph associated to it");
			}
		} else {
			if ("LM".equals(name)) {
				lmLevel++;
				trans = new Transition();
				push(trans);
				//String sid = atts.getValue(atts.getIndex("id"));
				//System.err.println("lm level is now " + lmLevel + "/" + sid);
			} else if ("afun".equals(name)) {
				currentLabel = "";
				isInLabel = true;
			} else if ("ord".equals(name)) {
				currentSInd = "";
				isInInd = true;
			} else if ("children".equals(name)) {
				parentQueue.push(peek());
			}
		}
		
	}
	
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		
	}
	
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		
	}
	
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		
	}
	
	@Override
	public void setDocumentLocator(Locator locator) {
		
	}
	
	@Override
	public void skippedEntity(String name) throws SAXException {
		
	}
	
	@Override
	public void startDocument() throws SAXException {
		errors = new ArrayList<Integer>();
		isInSentence = false;
		isInLabel = false;
		isInInd = false;
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		
	}
	
	@Override
	public void error(SAXParseException e) throws SAXException {
		this.processor.logger.warning("Error in sentence " + cnt + ": "
				+ e.getMessage());
		errors.add(cnt);
		isInSentence = false;
		isInLabel = false;
		isInInd = false;
	}
	
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		this.processor.logger.warning("Fatal error in sentence " + cnt + ": "
				+ e.getMessage());
		isInSentence = false;
		isInLabel = false;
		isInInd = false;
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
		this.processor.logger.warning("Warning in sentence " + cnt + ": "
				+ e.getMessage());
		isInSentence = false;
		isInLabel = false;
		isInInd = false;
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		return new InputSource(new StringReader(""));
	}
	
	@Override
	public Source resolve(String href, String base) throws TransformerException {
		return new StreamSource(new StringReader(""));
	}
}
