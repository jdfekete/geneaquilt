package geneaquilt.hull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.FamGeneration;
import geneaquilt.nodes.IndiGeneration;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.QuiltManager;

/**
 * 
 * This class quickly computes the hull (envelope) of a quilt. It is used by the BirdsEyeView to constrain
 * the panning to a predefined path and for performing automatic zooming.
 * 
 * The hull is computed by first building a succession of vertical bins that are laid out horizontally.
 * Each bin is aligned with a PNode (PFam or IndiGeneration). At first, bins are given the size of their
 * PNode. They then extend vertically to include every graphical object above and below, including grid
 * lines connecting distant nodes. Once bins have been computed, two smooth curves are created that
 * approximate the envelope above and below the quilt. These two curves are averaged into a third curve
 * that represents the spine of the quilt. 
 * 
 * Note: This class extends PNode for debugging purposes only. It does not need to be added
 * to the Piccolo scenegraph.
 * 
 * @author dragice
 *
 */
public class Hull extends PNode {

	QuiltManager manager;
	
	// For debugging
	static Color bincolor = new Color(0, 0, 1, 0.1f);
	static Color toppathcolor = new Color(0, 0, 1, 0.5f);
	static Color bottompathcolor = new Color(1, 0, 0, 0.5f);
	
	// Bins
	HullBin[] bins;
	double[] x0, x1;
	double[] miny, maxy;
	
    // Outline
	Path topPath;
	Path bottomPath;
	Path midPath;
	Path tmpPath;
	
	/**
	 * Creates a new Hull.
	 * 
	 * @param manager
	 */
	public Hull(QuiltManager manager) {
		this.manager = manager;
		setPickable(false);
	}
	
	/**
	 * Creates the vertical bins, once the QuiltManager has added all its children.
	 */
	public void createBins() {
		
		ArrayList<HullBin> tmpbins = new ArrayList<HullBin>();

		FamGeneration[] fgens = manager.getFamGenerations();
		IndiGeneration[] igens = manager.getIndiGenerations();
		for (int i=0; i<fgens.length; i++) {
			if (igens[i] != null)
				tmpbins.add(igens[i]);
			if (fgens[i] != null) {
				for (Object o : fgens[i].getChildrenReference()) {
					if (o instanceof PFam) {
						tmpbins.add((PFam)o);
					}
				}
			}
		}
		
		int nbBins = tmpbins.size();
		bins = new HullBin[nbBins];
		miny = new double[nbBins];
		maxy = new double[nbBins];
		x0 = new double[nbBins];
		x1 = new double[nbBins];
		topPath = new Path(nbBins+2);
		bottomPath = new Path(nbBins+2);
		midPath = new Path(nbBins+2);
		tmpPath = new Path(nbBins+2);
		for (int i=0; i<nbBins; i++) {
			bins[i] = tmpbins.get(i);
			bins[i].setHullBinIndex(i);
		}
	}

	/**
	 * Recomputes the shape of the hull, once the QuiltManager has updated its layout.
	 */
	public void updateShape() {
		
//		Benchmark.beginTask("Update bins");
		updateBins();
//		Benchmark.endTask();

//		Benchmark.beginTask("Update outline");
		updatePaths();
//		Benchmark.endTask();
	}
	
	private void updateBins() {
			
		PBounds bounds;
		for (int i=0; i<bins.length; i++) {
			bounds = bins[i].getFullBoundsReference();
			miny[i] = bounds.getY();
			maxy[i] = bounds.getY() + bounds.getHeight();
			if (i == 0)
				x0[i] = bounds.getX();
			else {
				// connect bins horizontally
				x0[i] = (x1[i-1] + bounds.getX())/2;
				x1[i-1] = x0[i];
			}
			x1[i] = bounds.getX() + bounds.getWidth();
		}
		
		Network network = manager.getNetwork();
        for (Edge edge : network.getEdges()) {
            PBounds nb = edge.getNode().getFullBoundsReference();
            if (nb.getWidth() == 0)
            	continue;
            Vertex from = edge.getFromVertex();
            Vertex to = edge.getToVertex();
            PFam fam;
        	PBounds fb;
            
            if (to instanceof Fam) {
            	// Propagate the bounds to the right
            	double xmin = nb.getX();
            	double ymax = nb.getY() + nb.getHeight();
            	fam = (PFam)to.getNode();
            	int i = fam.getHullBinIndex();
            	do {
            		fb = bins[i].getFullBoundsReference();
            		addYCoordinate(i, ymax);
            		i++;
            	} while(i < bins.length && fb.getX() + fb.getWidth() >= xmin && fb.getY() <= ymax);
            } else if (from instanceof Fam) {
            	// Propagate the bounds to the left
            	double xmax = nb.getX() + nb.getWidth();
            	double ymin = nb.getY();
            	fam = (PFam)from.getNode();
            	int i = fam.getHullBinIndex();
            	do {
            		fb = bins[i].getFullBoundsReference();
            		addYCoordinate(i, ymin);
            		i--;
            	} while(i > 0 && fb.getX() <= xmax && fb.getY() + fb.getHeight() >= ymin);
            }
        }
	}
	
	private void addYCoordinate(int binIndex, double y) {
		if (y < miny[binIndex])
			miny[binIndex] = y;
		if (y > maxy[binIndex])
			maxy[binIndex] = y;
	}
	
	private void updatePaths() {
		topPath.x[0] = x0[0]; 
		topPath.y[0] = miny[0];
		bottomPath.x[0] = x0[0]; 
		bottomPath.y[0] = maxy[0];

		for (int i=0; i<bins.length; i++) {
			topPath.x[i+1] = x1[i]; 
			topPath.y[i+1] = miny[i];
			bottomPath.x[i+1] = x0[i]; 
			bottomPath.y[i+1] = maxy[i];
		}
		
		int i1 = bins.length-1;
		topPath.x[i1+2] = x1[i1]; 
		topPath.y[i1+2] = miny[i1];
		bottomPath.x[i1+2] = x1[i1]; 
		bottomPath.y[i1+2] = maxy[i1];
		
		// Here we try to smoothen the curves while at the same time ensuring they will still contain
		// all the graphical objects.
		
		topPath.copy(tmpPath);
		topPath.translate(200, -200);
		topPath.smoothen(5, 6, true);
		topPath.minY(tmpPath);
		topPath.translate(60, -60);
		topPath.smoothen(1, 6, true);
		
		bottomPath.copy(tmpPath);
		bottomPath.translate(-200, 200);
		bottomPath.smoothen(5, 6, true);
		bottomPath.maxY(tmpPath);
		bottomPath.translate(-60, 60);
		bottomPath.smoothen(1, 6, true);
		
		bottomPath.copy(midPath);
		midPath.average(topPath);
	}
	
	/**
	 * @param p
	 * @return the closest control point on the middle path (spine).
	 */
	public Point2D getPointOnPath(Point2D p) {
		if (midPath == null)
			return null;
		return midPath.getClosestPoint(p);
	}
	
	/**
	 * Return the next point
	 * @param p point close
	 * @return the next point
	 */
	public Point2D getNextPoint(Point2D p) {
		if (midPath == null)
			return null;
		return midPath.getControlPoint(midPath.getClosestPointIndex(p) + 1);
	}
	
	/**
	 * Return the previous point
	 * @param p the point close
	 * @return the previous point
	 */
	public Point2D getPreviousPoint(Point2D p) {
		if (midPath == null)
			return null;
		return midPath.getControlPoint(midPath.getClosestPointIndex(p) - 1);
	}

	/**
	 * @param p
	 * @return the bounding rectangle associated to the closest point on the middle path (spine). The
	 * center of the rectangle belongs to the path. Its size is large enough to contain all graphical
	 * objects around.
	 */
	public Rectangle2D getAreaToZoom(Point2D p) {
		if (midPath == null)
			return null;
		int i = midPath.getClosestPointIndex(p);
		return new Rectangle2D.Double(
			bottomPath.x[i],
			topPath.y[i],
			topPath.x[i] - bottomPath.x[i],
			bottomPath.y[i] - topPath.y[i]);
	}
	
	/**
	 * For debugging
	 */
    @Override
    protected void paint(PPaintContext paintContext) {
        final Graphics2D g2 = paintContext.getGraphics();

        if (bins  != null) {
            g2.setColor(bincolor);
            for (int i=0; i<bins.length; i++) {
            	g2.fillRect((int)x0[i], (int)miny[i], (int)(x1[i] - x0[i]), (int)(maxy[i] - miny[i]));
            }     
        }
        
        if (topPath != null)
        	topPath.paint(paintContext, toppathcolor);
        if (bottomPath != null)
        	bottomPath.paint(paintContext, toppathcolor);
        if (midPath != null)
        	midPath.paint(paintContext, bottompathcolor);
        
        g2.setColor(new Color(0, 1, 0, 0.8f));
        for (int i=0; i<midPath.x.length; i++) {
        	if (i%10 == 0)
        		g2.drawLine((int)topPath.x[i], (int)topPath.y[i], (int)bottomPath.x[i], (int)bottomPath.y[i]);
        }
    }
    
}
