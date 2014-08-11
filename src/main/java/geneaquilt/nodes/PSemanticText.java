/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.nodes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.Printer;
import geneaquilt.utils.PrintConstants;

/**
 * <b>PSemanticText</b> draws text as a rectangle when it is below readable size.
 * It also makes the rectangle big enough to be seen when its Paint attribute is not null.
 * 
 * @author Pierre Dragicevic
 */
public class PSemanticText extends PText {

	private Rectangle2D.Float smallTextBounds = new Rectangle2D.Float();
	private static final float effective_y0 = 0f;//0.4f;
	private static final float effective_h = 1f;//0.5f;

	private static float minimumScreenWidth = 3;
	private static float minimumScreenHeight = 3;
	protected double greekThreshold2;

	/**
	 * Creates a ptext with tunable effective height
	 * @param label
	 */
	public PSemanticText(String label) {
		super(label);
		greekThreshold2 = super.getGreekThreshold();
		setGreekThreshold(0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void recomputeLayout() {
		super.recomputeLayout();
		Rectangle2D b = getBoundsReference();
		if (b != null && smallTextBounds != null)
			smallTextBounds.setFrame(
			        b.getX(), 
			        b.getY() + (int)(effective_y0*b.getHeight()), 
			        b.getWidth(), 
			        (int)(effective_h*b.getHeight())); 
	}

	protected void paint(PPaintContext paintContext) {
		float scale = (float) PrintConstants.instance.getScale(paintContext);
		float screenFontSize = getFont().getSize() * scale;
		if (getTextPaint() != null 
		        && (Printer.isPrinting() || screenFontSize > greekThreshold2)) {
			super.paint(paintContext);
		} else {
			Graphics2D g = paintContext.getGraphics();
			if (getPaint() == null) {
				g.setColor(GraphicsConstants.SMALL_TEXT_COLOR);
				g.fill(smallTextBounds);
			} else {
				g.setPaint(getPaint());
				float h = (float)(smallTextBounds.getHeight() * scale);
				float w = (float)(smallTextBounds.getWidth() * scale);
				if (h >= minimumScreenHeight && w > minimumScreenWidth) {
					g.fill(smallTextBounds);
				} else {
					// Grow the rectangle
					float h2 = Math.max((float)smallTextBounds.getHeight(), minimumScreenHeight / scale);
					float w2 = Math.max((float)smallTextBounds.getWidth(), minimumScreenWidth / scale);
					g.fillRect(
						(int)(smallTextBounds.getCenterX() - w2 / 2),
						(int)(smallTextBounds.getCenterY() - h2 / 2),
						(int)(w2 + 0.5f),
						(int)(h2 + 0.5f));
				}
				
			}
		}
	}
	
	static Color bgColor = new Color(1, 1, 1, 0.1f);
	static Color outlineColor = new Color(1, 1, 1, 1f);
//	static PAffineTransform outlineShifts[][] = new PAffineTransform[3][3];
//	static {
//		for (int x=0; x<3; x++)
//			for (int y=0; y<3; y++) {
//				outlineShifts[x][y] = new PAffineTransform(AffineTransform.getTranslateInstance((x-1)/1.0, (y-1)/1.0));
//			}
//	}

	/**
	 * 
	 * @param paintContext
	 */
	public void paintWithOutline(PPaintContext paintContext) {
		
		float scale = (float) PrintConstants.instance.getScale(paintContext);
		float screenFontSize = getFont().getSize() * scale;
		if (getTextPaint() != null && (Printer.isPrinting() || screenFontSize > greekThreshold)) {
			
			Graphics2D g = paintContext.getGraphics();
			if (getPaint() == null) {
				g.setColor(bgColor);
				g.fillRect(
						(int)(smallTextBounds.getX()),
						(int)(smallTextBounds.getY() + 4), // FIXME
						(int)(smallTextBounds.getWidth()),
						(int)(smallTextBounds.getHeight() - 6)); // FIXME
			} 
			
			Paint oldTextPaint = getTextPaint();
			setTextPaint(outlineColor);

			for (int x=0; x<3; x++)
				for (int y=0; y<3; y++) {
					if (x != 0 || y != 0) {
//						paintContext.pushTransform(outlineShifts[x][y]);
					    g.translate(x-1, y-1);
						super.paint(paintContext);
						g.translate(-x+1, -y+1);
						//paintContext.popTransform(outlineShifts[x][y]);
					}
				}

			super.paint(paintContext);
			setTextPaint(oldTextPaint);
			super.paint(paintContext);
		}
	}
	
}
