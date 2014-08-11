package geneaquilt.selection.highlight;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.nodes.GraphicsConstants;
import geneaquilt.nodes.PIsoShape;
import geneaquilt.utils.MulticolorStroke;
import geneaquilt.utils.PrintConstants;

/**
 * The graphical object that represents a path highlight.
 * @author dragice
 *
 */
public class PathHighlight extends Highlight {

	float pathWidth;
	
	/**
	 * Creates a highlight on one node, with no associated selection.
	 * @param node
	 */
	public PathHighlight(PNode node) {
		super(node);
	}
	
	/**
	 * Creates a highlight between two nodes, with no associated selection.
	 * @param node
	 */
	public PathHighlight(PNode from, PNode to) {
		super(from, to);
	}
	
	   /**
     * Computes the shape and bounds of the highlight according to the position of the object(s) to be highlighted.
     * Call this method whenever the bounds of the "from" or "to" node you passed to the constructor change.
     */
    public void updateShape() {
    	if (to == null) {
    		
    		PBounds b = from.getFullBoundsReference();
    		if (from instanceof PIsoShape) {
    			// Special case: edges
    			shape = ((PIsoShape)from).getShape();
    		} else {
	    		if (shape instanceof Rectangle2D) {
	    			((Rectangle2D)shape).setRect(b);
	    		} else {
	    			shape = new Rectangle2D.Double(b.getX(), b.getY(), b.getWidth(), b.getHeight());
	    		}
    		}
    		setBounds(b);
    	} else {
    		PBounds b1 = from.getFullBoundsReference();
    		PBounds b2 = to.getFullBoundsReference();
    		if (shape instanceof Line2D) {
    			((Line2D)shape).setLine(b1.getCenterX(), b1.getCenterY(), b2.getCenterX(), b2.getCenterY());
    		} else {
    			shape = new Line2D.Double(b1.getCenterX(), b1.getCenterY(), b2.getCenterX(), b2.getCenterY());
    		}
    		double x0 = Math.min(b1.getCenterX(), b2.getCenterX());
    		double y0 = Math.min(b1.getCenterY(), b2.getCenterY());
    		double x1 = Math.max(b1.getCenterX(), b2.getCenterX());
    		double y1 = Math.max(b1.getCenterY(), b2.getCenterY());
    		
    		if (Math.abs(b1.getCenterX() - b2.getCenterX()) < Math.abs(b1.getCenterY() - b2.getCenterY())) {
    			pathWidth = (float)Math.min(b1.getWidth(), b2.getWidth());
        		pathWidth = Math.max(3, pathWidth - 5);
    		} else {
    			pathWidth = (float)Math.min(b1.getHeight(), b2.getHeight());
        		pathWidth = Math.max(3, pathWidth - 8);
    		}
    		
    		setBounds(
    			x0 - pathWidth / 2 - 1,
    			y0 - pathWidth / 2 - 1,
    			x1 - x0 + pathWidth + 2,
    			y1 - y0 + pathWidth + 2);
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void paint(PPaintContext paintContext) {
        if (!isEmpty()) {
            final Graphics2D g2 = paintContext.getGraphics();
            if (to == null && from instanceof PIsoShape) {
            	// special case: edges
            	g2.setColor(selections.getOpaqueCombinedColor());
            	PIsoShape.paintIsoShape(paintContext, shape, getFullBoundsReference()); // temporary fix
            } else if (to == null) {
            	// Highlight a unique object
            	g2.setColor(selections.getTranslucentCombinedColor());
            	g2.fill(shape);
            } else {
            	// Highlight the line connecting two objects
            	double scale = PrintConstants.instance.getScale(paintContext);
            	if (scale < 1 / GraphicsConstants.PATH_HIGHLIGHT_WIDTH && paintContext.getRenderQuality() == PPaintContext.LOW_QUALITY_RENDERING) {
            		g2.setColor(selections.getLightCombinedColor());
        			g2.setStroke(GraphicsConstants.NULL_WIDTH_STROKE);
            		g2.draw(shape);
            	} else {
	            	if (scale < GraphicsConstants.MULTICOLOR_STROKE_ZOOM_FACTOR) {
	            		g2.setColor(selections.getTranslucentCombinedColor());
            			g2.setStroke(GraphicsConstants.PATH_HIGHLIGHT_STROKE);
	            		g2.draw(shape);
	            	} else {
	            		MulticolorStroke mstroke = selections.getMulticolorStroke(pathWidth, selections.getSelectionCount() == 1);
	            		mstroke.draw(g2, shape);
	            	}
            	}
            }
        }
    }
}
