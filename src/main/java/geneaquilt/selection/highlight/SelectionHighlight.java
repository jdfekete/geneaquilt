/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection.highlight;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.nodes.GraphicsConstants;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.selection.Selection;
import geneaquilt.selection.Selection.HighlightMode;
import geneaquilt.utils.MulticolorStroke;
import geneaquilt.utils.PrintConstants;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * The graphical object that represents a selection highlight.
 * @author dragice
 *
 */
public class SelectionHighlight extends Highlight {

	ArrayList<Selection> orderedSelections = new ArrayList<Selection>();
	SelectionCombination predecessorSelections = SelectionCombination.getEmptyInstance();
	SelectionCombination successorSelections = SelectionCombination.getEmptyInstance();
	private Shape predecessorModeFeedback = null, successorModeFeedback = null;
	
	private static float minimumScreenWidth = 3;
	private static float minimumScreenHeight = 3;

	
	/**
	 * Creates a selection highlight on a node, with no associated selection.
	 * @param node
	 */
	public SelectionHighlight(PNode node) {
		super(node);
	}
	
	public void addSelection(Selection s) {
		super.addSelection(s);
		orderedSelections.add(s);
		selectionModeChanged();
	}
	
	public void removeSelection(Selection s) {
		super.removeSelection(s);
		orderedSelections.remove(s);
		selectionModeChanged();
	}
	
	/**
	 * Called when the highlighting mode has changed in one of the selections
	 */
	public void selectionModeChanged() {
		predecessorSelections = SelectionCombination.getEmptyInstance();
		successorSelections = SelectionCombination.getEmptyInstance();
		for (Selection s: selections.getSelections()) {
			if (s.getHighlightMode() == HighlightMode.HIGHLIGHT_ALL || s.getHighlightMode() == HighlightMode.HIGHLIGHT_SUCCESSORS)
				successorSelections = successorSelections.getInstanceWithSelection(s);
			if (s.getHighlightMode() == HighlightMode.HIGHLIGHT_ALL || s.getHighlightMode() == HighlightMode.HIGHLIGHT_PREDECESSORS)
				predecessorSelections = predecessorSelections.getInstanceWithSelection(s);
		}
		updateShape();
	}
	
	public Selection getLastSelection() {
		if (isEmpty())
			return null;
		return orderedSelections.get(orderedSelections.size() - 1);
	}
	
    /**
     * Computes the shape and bounds of the highlight according to the position of the object to be highlighted.
     */
    public void updateShape() {
		PBounds b = from.getFullBoundsReference();
		
		if (! (shape instanceof Rectangle2D))
			shape = new Rectangle2D.Double();
		float growx = 0;
		float growy = -1;
		shape = new Rectangle2D.Double(b.getX() - growx, b.getY() - growy, b.getWidth() + growx*2, b.getHeight() + growy*2);
		// grow rectangle
    	//float m = -2;//GraphicsConstants.SELECTION_HIGHLIGHT_WIDTH / 2 + 1;
		//finalbounds.setFrame(finalbounds.getX() - m, finalbounds.getY() - m, finalbounds.getWidth() + m*2, finalbounds.getHeight() + m*2);
	 
		// Add highlighting mode feedback
    	
    	PNode selectedObject = from;
		Rectangle2D finalbounds = new Rectangle2D.Double();
		finalbounds.setFrame(shape.getBounds2D());
				
    	predecessorModeFeedback = null;
    	if (predecessorSelections != null && !predecessorSelections.isEmpty()) {
            if (selectedObject instanceof PIndi)
            	predecessorModeFeedback = createModeFeedback(b, 1, 0);
            else if (selectedObject instanceof PFam)
            	predecessorModeFeedback = createModeFeedback(b, 0, 1);
            if (predecessorModeFeedback != null)
            	finalbounds = finalbounds.createUnion(predecessorModeFeedback.getBounds2D());
    	}
    	successorModeFeedback = null;
    	if (successorSelections != null && !successorSelections.isEmpty()) {
            if (selectedObject instanceof PIndi)
            	successorModeFeedback = createModeFeedback(b, -1, 0);
            else if (selectedObject instanceof PFam)
            	successorModeFeedback = createModeFeedback(b, 0, -1);
            if (successorModeFeedback != null)
            	finalbounds = finalbounds.createUnion(successorModeFeedback.getBounds2D());
    	}    	

		setBounds(finalbounds);//.getX() - m, finalbounds.getY() - m, finalbounds.getWidth() + m*2, finalbounds.getHeight() + m*2);

    }
    
    private Shape createModeFeedback(PBounds r, int x, int y) {
    	final float d = 4;
    	if (x == -1) {
    		return new Line2D.Float((float)r.getX() - d, (float)r.getY(), (float)r.getX() - d, (float)r.getY() + (float)r.getHeight());
    	} else if (x == 1) {
    		return new Line2D.Float((float)r.getX() + (float)r.getWidth() + d, (float)r.getY(), (float)r.getX() + (float)r.getWidth() + d, (float)r.getY() + (float)r.getHeight());
    	} else if (y == -1) {
    		return new Line2D.Float((float)r.getX(), (float)r.getY() - d, (float)r.getX() + (float)r.getWidth(), (float)r.getY() - d);
    	} else if (y == 1) {
    		return new Line2D.Float((float)r.getX(), (float)r.getY() + (float)r.getHeight() + d, (float)r.getX() + (float)r.getWidth(), (float)r.getY() + (float)r.getHeight() + d);
    	}
    	return null;
    }
		
    /**
     * {@inheritDoc}
     */
    @Override
    protected void paint(PPaintContext paintContext) {
        if (shape != null && !isEmpty()) {
            final Graphics2D g2 = paintContext.getGraphics();
        	double scale = PrintConstants.instance.getScale(paintContext);
        	
        	Rectangle2D bounds = shape.getBounds2D();
			float h = (float)(bounds.getHeight() * scale);
			float w = (float)(bounds.getWidth() * scale);
			if (h <= minimumScreenHeight || w < minimumScreenWidth) {
        		g2.setColor(selections.getTranslucentCombinedColor());
    			g2.setStroke(GraphicsConstants.SELECTION_STROKE);
				// Grow the rectangle
				float h2 = (float)Math.max(bounds.getHeight(), minimumScreenHeight / scale);
				float w2 = (float)Math.max(bounds.getWidth(), minimumScreenWidth / scale);
				g2.fillRect(
					(int)(bounds.getCenterX() - w2 / 2),
					(int)(bounds.getCenterY() - h2 / 2),
					(int)(w2 + 0.5f),
					(int)(h2 + 0.5f));
			} else if (scale < GraphicsConstants.MULTICOLOR_STROKE_ZOOM_FACTOR) {
        		g2.setColor(selections.getTranslucentCombinedColor());
    			g2.setStroke(GraphicsConstants.SELECTION_STROKE);
        		g2.draw(shape);
        		if (predecessorModeFeedback != null) {
	        		g2.setColor(predecessorSelections.getTranslucentCombinedColor());
	    			g2.setStroke(GraphicsConstants.SELECTION_STROKE);
	        		g2.draw(predecessorModeFeedback);
        		}
        		if (successorModeFeedback != null) {
	        		g2.setColor(successorSelections.getTranslucentCombinedColor());
	    			g2.setStroke(GraphicsConstants.SELECTION_STROKE);
	        		g2.draw(successorModeFeedback);
        		}
        	} else {
        		MulticolorStroke mstroke = selections.getMulticolorStroke(GraphicsConstants.SELECTION_HIGHLIGHT_WIDTH, false);
        		mstroke.draw(g2, shape);
        		if (predecessorModeFeedback != null) {
        			mstroke = predecessorSelections.getMulticolorStroke(GraphicsConstants.SELECTION_HIGHLIGHT_WIDTH, false);
            		mstroke.draw(g2, predecessorModeFeedback);
        		}
        		if (successorModeFeedback != null) {
        			mstroke = successorSelections.getMulticolorStroke(GraphicsConstants.SELECTION_HIGHLIGHT_WIDTH, false);
            		mstroke.draw(g2, successorModeFeedback);
        		}
        	}
        }
    }
}
