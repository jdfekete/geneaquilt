/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.utils;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

public class PiccoloUtils {
	
	/**
	 * Sets component's global location by taking into acount its scale.
	 * @param node
	 * @param x
	 * @param y
	 * @param updateBounds
	 */
	public static void setLocation(PNode node, double x, double y, boolean invalidateBounds) {
		double scale = node.getScale();
		PBounds b = node.getBoundsReference();
		b.x = x/scale;
		b.y = y/scale;
		if (invalidateBounds)
			node.signalBoundsChanged();	
	}
}
