/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.nodes;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.Printer;
import geneaquilt.utils.PrintConstants;

/**
 * <b>PIsoShape</b> show a shape centered and scaled to
 * fit the specified bounds. The scale is the same in X and Y.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class PIsoShape extends PNode {
    private Shape shape;
    
    /**
     * Circle shape
     */
    public static final Shape CIRCLE = new Ellipse2D.Double(0, 0, 10, 10);

    /**
     * Creates a PIsoShape with the specified shape.
     * 
     * IMPORTANT: The shape bounds must be within (0, 0, 10, 10). This allows to specify margins.
     * @param shape the shape
     */
    public PIsoShape(Shape shape) {
        this.shape = shape;
    }
    
    /**
     * @return the shape
     */
    public Shape getShape() {
    	return shape;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void paint(PPaintContext paintContext) {
        if (getPaint() != null) {
            final Graphics2D g2 = paintContext.getGraphics();
            g2.setPaint(getPaint());
            paintIsoShape(paintContext, shape, getBoundsReference());
        }
    }
    
    /**
     * Temporary fix. This allows PathHighlight to render on top of this node using the same shape.
     * @param paintContext the paint context
     * @param shape the shape to paint
     * @param b the bounding box where the shape should be drawn
     */
    public static void paintIsoShape(PPaintContext paintContext, Shape shape, PBounds b) {
        Rectangle2D sb = shape.getBounds2D();
        double w = Math.min(b.getWidth(), b.getHeight());
        double minSize 
            = paintContext.getRenderQuality() == PPaintContext.LOW_QUALITY_RENDERING 
                ? 1 : 0.25f;

        // don't draw edges in small scales. This will accelerate the rendering of
        // the overview.
        if (Printer.isPrinting() || PrintConstants.instance.getScale(paintContext) * w > minSize) {
            PAffineTransform at = new PAffineTransform();

            double sx = b.getWidth() / 10;//sb.getWidth();
            double sy = b.getHeight() / 10;//sb.getHeight();
            double s = Math.min(sx, sy);
//            at.scaleAboutPoint(s, b.getCenterX(), b.getCenterY());
            at.translate(b.getCenterX(), b.getCenterY());
            at.scale(s, s);
            at.translate(-sb.getCenterX(), -sb.getCenterY());
            
            if (Printer.isPrinting()) {
                Graphics2D g2 = (Graphics2D)paintContext.getGraphics();
                g2 = (Graphics2D)g2.create();
                g2.transform(at);
                g2.fill(shape);
                g2.dispose();
            }
            else {
                paintContext.pushTransform(at);
                paintContext.getGraphics().fill(shape);
                paintContext.popTransform(at);
            }
        }
    }
}
