/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.event;

import java.awt.geom.AffineTransform;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * <b>DisabablePanEventHandler</b> is a pan event that can be disabled.
 * 
 * @author Pierre Dragicevic
 */
public class DisabablePanEventHandler extends PPanEventHandler {

	private boolean enabled = true;

	/**
	 * Sets the enabled state
	 * @param enabled new state
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * @return if the handler is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	protected void pan(PInputEvent e) {
		if (enabled)
			super.pan(e);
	}
	
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(PInputEvent event) {
    	if (enabled && (event.getClickCount() == 2)) {
            PCamera camera = event.getCamera();
            PBounds cb = camera.getBoundsReference();
            AffineTransform t2 = AffineTransform.getTranslateInstance(-event.getPosition().getX() + cb.getWidth()/2, -event.getPosition().getY() + cb.getHeight()/2);
            PTransformActivity activity = camera.animateViewToTransform(t2, 250);
            activity.setSlowInSlowOut(false);
    	} else {
    		super.mouseClicked(event);
    	}
    }
}
