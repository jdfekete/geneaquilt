package geneaquilt.algorithms;

import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Class LayerRank<Vertex,Edge>
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class LayerRank extends AbstractAlgorithm {
    /** Name of the algorithm */
    public static final String LAYER = "layer";
    
    private Map<Vertex,Integer> rank = new HashMap<Vertex,Integer>();
    private Set<Vertex> unprocessed = new HashSet<Vertex>();
    private int maxRank = -1;
//    private Set<Vertex> movedup = new HashSet<Vertex>();

    /**
     * Constructor which initializes the algorithm
     * @param g the graph whose nodes are to be analyzed
     */
    public LayerRank(Network g) {
        super(g);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void compute() {
        computeLayers();
    }
    
    private void computeLayers() {
        Collection<Vertex> vertices = network.getVertices();
        rank.clear();
        unprocessed.clear();
        unprocessed.addAll(vertices);
        int maxLayers = 0;
        //Vertex longestRoot = null;
        
        for (Vertex v : vertices) {
            if (network.getSuccessorCount(v)==0) {
                int layers = setRoot(v);
                if (layers > maxLayers) {
                    maxLayers = layers;
                    //longestRoot = v;
                }
            }
        }
        if (! unprocessed.isEmpty()) {
            // loops
            System.err.println("Loops found in graph");
            Vertex best = null;
            int lowest = maxRank+1;
            for (Vertex v : unprocessed) {
                // Look for the lowest ranked neighbor to cut
                for (Vertex other : network.getPredecessors(v)) {
                    Integer i = rank.get(other);
                    if (i != null && i.intValue() < lowest) {
                        best = other;
                        lowest = i.intValue();
                    }
                }
                // cut at V
                if (best != null)
                    rank.put(v, new Integer(lowest-1));
                else
                    rank.put(v, new Integer(0));
            }
        }
        for (Vertex v : vertices) {
            if (network.getPredecessorCount(v)==0)
                setTop(v);
        }
        //bumpUp(vertices);
    }
    
    private int setRoot(Vertex v) {
        int r = (v instanceof Indi) ? 0 : 1;

        rank.put(v, new Integer(r));
        unprocessed.remove(v);
        System.out.println("Root: "+v);
        return assignRank(v, r);
    }
    
    private int assignRank(Vertex v, int level) {
        int maxRank = level;
        for (Vertex child : network.getPredecessors(v)) {
            Integer o = rank.get(child);
            int newRank;
            if (o != null) { 
                int oldRank = o.intValue();
                newRank = Math.max(oldRank, level+1);
            }
            else
                newRank = level+1;
            rank.put(child, new Integer(newRank));
            unprocessed.remove(child);
            if (newRank > maxRank)
                maxRank = newRank;
            assignRank(child, newRank);
        }
        if (maxRank > this.maxRank)
            this.maxRank = maxRank;
        return maxRank;
    }
    
//    private int maxPred(Vertex v) {
//        int m = -1;
//        for (Vertex child : graph.getPredecessors(v)) {
//            m = Math.max(m, rank.get(child).intValue());
//        }
//        return m;
//    }
    
    private int minPred(Vertex v) {
        int m = Integer.MAX_VALUE;
        for (Vertex child : network.getPredecessors(v)) {
            m = Math.min(m, rank.get(child).intValue());
        }
        if (m == Integer.MAX_VALUE)
            return -1;
        return m;
    }
    
//    private void bumpUp(Collection<Vertex> vertices) {
//        Vertex[] sorted = new Vertex[vertices.size()];
//        Comparator<Vertex> comparator = new Comparator<Vertex>() {
//            public int compare(Vertex o1, Vertex o2) {
//                return rank.get(o2).intValue()-rank.get(o1).intValue();
//            }
//        };
//
//        vertices.toArray(sorted);
//        //TreeSet<Vertex> heap = new TreeSet<Vertex>( 
//        //MapBinaryHeap<Vertex> heap = new MapBinaryHeap<Vertex>(
//        Arrays.sort(sorted, comparator);
//        for (int n = 0; n < sorted.length; n++) {
//            Vertex child = sorted[n];
//            int oldRank = rank.get(child).intValue();
//            int newRank = minPred(child)-1;
//            if (newRank > 0 && oldRank != newRank) {
//                if (newRank < oldRank)
//                    assert(false);
//                System.out.println("Moved "+child+" from "+oldRank+" to "+newRank);
//                rank.put(child, new Integer(newRank));
//                int index = Arrays.binarySearch(sorted, child, comparator);
//                if (index < 0) {
//                    index = -index-1;
//                }
//                System.arraycopy(sorted, index, sorted, index+1, n-index);
//                sorted[index] = child;
//                n = index;
//            }
//        }
//    }

    private void setTop(Vertex v) {
        for (Vertex parent : network.getSuccessors(v)) {
            int oldRank = rank.get(parent).intValue();
            int newRank = minPred(parent)-1;
            if (newRank > oldRank) {
                rank.put(parent, new Integer(newRank));
            }
            setTop(parent);
        }
    }
    

    /**
     * @return the graph
     */
    public Network getGraph() {
        return network;
    }

    /**
     * Returns the rank of the specified vertex
     * @param v the vertex
     * @return the rank or -1
     */
    public int getRank(Vertex v) {
        Integer i = rank.get(v);
        if (i != null)
            return i.intValue();
        return -1;
    }
    
    /**
     * @return the maxRank
     */
    public int getMaxRank() {
        return maxRank;
    }
}
