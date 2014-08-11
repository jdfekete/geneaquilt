/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * <b>GraphicsConstants</b> defines graphic constants
 * 
 * @author Pierre Dragicevic
 */
@SuppressWarnings("all")
public class GraphicsConstants {

	public static GraphicsConstants instance = new GraphicsConstants();
	
    public static final float MAIN_WINDOW_SIZE = 0.9f; // ratio between window size and screen size 
    public static final int MAIN_WINDOW_MAX_WIDTH = 1200; // needed because very slow if too large 
    public static final int MAIN_WINDOW_MAX_HEIGHT = 1000; // needed because very slow if too large 
    public static final float BIRDSEYE_VIEW_SIZE = 0.4f; // ratio between birdseye window size and main window size 
    
	public Color gridColor() { return new Color(0.75f, 0.75f, 0.75f, 1f); }
	public static final Color GRID_COLOR_SMALL = new Color(0.9f, 0.9f, 0.9f, 1f);
	public Stroke gridStroke() { return new BasicStroke(1); }
	public static final double CELL_SIZE = 14; // height of individual names, width of family names 
	
	public static final Color INDI_COLOR = Color.BLACK;
	public static final Font INDI_FONT = new Font("Helvetica", Font.PLAIN, 12);
	public static final double INDI_LINE_SPACING = 0.85;
	
	public static final Color FAM_COLOR = Color.BLACK;
	public static final Font FAM_FONT = new Font("Helvetica", Font.PLAIN, 10);
	public Color famBorderColor() { return Color.WHITE; }
	
	public Color edgeColor() { return new Color(0.25f, 0.25f, 0.25f, 1f); }
	
	public double getScale(PPaintContext p) { return p.getScale(); }
	
	public static final Color FAM_GENERATION_COLOR = new Color(0.85f, 0.85f, 0.85f, 1f);
	public static final Color INDI_GENERATION_COLOR = new Color(0.75f, 0.75f, 0.75f, 1f);
	
	public static final float SELECTION_HIGHLIGHT_WIDTH = 3;
	public static final Stroke SELECTION_STROKE = new BasicStroke(SELECTION_HIGHLIGHT_WIDTH);
	public static final float PATH_HIGHLIGHT_WIDTH = 9; // FIXME
	public static final Stroke PATH_HIGHLIGHT_STROKE = new BasicStroke(PATH_HIGHLIGHT_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER); // FIXME
	public static final float PATH_HIGHLIGHT_MULTICOLOR_SPACING = 5; // in pixels 
	public static final Color[] SELECTION_COLORS = new Color[] {
		new Color(1f, 0f, 0f),
		new Color(0.2f, 0.2f, 1f),
		new Color(0f, 0.8f, 0f),
		new Color(0.7f, 0.7f, 0f),
		new Color(0.7f, 0.0f, 0.7f),
		new Color(0.0f, 0.7f, 0.7f),
		new Color(0.4f, 0.4f, 0.4f),
	};
	public static final double MULTICOLOR_STROKE_ZOOM_FACTOR = 0.6;

	public static final Color SMALL_TEXT_COLOR = new Color(0.8f, 0.8f, 0.8f, 1f);
	
    public static final BasicStroke NULL_WIDTH_STROKE = new BasicStroke(0);


}
