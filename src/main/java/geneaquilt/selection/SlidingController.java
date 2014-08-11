/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.event.DisabablePanEventHandler;
import geneaquilt.nodes.PIndi;
import geneaquilt.nodes.PIsoShape;
import geneaquilt.nodes.PVertex;
import geneaquilt.nodes.QuiltManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

/**
 * <b>SlidingController</b> implements the Bring&Go + Link Sliding
 * navigation.
 * 
 * @author Jean-Daniel Fekete
 * TODO maybe show the spouses
 * TODO slide by zooming out the zooming in instead of panning
 */
public class SlidingController extends MouseSelectionController {
//    private static final Logger LOG = Logger.getLogger(SlidingController.class);
    private static int NODE_SLIDING_OFFSET = 50; // 5 pixels for sliding on node
    private static int SLIDING_DISTANCE = 50;
    private static int SLIDING_INNER = 40;
    private Network network;
    private PCamera camera;
    private PNode slider;
    private PNode sliderBackground = new PNode();
    private ArrayList<PVertex> neighbors = new ArrayList<PVertex>();
    private ArrayList<Line2D> lines = new ArrayList<Line2D>();
    private PIndi focusNode;
    private PIndi destinationNode;
    private Point2D focusOffset;
//    private double focusScale;
    private PBounds tmpBounds = new PBounds();
    private Arc2D.Double tmpArc = new Arc2D.Double();
    private PNode cursor;
    private static final int CURSOR_SIZE = 9;
    private int state;
    private static final int STATE_NONE = 0;
    private static final int STATE_IDLE = 3; // the difference with STATE_NONE is that sliding has been already triggered and we want to prevent the user from changing the selection
    private static final int STATE_SLIDING_LEFT = 1;
    private static final int STATE_SLIDING_RIGHT = 2;
    PTransformActivity currentCameraAnimation = null;
    private boolean smoothPanning = true; // change this to enable/disable smooth panning
    
    // Graphical attributes
    
    private static final Color textColor = new Color(0.2f, 0.2f, 0.8f, 1f);
    private static final Color cursorColor = new Color(0.3f, 0.3f, 0.9f, 1f);
    private static final Color pathColor = new Color(0.4f, 0.4f, 1f, 1f);
    private static final float pathStrokeWidth = 1;
    private static final Color bgColor = new Color(1f, 1f, 1f, 0.45f);
    private static final int bgRadius = 9;

    /**
     * Creates a sliding controller.
     * @param manager the QuiltManager
     * @param panEventHandler the underlying PanEventHandler
     */
    public SlidingController(QuiltManager manager, DisabablePanEventHandler panEventHandler) {
        super(manager, panEventHandler);
        this.network = manager.getNetwork();
        slider = new PNode();
        cursor = new PNode();
        cursor.setBounds(0, 0, CURSOR_SIZE, CURSOR_SIZE);
    }
    
    private void setState(PIndi pindi, int newState) {
        if (state == newState && pindi == focusNode) return;
        if (pindi == null) {
            assert(newState == STATE_NONE || newState == STATE_IDLE);
        }
        if (newState == STATE_NONE || newState == STATE_IDLE) {
	 		/*if (currentCameraAnimation != null && currentCameraAnimation.isStepping()) {
	 			currentCameraAnimation.terminate(PActivity.TERMINATE_AND_FINISH);
	 		}*/
	 		slider.removeAllChildren();
            sliderBackground.removeAllChildren();
            cursor.removeAllChildren();
            neighbors.clear();
            lines.clear();
        }
        else if (newState == STATE_SLIDING_LEFT) {
            if (state != STATE_IDLE) {
                setState(pindi, STATE_IDLE); // force to state
            }
            ArrayList<Indi> parents = network.getParents(pindi.getIndi());
            if (parents.isEmpty()) {
                setState(pindi, STATE_IDLE); // nothing to do
                return;
            }
            Collections.sort(parents, QuiltManager.COMPARATOR);

            PBounds b = getCameraBounds(pindi);
            PBounds fullb = pindi.getFullBoundsReference();
            Point2D cameraOrigin = camera.getViewBounds().getOrigin();
//            LOG.debug("Focus offset: "+focusOffset);
            focusOffset = new Point2D.Double(
                    cameraOrigin.getX()-fullb.getX(),
                    cameraOrigin.getY()-fullb.getCenterY());
//            focusScale = camera.getScale();
            
            PIndi me = new PIndi(pindi.getIndi());
            me.setBounds(b);
            slider.addChild(sliderBackground);
            addNodeWithBackground(slider, sliderBackground, me);

            int i = 1;
            tmpArc.setArcByCenter(
                    b.getX(), 
                    b.getCenterY(),
                    SLIDING_INNER,
                    90,
                    180,
                    Arc2D.OPEN);
            
            addPathWithBackground(slider, sliderBackground, tmpArc);
            double angle = Math.PI / (parents.size()+1);

            for (Indi p : parents) {
                PIndi pp = new PIndi(p);
//                PBounds pb = p.getNode().getFullBoundsReference();
//                if (camera.getViewBounds().contains(pb)) {
//                    pp.setBounds(getCameraBounds(p.getNode()));
//                }
//                else {
                    pp.setX(b.x-Math.sin(angle*i)*SLIDING_DISTANCE-pp.getWidth());
                    pp.setY(b.y - Math.cos(angle*i)*SLIDING_DISTANCE);
//                }
                pp.setPaint(new Color(1, 1, 1, 1));
                neighbors.add(pp);
                addNodeWithBackground(slider, sliderBackground, pp);
                Line2D.Double line = new Line2D.Double(
                        b.getX(), 
                        b.getCenterY(),
                        pp.getFullBoundsReference().getMaxX(), 
                        pp.getFullBoundsReference().getCenterY());
                lines.add(line);
                
                addPathWithBackground(slider, sliderBackground, line);
                
                i++;
            }
            
            PIsoShape cursorshape = new PIsoShape(PIsoShape.CIRCLE);
            cursorshape.setBounds(0, 0, CURSOR_SIZE,  CURSOR_SIZE);
            addNodeWithBackground(cursor, cursor, cursorshape);
            cursor.setTransform(AffineTransform.getTranslateInstance(b.getX()-CURSOR_SIZE/2, b.getCenterY()-CURSOR_SIZE/2));
            slider.addChild(cursor);
            
            camera.addChild(slider);
        }
        else if (newState == STATE_SLIDING_RIGHT) {
            if (state != STATE_IDLE) {
                setState(pindi, STATE_IDLE); // force to state
            }
            ArrayList<Vertex> fams = new ArrayList<Vertex>(
                    network.getDescendants(pindi.getIndi()));
            if (fams.isEmpty()) {
                setState(pindi, STATE_IDLE); // nothing to do
                return;
            }
            Collections.sort(fams, QuiltManager.COMPARATOR);
            
            ArrayList<Vertex> children = new ArrayList<Vertex>();
            for (Vertex v : fams) {
                children.addAll(network.getDescendants(v));
            }
            Collections.sort(children, QuiltManager.COMPARATOR);
            //TODO change the sliding radius if there are too many children
            PBounds b = getCameraBounds(pindi);
            PBounds fullb = pindi.getFullBoundsReference();
            Point2D cameraOrigin = camera.getViewBounds().getOrigin();
            focusOffset = new Point2D.Double(
                    cameraOrigin.getX()-fullb.getMaxX(),
                    cameraOrigin.getY()-fullb.getCenterY());
//            focusScale = camera.getScale();
                
            PIndi me = new PIndi(pindi.getIndi());
            me.setBounds(b);
            slider.addChild(sliderBackground);
            addNodeWithBackground(slider, sliderBackground, me);
            double childAngle = -180.0 / (children.size()+1); 
            double initialAngle = 90;
            int i = 1;
            for (Vertex f : fams) {
                double startAngle = initialAngle+i*childAngle-childAngle/4;
                i += network.getDescendantCount(f);
                double endAngle = initialAngle+(i-1)*childAngle+childAngle/4;
                if (endAngle < startAngle) {
                    tmpArc.setArcByCenter(
                            b.getMaxX(), 
                            b.getCenterY(),
                            SLIDING_INNER,
                            startAngle,
                            endAngle-startAngle,
                            Arc2D.OPEN);
                    
                    addPathWithBackground(slider, sliderBackground, tmpArc);
                }
            }
            double angle = Math.PI / (children.size()+1);
            double dist = 2*me.getHeight()/angle;
            dist = Math.max(dist, SLIDING_DISTANCE);
            
            i = 1;
            for (Vertex p : children) {
                PIndi pp = new PIndi((Indi)p);
//                PBounds pb = p.getNode().getFullBoundsReference();
//                if (camera.getViewBounds().contains(pb)) {
//                    pp.setBounds(getCameraBounds(p.getNode()));
//                }
//                else {
                    pp.setX(b.getMaxX()+Math.sin(angle*i)*dist);
                    pp.setY(b.y - Math.cos(angle*i)*dist);
//                }
                neighbors.add(pp);
                addNodeWithBackground(slider, sliderBackground, pp);
                Line2D.Double line = new Line2D.Double(
                        b.getMaxX(), 
                        b.getCenterY(),
                        pp.getFullBoundsReference().getMinX(), 
                        pp.getFullBoundsReference().getCenterY());
                lines.add(line);
                
                addPathWithBackground(slider, sliderBackground, line);
                i++;
            }
            
            PIsoShape cursorshape = new PIsoShape(PIsoShape.CIRCLE);
            cursorshape.setBounds(0, 0, CURSOR_SIZE,  CURSOR_SIZE);
            addNodeWithBackground(cursor, cursor, cursorshape);
            cursor.setTransform(AffineTransform.getTranslateInstance(b.getMaxX()-CURSOR_SIZE/2, b.getCenterY()-CURSOR_SIZE/2));
            slider.addChild(cursor);

            camera.addChild(slider);            
        }
        state = newState;
        focusNode = pindi;
    }
    
    private static double closestParamToSegment(Line2D l, Point2D p) {
        if (l == null || p == null) return 0; // defensive
        final double dx = l.getX2() - l.getX1();
        final double dy = l.getY2() - l.getY1();
    
        if ((dx == 0) && (dy == 0)) {
            throw new IllegalArgumentException("p1 and p2 cannot be the same point");
        }
    
        return ((p.getX() - l.getX1()) * dx 
                + (p.getY() - l.getY1()) * dy) 
                / (dx*dx + dy*dy);
    }

    
    private double slide(Point2D pos) {
        double minDistSq = Double.MAX_VALUE;
        Line2D closest = null;
        int closestIdx = -1;
        int i = 0;
        for (Line2D l : lines) {
            double d = l.ptSegDistSq(pos);
            if (d < minDistSq) {
                minDistSq = d;
                closest = l;
                closestIdx = i;
            }
            i++;
        }
        if (closest == null) {
            return 0;
        }
        double u = closestParamToSegment(closest, pos);
        if (u < 0)
            u = 0;
        else if (u > 1) {
            u = 1;
        }
        cursor.setTransform(AffineTransform.getTranslateInstance((1-u)*closest.getX1() + u*closest.getX2() - CURSOR_SIZE/2,
              (1-u)*closest.getY1() + u*closest.getY2() - CURSOR_SIZE/2));
        PBounds fnb = focusNode.getFullBoundsReference();
        Point2D p1;
        if (state == STATE_SLIDING_LEFT) {
            p1 = new Point2D.Double(fnb.getX(), fnb.getCenterY());
        }
        else {
            p1 = new Point2D.Double(fnb.getMaxX(), fnb.getCenterY());
        }

//        LOG.debug("Starting node: "+p1);
        destinationNode = (PIndi)neighbors.get(closestIdx).getVertex().getNode();
        PBounds p2b = destinationNode.getFullBoundsReference();
        PDimension d = new PDimension(closest.getP1(), closest.getP2());
        camera.getViewTransformReference().inverseTransform(d, d);
        
        Point2D p2;
        if (state == STATE_SLIDING_LEFT) {
            p2 = new Point2D.Double(
                p2b.getCenterX()-d.getWidth(), 
                p2b.getCenterY()-d.getHeight());
        }
        else {
            p2 = new Point2D.Double(
                    p2b.getCenterX()-d.getWidth(), 
                    p2b.getCenterY()-d.getHeight());
        }
        PBounds b = camera.getViewBounds();
        if (Math.abs(p2.getX()-p1.getX()) < b.getWidth() && Math.abs(p2.getY()-p1.getY()) < b.getHeight()) {
            b.x = u*p2.getX() + (1-u)*p1.getX() + focusOffset.getX();
            b.y = u*p2.getY() + (1-u)*p1.getY() + focusOffset.getY();
        }
        else {
            // zoom out first, then zoom in
            double phase = u*2;
            if (phase < 1) {
                // zoom out
            }
            else if (phase < 2) {
                // zoom in
            }
            b.x = u*p2.getX() + (1-u)*p1.getX() + focusOffset.getX();
            b.y = u*p2.getY() + (1-u)*p1.getY() + focusOffset.getY();
//            camera.setScale(focusScale);
        }
        setViewBounds(camera, b);
        return u;
    }
    
    private PBounds getCameraBounds(PNode node) {
        PBounds targetBounds = node.getFullBounds();
        camera.viewToLocal(targetBounds);
        camera.globalToLocal(targetBounds);
        return targetBounds;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void processEvent(PInputEvent event, int type) {
        this.camera = event.getCamera();
        super.processEvent(event, type);
    }
	
    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(PInputEvent event) {
        if (currentSelection != null) {
            PNode selNode = currentSelection.getSelectedObject();
            if (state == STATE_NONE || state == STATE_IDLE) {
                if (selNode instanceof PIndi) {
                    PIndi pindi = (PIndi)selNode;
                    if (inLeftZone(selNode, event)) {
                    	if (!isAnimatingPan())
                    		setState(pindi, STATE_SLIDING_LEFT);
                    }
                    else if (!isAnimatingPan() && inRightZone(selNode, event)) {
                    	if (!isAnimatingPan())
                    		setState(pindi, STATE_SLIDING_RIGHT);
                    }
                }
            }
            else if (state == STATE_SLIDING_LEFT) {
                PIndi pindi = (PIndi)selNode;
                if (! inLeftZone(pindi, event) 
                        && pindi.getFullBoundsReference().contains(event.getPosition())) {
                	if (!isAnimatingPan())
                		setState(pindi, STATE_IDLE);
                }
                else {
                    double u = slide(event.getCanvasPosition());
                    if (u == 1) {
                        setState(pindi, STATE_IDLE);
                        setSelectedObject(destinationNode);
                    }
                }
                
            }
            else if (state == STATE_SLIDING_RIGHT) {
                PIndi pindi = (PIndi)selNode;
                if (! inRightZone(pindi, event)
                        && pindi.getFullBoundsReference().contains(event.getPosition())) {
                	if (!isAnimatingPan())
                		setState(pindi, STATE_IDLE);
                }
                else {
                    double u = slide(event.getCanvasPosition());
                    if (u == 1) {
                        setState(pindi, STATE_IDLE);
                        setSelectedObject(destinationNode);
                    }
                }
            }
            else {
                PBounds b = selNode.getFullBoundsReference();
                if (selNode instanceof PIndi 
                        && b.contains(event.getPosition())) {
                    setState((PIndi)selNode, STATE_IDLE);
                }
            }
        }
          
        if (state == STATE_NONE)
        	super.mouseDragged(event);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(PInputEvent event) {
        setState(null, STATE_NONE);
        super.mouseReleased(event);
    }
    
    
    private boolean inLeftZone(PNode node, PInputEvent event) {
        PBounds b = node.getFullBoundsReference();
    /*   if (state == STATE_NONE) {
	        tmpBounds.setFrame(b);
	        tmpBounds.x -= NODE_SLIDING_OFFSET*4/5;
	        tmpBounds.width =  NODE_SLIDING_OFFSET;
	        return tmpBounds.contains(event.getPosition());
        } else {*/
        	return event.getPosition().getX() < b.getX();
       // }
    }
    
    private boolean inRightZone(PNode node, PInputEvent event) {
        PBounds b = node.getFullBoundsReference();
    /*	if (state == STATE_NONE) {
	        tmpBounds.setFrame(b);
	        tmpBounds.x += tmpBounds.width-NODE_SLIDING_OFFSET/5;
	        tmpBounds.width =  NODE_SLIDING_OFFSET;
	        return tmpBounds.contains(event.getPosition()); 
    	} else {*/
    		return event.getPosition().getX() > b.getMaxX();
    	//}
    }
   
    /**
     * Creates a ppath with a blurred white background and adds it to the node.
     */
    protected static void addPathWithBackground(PNode parent, PNode backgroundLayer, Shape shape) {
    	
    	for (float width = bgRadius + pathStrokeWidth; width > pathStrokeWidth; width -= 3) {
        	PPath bgpath = new PPath(shape);
        	bgpath.setPaint(null);
        	bgpath.setStroke(new BasicStroke(width));
        	bgpath.setStrokePaint(bgColor);
        	backgroundLayer.addChild(bgpath);
    	}
    	
    	PPath fgpath = new PPath(shape);
    	fgpath.setPaint(null);
    	fgpath.setStroke(new BasicStroke(pathStrokeWidth));
    	fgpath.setStrokePaint(pathColor);
    	parent.addChild(fgpath);
    }
    
    /**
     * Adds a blurred white background to a node.
     */
    protected static void addNodeWithBackground(PNode parent, PNode backgroundLayer, PNode child) {
    	
    	if (child instanceof PText)
    		((PText)child).setTextPaint(textColor);
    	else
    		child.setPaint(cursorColor);
    	PBounds bounds = child.getBoundsReference();
    	
    	for (float width = bgRadius; width >= -3; width -= 3) {
        	PPath bgrect = PPath.createRectangle((float)bounds.x - width / 2, (float)bounds.y - width /2, (float)bounds.width + width, (float)bounds.height + width);
        	bgrect.setStroke(null);
        	bgrect.setStrokePaint(null);
        	bgrect.setPaint(bgColor);
        	backgroundLayer.addChild(bgrect);
    	}
    	
    	parent.addChild(child);
    }
    
    /**
     * Changes the view bounds of a camera with a smooth animation.
     */
	protected void setViewBounds(PCamera camera, PBounds b) {
		if (!smoothPanning) {
			camera.setViewBounds(b);
		} else {
	 		// The following allows a camera animation to start while another animation
	 		// is already running.
	 		if (isAnimatingPan()) {
	 			currentCameraAnimation.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
	 		}
 			currentCameraAnimation = camera.animateViewToCenterBounds(b, true, 150);
 			currentCameraAnimation.setSlowInSlowOut(false);
			// Play one step right now, otherwise nothing will happen while dragging the mouse since animations
	 		// won't have time to play.
			currentCameraAnimation.setStartTime(System.currentTimeMillis() - currentCameraAnimation.getStepRate());
			currentCameraAnimation.processStep(System.currentTimeMillis());            		
		}
 	}
	
	protected boolean isAnimatingPan() {
		return currentCameraAnimation != null && currentCameraAnimation.isStepping();
	}

	protected void setSelectedObject(final PNode n) {
		
		if (currentSelection == null)
			return;
		
		if (!isAnimatingPan()) {
			currentSelection.setSelectedObject(n);
		} else {
			currentCameraAnimation.setDelegate(new PActivity.PActivityDelegate() {
				public void activityFinished(PActivity arg0) {
					currentSelection.setSelectedObject(n);
				}

				public void activityStarted(PActivity arg0) {
				}

				public void activityStepped(PActivity arg0) {
				}				
			});
		}
	}
}
