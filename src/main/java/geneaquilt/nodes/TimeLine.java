package geneaquilt.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.data.DateRange;
import geneaquilt.data.Indi;
import geneaquilt.data.Vertex;
import geneaquilt.selection.Selection;
import geneaquilt.selection.SelectionManager;
import geneaquilt.utils.GUIUtils;

/**
 * Class TimeLine
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class TimeLine extends PNode implements ChangeListener {
    QuiltManager               manager;
    PNode                      areaVisiblePNode;
    PCanvas                    canvas;
    PCanvas                    viewedCanvas;
    PropertyChangeListener     changeListener;
    DateRange                  fullRange;
    DateRange                  visibleRange;
    PropertyChangeSupport      propChange;
    Font                       font = new Font("SansSerif", Font.PLAIN, 12);
    Line2D.Double              line = new Line2D.Double();
    PBasicInputEventHandler    pickEventHandler = new PBasicInputEventHandler() {
        Collection<Vertex> selected;
        public void mouseClicked(PInputEvent ev) {
            if (ev.getClickCount()==1)
                selected = drag(ev.getPosition());
            else if (selected != null) {
                clearHighlights();
                manager.select(selected);
            }
            
        }
    };

    /** prop. visibleRange */
    public static final String PROP_VISIBLE_RANGE = "visibleRange";

    /**
     * Creates a TimeLine from a manager.
     * 
     * @param manager
     *            the manager
     */
    public TimeLine(QuiltManager manager) {
        this.manager = manager;
        //setPaint(new Color(200, 200, 200, 20));
        changeListener = new PropertyChangeListener() {
            public void propertyChange(final PropertyChangeEvent evt) {
                updateFromViewed();
            }
        };
        
        areaVisiblePNode = new PFlatRect();
        areaVisiblePNode.setPaint(new Color(0.5f, 0.5f, 1f, 0.3f));
        areaVisiblePNode.setTransparency(.5f);
        areaVisiblePNode.setBounds(0, 0, 100, getHeight());
        
        addChild(areaVisiblePNode);
        propChange = new PropertyChangeSupport(this);
    }

    /**
     * Connects to the specified canvas with the specified manager
     * @param viewedCanvas the viewed canvas
     * @param canvas my canvas
     */
    public void connect(final PCanvas viewedCanvas, final PCanvas canvas) {
        this.viewedCanvas = viewedCanvas;
        this.viewedCanvas.getCamera().addPropertyChangeListener(changeListener);
        this.canvas = canvas;
        this.canvas.getCamera().addPropertyChangeListener(changeListener);
        canvas.addInputEventListener(pickEventHandler);
    }

    /**
     * Disconnects from the watched canvas.
     */
    public void disconnect() {
        viewedCanvas.getCamera().removePropertyChangeListener(changeListener);
        canvas.getCamera().removePropertyChangeListener(changeListener);
        canvas.removeInputEventListener(pickEventHandler);
    }

    DateRange computeFullRange() {
        DateRange full = new DateRange();
        for (Vertex v : manager.getNetwork().getVertices()) {
            full.union(v.getDateRange());
        }
        return full;
    }

    /**
     * @return the full range of dates
     */
    public DateRange getFullRange() {
        if (fullRange == null) {
            fullRange = computeFullRange();
        }
        return fullRange;
    }

    DateRange computeVisibleRange() {
        PBounds viewBounds = viewedCanvas.getCamera().getViewBounds();
        PBounds fullBounds = viewedCanvas.getLayer().getFullBoundsReference();
        viewBounds.y = fullBounds.y;
        viewBounds.height = fullBounds.height;
        DateRange visible = new DateRange();
        visible.setInvalid();
        ArrayList<PNode> picked = new ArrayList<PNode>();
        viewedCanvas.getLayer().findIntersectingNodes(viewBounds, picked);
        for (PNode n : picked) {
            if (n instanceof PFam) {
                PFam pfam = (PFam) n;
                visible.union(pfam.getFam().getDateRange());
            }
            else if (n instanceof PIndi) {
                PIndi pindi = (PIndi) n;
                visible.union(pindi.getIndi().getDateRange());
            }
            // else if (n instanceof PEdge) {
            // PEdge pedge = (PEdge) n;
            // visible.union(pedge.getEdge().getDateRange());
            // }
        }

        return visible;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void paintAfterChildren(PPaintContext paintContext) {
        paintTicks(paintContext);
        paintVertices(paintContext);
    }
    
    private void paintTicks(PPaintContext paintContext) {
        getFullRange();
        if (fullRange == null || !fullRange.isValid()) 
            return;
        int[] start = DateRange.getYMD(fullRange.getStart(), null);
        int[] end = DateRange.getYMD(fullRange.getEnd(), null);
        int step = computeStep(start[0], end[0], (int)(getWidth()/100));
        if (step == 0) return;
        double scale = getWidth() / (fullRange.getEnd() - fullRange.getStart());
        double offset = fullRange.getStart();
        
        Graphics2D g = paintContext.getGraphics();
        g.setColor(Color.BLUE);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        line.y1 = getBoundsReference().getMinY()+fm.getHeight();
        line.y2 = getBoundsReference().getMaxY();

        String s = Integer.toString(start[0]);
        Rectangle2D sb = fm.getStringBounds(s, g);
        float sx = (float)(getX());
        float sy = (float)(getY() + fm.getHeight());
        g.drawString(s, sx, sy);
        double rightExtent = (getX() + sb.getMaxX()); 


        int y0 = (start[0] % step);
        if (y0 != 0)
            y0 = step - y0;
        for (int y = start[0]+y0; y <= end[0]; y += step) {
            long d = DateRange.yearFloor(y);
//            assert(d >= fullRange.getStart());
//            assert(d <= fullRange.getEnd());
            line.x1 = line.x2 = (d-offset)*scale+getX();
            g.draw(line);
            s = Integer.toString(y);
            sb = fm.getStringBounds(s, g);
            sx = (float)(line.x1 - sb.getCenterX());
            if (sx >= rightExtent) {
                g.drawString(s, sx, sy);
                rightExtent = (float)(line.x1 + sb.getCenterX());
            }
        }
        s = Integer.toString(end[0]);
        sb = fm.getStringBounds(s, g);
        sx = (float)(getBoundsReference().getMaxX()-sb.getMaxX());
        if (sx >= rightExtent) {
            g.drawString(s, sx, sy);
        }
        
        line.y1 = getBoundsReference().getCenterY();
        step /= 5;
        if (step != 0) {        
            y0 = (start[0] % step);
            if (y0 != 0)
                y0 = step - y0;
            for (int y = start[0]+y0; y <= end[0]; y += step) {
                long d = DateRange.yearFloor(y);
//                assert(d >= fullRange.getStart());
//                assert(d <= fullRange.getEnd());
                line.x1 = line.x2 = (d-offset)*scale+getX();
                g.draw(line);
            }
//            s = Integer.toString(y);
//            sb = fm.getStringBounds(s, g);
//            sx = (float)(line.x1 - sb.getCenterX());
//            if (sx >= rightExtent) {
//                g.drawString(s, sx, sy);
//                rightExtent = (float)(line.x1 + sb.getCenterX());
//            }
        }
        
    }
    
    static double drawString(Graphics2D g, String s, double x, double y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D sb = fm.getStringBounds(s, g);
        g.drawString(
                s, 
                (float)(x - sb.getCenterX()), 
                (float)(y + sb.getHeight()));
        return x +sb.getCenterX();
    }
    
    static int computeStep(int s, int e, int max) {
        int dy = e - s;
        if (dy <= 1)
            return 1;
        int step = 1;
        int mul = 5;
        while ((dy/step) > max) {
            step *= mul;
            if (mul == 5)
                mul = 2;
            else
                mul = 5;
        }
        return step;
    }
    
    private void paintVertices(PPaintContext paintContext) {
        getFullRange();
        if (fullRange == null || !fullRange.isValid()) 
            return;

        double scale = getWidth() / (fullRange.getEnd() - fullRange.getStart());
        double offset = fullRange.getStart();
        double scaley = (getHeight()-20) / manager.getLayerCount();
        Graphics2D g = paintContext.getGraphics();
        g.setColor(Color.BLACK);
        for (Vertex v : manager.getNetwork().getVertices()) {
            DateRange dr = v.getDateRange();
            if (dr == null || ! dr.isValid())
                continue;
            line.y1 = line.y2 = 20 + v.getLayer()*scaley + getY();
            line.x1 = (dr.getStart() - offset) * scale + getX();
            line.x2 = (dr.getEnd() - offset) * scale + getX();
            g.draw(line);
        }
        g.setStroke(new BasicStroke(3));
        SelectionManager selManager = manager.getSelectionManager();
        for (Selection s : selManager.getSelections()) {
            PNode n = s.getSelectedObject();
            if (n instanceof PVertex) {
                PVertex pv = (PVertex) n;
                Vertex v = pv.getVertex();
                DateRange dr = v.getDateRange();
                if (dr == null || ! dr.isValid())
                    continue;

                line.y1 = line.y2 = 20 + v.getLayer()*scaley + getY();
                line.x1 = (dr.getStart() - offset) * scale + getX();
                line.x2 = (dr.getEnd() - offset) * scale + getX();
                g.setColor(s.getStrongColor());
                g.draw(line);
            }
        }
    }
    
    /**
     * Returns all the vertices that pick the selected box.
     * @param box the bounding box to pick
     * @param pick a list of vertices to add to or null
     * @return a list of vertices
     */
    public Collection<Vertex> pick(PBounds box, Collection<Vertex> pick) {
        getFullRange();
        if (fullRange == null || !fullRange.isValid()) 
            return null;

        if (pick == null) {
            pick = new ArrayList<Vertex>();
        }
        double scale = getWidth() / (fullRange.getEnd() - fullRange.getStart());
        double offset = fullRange.getStart();
        double scaley = (getHeight()-20) / manager.getLayerCount();
        for (Vertex v : manager.getNetwork().getVertices()) {
            DateRange dr = v.getDateRange();
            if (dr == null || ! dr.isValid())
                continue;
            line.y1 = line.y2 = 20 + v.getLayer()*scaley + getY();
            line.x1 = (dr.getStart() - offset) * scale + getX();
            line.x2 = (dr.getEnd() - offset) * scale + getX();
            if (line.intersects(box))
                pick.add(v);
        }

        return pick;
    }
    
    SelectionManager getSelectionManager() {
        return manager.getSelectionManager();
    }
    
    /**
     * Selects the object under the specified position.
     * @param pos the position
     * @return the collection of selected vertices
     */
    public Collection<Vertex> drag(Point2D pos) {
        PBounds bb = new PBounds(pos.getX()-1, getY(), 2, getHeight());
        HashSet<Vertex> verts = new HashSet<Vertex>();
        pick(bb, verts);
        if (! verts.isEmpty()) {
            SelectionManager selManager = getSelectionManager();
            ArrayList<Vertex> selection = new ArrayList<Vertex>();
            Color selColor = GUIUtils.multiplyAlpha(selManager.getNextSelectionColor(), 0.7f);
            for (Vertex v : manager.getNetwork().getVertices()) {
                if (v instanceof Indi) {
                    Indi indi = (Indi) v;
//                    if (indi.search(text, field)) {
                    if (verts.contains(indi)) {
                        PNode pindi = indi.getNode();
                        pindi.setPaint(selColor);
                        selection.add(indi);
                    }
                    else {
                        PNode pindi = indi.getNode();
                        pindi.setPaint(null);                    
                    }
                }
            }
            if (! selection.isEmpty()) {
                Vertex first = selection.get(0);
                viewedCanvas.getCamera().animateViewToPanToBounds(
                        first.getNode().getFullBounds(), 
                        200);
            }
        }
        return verts;
    }
    
    void clearHighlights() {
        for (Vertex v : manager.getNetwork().getVertices()) {
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
                indi.getNode().setPaint(null);
            }
        }
    }

    /**
     * This method will get called when the viewed canvas changes
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(final PropertyChangeEvent event) {
        updateFromViewed();
    }

    /**
     * This method gets the state of the viewed canvas and updates the TimeLine
     * view. This can be called from outside code
     */
    public void updateFromViewed() {
        PBounds bounds = canvas.getCamera().getBounds();
        setBounds(bounds);
        DateRange fullRange = getFullRange();
        if (fullRange == null || !fullRange.isValid()) {
            areaVisiblePNode.setVisible(false);
            fullRange = null;
            return;
        }
        double scale = getWidth() / (fullRange.getEnd() - fullRange.getStart());
        double offset = fullRange.getStart();
        setVisibleRange(computeVisibleRange());
        if (!visibleRange.isValid()) {
            areaVisiblePNode.setVisible(false);
        }
        else {
            assert (visibleRange.getStart() >= fullRange.getStart());
            assert (visibleRange.getEnd() <= fullRange.getEnd());
            double xmin = (visibleRange.getStart() - offset) * scale + getX();
            double xmax = (visibleRange.getEnd() - offset) * scale + getX();
            areaVisiblePNode.setVisible(true);
            areaVisiblePNode.setBounds(xmin, 0, xmax-xmin, getHeight());
        }
    }
    
//    private double computeScale() {
//        if (fullRange == null || !fullRange.isValid())
//            return 1;
//        return getWidth() / (fullRange.getEnd() - fullRange.getStart());
//    }
    
    /**
     * @return the visibleRange
     */
    public DateRange getVisibleRange() {
        return visibleRange;
    }

    /**
     * @param visibleRange
     *            the visibleRange to set
     */
    public void setVisibleRange(DateRange visibleRange) {
        if (visibleRange.equals(this.visibleRange))
            return;
        DateRange old = this.visibleRange;
        this.visibleRange = visibleRange;
        firePropertyChange(PROP_VISIBLE_RANGE, old, visibleRange);
    }

    /**
     * {@inheritDoc}
     */
    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        propChange.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return the property change listeners
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propChange.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return the property listeners
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        return propChange.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return true if it has listeners on the specified property
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
        return propChange.hasListeners(propertyName);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(
            String propertyName,
            PropertyChangeListener listener) {
        propChange.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @see java.beans.PropertyChangeSupport#firePropertyChange(java.lang.String,
     *      java.lang.Object, java.lang.Object)
     */
    public void firePropertyChange(
            String propertyName,
            Object oldValue,
            Object newValue) {
        propChange.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * @return the font
     */
    public Font getFont() {
        return font;
    }
    
    /**
     * @param font the font to set
     */
    public void setFont(Font font) {
        this.font = font;
        invalidatePaint();
    }
    
    /**
     * {@inheritDoc}
     */
    public void stateChanged(ChangeEvent e) {
        updateSelection((SelectionManager)e.getSource());
    }
    
    protected void updateSelection(SelectionManager selManager) {
        invalidatePaint();
    }
}
