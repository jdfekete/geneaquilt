/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.utils;

import java.awt.Graphics2D;

import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * <b>PPrintContext</b> manages a print context
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class PPrintContext extends PPaintContext {

    /**
     * Creates a PPrintContext associated with the given graphics context.
     * 
     * @param graphics graphics context to associate with this paint context
     */
    public PPrintContext(Graphics2D graphics) {
        super(graphics);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushTransform(PAffineTransform aTransform) {
        if (aTransform != null && ! aTransform.isIdentity())
            super.pushTransform(aTransform);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void popTransform(PAffineTransform aTransform) {
        if (aTransform != null && ! aTransform.isIdentity())
            super.popTransform(aTransform);
    }
    
}
