/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection.highlight;

import java.awt.Color;
import java.awt.Shape;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.selection.Selection;

/**
 * <b>Highlight</b> manages a highlight
 * 
 * @author Pierre Dragicevic
 * @version $Revision$
 */
public abstract class Highlight extends PNode {

	static Hashtable<String, Color> mixedColors = new Hashtable<String, Color>();

	PNode from;
	PNode to;
	SelectionCombination selections = SelectionCombination.getEmptyInstance();
	Shape shape = null;
	
	/**
	 * Creates a highlight on one node, with no associated selection.
	 * @param node
	 */
	public Highlight(PNode node) {
		this(node, null);
	}
	
	/**
	 * Creates a highlight between two nodes, with no associated selection.
	 * @param from the starting node
	 * @param to the ending node
	 */
	public Highlight(PNode from, PNode to) {
		this.from = from;
		this.to = to;
		setPickable(false);
		updateShape();
	}
	
	/**
	 * Assign a selection to this highlight. Several selections can share the same highlight.
	 * @param s
	 */
	public void addSelection(Selection s) {
		selections = selections.getInstanceWithSelection(s);
//		repaint();
	}
	
	/**
	 * Adds a collection of selections in the current hightlighted selection
	 * @param selections the collection
	 */
	public void addSelections(Collection<Selection> selections) {
		for (Selection s : selections)
			addSelection(s);
	}
	
	/**
	 * Remove a selection from this highlight.
	 * @param s
	 */
	public void removeSelection(Selection s) {
		selections = selections.getInstanceWithoutSelection(s);
//		repaint();
	}
	
	/**
	 * Tests if a specified selection is contained in this selection
	 * @param s the selection
	 * @return true/false
	 */
	public boolean containsSelection(Selection s) {
		return selections.contains(s);
	}
	
	/**
	 * @return true if the selection is empty
	 */
	public boolean isEmpty() {
		return selections.isEmpty();
	}
	
	/**
	 * @return the selection set
	 */
	public Set<Selection> getSelections() {
		return selections.getSelections();
	}

	/**
	 * @return the number of selections
	 */
	public int getSelectionCount() {
		return selections.getSelectionCount();
	}
	
	
    /**
     * Computes the shape and bounds of the highlight according to the position of the object(s) to be highlighted.
     * Call this method whenever the bounds of the "from" or "to" node you passed to the constructor change.
     */
    public abstract void updateShape();   
}
