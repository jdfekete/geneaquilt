package geneaquilt.hull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A 2D path approximated by a polyline.
 * 
 * @author dragice
 *
 */
public class Path {

	// For debugging
	static Stroke pathstroke = new BasicStroke(2);

	double[] x;
	double[] y;
	double[] tmpx;
	double[] tmpy;

	public Path(int nbPoints) {
		x = new double[nbPoints];
		y = new double[nbPoints];
		tmpx = new double[nbPoints];
		tmpy = new double[nbPoints];
	}

	public void copy(Path to) {
		for (int i=0; i<x.length; i++) {
			to.x[i] = x[i]; 
			to.y[i] = y[i]; 
		}
	}
	
	public void translate(int dx, int dy) {
		for (int i=0; i<x.length; i++) {
			x[i] += dx; 
			y[i] += dy; 
		}
	}
	
	public void minY(Path p2) {
		double ymin;
		for (int i=0; i<x.length; i++) {
			ymin = p2.getYCoord(x[i]);
			if (ymin < y[i])
				y[i] = ymin;
		}
	}
	
	public void maxY(Path p2) {
		double ymax;
		for (int i=0; i<x.length; i++) {
			ymax = p2.getYCoord(x[i]);
			if (ymax > y[i])
				y[i] = ymax;
		}
	}
	
	public void average(Path p2) {
		for (int i=0; i<x.length; i++) {
			x[i] = (x[i] + p2.x[i])/2; 
			y[i] = (y[i] + p2.y[i])/2; 
		}
	}
	
	public double getYCoord(double xcoord) {
		int i=0;
		for (; i<x.length && x[i] < xcoord; i++);
		if (i == 0)
			return y[0];
		if (i == x.length)
			return y[x.length-1];
		return y[i-1] + (xcoord - x[i-1]) / (x[i] - x[i-1]) * (y[i] - y[i-1]); 
	}
	
	public Point2D getClosestPoint(Point2D p) {
		int closest_i = getClosestPointIndex(p);
		return new Point2D.Double(x[closest_i], y[closest_i]);
	}
	
	public Point2D getControlPoint(int index) {
		if (index < 0)
			index = 0;
		if (index > x.length - 1)
			index = x.length -1;
		return new Point2D.Double(x[index], y[index]);
	}
	
	public int getClosestPointIndex(Point2D p) {
		int closest_i = 0;
		double min_dist = Double.MAX_VALUE;
		double dist;
		for (int i=0; i<x.length; i++) {
			dist = (p.getX() - x[i])*(p.getX() - x[i]) + (p.getY() - y[i])*(p.getY() - y[i]);
			if (dist < min_dist) {
				min_dist = dist;
				closest_i = i;
			}
		}
		return closest_i;
	}
	
	/**
	 * Smoothen the curve.
	 * 
	 * @param radius
	 * @param iterations
	 * @param preserveExtremities
	 */
	public void smoothen(int radius, int iterations, boolean preserveExtremities) {
		for (int i=0; i<iterations; i++)
			smoothen(radius, preserveExtremities);
	}
	
	private void smoothen(int radius, boolean preserveExtremities) {
		for (int i=0; i<x.length; i++) {
			tmpx[i] = x[i];
			tmpy[i] = y[i];
		}
		double sumx, sumy;
		int j0, j1, r;
		for (int i=0; i<x.length; i++) {
			sumx = 0;
			sumy = 0;
			if (preserveExtremities) {
				int distanceFromExtremity = Math.min(i, x.length-1-i);
				r = Math.min(distanceFromExtremity, radius);
			} else
				r = radius;
			j0 = Math.max(0, i-r);
			j1 = Math.min(x.length-1, i+r);
			if (j1 > j0) {
				for (int j = j0; j<j1; j++) {
					sumx += tmpx[j];
					sumy += tmpy[j];
				}
				double dx = sumx / (j1 - j0) - x[i];
				double dy = sumy / (j1 - j0) - y[i];
				x[i] += dx;
				y[i] += dy;
//				x[i] = sumx / (j1 - j0); 
//				y[i] = sumy / (j1 - j0);
			}
		}
	}
	
	/**
	 * For debugging
	 */
    public void paint(PPaintContext paintContext, Color pathcolor) {
        final Graphics2D g2 = paintContext.getGraphics();
        g2.setColor(pathcolor);
        g2.setStroke(pathstroke);
        for (int i=0; i<x.length-1; i++) {
        	g2.drawLine((int)x[i], (int)y[i], (int)x[i+1], (int)y[i+1]);
        }
        final int r = 3;
        for (int i=0; i<x.length; i++) {
        	g2.fillRect((int)x[i]-r, (int)y[i]-r, r*2+1, r*2+1);
        }
    }
}
