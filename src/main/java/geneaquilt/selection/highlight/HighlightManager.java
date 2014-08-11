/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection.highlight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import geneaquilt.nodes.PEdge;
import geneaquilt.selection.Selection;

public class HighlightManager extends PNode {
	
	private class HighlightKey {
		PNode from, to;
		public HighlightKey(PNode from, PNode to) {
			this.from = from;
			this.to = to;
		}
		public HighlightKey(Highlight h) {
			this(h.from, h.to);
		}
		public boolean equals(Object o) {
			if (!(o instanceof HighlightKey))
				return false;
			return (((HighlightKey)o).from == from && ((HighlightKey)o).to == to)
		||
		(((HighlightKey)o).from == to && ((HighlightKey)o).to == from);
		}
		public int hashCode() {
			return from.hashCode() + (to == null ? 0 : to.hashCode());
		}
	}
	
	Hashtable<HighlightKey, PathHighlight> pathHighlights = new Hashtable<HighlightKey, PathHighlight>();
	Hashtable<PNode, Set<PathHighlight>> pathHighlightsPerNode = new Hashtable<PNode, Set<PathHighlight>>();
	Hashtable<PNode, SelectionHighlight> selectionHighlights = new Hashtable<PNode, SelectionHighlight>();
	boolean testOverlap = false;
	PBounds fullBounds;
	
	public HighlightManager(PBounds fullBoundsReference) {
		super();
		this.fullBounds = fullBoundsReference;
	}
	
	protected boolean validateFullBounds() {
		return false;
	}
	
	public PBounds getFullBoundsReference() {
		return fullBounds;
	}
	
	public void setTestOverlap(boolean test) {
		testOverlap = true;
	}
	
	public void setPathHighlighted(PNode node, Selection selection, boolean highlight) {
		setPathHighlighted(node, null, selection, highlight);
	}
	
	public void setPathHighlighted(PNode from, PNode to, Selection selection, boolean highlight) {

		PathHighlight h = getPathHighlight(from, to); 

		if (highlight) {
			if (h == null) {
				h = new PathHighlight(from, to);
				boolean overlapto = testOverlap && to != null && !(to instanceof PEdge) && willOverlap(from, to, pathHighlightsPerNode.get(to));
				boolean overlapfrom = testOverlap && to != null && !(from instanceof PEdge) && willOverlap(from, to, pathHighlightsPerNode.get(from));
				h.addSelection(selection);
				add(h);
				if (overlapto)
					breakHighlights(from, to, pathHighlightsPerNode.get(to));
				if (overlapfrom)
					breakHighlights(from, to, pathHighlightsPerNode.get(from));
			} else {
				h.addSelection(selection);
				if (h.getSelectionCount() > 1)
					moveToFront(h);
			}
		} else {
			if (h != null) {
				h.removeSelection(selection);
				if (h.isEmpty()) {
					remove(h);
				} else {
					if (h.getSelectionCount() == 0)
						moveToBack(h);
				}
			}
		}
	}
	
	// faster than PNode.moveToFront()
	private void moveToFront(PNode n) {
		List l = getChildrenReference();
		l.remove(l.indexOf(n));
		l.add(n);
	}
	
	// faster than PNode.moveToFront()
	private void moveToBack(PNode n) {
		List l = getChildrenReference();
		l.remove(l.indexOf(n));
		l.add(0, n);
	}

	
	private boolean willOverlap(PNode from, PNode to, Set<PathHighlight> sh) {
		if (from == null || to == null || sh == null || sh.size() < 1)
			return false;
		
		for (PathHighlight h: sh) {
			if (overlap(from, to, h.from, h.to))
				return true;
		}
		return false;
	}
	
	private boolean overlap(PNode from, PNode to, PNode from2, PNode to2) {
		if (from == null || to == null || from2 == null || to2 == null)
			return false;
		
		PBounds bfrom = from.getFullBoundsReference();
		PBounds bto = to.getFullBoundsReference();
		PBounds bfrom2 = from2.getFullBoundsReference();
		PBounds bto2 = to2.getFullBoundsReference();
		// vertical case
		if (bfrom.getCenterX() == bto.getCenterX()) {
			if (Math.min(bfrom.getCenterY(), bto.getCenterY()) >= Math.max(bfrom2.getCenterY(), bto2.getCenterY()))
				return false;
			if (Math.max(bfrom.getCenterY(), bto.getCenterY()) <= Math.min(bfrom2.getCenterY(), bto2.getCenterY()))
				return false;
			return true;
		}
		// horizontal case
		if (bfrom.getCenterY() == bto.getCenterY()) {
			if (Math.min(bfrom.getCenterX(), bto.getCenterX()) >= Math.max(bfrom2.getCenterX(), bto2.getCenterX()))
				return false;
			if (Math.max(bfrom.getCenterX(), bto.getCenterX()) <= Math.min(bfrom2.getCenterX(), bto2.getCenterX()))
				return false;
			return true;
		}
		return false;
	}
	
	private void breakHighlights(PNode from, PNode to, Set<PathHighlight> sh) {
		if (sh == null || sh.size() < 2)
			return;
		
		Set<PathHighlight> sh2 = new HashSet<PathHighlight>(sh);

		final boolean vertical = (from.getFullBoundsReference().getCenterX() == to.getFullBoundsReference().getCenterX());
		
		ArrayList<PNode> sortedNodes = new ArrayList<PNode>();
		for (Highlight h : sh) {
			if (!sortedNodes.contains(h.from))
				sortedNodes.add(h.from);
			if (h.to != null && !sortedNodes.contains(h.to))
				sortedNodes.add(h.to);
		}
		
		int count = sortedNodes.size();
		if (count < 2)
			return;
		
		Collections.sort(sortedNodes, new Comparator<PNode>() {
			public int compare(PNode o1, PNode o2) {
				if (vertical)
					return Double.compare(o1.getGlobalBounds().getCenterY(), o2.getGlobalBounds().getCenterY());
				else
					return Double.compare(o1.getGlobalBounds().getCenterX(), o2.getGlobalBounds().getCenterX());
			}
		});
		
		ArrayList<Highlight> shortHighlights = new ArrayList<Highlight>();

		for (int i=0; i<count-1; i++) {
			PathHighlight h = getPathHighlight(sortedNodes.get(i), sortedNodes.get(i+1));
			if (h == null) {
				h = new PathHighlight(sortedNodes.get(i), sortedNodes.get(i+1));
				add(h);
			}
			shortHighlights.add(h);
			for (Highlight h2 : sh) {
				if (h2 != h && overlap(h.from, h.to, h2.from, h2.to)) {
					h.addSelections(h2.getSelections());
				}
			}
			if (h.getSelectionCount() > 1)
				moveToFront(h);
		}
		
		for (Highlight h : sh2) {
			if (!shortHighlights.contains(h) && h.to != null)
				remove(h);
		}
	}
	
	public void setSelectionHighlighted(PNode node, Selection selection, boolean highlight) {

		SelectionHighlight h = getSelectionHighlight(node); 

		if (highlight) {
			if (h == null) {
				h = new SelectionHighlight(node);
				h.addSelection(selection);
				add(h);
			} else {
				h.addSelection(selection);
				moveToFront(h);
			}
		} else {
			if (h != null) {
				h.removeSelection(selection);
				if (h.isEmpty()) {
					remove(h);
				}
			}
		}
				
	}
	
	public PathHighlight getPathHighlight(PNode node) {
		return getPathHighlight(node, null);
	}
	
	public PathHighlight getPathHighlight(PNode from, PNode to) {
		return pathHighlights.get(new HighlightKey(from, to));
	}

	public SelectionHighlight getSelectionHighlight(PNode node) {
		return selectionHighlights.get(node);
	}
	
	public Set<PNode> getHighlightedObjects(Selection s) {
		Set<PNode> res = new HashSet<PNode>();
		for (Object o : getChildrenReference()) {
			if (o instanceof Highlight) {
				Highlight h = (Highlight)o;
				if (h.containsSelection(s)) {
					res.add(h.from);
					if (h.to != null)
						res.add(h.to);
				}
			}
		}
		return res;
	}
	
	public void clearHighlights(Selection s) {
		List children_copy = new ArrayList(getChildrenReference());
		for (Object o : children_copy) {
			if (o instanceof Highlight) {
				Highlight h = (Highlight)o;
				if (h.containsSelection(s)) {
					h.removeSelection(s);
					if (h.isEmpty()) {
						remove(h);
					} else {
						if (h instanceof PathHighlight && h.getSelectionCount() == 1)
							moveToBack(h);
					}
				}
			}
		}
	}
		
	private void add(Highlight h) {
		
//		if (h.getSelectionCount() < 2)
//			addChild(0, h);
//		else
			addChild(h);
	
		if (h instanceof PathHighlight) {
			PathHighlight ph = (PathHighlight)h;
			pathHighlights.put(new HighlightKey(ph), ph);
			Set<PathHighlight> sh = pathHighlightsPerNode.get(ph.from);
			if (sh == null) {
				sh = new HashSet<PathHighlight>();
				pathHighlightsPerNode.put(ph.from, sh);
			}
			sh.add(ph);
			if (ph.to != null) {
				sh = pathHighlightsPerNode.get(ph.to);
				if (sh == null) {
					sh = new HashSet<PathHighlight>();
					pathHighlightsPerNode.put(ph.to, sh);
				}
				sh.add(ph);
			}
		} else if (h instanceof SelectionHighlight) {
			SelectionHighlight sh = (SelectionHighlight)h;
			selectionHighlights.put(sh.from, sh);
		}
	}
	
	private void remove(Highlight h) {
		
		removeChild(h);
		
		if (h instanceof PathHighlight) {		
			pathHighlights.remove(new HighlightKey(h));
			Set<PathHighlight> sh = pathHighlightsPerNode.get(h.from);
			if (sh != null) {
				sh.remove(h);
				if (sh.isEmpty())
					pathHighlightsPerNode.remove(h.from);
			}
			if (h.to != null) {
				sh = pathHighlightsPerNode.get(h.to);
				if (sh != null) {
					sh.remove(h);
					if (sh.isEmpty())
						pathHighlightsPerNode.remove(h.from);
				}
			}
		} else if (h instanceof SelectionHighlight) {
			selectionHighlights.remove(h.from);
		}
	}
	
	public void clear() {
		removeAllChildren();
		pathHighlights.clear();
		pathHighlightsPerNode.clear();
		selectionHighlights.clear();
	}
	
	public void updateHighlightShapes() {
		for (Object o : getChildrenReference()) {
			if (o instanceof Highlight) {
				((Highlight)o).updateShape();
			}
		}
	}
	
	public void selectionModeChanged(Selection s) {
		PNode n = s.getSelectedObject();
		SelectionHighlight h = getSelectionHighlight(n);
		if (h != null)
			h.selectionModeChanged();
	}
}
