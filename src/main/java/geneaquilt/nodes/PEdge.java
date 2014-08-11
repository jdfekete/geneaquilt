package geneaquilt.nodes;

import geneaquilt.data.Edge;
import geneaquilt.data.Indi;
import geneaquilt.data.Vertex;

import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Class PEdge
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class PEdge extends PIsoShape {
    Edge edge;
    static final Ellipse2D.Double circle = new Ellipse2D.Double(1, 1, 8, 8);
    static final Rectangle2D.Double rectangle = new Rectangle2D.Double(1.5, 1.5, 7, 7);
    static final int[] tri_x = { 0, 5, 10};
    static final int[] tri_y = { 10, 0, 10 };
    static final Polygon triangle = new Polygon(tri_x, tri_y, 3);
//    static final PBounds rectangle = new PBounds(1.5, 1.5, 7, 7);
    
    /**
     * Creates a PEdge
     * @param edge
     */
    public PEdge(Edge edge) {
        super("M".equals(edge.getSex()) ? rectangle : circle);
        setBounds(0, 0, 10, 10);
        this.edge = edge;
        setPaint(GraphicsConstants.instance.edgeColor());
    }
    
    /**
     * @return the edge
     */
    public Edge getEdge() {
        return edge;
    }

//    /**
//     * Indicate that the bounds are volatile for this group
//     */
//    public boolean getBoundsVolatile() {
//        return true;
//    }

    /**
     * Computes the position according to the position of the
     * vertices.
     */
    public void updateBounds() {
        double x;
        double y;
        double w;
        double h;
        Vertex from = edge.getFromVertex();
        Vertex to = edge.getToVertex();
        if (from instanceof Indi) {
            x = to.getNode().getFullBoundsReference().getX();
            w = to.getNode().getFullBoundsReference().getWidth();
            y = from.getNode().getFullBoundsReference().getY();
            h = from.getNode().getFullBoundsReference().getHeight();
        }
        else {
            x = from.getNode().getFullBoundsReference().getX();
            w = from.getNode().getFullBoundsReference().getWidth();
            y = to.getNode().getFullBoundsReference().getY();
            h = to.getNode().getFullBoundsReference().getHeight();
        }

        if (w <= 0)
            w = 0.0000000001;
        if (h <= 0)
            h = 0.0000000001;
        setBounds(x, y, w, h);
        setVisible(from.getNode().getVisible() && to.getNode().getVisible());
    }
    
}
