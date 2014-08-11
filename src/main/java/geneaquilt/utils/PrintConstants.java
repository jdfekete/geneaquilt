/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.utils;

import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.nodes.GraphicsConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

/**
 * <b>GraphicsConstants</b> defines graphic constants
 * 
 * @author Pierre Dragicevic
 */
@SuppressWarnings("all")
public class PrintConstants extends GraphicsConstants {

	@Override
    public Color gridColor() { return Color.BLACK; }

	@Override
	public Stroke gridStroke() { return new BasicStroke(0.2f); }

	@Override
	public Color edgeColor() { return Color.BLACK; }

	@Override
	public Color famBorderColor() { return Color.BLACK; }
	
	@Override
	public double getScale(PPaintContext p) { return 1; }

}
