/*******************************************************************************
 * File LinkedListNode.java
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
package de.tuebingen.rparse.misc;

import java.util.ArrayList;
import java.util.List;

/**
 * As the name suggests, an element in a linked list.
 * 
 * @author ke
 * @param <T>
 *            Element type in the linked list node
 */
public class LinkedListNode<T> {

    private final T           data;

    private LinkedListNode<T> next;

    public LinkedListNode(T data) {
        this.data = data;
    }

    public LinkedListNode(T data, LinkedListNode<T> next) {
        this(data);
        setNext(next);
    }

    public T getData() {
        return data;
    }

    public void setNext(LinkedListNode<T> next) {
        this.next = next;
    }

    public LinkedListNode<T> getNext() {
        return next;
    }

    public List<T> toList() {
        List<T> result = new ArrayList<T>();
        toList(result);
        return result;
    }

    private void toList(List<T> list) {
        list.add(data);

        if (next != null) {
            next.toList(list);
        }
    }

    /**
     * Creates a copy of list1 and returns the result of appending that copy with list2. Neither list1 nor list2 is
     * modified.
     * 
     * @param <T>
     * @param list1
     * @param list2
     * @return
     */
    public static <T> LinkedListNode<T> append(LinkedListNode<T> list1,
            LinkedListNode<T> list2) {
        if (list1 == null) {
            return list2;
        }

        LinkedListNode<T> currentNode = new LinkedListNode<T>(list1.getData());
        LinkedListNode<T> result = currentNode;
        list1 = list1.getNext();

        while (list1 != null) {
            LinkedListNode<T> newNode = new LinkedListNode<T>(list1.getData());
            currentNode.setNext(newNode);
            currentNode = newNode;
            list1 = list1.getNext();
        }

        currentNode.setNext(list2);
        return result;
    }

}
