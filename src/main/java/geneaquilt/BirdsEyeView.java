/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.hull.Hull;
import geneaquilt.nodes.PFlatRect;
import geneaquilt.utils.GUIUtils;
import geneaquilt.utils.GUIUtils.AdvancedKeyListener;

/**
 * Class BirdsEyeView
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
/**
 * The Birds Eye View Class
 */
public class BirdsEyeView extends PCanvas implements PropertyChangeListener, AdvancedKeyListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * This is the node that shows the viewed area.
     */
    PFlatRect areaVisiblePNode;

    /**
     * This is the canvas that is being viewed
     */
    PCanvas viewedCanvas;

    /**
     * The change listener to know when to update the birds eye view.
     */
    PropertyChangeListener changeListener;

    int layerCount;
    
    final Hull hull;
    
    PDragSequenceEventHandler dragHandler;
    
    PTransformActivity currentCameraAnimation = null;
    
    private boolean smoothPanning = true;
    

    /**
     * Creates a new instance of a BirdsEyeView
     * @param hull_ the hull manager
     */
    public BirdsEyeView(Hull hull_) {

    	this.hull = hull_;
    	
        // create the PropertyChangeListener for listening to the viewed
        // canvas
        changeListener = new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                updateFromViewed();
            }
        };

        // create the coverage node
        areaVisiblePNode = new PFlatRect();
        areaVisiblePNode.setPaint(new Color(0.5f, 0.5f, 1f, 0.3f));
        areaVisiblePNode.setTransparency(.5f);
        areaVisiblePNode.setBounds(0, 0, 100, 100);
        getCamera().addChild(areaVisiblePNode);

        // add the drag event handler
        dragHandler = new PDragSequenceEventHandler() {
        	
        	Point2D dragOffset = new Point2D.Double();
        	Point2D p = new Point2D.Double();
        	
            protected void startDrag(final PInputEvent e) {
            	
                if (e.getPickedNode() == areaVisiblePNode) {
                	PBounds b = viewedCanvas.getCamera().getViewBounds();
                	dragOffset.setLocation(e.getPosition().getX() - b.getCenterX(), e.getPosition().getY() - b.getCenterY());
                	super.startDrag(e);
                } else {
                	if (e.isShiftDown()) {
                		PBounds b = viewedCanvas.getCamera().getViewBounds();
                		viewedCanvas.getCamera().translateView(b.x - e.getPosition().getX() + b.width/2, b.y - e.getPosition().getY() + b.height / 2);
	                    super.startDrag(e);
                	} else {
	                	dragOffset.setLocation(0, 0);
	                	moveCameraOnPath(e.getPosition(), true);
	                    super.startDrag(e);
                	}
                }
                viewedCanvas.setInteracting(true);
            }

            protected void drag(final PInputEvent e) {
            	if (e.isShiftDown()) {
            		final PDimension dim = e.getDelta();
            		viewedCanvas.getCamera().translateView(0 - dim.getWidth(), 0 - dim.getHeight());
            	} else {
	            	Point2D pos = e.getPosition();
	            	p.setLocation(pos.getX() - dragOffset.getX(), pos.getY() - dragOffset.getY());
	            	moveCameraOnPath(p, false);
            	}
            }
            
            protected void endDrag(final PInputEvent e) {
                viewedCanvas.setInteracting(false);
                super.endDrag(e);
            }

        };
        
        getCamera().addInputEventListener(dragHandler);
        
        addComponentListener(new ComponentAdapter() {
        	public void componentResized(ComponentEvent e) {
        		autoScale();
        		updateFromViewed();
        	}
        });

        // remove Pan and Zoom
        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());

        // Scrolling with keys
        GUIUtils.addAdvancedKeyListener(null, this, true);
        
        setDefaultRenderQuality(PPaintContext.LOW_QUALITY_RENDERING);

    }

    /**
     * Connects to a canvas and some layers.
     * @param canvas the canvas
     * @param viewed_layers the layers
     */
    public void connect(final PCanvas canvas, final PLayer[] viewed_layers) {

        viewedCanvas = canvas;
        layerCount = 0;

        viewedCanvas.getCamera().addPropertyChangeListener(changeListener);

        for (layerCount = 0; layerCount < viewed_layers.length; ++layerCount) {
            getCamera().addLayer(layerCount, viewed_layers[layerCount]);
        }

    }

    /**
     * Add a layer to list of viewed layers
     * @param new_layer the layer to add
     */
    public void addLayer(final PLayer new_layer) {
        getCamera().addLayer(new_layer);
        layerCount++;
    }

    /**
     * Remove the layer from the viewed layers
     * @param old_layer the layer to remove
     */
    public void removeLayer(final PLayer old_layer) {
        getCamera().removeLayer(old_layer);
        layerCount--;
    }

    /**
     * Stop the birds eye view from receiving events from the viewed canvas
     * and remove all layers
     */
    public void disconnect() {
        viewedCanvas.getCamera().removePropertyChangeListener(changeListener);

        for (int i = 0; i < getCamera().getLayerCount(); ++i) {
            getCamera().removeLayer(i);
        }

    }

    /**
     * This method will get called when the viewed canvas changes
     */
    public void propertyChange(final PropertyChangeEvent event) {
        updateFromViewed();
    }

    /**
     * This method gets the state of the viewed canvas and updates the
     * BirdsEyeViewer This can be called from outside code
     */
    public void updateFromViewed() {
        double viewedX;
        double viewedY;
        double viewedHeight;
        double viewedWidth;

        final double ul_camera_x = viewedCanvas.getCamera().getViewBounds().getX();
        final double ul_camera_y = viewedCanvas.getCamera().getViewBounds().getY();
        final double lr_camera_x = ul_camera_x + viewedCanvas.getCamera().getViewBounds().getWidth();
        final double lr_camera_y = ul_camera_y + viewedCanvas.getCamera().getViewBounds().getHeight();

//        final Rectangle2D drag_bounds = getCamera().getUnionOfLayerFullBounds();

//        final double ul_layer_x = drag_bounds.getX();
//        final double ul_layer_y = drag_bounds.getY();
//        final double lr_layer_x = drag_bounds.getX() + drag_bounds.getWidth();
//        final double lr_layer_y = drag_bounds.getY() + drag_bounds.getHeight();

        // find the upper left corner

        // set to the lesser value
//        if (ul_camera_x < ul_layer_x) {
//            viewedX = ul_layer_x;
//        }
//        else {
            viewedX = ul_camera_x;
//        }

        // same for y
//        if (ul_camera_y < ul_layer_y) {
//           viewedY = ul_layer_y;
//        }
//        else {
            viewedY = ul_camera_y;
//        }

        // find the lower right corner

        // set to the greater value
//        if (lr_camera_x < lr_layer_x) {
            viewedWidth = lr_camera_x - viewedX;
//        }
//        else {
//            viewedWidth = lr_layer_x - viewedX;
//        }

        // same for height
//        if (lr_camera_y < lr_layer_y) {
            viewedHeight = lr_camera_y - viewedY;
//        }
//        else {
 //           viewedHeight = lr_layer_y - viewedY;
//        }

        Rectangle2D bounds = new Rectangle2D.Double(viewedX, viewedY, viewedWidth, viewedHeight);
        bounds = getCamera().viewToLocal(bounds);
        areaVisiblePNode.setBounds(bounds);
    }
    
    /**
     * Auto scales the viewport.
     */
    public void autoScale() {
    	getCamera().setViewBounds(getCamera().getUnionOfLayerFullBounds());
    }
    /**
     * Moves along a path
     * @param p the point
     * @param jump true if jumping
     */
    public void moveCameraOnPath(Point2D p, boolean jump) {
    	Rectangle2D area = hull.getAreaToZoom(p);
    	int minZoomHeight = (int)viewedCanvas.getCamera().getHeight();
    	if (area.getHeight() < minZoomHeight)
    		area.setFrame(area.getX(), area.getCenterY() - minZoomHeight/2, area.getWidth(), minZoomHeight);
     	if (jump || smoothPanning) {
     		// The following allows a camera animation to start while another animation
     		// is already running.
     		if (currentCameraAnimation != null && currentCameraAnimation.isStepping()) {
     			currentCameraAnimation.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
     		}
     		if (jump) {
     			currentCameraAnimation = viewedCanvas.getCamera().animateViewToCenterBounds(area, true, 300);
     			currentCameraAnimation.setSlowInSlowOut(true);
     		} else {
     			currentCameraAnimation = viewedCanvas.getCamera().animateViewToCenterBounds(area, true, 150);
     			currentCameraAnimation.setSlowInSlowOut(false);
     		}
    		// Play one step right now, otherwise nothing will happen while dragging the mouse since animations
     		// won't have time to play.
    		currentCameraAnimation.setStartTime(System.currentTimeMillis() - currentCameraAnimation.getStepRate());
    		currentCameraAnimation.processStep(System.currentTimeMillis());            		
     	} else
    		viewedCanvas.getCamera().setViewBounds(area);
    }

    boolean keyScroll = false;
    float currentScale = 1; 
    
    /**
     * {@inheritDoc}
     */
	public void keyPressedOnce(KeyEvent e) {
		if ((!viewedCanvas.getInteracting()) && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)) {
			keyScroll = true;
			viewedCanvas.setInteracting(true);
			smoothPanning = false;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public void keyRepeated(KeyEvent e) {
		// scrolling with arrow keys
		if (keyScroll) {
        	PBounds b = viewedCanvas.getCamera().getViewBounds();
        	Point2D p = new Point2D.Double(b.getCenterX(), b.getCenterY());
        	Point2D p2;
        	if (e.getKeyCode() == KeyEvent.VK_LEFT) {
        		p2 = hull.getPreviousPoint(p);
        	} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
        		p2 = hull.getNextPoint(p);
        	} else {
        		return;
        	}
        	
        	moveCameraOnPath(p2, false); 
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void keyPressed(KeyEvent event) {
	}

	/**
     * {@inheritDoc}
     */
    public void keyReleased(KeyEvent event) {
		if (keyScroll) {
			viewedCanvas.setInteracting(false);
			keyScroll = false;
			smoothPanning = true;
		}
	}

    /**
     * {@inheritDoc}
     */
    public void keyTyped(KeyEvent arg0) {
	}

} // class BirdsEyeView
