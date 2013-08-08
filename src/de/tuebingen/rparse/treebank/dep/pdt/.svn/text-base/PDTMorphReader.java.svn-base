/*******************************************************************************
 * File PDTMorphReader.java
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

class PDTMorphReader implements ContentHandler, ErrorHandler,
		EntityResolver, URIResolver {

	private final PDTProcessor processor;

	/**
	 * @param processor
	 */
	PDTMorphReader(PDTProcessor processor) {
		this.processor = processor;
	}

	private Map<String, DependencyForest<DependencyForestNodeLabel, String>> graphs;
	private DependencyForest<DependencyForestNodeLabel, String> graph;
	private Map<Integer, String> tags;
	private Map<Integer, String> words;
	private int wordid;
	private int tagid;
	
	private int cnt;
	private List<Integer> errors;
	
	private boolean isInSentence;
	private boolean isInMorph;
	private boolean isInForm;
	private boolean isInTag;
	private String sentenceId;
	
	public void process(
			Reader treebankReader, Map<String, DependencyForest<DependencyForestNodeLabel, String>> graphs) throws TreebankException,
			ParserConfigurationException, SAXException, IOException {
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
	
	private String word;
	private String tag;
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
        if (isInForm) {
        	for (int i = start; i < start + length; i++) {
        		switch (ch[i]) {
        			case '\\' :
        				word += "\\\\";
        				break;
        			case '"' :
        				word += "\\\"";
        				break;
        			case '\t' :
        				word += "\\t";
        				break;
        			case '\n' :
        				word += " ";
        				break;
        			default :
        				word += ch[i];
        			break;
        		}
        	}
		}
        if (isInTag) {
        	for (int i = start; i < start + length; i++) {
        		switch (ch[i]) {
        			case '\\' :
        				tag += "\\\\";
        				break;
        			case '"' :
        				tag += "\\\"";
        				break;
        			case '\t' :
        				tag += "\\t";
        				break;
        			case '\n' :
        				tag += " ";
        				break;
        			default :
        				tag += ch[i];
        			break;
        		}
        	}
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if (isInSentence) {
			if (isInMorph) {
				if ("form".equals(name)) {
					isInForm = true;
					word = "";
				}
				if ("tag".equals(name)) {
					isInTag = true;
					tag = "";
				}
			} else {
				if ("m".equals(name)) {
					isInMorph = true;
				}
			}
		} else {
			if ("s".equals(name)) {
				isInSentence = true;
				wordid = 1;
				tagid = 1;
				++cnt;
				sentenceId = atts.getValue(atts.getIndex("id")).substring(2);
				graph = new DependencyForest<DependencyForestNodeLabel, String>();
				tags = new HashMap<Integer, String>();
				words = new HashMap<Integer, String>();
				graph.id = cnt;
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (isInSentence) {
			if ("s".equals(name)) {
				isInSentence = false;
				for (int termind : words.keySet()) {
					String word = words.get(termind);
					String tag = tags.get(termind);
					// like Collins (1999): only first letter	
					int ind = 2;
					//for (; ind < tag.length() && Character.isLetter(tag.charAt(ind)); ++ind);
							//&& !Character.isDigit(tag.charAt(ind))
							//&& tag.charAt(ind) != '-'; ++ind)
					//	;
					tag = tag.substring(0, ind);
					graph.addNode(new DependencyForestNodeLabel(word, tag));
				}
				graphs.put(sentenceId, graph);
			} else {
				if (isInMorph) {
					if ("m".equals(name)) {
						if (tagid != wordid)
							throw new SAXException("Sentence found which has more words than tags or vice versa");
						isInMorph = false;
					}
					if ("form".equals(name)) {
						isInForm = false;
						words.put(wordid, word);
						wordid++;
					}
					if ("tag".equals(name)) {
						isInTag = false;
						tags.put(tagid, tag);
						tagid++;
					}
				} 
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
		isInMorph = false;
		isInForm = false;
		isInTag = false;
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		
	}
	
	@Override
	public void error(SAXParseException e) throws SAXException {
		this.processor.logger.warning("Error in sentence " + cnt + ": " + e.getMessage());
		errors.add(cnt);
		isInSentence = false;
		isInMorph = false;
		isInForm = false;
		isInTag = false;
		
	}
	
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		this.processor.logger.warning("Fatal error in sentence " + cnt + ": "
				+ e.getMessage());
		isInSentence = false;
		isInMorph = false;
		isInForm = false;
		isInTag = false;
	}
	
	@Override
	public void warning(SAXParseException e) throws SAXException {
		this.processor.logger
				.warning("Warning in sentence " + cnt + ": "
						+ e.getMessage());
		isInSentence = false;
		isInMorph = false;
		isInForm = false;
		isInTag = false;
	}
	
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		return new InputSource(new StringReader(""));
	}
	
	@Override
	public Source resolve(String href, String base)
			throws TransformerException {
		return new StreamSource(new StringReader(""));
	}
	
}
