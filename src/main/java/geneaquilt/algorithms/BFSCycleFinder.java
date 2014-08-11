package geneaquilt.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * <b>BFSCycleFinder</b> implements the algorithm to break cycles in graphs
 * described in:
 * Gansner, E. R., Koutsofios, E., North, S. C., and Vo, K. 1993. A
 * Technique for Drawing Directed Graphs. IEEE Trans. Softw. Eng. 19, 3
 * (Mar. 1993), 214-230
 * 
 * @param <V> the vertex class
 * @param <E> the edge class
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class BFSCycleFinder<V,E> {
    protected DirectedGraph<V, E> graph;
    protected Set<E> cycles;
   
    /**
     * Creates a BFSCycleFinder from the specified graph
     * and connected component. 
     * @param graph the graph
     */
    public BFSCycleFinder(DirectedGraph<V, E> graph) {
        this.graph = graph;
    }
    
    /**
     * Finds all the cyclic edges on the specified weak connected component.
     * @param comp the component or null for the whole graph
     * @return a collection of edges to invert
     */
    public Set<E> findCycles(Collection<V> comp) {
        if (comp == null) 
            comp = graph.getVertices();
        cycles = new HashSet<E>();
        Set<V> mark = new HashSet<V>();
        Set<V> onStack = new HashSet<V>();

        for (V v : comp) {
            dfs(v, mark, onStack);
        }
        return cycles;
    }
    
    private void dfs(V v, Set<V> mark, Set<V> onStack) {
        if (mark.contains(v))
            return;
        mark.add(v);
        onStack.add(v);
        for (E e : graph.getOutEdges(v)) {
            V w = graph.getDest(e);
            if (onStack.contains(w)) {
                cycles.add(e);
                w = graph.getSource(e);
            }
            else {
//                mark.remove(w);
                dfs(w, mark, onStack);
            }
        }
        onStack.remove(v);
    }
}
