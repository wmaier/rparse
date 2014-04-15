package de.tuebingen.rparse.grammar.read;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import de.tuebingen.rparse.grammar.GrammarException;

public abstract class GrammarReader<T> extends BufferedReader {

	public GrammarReader(Reader in) {
		super(in);
	}
	
	abstract public T getRCG() throws IOException, GrammarException;

}
