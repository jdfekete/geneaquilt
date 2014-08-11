package geneaquilt.selection;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.data.Edge;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.GraphicsConstants;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.nodes.PVertex;
import geneaquilt.nodes.QuiltManager;
import geneaquilt.selection.highlight.HighlightManager;
import geneaquilt.selection.highlight.SelectionCombination;
import geneaquilt.selection.highlight.SelectionHighlight;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * <b>SelectionManager</b> manages selections.
 * 
 * @author Pierre Dragicevic
 */
public class SelectionManager {

	private QuiltManager quiltManager;
	private HighlightManager highlightManager;
    private ArrayList<Selection> selections;
    private int nextSelectionColorIndex = 0;
    private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
    private int inhibitNotify = 0;
    JMenuItem exportSelectionItem = null; // to disable and unable selection export

    /**
     * Creates a selection manager managing a quilt manager.
     * @param quiltManager the quilt manager
     * @param backgroundLayer the background layer node
     * @param foregroundLayer the foreground layer node
     */
    public SelectionManager(
            QuiltManager quiltManager, 
            PNode selectionLayer) {
    	this.quiltManager = quiltManager;
    	this.highlightManager = new HighlightManager(quiltManager.getFullBoundsReference());
    	selectionLayer.addChild(highlightManager);
    	selections = new ArrayList<Selection>();
    }

    /**
     * @return the network
     */
    public Network getNetwork() {
    	return quiltManager.getNetwork();
    }
    
    /**
     * @return the selected network
     */
    public Network getSelectedNetwork() {
    	
    	// Built the list of selected vertices and edges
    	Set<Vertex> vertices = new HashSet<Vertex>();
    	Set<Edge> edges = new HashSet<Edge>();
    	for (Selection s : selections) {
    		Set<PNode> nodes = s.getHighlightedObjects();
    		for (PNode n : nodes)
    			if (n instanceof PVertex)
    				vertices.add(((PVertex)n).getVertex());
    			else if (n instanceof PEdge)
					edges.add(((PEdge)n).getEdge());
    	}
    	
    	// Build the network
    	Network selNetwork = new Network();
    	for (Vertex v : vertices)
        	selNetwork.addVertex(v);
    	for (Edge e : edges)
        	selNetwork.addEdge(e, e.getFromVertex(), e.getToVertex());
    		
    	return selNetwork;
    }
    
    
    /**
     * @return the foreground layer
     */
    public HighlightManager getHighlightManager() {
    	return highlightManager;
    }
    
    /**
     * Removes all selections
     */
    public void clearSelections() {
    	highlightManager.clear();
		SelectionCombination.clear();
    	selections.clear();
    	nextSelectionColorIndex = 0;
    	fireChangeListeners();
        highlightManager.setTestOverlap(false);
    }
  
    /**
     * @return if the selection is empty
     */
    public boolean isEmpty() {
        return selections.isEmpty();
    }
    
    /**
     * Returns true if the specified node is selectable
     * @param node the node
     * @return true if it can be selected
     */
    public boolean isSelectable(PNode node) {
    	if (node == null)
    		return false;
//    	if (isSelected(node))
//    		return false;
    	return node instanceof PEdge 
    	    || node instanceof PFam 
    	    || node instanceof PIndi;
    }
    
    /**
     * Selects the specified node.
     * @param node the node
     * @return the new selection object or null
     */
    public Selection select(PNode node) {
        if (!isSelectable(node))
        	return null;
        highlightManager.setTestOverlap(selections.size() > 0);
        Selection newSelection = new Selection(this, node, getNextSelectionColor());
        selections.add(newSelection);
        nextSelectionColorIndex++;
        fireChangeListeners();
        return newSelection;
    }

    /**
     * Returns true if the specified node is selected.
     * @param node the node
     * @return true/false
     */
    public boolean isSelected(PNode node) {
        for (Selection s : selections)
        	if (s.getSelectedObject() == node)
        		return true;
        return false;
    }
    
    /**
     * Returns the selection associated with the node
     * @param node the node
     * @return the selection or null
     */
    public Selection getLastSelection(PNode node) {
//        for (Selection s : selections)
//        	if (s.getSelectedObject() == node)
//        		return s;
    	SelectionHighlight h = highlightManager.getSelectionHighlight(node);
    	if (h == null)
    		return null;
    	return h.getLastSelection();
    }

    /**
     * Sets the selection color index for the next selection
     * @param index the new index
     */
    public void setNextSelectionColorIndex(int index) {
    	nextSelectionColorIndex = index;
    }
    
    /**
     * @return the selection color index for the next selection
     */
    public int getNextSelectionColorIndex() {
    	return nextSelectionColorIndex;
    }
    
    /**
     * @return the selection color for the next selection
     */
    public Color getNextSelectionColor() {
    	return GraphicsConstants.SELECTION_COLORS[nextSelectionColorIndex % GraphicsConstants.SELECTION_COLORS.length];
    }

    /**
     * @return the selections
     */
    public Collection<Selection> getSelections() {
        return selections;
    }
    
    /**
     * Fires a change
     */
    public void fireChangeListeners() {
        if (inhibitNotify != 0)
            return;
        if (changeListeners.isEmpty())
            return;
        ChangeEvent ev = new ChangeEvent(this);
        for (ChangeListener l : changeListeners) {
            l.stateChanged(ev);
        }
        if (exportSelectionItem != null)
        	exportSelectionItem.setEnabled(selections.size() > 0);
    }
    
    /**
     * 
     * Register a new change listener
     * @param l the listener
     */
    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }
    
    /**
     * Removes the specified change listener
     * @param l the listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }
    
    /**
     * Removes all the listeners.
     */
    public void removeAllChangerListeners() {
        changeListeners.clear();
    }
    
	public void setExportSelectionItem(JMenuItem exportSelectionItem) {
		this.exportSelectionItem = exportSelectionItem;
	}
}
