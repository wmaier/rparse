/*******************************************************************************
 * File Utilities.java
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
package de.tuebingen.rparse.misc;

import java.io.File;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Misc static utility methods
 * 
 * @author wmaier
 */
public class Utilities {

    /**
     * Hash code from a bunch of objects
     * 
     * @param objects
     * @return
     */
    public static int hashCode(Object... objects) {
        int result = 31 + hashCode(objects[0]);

        for (int i = 1; i < objects.length; i++) {
            result *= 31 + hashCode(objects[i]);
        }

        return result;
    }

    /**
     * The minimum of a list of integers.
     * 
     * @param numbers
     * @return
     */
    public static int min(int... numbers) {
        int result = numbers[0];

        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i] < result) {
                result = numbers[i];
            }
        }

        return result;
    }

    /**
     * A List of Integer from an array of int.
     * 
     * @param a
     * @return
     */
    public static List<Integer> asList(final int[] a) {

        return new AbstractList<Integer>() {

            public Integer get(int i) {
                return a[i];
            }

            public Integer set(int i, Integer val) {
                Integer oldVal = a[i];
                a[i] = val;
                return oldVal;
            }

            public int size() {
                return a.length;
            }
        };

    }

    /**
     * Remove the arity from a predicate name
     * 
     * @param predicateNameWithArity
     * @return
     */
    public static String removeArity(String predicateNameWithArity) {
        int i = predicateNameWithArity.length() - 1;

        if (!Character.isDigit(predicateNameWithArity.charAt(i))) {
            return predicateNameWithArity;
        }

        for (i--; i >= 0; i--) {
            if (!Character.isDigit(predicateNameWithArity.charAt(i))) {
                return predicateNameWithArity.substring(0, i + 1);
            }
        }

        return "";
    }

    /**
     * Get the artiy from a predicate name
     * 
     * @param predicateNameWithArity
     * @return
     */
    public static int getArity(String predicateNameWithArity) {
        int i = predicateNameWithArity.length() - 1;

        if (!Character.isDigit(predicateNameWithArity.charAt(i))) {
            return -1;
        }

        for (i--; i >= 0; i--) {
            if (!Character.isDigit(predicateNameWithArity.charAt(i))) {
                return Integer.valueOf(predicateNameWithArity.substring(i + 1));
            }
        }

        return -1;
    }

    /**
     * Splits a List of non-negative Integers in a List of List of integers, such that each of the sublists is a
     * continuous block. The result is not defined if the input list is not sorted or if the input list contains
     * duplicates or if the input list contains negative numbers.
     * 
     * @param l
     *            The input list.
     * @return The two-dimensional output list.
     */
    public static List<List<Integer>> splitContinuous(List<Integer> l) {
        List<List<Integer>> splits = new ArrayList<List<Integer>>();
        splits.add(new ArrayList<Integer>());
        for (int i = 0; i < l.size(); ++i) {
            if (i > 0 && l.get(i) - 1 != l.get(i - 1))
                splits.add(new ArrayList<Integer>());
            splits.get(splits.size() - 1).add(l.get(i));
        }
        return splits;
    }

    /**
     * Convert a List of List of Integers into a two dimensional int array.
     * 
     * @param ll
     *            The List of List of Integers.
     * @return The two dimensional int array.
     */
    public static int[][] asIntArrayArray(List<List<Integer>> ll) {
        int[][] llist = new int[ll.size()][];
        for (int i = 0; i < ll.size(); ++i) {
            int[] list = new int[ll.get(i).size()];
            for (int j = 0; j < ll.get(i).size(); ++j)
                list[j] = ll.get(i).get(j);
            llist[i] = list;
        }
        return llist;
    }

    /**
     * Convert an List of Integers into an int array.
     * 
     * @param l
     *            The List of Integers
     * @return The int array.
     */
    public static int[] asIntArray(List<Integer> l) {
        int[] list = new int[l.size()];
        for (int i = 0; i < l.size(); ++i) {
            list[i] = l.get(i);
        }
        return list;
    }

    /**
     * Deep copy of a two-dimensional array
     * 
     * @param a
     *            The two dimensional array to be copied.
     * @return The copy.
     */
    public static int[][] arrayDeepCopy(int[][] a) {
        int[][] ret = new int[a.length][];
        for (int i = 0; i < a.length; ++i)
            ret[i] = Arrays.copyOf(a[i], a[i].length);
        return ret;
    }

    /**
     * equals for a list of objects
     * 
     * @param objects
     * @return
     */
    public static boolean equal(List<?> objects) {
        int size = objects.size();

        if (size < 2) {
            return true;
        }

        Object object = objects.get(0);

        for (int i = 1; i < size; i++) {
            if (!objects.get(i).equals(object)) {
                return false;
            }
        }

        return true;
    }

    /**
     * equals with null check
     * 
     * @param one
     * @param another
     * @return
     */
    public static boolean equal(Object one, Object another) {
        if (one == null) {
            return another == null;
        }

        return one.equals(another);
    }

    /**
     * Equals for a series of objects
     * 
     * @param objects
     *            The sequence of objects
     * @return True if all objects in the series are equal (determined by equals()), otherwise false.
     */
    public static boolean equals(Object... objects) {
        if (objects.length < 2) {
            return true;
        }

        for (int i = 1; i < objects.length; i++) {
            if (!equal(objects[0], objects[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if two lists have a common suffix.
     * 
     * @param list1
     *            The first list to be checked
     * @param list2
     *            The second list to be checked
     * @param suffix
     *            Contains the reverse of the common suffix (after returning)
     * @param newList1
     *            Contains the part of list1 without the suffix (after returning)
     * @param newList2
     *            Contains the part of list2 without the suffix (after returning)
     */
    public static <T> void commonSuffix(List<T> list1, List<T> list2,
            List<T> suffix, List<T> newList1, List<T> newList2) {
        int i1 = list1.size() - 1;
        int i2 = list2.size() - 1;

        while (i1 >= 0 && i2 >= 0) {
            T element1 = list1.get(i1);
            T element2 = list2.get(i2);

            if (!equal(element1, element2)) {
                break;
            }

            suffix.add(element1);
            i1--;
            i2--;
        }

        for (int i = 0; i <= i1; i++) {
            newList1.add(list1.get(i));
        }

        for (int i = 0; i <= i2; i++) {
            newList2.add(list2.get(i));
        }

        Collections.reverse(suffix);
    }

    /**
     * Checks if an array contains a certain object
     * 
     * @param haystack
     *            The array
     * @param needle
     *            The object
     * @return True if the object is contained in the array, otherwise false.
     */
    public static boolean arrayContains(Object[] haystack, Object needle) {
        for (Object straw : haystack) {
            if (equals(straw, needle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Recursively delete a directory
     * 
     * @param file
     *            The directory
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }

        file.delete();
    }

    /**
     * Integer null converted to "0"
     * 
     * @param integer
     *            The Integer instance
     * @return "=" if input is "null", otherwise the Integer itself
     */
    public static int nullToZero(Integer integer) {
        if (integer == null) {
            return 0;
        }

        return integer;
    }

    /**
     * A visual message for a Throwable
     * 
     * @param message
     *            The message
     * @param t
     *            The throwable
     * @return
     */
    public static Object visualError(String message, Throwable t) {
        return new Object[]{
                message,
                new JScrollPane(new JTextArea(join(
                        System.getProperty("line.separator"),
                        portrayStackTrace(t)), 20, 80))};
    }

    /**
     * Nicer stack trace for visual representation
     * 
     * @param t
     *            The Throwable where the stack trace comes from
     * @return
     */
    public static List<String> portrayStackTrace(Throwable t) {
        List<String> result = new ArrayList<String>();
        result.add(t.toString());
        for (StackTraceElement element : t.getStackTrace()) {
            result.add("        at " + element);
        }
        t = t.getCause();
        while (t != null) {
            result.add("Caused by: " + t);
            for (StackTraceElement element : t.getStackTrace()) {
                result.add("        at " + element);
            }
            t = t.getCause();
        }
        return result;
    }

    /**
     * Join the toString representations of a list of objects, separated by a given string.
     * 
     * @param glue
     *            The separator string.
     * @param pieces
     *            The objects, as an Iterable.
     * @return The corresponding string.
     */
    public static String join(String glue, Iterable<?> pieces) {
        StringBuilder result = new StringBuilder();
        Iterator<?> it = pieces.iterator();
        if (it.hasNext()) {
            result.append(it.next());
        }
        while (it.hasNext()) {
            result.append(glue);
            result.append(it.next());
        }
        return result.toString();
    }

}
