/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection;

import edu.uci.ics.jung.algorithms.util.MapBinaryHeap;
import edu.umd.cs.piccolo.PNode;
import geneaquilt.data.Edge;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PVertex;
import geneaquilt.nodes.QuiltManager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>DOIManager</b> manages the degree of interest
 * of nodes.
 * 
 * @author Jean-Daniel Fekete
 */
public class DOIManager {
    private SelectionManager selectionManager;
    private QuiltManager quiltManager;
    private Map<PNode,Double> distance = new HashMap<PNode, Double>();
    private MapBinaryHeap<PNode> heap; 
    private double highlightDistance = 0;
    private double maxDistance = 10;
    
    /**
     * Creates a DOIManager for a specified quiltManager and selectionManager.
     * @param quiltManager the QuiltManager
     * @param selectionManager the SelectionManager
     */
    public DOIManager(
            QuiltManager quiltManager) {
        this.quiltManager = quiltManager;
        this.selectionManager = quiltManager.getSelectionManager();
    }
    
    /**
     * @return the selectionManager
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
    
    /**
     * @return the quiltManager
     */
    public QuiltManager getQuiltManager() {
        return quiltManager;
    }
    
    /**
     * @return the network
     */
    public Network getNetwork() {
        return quiltManager.getNetwork();
    }
    
    private final Comparator<PNode> nodeComparator =
        new Comparator<PNode>() {
        public int compare(PNode o1, PNode o2) {
            Double d1 = distance.get(o1);
            Double d2 = distance.get(o2);
            return (int)Math.signum(d1.doubleValue() - d2.doubleValue());
        }
    };
    
    /**
     * Computes the DOI in the network.
     */
    public void computeDOI() {
        distance.clear();
        heap = new MapBinaryHeap<PNode>(nodeComparator);
//        Set<Set<Vertex>> components = new HashSet<Set<Vertex>>();
        
        for (Selection sel : selectionManager.getSelections()) {
            PNode n = sel.getSelectedObject();
            updateDistance(n, 0);
//            if (n instanceof PVertex) {
//                PVertex pv = (PVertex) n;
//                components.add(
//                        getNetwork().getComponentSet(pv.getVertex()));                
//            }
//            else {
//                PEdge pe = (PEdge)n;
//                Vertex v = getNetwork().getSource(pe.getEdge());
//                components.add(
//                        getNetwork().getComponentSet(v));
//            }
            
            for (PNode h : sel.getHighlightedObjects()) {
                updateDistance(h, highlightDistance);
            }
        }
        while (! heap.isEmpty()) {
            PNode n = heap.remove();
            double newD = nextDistFrom(n);
//            if (newD > maxDistance)
//                continue;
            if (n instanceof PVertex) {
                PVertex pv = (PVertex)n;
                Vertex v = pv.getVertex();
                for (Edge edge : getNetwork().getOutEdges(v)) {
                    updateDistance(edge.getNode(), newD);
                }
                for (Edge edge : getNetwork().getInEdges(v)) {
                    updateDistance(edge.getNode(), newD);
                }
            }
            else if (n instanceof PEdge) {
                PEdge pedge = (PEdge) n;
                Edge edge = pedge.getEdge();
                updateDistance(getNetwork().getSource(edge).getNode(), newD);
                updateDistance(getNetwork().getDest(edge).getNode(), newD); 
            }
            else {
                System.err.println("Unexpected node "+n);
            }
        }
        for (Vertex v : getNetwork().getVertices()) {
            Double d = distance.get(v.getNode());
            if (d == null) {
//                assert(!components.contains(getNetwork().getComponentSet(v)));
                v.setDOI(maxDistance);
            }
            else {
                v.setDOI(d.doubleValue());
            }
        }
    }
    
    private boolean updateDistance(PNode node, double newD) {
        Double d = distance.get(node);
        if (d == null) {
            distance.put(node, new Double(newD));
            heap.add(node);
            return true;
        }
        else if (d.doubleValue() > newD) {
            distance.put(node, new Double(newD));
            heap.update(node);
            return true;
        }
        return false;
    }
    
    protected double nextDistFrom(PNode n) {
        Double d = distance.get(n);
        return d.doubleValue()+1; // can be log
    }
}
