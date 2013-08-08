/*******************************************************************************
 * File IncrementalMrgProcessor.java
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
package de.tuebingen.rparse.treebank.constituent.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tuebingen.rparse.misc.Numberer;
import de.tuebingen.rparse.treebank.IncrementalTreebankProcessor;
import de.tuebingen.rparse.treebank.TreebankException;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeLabel;
import de.tuebingen.rparse.treebank.constituent.Tree;

/**
 * Incremental treebank processor for the context-free bracketing format of the
 * Penn Treebank (MRG).
 * 
 * @author ke
 * 
 */
public class IncrementalMrgProcessor extends IncrementalTreebankProcessor<Tree> {

	private static final Pattern TAG_WITH_EDGE_LABEL = Pattern
			.compile("([^\\-]+)-(\\D.*?)((?:=\\d+)?(?:-\\d+)?)");

	// character codes:

	private static final int LEFT_BRACKET = 40;

	private static final int RIGHT_BRACKET = 41;

	private static final int END = -1;

	// default values for node labels:

	private static final int DEFAULT_NUM = -1;

	private static final String DEFAULT_EDGE = "--";

	private static final String DEFAULT_PARENT = "-1";

	private static final String TOP_TAG = "VROOT";

	private static final String DEFAULT_SECEDGE = "";

	private static final String DEFAULT_COMMENT = "";

	private static final String DEFAULT_WORD = "";

	private static final String DEFAULT_MORPH = "--";

	private static final String DEFAULT_LEMMA = "--";

	private final Numberer nb;
	
	private Reader reader;
	
	private int sentenceNumber;

	public IncrementalMrgProcessor(Numberer nb) {
		this.nb = nb;
	}

	/**
	 * Parses the next sentence from the reader.
	 * 
	 * @param reader
	 * @return The sentence as a tree, or {@code null} if there is no next
	 *         sentence.
	 * @throws IOException
	 * @throws TreebankException
	 *             On unexpected input from the reader.
	 */
	private Tree parseSentence(Reader reader, int sentenceNumber) throws IOException,
			TreebankException {
		int character = firstCharacterAfterWhitespace(reader);

		if (character == END) {
			return null; // signal that there is no next sentence
		}

		if (character == LEFT_BRACKET) {
			String tag = parseTag(reader);
			Node root;

			if (tag == null) {
				// tagless sentence bracket
				root = new Node(new NodeLabel(-1, DEFAULT_WORD, TOP_TAG,
						DEFAULT_LEMMA, DEFAULT_MORPH, DEFAULT_EDGE,
						DEFAULT_PARENT, DEFAULT_SECEDGE, DEFAULT_COMMENT));
			} else {
				root = createNonTerminal(tag);
			}

			List<Node> terminals = new ArrayList<Node>();
			parseChildren(reader, root, terminals);
			int size = root.getChildren().size();

			if (size < 1) {
				throw new TreebankException("Empty sentence.");
			}

			Tree sentence = new Tree(root, nb);
			sentence.setTerminals(terminals);
			sentence.setLastterm(terminals.size());
			sentence.setId(sentenceNumber);
			return sentence;
		}

		throw new TreebankException(
				"Syntax error: missing opening sentence bracket, sentence number " + sentenceNumber + ".");
	}

	/**
	 * Recursively adds the descendants to an existing parent node. Also
	 * consumes the closing constituent bracket of that parent node.
	 * 
	 * @param reader
	 *            Initially must be positioned after the tag of the constituent
	 *            {@link parent} represents, and possibly after following
	 *            whitespace.
	 * @param parent
	 * @param terminals
	 * @throws IOException
	 * @throws TreebankException
	 */
	private void parseChildren(Reader reader, Node parent, List<Node> terminals)
			throws IOException, TreebankException {
		while (true) {
			int character = firstCharacterAfterWhitespace(reader);

			if (character == RIGHT_BRACKET) {
				// No more children, we're done.
				return;
			}

			if (character == END) {
				throw new TreebankException(
						"Syntax error: stream ended while parsing children.");
			}

			if (character == LEFT_BRACKET) {
				// determine tag and edge label:
				String tag = parseTag(reader);

				if (tag == null) {
					throw new TreebankException("Syntax error: tag missing.");
				}

				Node child = createNonTerminal(tag);
				parseChildren(reader, child, terminals);
				parent.appendChild(child);
				// continue with next child
			} else {
				// Turns out parent is a terminal, so we add it to the list and
				// modify the node label accordingly.
				terminals.add(parent);
				NodeLabel label = parent.getLabel();
				// parseWord(Reader, int) also consumes the closing constituent
				// bracket:
				label.setWord(parseWord(reader, character));
				label.setNum(terminals.size());
				return;
			}
		}
	}

	private Node createNonTerminal(String tag) {
		String edgeLabel;
		Matcher matcher = TAG_WITH_EDGE_LABEL.matcher(tag);

		if (matcher.matches()) {
			// Storing the index as part of the tag is a hack.
			// CoindexationEdgeConverter requires it. The Node class
			// would have to be extended to support explicit secedges.
			// Storing node numbers in the secedge field of the node
			// does not seem to be the way to go since node numbers
			// rely on the export numbering, which may change
			// frequently.
			tag = matcher.group(1) + matcher.group(3);
			edgeLabel = matcher.group(2);
		} else {
			edgeLabel = DEFAULT_EDGE;
		}

		return new Node(new NodeLabel(DEFAULT_NUM, DEFAULT_WORD, tag,
				DEFAULT_LEMMA, DEFAULT_MORPH, edgeLabel, DEFAULT_PARENT,
				DEFAULT_SECEDGE, DEFAULT_COMMENT));
	}

	/**
	 * Reads a word from the reader. Also consumes the closing constituent
	 * bracket of the corresponding terminal.
	 * 
	 * @param reader
	 *            Initially must be positioned directly after the first
	 *            character of the word.
	 * @param character
	 *            The first character of the word.
	 * @return
	 * @throws IOException
	 * @throws TreebankException
	 */
	private String parseWord(Reader reader, int firstCharacter)
			throws IOException, TreebankException {
		StringBuilder result = new StringBuilder();
		result.append((char) firstCharacter);

		while (true) {
			int number = reader.read();

			if (number == END) {
				throw new TreebankException(
						"Syntax error: stream ended while reading word.");
			}

			if (number == RIGHT_BRACKET) {
				return result.toString();
			}

			result.append((char) number);
		}
	}

	/**
	 * Reads the tag of a constituent. Also consumes the first whitespace
	 * character following the tag.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws TreebankException
	 */
	private String parseTag(Reader reader) throws IOException,
			TreebankException {
		consumeWhitespace(reader);
		StringBuilder result = new StringBuilder();

		while (true) {
			reader.mark(1);
			int number = reader.read();

			if (number == END) {
				throw new TreebankException(
						"Syntax error: stream ended while reading tag.");
			}

			if (number == LEFT_BRACKET || number == RIGHT_BRACKET) {
				// We have already consumed one character too much.
				reader.reset();

				if (result.length() == 0) {
					return null;
				}

				return result.toString();
			}

			char character = (char) number;

			if (Character.isWhitespace(character)) {
				return result.toString();
			}

			result.append(character);
		}
	}

	@Override
	public void skipNextSentence() throws TreebankException, IOException {
		sentenceNumber++;
		
		// skip non-bracket material:
		while (true) {
			int ord = reader.read();

			if (ord == -1) {
				return;
			}

			if (ord == LEFT_BRACKET) {
				break;
			}
		}

		int depth = 1;

		// balance brackets to skip one sentence:
		while (depth > 0) {
			int character = reader.read();

			if (character == -1) {
				throw new TreebankException(
						"Syntax error: stream ended before brackets were balanced.");
			}

			if (character == LEFT_BRACKET) {
				depth++;
			} else if (character == RIGHT_BRACKET) {
				depth--;
			}
		}
	}

	/**
	 * This method is left over from a dark age and a pain to use. Use
	 * {@link #consumeWhitespace(Reader)} instead.
	 * 
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	private int firstCharacterAfterWhitespace(Reader reader) throws IOException {
		while (true) {
			int character = reader.read();

			if (character == -1 || !Character.isWhitespace((char) character)) {
				return character;
			}
		}
	}

	private void consumeWhitespace(Reader reader) throws IOException {
		do {
			reader.mark(1);
		} while (Character.isWhitespace(reader.read()));

		reader.reset();
	}

	@Override
	protected void doInitialize(Reader reader) {
		if (!reader.markSupported()) {
			reader = new BufferedReader(reader);
		}
		
		this.reader = reader;
		sentenceNumber = 1;
	}

	@Override
	public int getLength(Tree sentence) {
		return sentence.getOrderedTerminals().size();
	}

	@Override
	protected Tree getNextSentence() {
		try {
			return parseSentence(reader, sentenceNumber++);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
