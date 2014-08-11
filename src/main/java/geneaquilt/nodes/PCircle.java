/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.nodes;

import java.awt.geom.Ellipse2D;

/**
 * Class PCircle
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class PCircle extends PIsoShape {
    static Ellipse2D.Double ellipse = new Ellipse2D.Double();
    /**
     * Creates a PCircle 
     */
    public PCircle() {
        super(ellipse);
    }

    /**
     * @return the shape
     */
    public Ellipse2D getEllipse() {
    	return ellipse;
    }
}
