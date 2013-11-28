/*******************************************************************************
 * File PTBUtil.java
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
package de.tuebingen.rparse.treebank.constituent.ptb;

import java.util.HashSet;
import java.util.Set;

import de.tuebingen.rparse.misc.Test;
import de.tuebingen.rparse.treebank.constituent.Node;
import de.tuebingen.rparse.treebank.constituent.NodeLabel;

public class PTBUtil {

    public static final Test<Node>   IS_NULL_ELEMENT = new Test<Node>() {

                                                         @Override
                                                         public boolean test(
                                                                 Node object) {
                                                             return PTBUtil
                                                                     .isNullElement(object);
                                                         }

                                                     };

    private static final Set<String> PUNCTUTATION_TAGS;

    static {
        PUNCTUTATION_TAGS = new HashSet<String>(7);
        PUNCTUTATION_TAGS.add("''");
        PUNCTUTATION_TAGS.add("``");
        PUNCTUTATION_TAGS.add("-LRB-");
        PUNCTUTATION_TAGS.add("-RRB-");
        PUNCTUTATION_TAGS.add(".");
        PUNCTUTATION_TAGS.add(":");
        PUNCTUTATION_TAGS.add(",");
    }

    public static String pureTag(Node node) {
        return pureTag(node.getLabel().getTag());
    }

    public static String pureTag(String tag) {
        if (tag.startsWith("-")) {
            return tag;
        }

        int end = tag.indexOf('=');

        if (end == -1) {
            end = tag.indexOf('-');

            if (end == -1) {
                return tag;
            }
        }

        return tag.substring(0, end);
    }

    public static boolean isPunctuationTag(String tag) {
        return PUNCTUTATION_TAGS.contains(tag);
    }

    public static String removeIndexFromNullElement(String word) {
        int index = word.lastIndexOf('-');

        if (index == -1) {
            return word;
        }

        return word.substring(0, index);
    }

    public static boolean isNullElement(Node object) {
        return isNullElementTag(object.getLabel().getTag());
    }

    private static boolean isNullElementTag(String tag) {
        return "-NONE-".equals(tag);
    }

    public static void setPureTag(Node node, String tag) {
        NodeLabel label = node.getLabel();
        String oldTag = label.getTag();
        label.setTag(tag + oldTag.substring(pureTag(oldTag).length()));
    }

    public static boolean pureTagIs(String string, Node node) {
        return string.equals(pureTag(node));
    }

}
