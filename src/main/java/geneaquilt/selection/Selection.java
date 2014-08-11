/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.data.Edge;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.selection.highlight.HighlightManager;
import geneaquilt.utils.GUIUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * A node representing a selection.
 * 
 * @author dragice
 *
 */
public class Selection {
	
    private final SelectionManager selectionManager;
    private final HighlightManager highlightManager;
    private Network network;
    private final Color selectionColor_light;
    private final Color selectionColor_strong;
    private final Color selectionColor_opaque;
    private PNode selectedObject;
    
    private final ArrayList<PNode> highlighted_successors_tmp = new ArrayList<PNode>(); // for optimization
    private final ArrayList<PNode> highlighted_predecessors_tmp = new ArrayList<PNode>(); // for optimization
    
    /** The Highlight mode. */
    public enum HighlightMode {
        /** predecessors and successors are highlighted */
    	HIGHLIGHT_ALL, 
    	/** none of the predecessors and successors are highlighted */
    	HIGHLIGHT_NONE, 
    	/** only the successors are highlighted */
    	HIGHLIGHT_SUCCESSORS,
    	/** only the predecessors are highlighted */
    	HIGHLIGHT_PREDECESSORS
    }
    private HighlightMode mode;

    /**
     * Creates a selection with a manager, a selected object and a color.
     * @param selectionManager the manager
     * @param selectedObject the object
     * @param selectionColor the color
     */
    public Selection(SelectionManager selectionManager, PNode selectedObject, Color selectionColor) {
    	this.selectionManager = selectionManager;
    	this.network = selectionManager.getNetwork();
    	this.selectionColor_light = GUIUtils.multiplyAlpha(selectionColor, 0.3f);
    	this.selectionColor_strong = GUIUtils.multiplyAlpha(selectionColor, 0.5f);
    	this.selectionColor_opaque = selectionColor;
    	this.selectedObject = selectedObject;
    	this.highlightManager = selectionManager.getHighlightManager();
    	setHighlightMode(HighlightMode.HIGHLIGHT_ALL);
    }
    
    /**
     * @return the selected object
     */
    public PNode getSelectedObject() {
    	return selectedObject;
    }
    
    /**
     * Sets the selected object
     * @param selectedObject the next selected object
     */
    public void setSelectedObject(PNode selectedObject) {
    	this.selectedObject = selectedObject;
    	updateHighlights();
    	selectionManager.fireChangeListeners();
    }

    /**
     * Sets the highlight mode
     * @param mode the new mode
     */
    public void setHighlightMode(HighlightMode mode) {
    	this.mode = mode;
    	updateHighlights();
    }
    
    /**
     * @return the highlight mode
     */
    public HighlightMode getHighlightMode() {
    	return mode;
    }
    
    /**
     * Toggles the highlight mode
     */
    public void toggleHighlightMode() {
    	switch (mode) {
    		case HIGHLIGHT_ALL:
    			mode = HighlightMode.HIGHLIGHT_NONE;
    			break;
    		case HIGHLIGHT_NONE:
    			mode = HighlightMode.HIGHLIGHT_PREDECESSORS;
    			break;
    		case HIGHLIGHT_PREDECESSORS:
    			mode = HighlightMode.HIGHLIGHT_SUCCESSORS;
    			break;
    		case HIGHLIGHT_SUCCESSORS:
    			mode = HighlightMode.HIGHLIGHT_ALL;
    			break;
    	}
    	updateHighlights();
    }
    
    /**
     * Recomputes the highlights.
     */
    public void updateHighlights() {
   		highlightManager.repaint();
    	clearHighlights();
    	if (mode == HighlightMode.HIGHLIGHT_ALL || mode == HighlightMode.HIGHLIGHT_PREDECESSORS)
    		highlightPredecessors();
    	if (mode == HighlightMode.HIGHLIGHT_ALL || mode == HighlightMode.HIGHLIGHT_SUCCESSORS)
    		highlightSuccessors();
   		highlightSelection();
   		highlightManager.selectionModeChanged(this);
   		highlightManager.repaint();
    }
    
    /**
     * Highlights the selection.
     */
    protected void highlightSelection() {
    	
    	highlightManager.setSelectionHighlighted(selectedObject, this, true);
    	
     }

    /**
     * Highlights the ascendants/predecessors.
     */
    protected void highlightPredecessors() {
    	highlighted_predecessors_tmp.clear();
        if (selectedObject instanceof PEdge) {
            PEdge pedge = (PEdge) selectedObject;
           	highlightPredecessors(pedge.getEdge().getFromVertex());
        }
        else if (selectedObject instanceof PIndi) {
            PIndi pindi = (PIndi) selectedObject;
            highlightPredecessors(pindi.getIndi());
        }
        else if (selectedObject instanceof PFam) {
            PFam pfam = (PFam) selectedObject;
            highlightPredecessors(pfam.getFam());
        }
    	highlighted_predecessors_tmp.clear();
    }

    /**
     * Hightlight the descendants/successors
     */
    protected void highlightSuccessors() {
    	highlighted_successors_tmp.clear();
        if (selectedObject instanceof PEdge) {
            PEdge pedge = (PEdge) selectedObject;
           	highlightSuccessors(pedge.getEdge().getFromVertex());
        }
        else if (selectedObject instanceof PIndi) {
            PIndi pindi = (PIndi) selectedObject;
            highlightSuccessors(pindi.getIndi());
        }
        else if (selectedObject instanceof PFam) {
            PFam pfam = (PFam) selectedObject;
            highlightSuccessors(pfam.getFam());
        }
    	highlighted_successors_tmp.clear();
    }

    protected void highlightPredecessors(Vertex v) {
    	    	
        highlight(v.getNode());
        
        Collection<Edge> inEdges = network.getInEdges(v);
        
        // -- Avoid overlapping
        Edge highlightUnique = null;
        boolean showFrom = true, showTo = true;
        if (v.getNode() instanceof PFam) {
        	highlightUnique = getBottommostEdge(inEdges);
        	showTo = false;
        } else if (v.getNode() instanceof PIndi) {
        	highlightUnique = getRightmostEdge(inEdges);
        	showTo = false;
        }
        
        for (Edge e : inEdges) {
        	if (highlightUnique == null || e == highlightUnique)
        		highlight(e.getNode());
        	else
        		highlight(e.getNode(), showFrom, showTo);
        	if (!highlighted_predecessors_tmp.contains(network.getSource(e).getNode())) {
        		highlighted_predecessors_tmp.add(network.getSource(e).getNode());
                highlightPredecessors(network.getSource(e));
        	}
        }
    }
    
    protected void highlightSuccessors(Vertex v) {
    	
        highlight(v.getNode());
        
        Collection<Edge> outEdges = network.getOutEdges(v);
        
        // -- Avoid overlapping
        Edge highlightUnique = null;
        boolean showFrom = true, showTo = true;
        if (v.getNode() instanceof PFam) {
        	highlightUnique = getTopmostEdge(outEdges);
        	showFrom = false;
        } else if (v.getNode() instanceof PIndi) {
        	highlightUnique = getLeftmostEdge(outEdges);
        	showFrom = false;
        }

        for (Edge e : outEdges) {
        	if (highlightUnique == null || e == highlightUnique)
        		highlight(e.getNode());
        	else
        		highlight(e.getNode(), showFrom, showTo);
        	if (!highlighted_successors_tmp.contains(network.getDest(e).getNode())) {
        		highlighted_successors_tmp.add(network.getDest(e).getNode());
        		highlightSuccessors(network.getDest(e));
        	}
        }
    }
    
    protected void highlight(PNode n) {
    	highlight(n, true, true);
    }
    
  //revised: ancestor: out
    protected PNode findNextPredecessor(Vertex v) {
    	//highlightFound(v.getNode());
        Collection<Edge> outEdges = network.getOutEdges(v);

        PNode nextEdge=null;
        Edge highlightUnique = null;
        if (v.getNode() instanceof PFam) {
        	highlightUnique = getTopmostEdge(outEdges);
        	if(highlightUnique!=null)
        		nextEdge=highlightUnique.getNode();
        } else if (v.getNode() instanceof PIndi) {
        	highlightUnique = getLeftmostEdge(outEdges);
        	if(highlightUnique!=null)
        		nextEdge=highlightUnique.getNode();
        }

        //highlightFound(nextEdge);
        return nextEdge;
    }
    
    //added: descendant: in
    protected PNode findNextSuccessor(Vertex v) {
    	//highlightFound(v.getNode());
        Collection<Edge> inEdges = network.getInEdges(v);

        PNode nextEdge=null;
        Edge highlightUnique = null;
//        boolean showFrom = true, showTo = true;
        if (v.getNode() instanceof PFam) {
        	highlightUnique = getBottommostEdge(inEdges);
//        	showTo = false;
        	if(highlightUnique!=null)
        		nextEdge=highlightUnique.getNode();
        } else if (v.getNode() instanceof PIndi) {
        	highlightUnique = getRightmostEdge(inEdges);
//        	showTo = false;
        	if(highlightUnique!=null)
        		nextEdge=highlightUnique.getNode();
        }
        //highlightFound(nextEdge);
        return nextEdge;
    }
    
    protected void highlight(PNode n, boolean showFromConnector, boolean showToConnector) {

    	if (n instanceof PFam) {
	    	highlightManager.setPathHighlighted(n, this, true);
    	}

    	if (n instanceof PEdge && (showFromConnector || showToConnector)) {
    		PEdge e = (PEdge)n;
	        PNode from = e.getEdge().getFromVertex().getNode();
	        PNode to = e.getEdge().getToVertex().getNode();
	        if (showFromConnector) {
		    	highlightManager.setPathHighlighted(from, n, this, true);
	        }
	        if (showToConnector) {
		    	highlightManager.setPathHighlighted(n, to, this, true);
	        }
    	}
    	
    	if (n instanceof PEdge) {
	    	highlightManager.setPathHighlighted(n, this, true);
    	}
    }
    
    protected void clearHighlights() {
    	highlightManager.clearHighlights(this);
    }
    
    /**
     * @return the highlighted objects
     */
	public Set<PNode> getHighlightedObjects() {
		return highlightManager.getHighlightedObjects(this);
	}
    
    private static Edge getTopmostEdge(Collection<Edge> edges) {
    	float y, miny = Float.MAX_VALUE;
    	Edge result = null;
    	for (Edge e : edges) {
    		y = (float)e.getNode().getFullBoundsReference().getY();
    		if (y < miny) {
    			miny = y;
    			result = e;
    		}
    	}
    	return result;
    }
    
    private static Edge getBottommostEdge(Collection<Edge> edges) {
    	float y, maxy = Float.MIN_VALUE;
    	Edge result = null;
    	for (Edge e : edges) {
    		y = (float)e.getNode().getFullBoundsReference().getY();
    		if (y > maxy) {
    			maxy = y;
    			result = e;
    		}
    	}
    	return result;
    }
    
    private static Edge getLeftmostEdge(Collection<Edge> edges) {
    	float x, minx = Float.MAX_VALUE;
    	Edge result = null;
    	for (Edge e : edges) {
    		x = (float)e.getNode().getFullBoundsReference().getX();
    		if (x < minx) {
    			minx = x;
    			result = e;
    		}
    	}
    	return result;
    }
     
    private static Edge getRightmostEdge(Collection<Edge> edges) {
    	float x, maxx = Float.MIN_VALUE;
    	Edge result = null;
    	for (Edge e : edges) {
    		x = (float)e.getNode().getFullBoundsReference().getX();
    		if (x > maxx) {
    			maxx = x;
    			result = e;
    		}
    	}
    	return result;
    }
    
    /**
     * @return Returns the selection color
     */
    public Color getOpaqueColor() {
        return selectionColor_opaque;
    }
    
    /**
     * @return the light color
     */
    public Color getLightColor() {
    	return selectionColor_light;
    }
    
    /**
     * @return the strong color
     */
    public Color getStrongColor() {
    	return selectionColor_strong;
    }

}
