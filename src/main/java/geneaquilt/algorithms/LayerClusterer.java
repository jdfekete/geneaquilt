/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.map.HashedMap;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Tree;

/**
 * Class LayerClusterer
 * @param <V> vertex class
 * @param <E> edge class
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class LayerClusterer<V,E> 
    implements Transformer<DirectedGraph<V, E>, List<Set<V>>> {
    Map<V,Integer> rank;
    int maxRank;
    DirectedGraph<V, E> graph;

    /**
     * {@inheritDoc}
     */
    public List<Set<V>> transform(DirectedGraph<V, E> graph) {
        WeakComponentClusterer<V, E> clust = new WeakComponentClusterer<V, E>();
        Set<Set<V>> components = clust.transform(graph);
        ArrayList<Set<V>> res = new ArrayList<Set<V>>();        
        for (Set<V> comp : components) {
            List<Set<V>> r = rank(graph, comp);
            for (int i = 0; i < r.size(); i++) {
                Set<V> s = r.get(i);
                if (res.size() <= i) {
                    res.add(s);
                }
                else {
                    res.get(i).addAll(s);
                }
            }
        }
        
        return res;
    }
    
    /**
     * Assign ranks to a connected component
     * @param graph the graph
     * @param comp the component
     * @return an ordered list of vertices with the same rank
     */
    public List<Set<V>> rank(DirectedGraph<V, E> graph, Collection<V> comp) {
        this.graph = graph;
        this.rank = new HashedMap<V, Integer>();
        this.maxRank = 0;
        //Tree<V, E> tree = feasibleTree(comp);
        //TODO
        return null;
    }
    
    protected Tree<V,E> feasibleTree(Collection<V> comp) {
        return null;
    }

    protected void init_rank(Collection<V> comp) {
        for (V v : comp) {
            if (graph.getSuccessorCount(v)==0)
                setRoot(v);
        }
    }
    
    private void setRoot(V v) {
        rank.put(v, new Integer(0));
        assignRank(v, 0);
    }
    
    private void assignRank(V v, int vRank) {
        for (V child : graph.getPredecessors(v)) {
            Integer o = rank.get(child);
            int newRank;
            if (o != null) { 
                int oldRank = o.intValue();
                newRank = Math.max(oldRank, vRank+1);
            }
            else
                newRank = vRank+1;
            
            rank.put(child, new Integer(newRank));
            if (newRank > maxRank)
                maxRank = newRank;
            assignRank(child, newRank);
        }
    }
}
