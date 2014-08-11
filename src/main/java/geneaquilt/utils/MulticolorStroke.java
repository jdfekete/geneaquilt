/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

/**
 * This class allows to paint shapes using a stroke with cycling colors.
 * 
 * @author dragice
 *
 */
public class MulticolorStroke {

	final Color[] colors;
	final Stroke[] strokes;
	final float spacing;
	final float width;
	
	/**
	 * Creates a multicolor stroke.
	 * 
	 * @param width
	 * @param colors
	 * @param spacing
	 */
	public MulticolorStroke(float width, Color[] colors, float spacing) {
		this.width = width;
		this.colors = colors;
		this.spacing = spacing;
		this.strokes = new Stroke[colors.length];
		createStrokes();
	}

	/**
	 * Creates a regular single-color stroke.
	 * 
	 * @param width
	 * @param color
	 */
	public MulticolorStroke(float width, Color color) {
		this(width, new Color[]{color}, 0);
	}
	
	private void createStrokes() {
		if (colors.length == 1) {
			strokes[0] = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
		} else {
			float[] dash = new float[2];
			dash[0] = spacing;
			dash[1] = spacing * (colors.length - 1);
			for (int i = 0; i<colors.length; i++) {
				strokes[i] = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, spacing * i);
			}
		}
	}

	/**
	 * Paints the shape using a multicolor stroke and no fill.
	 * 
	 * @param g the graphics
	 * @param shape the shape to draw
	 */
	public void draw(Graphics2D g, Shape shape) {
		for (int i=0; i<colors.length; i++) {
			g.setColor(colors[i]);
			g.setStroke(strokes[i]);
			g.draw(shape);
		}		
	}
	
}
