/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.algorithms;

import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Class GenerationRank
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class GenerationRank extends AbstractAlgorithm {
    private static final Logger LOG = Logger.getLogger(GenerationRank.class);
    private Set<Vertex> treeNode = new HashSet<Vertex>();
    private Set<Edge> treeEdge = new HashSet<Edge>();

    /**
     * Creates a GenerationRank.
     * @param network the network
     */
    public GenerationRank(Network network) {
        super(network);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        assignLayers();
    }
    
    private void assignLayers() {
        Set<Edge> cycles = network.getCycles();
        LOG.debug("Cyclic edges: "+cycles.size());
        try {
//            for (Edge e : network.getEdges())
//                e.setInverted(cycles.contains(e));
            for (Edge e : cycles) {
                network.removeEdge(e);
                network.addEdge(e, e.getToVertex(), e.getFromVertex());
            }
            
            network.resetLayers();
            for (Set<Vertex> comp : network.getComponents()) {
                 assignLayers(comp);
            }
            int min = network.getMinLayer();
            if (min != 0)
                network.offsetLayer(-min, network.getVertices());
            treeNode.clear();
            treeEdge.clear();
        }
        finally {
            for (Edge e : cycles) {
                network.removeEdge(e);
                network.addEdge(e, e.getFromVertex(), e.getToVertex());
            }
        }
    }
    
    private void initRank(Set<Vertex> comp) {
        LinkedList<Vertex> queue = new LinkedList<Vertex>();
        HashSet<Vertex> processed = new HashSet<Vertex>();
        int ctr = 0;
        
        for (Vertex v : comp) {
            if (network.isOrphan(v)) {
                queue.add(v);
            }
        }
        
        while (! queue.isEmpty()) {
            Vertex v = queue.removeFirst();
            processed.add(v);
            ctr++;
            int layer = (v instanceof Fam) ? 1 : 0;
            for (Vertex p : network.getAscendants(v)) {
                layer = Math.max(layer, p.getLayer()+1);
            }
            v.setLayer(layer);
            for (Vertex d : network.getDescendants(v)) {
                if (processed.containsAll(network.getAscendants(d))) {
                    queue.addLast(d);
                }
            }
        }
        assert(ctr == comp.size());
    }
    
    private void assignLayers(Set<Vertex> comp) {
        initRank(comp);
        feasibleTree(comp);
        int min = comp.size();
        for (Vertex v : comp) {
            min = Math.min(min, v.getLayer());
        }
        if ((min % 2) == 1) // family is always odd
            min--;
        network.offsetLayer(-min, comp);
    }
    
    private void feasibleTree(Set<Vertex> comp) {
        if (comp.size() <= 1)
            return;
        while (tightTree(comp) < comp.size()) {
            Edge e = null;
            for (Vertex v : comp) {
                for (Edge f : network.getInEdges(v)) {
                    if (! treeEdge.contains(f)
                            && incident(f)!=null
                            && ((e == null)
                                    || (slack(f) < slack(e)))) {
                        e = f;
                    }
                }
            }
            if (e != null) {
                int delta = slack(e);
                if (delta != 0) {
                    if (incident(e)==network.getDescendant(e)) 
                        delta = -delta;//CHECK
                    network.offsetLayer(delta, treeNode);
                }
                else
                    LOG.error("Unexpected tight node");
            }
        }
    }
    
    private Vertex incident(Edge e) {
        Vertex source = network.getSource(e);
        Vertex dest = network.getDest(e);
        if (treeNode.contains(source)) {
            if (!treeNode.contains(dest))
                return source;
        }
        else if (treeNode.contains(dest)) {
            return dest;
        }
        return null;
    }
    
    private int slack(Edge e) {
        return network.getSource(e).getLayer()-network.getDest(e).getLayer()-1;
    }
    
    private int tightTree(Set<Vertex> comp) {
        treeNode.clear();
        treeEdge.clear();
        for (Vertex v : comp) {
            treeSearch(v, comp.size());
            if (! treeEdge.isEmpty())
                break;
        }
        
        return treeNode.size();
    }
    
    private boolean treeSearch(Vertex v, int n) {
        for (Edge e : network.getOutEdges(v)) {
            Vertex head = network.getDest(e);
            if (!treeNode.contains(head) && slack(e)==0) { 
                addTreeEdge(e);
                if (treeEdge.size()==n-1 || treeSearch(head, n)) {
                    return true;
                }
            }
        }
        for (Edge e : network.getInEdges(v)) {
            Vertex tail = network.getSource(e);
            if (!treeNode.contains(tail) && slack(e)==0) { 
                addTreeEdge(e);
                if (treeEdge.size()==n-1 || treeSearch(tail, n))
                    return true;
            }
        }
        return false;
    }
        
        private void addTreeEdge(Edge e) {
            assert(! treeEdge.contains(e));
            
            treeEdge.add(e);
            treeNode.add(network.getSource(e));
            treeNode.add(network.getDest(e));
        }
    
//    private int depth(Vertex v) {
//        int max = 0;
//        for (Vertex d : network.getDescendants(v)) {
//            max = Math.max(max, depth(d));
//        }
//        return max + 1;
//    }
//
//    private int assignLayer(Vertex first) {
//        int min = network.getVertexCount();
//        first.setLayer(min);
//        LinkedList<Vertex> queue = new LinkedList<Vertex>();
//        queue.addLast(first);
//        
//        while (! queue.isEmpty()) {
//            Vertex v = queue.removeFirst();
//            int l = v.getLayer();
//            for (Vertex d : network.getDescendants(v)) {
//                if (d.getLayer()!=-1) {// already assigned
//                    if (d.getLayer() > l)
//                        continue;
//                    else
//                        LOG.debug("Fixing up layer of "+d
//                                +" from "+d.getLayer()+" to "+(l+1));
//                }
//                d.setLayer(l+1);
//                LOG.debug("Vertex "+d+"="+Integer.toString(l+1));
//                queue.addLast(d);
//            }
//        }
//        return min;
//    }
}
