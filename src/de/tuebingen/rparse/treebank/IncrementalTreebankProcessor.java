/*******************************************************************************
 * File IncrementalTreebankProcessor.java
 * 
 * Authors:
 *    Kilian Evang, Wolfgang Maier
 *    
 * Copyright:
 *    Kilian Evang, Wolfgang Maier, 2011
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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.tuebingen.rparse.misc.Ranges;

/**
 * Allows for processing a treebank sentence by sentence. To do this, first call
 * {@link initialize()} with the reader to read the treebank from as well as a
 * processing task, then use the methods. Then use {@link hasNext()}, {@link
 * skipNext()}, and {@link processNext()} to incrementally process (parts of)
 * the treebank.
 * 
 * Alternatively, instances of this class can be used like a non-incremental
 * treebank processor by calling {@link process(Reader, ProcessingTask<? super
 * S>)}.
 * 
 * To implement this class for a specific treebank text format, implement the
 * methods {@link getNextSentence()}, {@link doInitialize(Reader)} and,
 * optionally, {@link skipNextSentence()}.
 * 
 * @author ke, wmaier
 * 
 * @param <S>
 */
public abstract class IncrementalTreebankProcessor<S extends HasSize> extends
		TreebankProcessor<S> implements Iterable<S>, Iterator<S> {
	
	private boolean mayHaveNext;
	
	private S next;
	
	/**
	 * Implements {@link TreebankProcessor}'s normal functionality of processing
	 * a (range in a) treebank all at once, but via the incremental processing
	 * interface.
	 */
	@Override
	public final void process(Reader reader, ProcessingTask<? super S> task,
			Ranges ranges, Integer maxlen) throws IOException,
			TreebankException {
		
		initialize(reader);
		
		int snum = 0;
		int next = 0;
		while (ranges.hasNext()) {
			next = ranges.next();
			while (next > snum + 1) {
				if (!hasNext())
					break;
				skipNext();
				++snum;
			}
			
			if (!hasNext())
				break;
			processNext(task, maxlen);
			++snum;
		}
		
		task.done();
	}
	
	/**
	 * Initializes the processor with a reader.
	 * @param reader The reader
	 */
	public final void initialize(Reader reader) {
		mayHaveNext = true;
		doInitialize(reader);
	}
	
	@Override
	public final boolean hasNext() {
		if (!mayHaveNext) {
			return false;
		}
		
		if (next == null) {
			next = getNextSentence();
			
			if (next == null) {
				mayHaveNext = false;
				return false;
			}
			
		}
		
		return true;
	}
	
	/**
	 * Get the length of the structure. TODO should be superseeded by hasSize. Why is this here anyway?!?
	 * @param sentence 
	 * @return 
	 */
	abstract public int getLength(S sentence);
	
	/**
	 * Skips the next sentence. 
	 * 
	 * @throws NoSuchElementException
	 *             if there is no next sentence.
	 * @throws TreebankException
	 */
	public final void skipNext() throws TreebankException {
		if (!mayHaveNext) {
			throw new NoSuchElementException();
		}
		
		if (next == null) {
			try {
				skipNextSentence();
			} catch (IOException e) {
				throw new TreebankException(e);
			}
		} else {
			next = null;
		}
	}
	
	/**
	 * Process the next sentence.
	 * 
	 * @param task The task to be used.
     * @param maxlen Will be excluded if too long. TODO really necessary?
	 * @throws TreebankException
	 * @throws NoSuchElementException
	 *             if there is no next sentence.
	 */
	public final void processNext(ProcessingTask<? super S> task, int maxlen)
			throws TreebankException {
		S next = next();
		if (next.size() <= maxlen)
			task.processSentence(next);
	}
	
	/**
	 * Get the next sentence.
	 * 
	 * @return the next sentence.
	 * @throws {@link NoSuchElementException} if there is no next sentence.
	 */
	@Override
	public final S next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		
		S result = next;
		next = null;
		return result;
	}
	
	/**
	 * Called before processing a new treebank to communicate the reader to read
	 * the treebank from.
	 * 
	 * @param reader
	 */
	protected abstract void doInitialize(Reader reader);
	
	/**
	 * Skips over one sentence in the input. This default implementation does
	 * this by calling {@link getNext()} and discarding the result. Subclasses
	 * may use a more efficient method.
	 * @throws IOException 
	 * @throws TreebankException 
	 */
	protected void skipNextSentence() throws IOException, TreebankException {
		getNextSentence();
	}
	
	/**
	 * Reads the next sentence from {@link reader}.
	 * 
	 * @return The next sentence, or {@code null} if there is there is no next
	 *         sentence. Implementors may assume that once this method has
	 *         returned {@code null} once, it will not be called before the
	 *         processor is re-initialized.
	 */
	protected abstract S getNextSentence();
	
	/**
	 * Iterate over the treebank
	 */
	@Override
	public Iterator<S> iterator() {
		return this;
	}
	
	/**
	 * Removal of elements is of course not supported
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Cannot remove sentences from a treebank");
	}
	
}
