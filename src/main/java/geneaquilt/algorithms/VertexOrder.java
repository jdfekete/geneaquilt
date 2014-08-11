/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.algorithms;

import geneaquilt.data.DateRange;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Class VertexOrder
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class VertexOrder extends AbstractAlgorithm {
    private Vertex[][] layers;
//    private HashMap<Vertex,Integer> bestOrder;
//    private int bestCrossings;
    /**
     * Creates the vertex orderer
     * @param network the network
     */
    public VertexOrder(Network network) {
        super(network);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        init();
//        bestOrder = computeOrder();
        // TODO Auto-generated method stub
    }

    private void init() {
        ArrayList<Vertex>[] lyrs = new ArrayList[network.getMaxLayer()+1];
        for (int l = 0; l < lyrs.length; l++) {
            lyrs[l] = new ArrayList<Vertex>();
        }
        for (Vertex v : network.getVertices()) {
            int l = v.getLayer();
            lyrs[l].add(v);
        }
        layers = new Vertex[network.getMaxLayer()+1][];
        for (int l = 0; l < lyrs.length; l++) {
            Vertex[] layer = lyrs[l].toArray(null);
            layers[l] = layer;
        }
        sortLayer(layers[0], birthOrder);
        for (int l = 1; l < layers.length; l++) {
            
        }
    }
    
    private static class BirthOrder implements Comparator<Vertex> {
        public int compare(Vertex a, Vertex b) {
            if (a.getComponent() != b.getComponent()) {
                return a.getComponent() -  b.getComponent();
            }
            DateRange da = a.getDateRange();
            DateRange db = b.getDateRange();
            return da.compareTo(db);
        }
    }
    private static final BirthOrder birthOrder = new BirthOrder();
    
    private void sortLayer(Vertex[] layer, Comparator<Vertex> order) {
        Arrays.sort(layer, order);
        setOrder(layer);
    }
    
    private void setOrder(Vertex[] layer) {
        for (int i = 0; i < layer.length; i++) {
            layer[i].setX(i);
        }
    }
//    
//    private int getOrder(Vertex v) {
//        return (int)v.getX();
//    }
//    
//    private HashMap<Vertex,Integer> computeOrder() {
//        HashMap<Vertex,Integer> order = new HashMap<Vertex, Integer>(network.getVertexCount());
//        for (int l = 0; l < layers.length; l++) {
//            Vertex[] layer = layers[l];
//            layers[l] = layer;
//            for (int i = 0; i < layer.length; i++) {
//                order.put(layer[i], new Integer(i));
//            }
//        }
//        return order;
//    }
    
//    private int minimizeCrossings(int startpass, int endpass) {
//        int currentCross = Integer.MAX_VALUE;
//        int bestCross = currentCross;
//        HashMap<Vertex,Integer> savedOrder = null;
//        
//        if (startpass > 1) {
//            bestCross = currentCross = computeCrossings();
//            savedOrder = computeOrder();
//        }
//        
//        for (int pass = startpass; pass <= endpass; pass++) {
//            
//        }
//        
//        return bestCross;
//    }
    
//    private int computeCrossings() {
//        int count = 0;
//        for (int l = network.getMinLayer(); l <= network.getMaxLayer(); l++) {
//            count += computeCrossings(l);
//        }
//        return count;
//    }
        
//    private int computeCrossings(int l) {
//        int cross = 0;
//        int max = 0;
//        Vertex[] layer = layers[l];
//        int[] count = new int[layer.length+1];
//        
//        for (int top = 0; top < layer.length; top++) {
//            Vertex v = layer[top];
//            if (max > 0) {
//                for (Vertex w : network.getDescendants(v)) {
//                    for (int k = getOrder(w)+1; k <= max; k++) {
//                        cross += count[k];
//                    }
//                }
//            }
//            for (Vertex w : network.getDescendants(v)) {
//                int inv = getOrder(w);
//                if (inv > max) max = inv;
//                count[inv]++;
//            }
//        }
//        
//        return cross;
//    }
}
