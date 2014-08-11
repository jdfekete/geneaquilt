/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import geneaquilt.algorithms.BFSCycleFinder;

/**
 * Class Network
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class Network extends DirectedSparseGraph<Vertex, Edge>{
    private static final Logger LOG = Logger.getLogger(Network.class);
    private List<Set<Vertex>> components;
    private Map<String,Vertex> index;
    private int maxLayer= -1;
    private int minLayer= -1;
    private boolean minMaxUpdated = false;
    private Set<Object> selection;
    private Set<Edge> cycles;

    /**
     * Creates a newtork.
     */
    public Network() {
        index = new HashMap<String, Vertex>();
        selection = new HashSet<Object>();
    }
    
    /**
     * Returns the vertex with the specified id.
     * @param id the id
     * @return a vertex or null
     */
    public Vertex getVertex(String id) {
        return index.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(Vertex vertex) {
        index.put(vertex.getId(), vertex);
        return super.addVertex(vertex);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(Vertex vertex) {
        index.remove(vertex.getId());
        return super.removeVertex(vertex);
    }
    
    /**
     * @return the connected components of this graph
     */
    public List<Set<Vertex>> getComponents() {
        if (components == null) {
            LOG.debug("Computing connected components");
            WeakComponentClusterer<Vertex, Edge> clusterer 
                = new WeakComponentClusterer<Vertex, Edge>();
            Set<Set<Vertex>> ret = clusterer.transform(this);
            components = new ArrayList<Set<Vertex>>(ret);
            LOG.debug("Sorting connected components");
            Collections.sort(
                    components,
                    new Comparator<Set<Vertex>>() {
                        public int compare(Set<Vertex> s1, Set<Vertex> s2) {
                            return s2.size()-s1.size();
                        }
                    });          
            int i = 0;
            for (Set<Vertex> comp : components) {
                LOG.debug("ComponentSize["+i+"]="+comp.size());
                for (Vertex v : comp) {
                    v.setComponent(i);
                }
                i++;
            }
        }
        return components;
    }
    
    /**
     * @return Returns the set of edges causing cycles
     */
    public Set<Edge> getCycles() {
        if (cycles == null) {
            BFSCycleFinder<Vertex, Edge> cycleFinder 
                = new BFSCycleFinder<Vertex, Edge>(this);
            cycles = new HashSet<Edge>();
            for (Set<Vertex> comp : getComponents()) {
                cycles.addAll(cycleFinder.findCycles(comp));
            }
        }
        return cycles;
    }
    
    /**
     * @return the number of components
     */
    public int getComponentCount() {
        return getComponents().size();
    }

    /**
     * Returns the component set containing the specified vertex.
     * @param v the vertex
     * @return the set or null
     */
    public Set<Vertex> getComponentSet(Vertex v) {
        for (Set<Vertex> s : getComponents()) {
            if (s.contains(v)) {
                return s;
            }
        }
        return null;
    }

    /**
     * @return the layerComputed
     */
    public boolean isLayerComputed() {
       updateMinMax();
        return maxLayer >= 0;
    }
    
    /**
     * Returns the layer of the specified vertex
     * @param v the vertex
     * @return the vertex's layer
     */
    public int getVertexLayer(Vertex v) {
        return v.getLayer();
    }
    
    /**
     * Sets the layer of the specified vertex.
     * @param v the vertex
     * @param layer the layer
     */
    public void setVertexLayer(Vertex v, int layer) {
        v.setLayer(layer);
        if (minMaxUpdated) {
            maxLayer = Math.max(layer, maxLayer);
            minLayer = Math.min(layer, minLayer);
        }
    }

    /**
     * Reset the layers to -1
     */
    public void resetLayers() {
        for (Vertex v : getVertices()) {
            v.setLayer(-1);
        }
        maxLayer = -1;
        minLayer = -1;
        minMaxUpdated = false;
    }
    
    /**
     * @return the maxLayer
     */
    public int getMaxLayer() {
        updateMinMax();
        return maxLayer;
    }
    
    /**
     * @return the minLayer
     */
    public int getMinLayer() {
        updateMinMax();
        return minLayer;
    }
    
    /**
     * Offsets the component by the specified offset 
     * @param dv the offset
     * @param comp the component
     */
    public void offsetLayer(int dv, Collection<Vertex> comp) {
        if (dv == 0) return;
        for (Vertex v : comp) {
            int l = v.getLayer()+dv;
            v.setLayer(l);
        }
    }
    
    /**
     * Recompute the min and max layer values, forcing if
     * necessary.
     * @param force true to force the recomputation
     */
    public void updateMinMax(boolean force) {
        minMaxUpdated &= !force;
        updateMinMax();
    }

    private void updateMinMax() {
        if (minMaxUpdated)
            return;
        int min = Integer.MAX_VALUE;
        int max = -1;
        for (Vertex v : getVertices()) {
            int l = v.getLayer();
            min = Math.min(min, l);
            max = Math.max(max, l);
        }
        minLayer = min;
        maxLayer = max;
        minMaxUpdated = true;
    }
    
    /**
     * Selects/unselects the specified edge
     * @param e the edge
     * @param sel selected or not
     */
    public void setSelected(Edge e, boolean sel) {
//        e.setSelected(sel);
        if (sel) {
            selection.add(e);
        }
        else {
            selection.remove(e);
        }
    }
    
    /**
     * Check if a vertex is orphan (no successor)
     * @param v the vertex
     * @return true/false
     */
    public boolean isOrphan(Vertex v) {
        return getAscendantCount(v)==0;
    }
    
    /**
     * Returns the ascendant of edge (dest)
     * @param e the edge
     * @return the ascendant
     */
    public Vertex getAscendant(Edge e) {
        return getDest(e);
    }
    
    /**
     * Returns the descendant of edge (source)
     * @param e the edge
     * @return the descendant
     */
    public Vertex getDescendant(Edge e) {
        return getSource(e);
    }
    
    /**
     * Check if a vertex is sterile (no predecessor)
     * @param v the vertex
     * @return true/false
     */
    public boolean isSterile(Vertex v) {
        return getDescendantCount(v)==0;
    }
    
    /**
     * Returns the collection of spouses of this individual
     * @param indi the individual
     * @return the spouses
     */
    public Collection<Indi> getSpouses(Indi indi) {
        int n = getDescendantCount(indi);
        if (n == 0)
            return Collections.EMPTY_LIST;
        ArrayList<Indi> spouses = new ArrayList<Indi>(n);
        for (Vertex f : getDescendants(indi)) {
            assert(f instanceof Fam);
            for (Vertex s : getAscendants(f)) {
                if (s != indi) {
                    spouses.add((Indi)s);
                }
            }
        }
        return spouses;
    }
    
    /**
     * Returns the parents of a specified individual.
     * @param indi the individual
     * @return a collection of up to two parents
     */
    public ArrayList<Indi> getParents(Indi indi) {
        ArrayList<Indi> parents = new ArrayList<Indi>(2);
        for (Vertex f : getAscendants(indi)) {
            assert(f instanceof Fam);
            for (Vertex p : getAscendants(f)) {
                parents.add((Indi)p);
            }
        }
        return parents;
    }

    /**
     * Returns the Ascendants of the specified vertex.
     * @param v the vertex
     * @return the ascendants
     */
    public Collection<Vertex> getAscendants(Vertex v) {
        return getSuccessors(v);
    }
    
    /**
     * Returns the number of Ascendants of the specified vertex.
     * @param v the vertex
     * @return the number of ascendants
     */
    public int getAscendantCount(Vertex v) {
        return getSuccessorCount(v);
    }
    
    /**
     * Returns the Descendants of the specified vertex.
     * @param v the vertex
     * @return the descendants
     */
    public Collection<Vertex> getDescendants(Vertex v) {
        return getPredecessors(v);
    }
    
    /**
     * Returns the number of Descendants of the specified vertex.
     * @param v the vertex
     * @return the number of descendants
     */
    public int getDescendantCount(Vertex v) {
        return getPredecessorCount(v);
    }

}
