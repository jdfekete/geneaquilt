package geneaquilt.algorithms;

import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Class LayerRank2
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class LayerRank2 extends AbstractAlgorithm {
    private static final Logger LOG = Logger.getLogger(LayerRank2.class);
    Map<Vertex,Integer> levelPred;
    Map<Vertex,Integer> levelSucc;
    int depth;
    
    /**
     * Creates athe layer ranker.
     * @param network the network
     */
    public LayerRank2(Network network) {
        super(network);
        levelPred = new HashMap<Vertex,Integer>();
        levelSucc = new HashMap<Vertex,Integer>();        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        Map<Vertex,Integer> map = computeLayers();
        for (Entry<Vertex, Integer> e : map.entrySet()) {
            network.setVertexLayer(e.getKey(), e.getValue().intValue());
        }
    }
    
    /**
     * Computes and assign the layers.
     * @return the layer assignment
     */
    public Map<Vertex,Integer> computeLayers() {
        Set<Edge> cycles = network.getCycles();
        LOG.debug("Cyclic edges: "+cycles.size());
        try {
            for (Edge e : cycles)
                network.removeEdge(e);
            levelPred.clear();
            levelSucc.clear();
            for (Vertex v : network.getVertices()) {
                if (network.isOrphan(v)) {
                    setDepthPred(v);
                }
                if (network.isSterile(v)) {
                    setDepthSucc(v);
                }
            }
            depth = 0;
            Map<Vertex,Integer> layers = new HashMap<Vertex, Integer>();
            for (Vertex v : network.getVertices()) {
                if (v instanceof Indi) {
                    Indi indi = (Indi) v;
                    int k = getLevelPred(indi)+getLevelSucc(indi);
                    assert(k>=0);
                    layers.put(indi, new Integer(k));
                    if (k > depth)
                        depth = k;
                }
            }
            for (Vertex v : network.getVertices()) {
                if (v instanceof Indi) {
                    Indi indi = (Indi) v;
                    int k = (depth - layers.get(indi).intValue())/2 + getLevelPred(indi);
                    layers.put(indi, new Integer(k));
                }
            }
            for (Vertex v : network.getVertices()) {
                if (v instanceof Fam) {
                    Fam fam = (Fam) v;
                    int k = famLayer(fam, layers);
                    layers.put(fam, new Integer(k));
                }
            }
            return layers;
        }
        finally {
            for (Edge e : cycles)
                network.addEdge(e, e.getFromVertex(), e.getToVertex());
        }
    }
    
    private int famLayer(Fam fam, Map<Vertex,Integer> layers) {
        int k = depth+1;
        for (Vertex v : network.getPredecessors(fam)) {
            k = Math.min(k, layers.get(v).intValue()-1);
        }
        if (k == depth+1) {
            k = 0;
            for (Vertex v : network.getSuccessors(fam)) {
                k = Math.max(k, layers.get(v).intValue()+1);
            }
            
        }
        return k;
    }
    
//    /**
//     * Returns the layer associated with the specified vertex.
//     * @param v the vertex
//     * @return the layer
//     */
//    public int getLayer(Vertex v) {
//        if (layers == null)
//            computeLayers();
//        Integer i = layers.get(v);
//        if (i == null)
//            return -1;
//        return i.intValue();
//    }
    
    private int getLevelPred(Vertex v) {
        Integer l = levelPred.get(v);
        if (l == null) {
            return -1;
        }
        return l.intValue();
    }
    
    private void setLevelPred(Vertex v, int d) {
//        LOG.debug("setLevelPred("+v+","+d+")");
        levelPred.put(v, new Integer(d));
    }
    
    private void setDepthPred(Vertex v) {
        Stack<Indi> stack = new Stack<Indi>();
        Stack<Integer> dstack = new Stack<Integer>();
        if (v instanceof Fam) {
            Fam fam = (Fam) v;
            for (Vertex w : network.getPredecessors(fam)) {
                stack.push((Indi)w);
                dstack.push(2);
                dstack.push(0);
            }
        }
        else {
            stack.push((Indi)v);
            dstack.push(0);
            dstack.push(0);
        }
        while (! stack.isEmpty()) {
            Indi indi = stack.pop();
            int d = dstack.pop().intValue();
            int i = dstack.pop().intValue();
            if (getLevelPred(indi)>d-i)
                continue;
            setLevelPred(indi, d);
            for (Indi s : network.getSpouses(indi)) {
                stack.push(s);
                dstack.push(1);
                dstack.push(d);
            }

            for (Vertex f : network.getPredecessors(indi)) {
                for (Vertex w : network.getPredecessors(f)) {
                    stack.push((Indi)w);
                    dstack.push(0);
                    dstack.push(d+2);
                }
            }
        }
    }
    
    private int getLevelSucc(Vertex v) {
        Integer l = levelSucc.get(v);
        if (l == null) {
            return -1;
        }
        return l.intValue();
    }
    
    private void setLevelSucc(Vertex v, int d) {
//        LOG.debug("setLevelSucc("+v+","+d+")");
        levelSucc.put(v, new Integer(d));
    }

    private void setDepthSucc(Vertex v) {
        Stack<Indi> stack = new Stack<Indi>();
        Stack<Integer> dstack = new Stack<Integer>();
        if (v instanceof Fam) {
            Fam fam = (Fam) v;
            for (Vertex w : network.getSuccessors(fam)) {
                stack.push((Indi)w);
                dstack.push(0);
                dstack.push(2);
            }
        }
        else {
            stack.push((Indi)v);
            dstack.push(0);
            dstack.push(0);
        }
        while (! stack.isEmpty()) {
            Indi indi = stack.pop();
            int d = dstack.pop().intValue();
            int i = dstack.pop().intValue();
            if (getLevelSucc(indi)>d-i)
                continue;
            setLevelSucc(indi, d);
            for (Indi s : network.getSpouses(indi)) {
                stack.push(s);
                dstack.push(d);
                dstack.push(1);
            }

            for (Vertex f : network.getSuccessors(indi)) {
                for (Vertex w : network.getSuccessors(f)) {
                    stack.push((Indi)w);
                    dstack.push(0);
                    dstack.push(d+2);
                }
            }
        }
    }
}
