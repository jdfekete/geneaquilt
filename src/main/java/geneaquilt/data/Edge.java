package geneaquilt.data;

import geneaquilt.nodes.PEdge;


/**
 * Class Edge
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class Edge {
    private PEdge node;
    private String from;
    private String to;
    private Vertex fromVertex;
    private Vertex toVertex;

    /**
     * Creates an edge with the name of the from and to vertices.
     * @param from the from vertex name
     * @param to the to vertex name
     */
    public Edge(String from, String to) {
        this.from = from;
        this.to = to;
    }

    
    /**
     * @return the node
     */
    public PEdge getNode() {
        if (node == null)
            node = createNode();
        return node;
    }
    
    protected PEdge createNode() {
        return new PEdge(this);
    }
    
    /**
     * Unreference the node
     */
    public void deleteNode() {
    	node = null;
    }

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this
            || (obj != null
                    && obj instanceof Edge
                    && ((Edge)obj).from.equals(from)
                    && ((Edge)obj).to.equals(to));
    }
    
    /**
     * @return the fromVertex
     */
    public Vertex getFromVertex() {
        return fromVertex;
    }
    
    /**
     * @param fromVertex the fromVertex to set
     */
    public void setFromVertex(Vertex fromVertex) {
        this.fromVertex = fromVertex;
    }

    /**
     * @return the sex of either of the vertices
     */
    public String getSex() {
        String s = fromVertex.getStringProperty("SEX");
        if (s != null)
            return s;
        return toVertex.getStringProperty("SEX");
    }
    
    /**
     * @return the toVertex
     */
    public Vertex getToVertex() {
        return toVertex;
    }
    
    /**
     * @param toVertex the toVertex to set
     */
    public void setToVertex(Vertex toVertex) {
        this.toVertex = toVertex;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return from.hashCode() + 31*to.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Edge["+from+"->"+to+"]";
    }
}
