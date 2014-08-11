package geneaquilt;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import geneaquilt.nodes.QuiltManager;

/**
 * Class ConstraintViewport
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class ConstraintViewport implements PropertyChangeListener {
    PCanvas          canvas;
    PNode            root;
    ArrayList<PNode> list = new ArrayList<PNode>();
    boolean enabled = true;

    /**
     * Creates a constraint viewport
     */
    public ConstraintViewport() {
    }
    
    /**
     * Enables/disables the constraint on the viewport
     * @param enabled the state to set
     */
    public void setEnabled(boolean enabled) {
    	this.enabled = enabled;
    }

    /**
     * Connects to the canvas, looking at the specified root node
     * 
     * @param canvas
     *            the canvas holding the camera
     * @param root
     *            the root node
     */
    public void connect(PCanvas canvas, PNode root) {
        this.canvas = canvas;
        this.root = root;
        canvas.getCamera().addPropertyChangeListener(this);
    }
    
    /**
     * Disconnects.
     */
    public void disconnect() {
        if (canvas == null) return;
        canvas.getCamera().removePropertyChangeListener(this);
        canvas = null;
        root = null;
    }

    /**
     * {@inheritDoc}
     */
    public void propertyChange(PropertyChangeEvent ev) {
        maybeMoveView();
    }

    protected void maybeMoveView() {
    	if (!enabled)
    		return;
        PBounds b = canvas.getCamera().getViewBounds();
        list.clear();
        root.findIntersectingNodes(b, list);
        if (list.size() < 2) {
            PBounds fb = root.getFullBounds();
            fb.x = b.x;
            fb.width = b.width;
            list.clear();
            root.findIntersectingNodes(fb, list);
            if (list.size() < 2)
                return; // nothing we can do
            PBounds stripBounds = new PBounds();
            for (PNode n : list) {
                if (!(n instanceof QuiltManager)) {
                    stripBounds.add(n.getBoundsReference());
                }
            }
            if (b.getMaxY() < stripBounds.getMinY()) {
                // the view is below the visible portion
                b.y = stripBounds.getMinY();
                canvas.getCamera().animateViewToPanToBounds(b, 200);
            }
            else if (b.getMinY() > stripBounds.getMaxY()) {
                b.y = stripBounds.getMaxY() - b.height;
                canvas.getCamera().animateViewToPanToBounds(b, 200);
            }
        }
    }
}
