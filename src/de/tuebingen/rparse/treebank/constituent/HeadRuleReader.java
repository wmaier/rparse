/*******************************************************************************
 * File HeadRuleReader.java
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
package de.tuebingen.rparse.treebank.constituent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import de.tuebingen.rparse.misc.Constants;

/**
 * Reads head rules from a file
 * @author wmaier
 *
 */
public class HeadRuleReader extends BufferedReader {

	private final static String ENCODING = "UTF8";

	public HeadRuleReader(String string) throws UnsupportedEncodingException, FileNotFoundException {
		this(new File(string));
	}

	public HeadRuleReader(File f) throws UnsupportedEncodingException, FileNotFoundException {
		this(new FileInputStream(f));
	}
	
	public HeadRuleReader(InputStream is) throws UnsupportedEncodingException {
		super(new InputStreamReader(is, ENCODING));
	}
	

	public HeadRules getHeadRules() throws IOException {
		HeadRules hrs = new HeadRules();
		String line = "";
		while ((line = super.readLine()) != null) {
			line = line.trim();
			if (line.length() > 7 && line.charAt(0) != '%') {
				String[] split = line.split("\\s+");
				if (split.length < 2) 
					continue;
				if (!(split[1].equals(Constants.LEFT_TO_RIGHT) || split[1].equals(Constants.RIGHT_TO_LEFT))) 
					continue;
				String direction = split[1];
				String[] labels = Arrays.copyOfRange(split, 2, split.length);
				for (int i = 0; i < labels.length; ++i)
					labels[i] = labels[i].toUpperCase();
				HeadRule hr = new HeadRule(direction, labels);
				hrs.addRule(split[0].toUpperCase(), hr);
			}
		}
		return hrs;
	}

}
