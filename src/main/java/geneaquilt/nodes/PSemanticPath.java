package geneaquilt.nodes;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.Printer;
import geneaquilt.utils.PrintConstants;

/**
 * 
 * A PPath that allows to set a minimum stroke screen width and a different stroke color when
 * its screen width is below a certain size.
 * 
 * @author dragice
 *
 */
public class PSemanticPath extends PPath {
    /**
     * 
     * <b>StrokeCache</b> implements a cache of strokes
     */
	/*public static class StrokeCache {
		private Hashtable<Integer, Stroke> cache = new Hashtable<Integer, Stroke>();
		private final int widthAccurracy = 1;
		/ **
         * Returns a stroke with the specified width.
		 * @param width the width
		 * @return a stroke
		 * /
		public Stroke getStrokeWithWidth(float width) {
			int key = (int)(width * widthAccurracy);
			Stroke s = cache.get(new Integer(key));
			if (s != null)
				return s;
			s = new BasicStroke(width);
			cache.put(new Integer(key), s);
			return s;
		}
	}*/
	
	//private static final StrokeCache strokeCache = new StrokeCache();
	private static Stroke lastStroke = null;
	private static float lastStrokeWidth = -1;

	private float normalStrokeWidth = 1;
	private float minimumScreenStrokeWidth = 1;
	private Paint smallStrokePaint = null;
	private float smallStrokePaintScale = 0;
	
	/**
	 * 
	 * @param minimumScreenStrokeWidth
	 */
	public void setMinimumScreenStrokeWidth(float minimumScreenStrokeWidth) {
		this.minimumScreenStrokeWidth = minimumScreenStrokeWidth;
	}
	
	/**
	 * 
	 * @param p
	 * @param scale
	 */
	public void setSmallStrokePaint(Paint p, float scale) {
		this.smallStrokePaint = p;
		this.smallStrokePaintScale = scale;
	}
	
	/**
	 * @return the small stroke paint
	 */
	public Paint getSmallStrokePaint() {
		return smallStrokePaint;
	}

	/**
	 * @return the small stroke paint scale
	 */
	public float getSmallStrokePaintScale() {
		return smallStrokePaintScale;
	}
	
	/**
	 * Creates a PSemanticPath.
	 */
	public PSemanticPath() {
		super();
		updateNormalStrokeWidth();
	}
	
	/**
	 * Creates a PSemanticPath with the specified shape.
	 * @param ppath the shape
	 */
	public PSemanticPath(Shape ppath) {
		super(ppath);
		updateNormalStrokeWidth();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setStroke(Stroke stroke) {
		super.setStroke(stroke);
		updateNormalStrokeWidth();
	}
	
	protected void updateNormalStrokeWidth() {
		if (getStroke() instanceof BasicStroke) {
			normalStrokeWidth = ((BasicStroke)getStroke()).getLineWidth();
		}
	}
	
	protected void paint(PPaintContext paintContext) {
				
		Paint p = getPaint();
		Graphics2D g2 = paintContext.getGraphics();
		
		if (p != null) {
			g2.setPaint(p);
			g2.fill(getPathReference());
		}

		float scale = (float) PrintConstants.instance.getScale(paintContext);
		Paint strokePaint;
		if (smallStrokePaint == null || scale >= smallStrokePaintScale)
			strokePaint = getStrokePaint();
		else
			strokePaint = getSmallStrokePaint();
		
		if (getStroke() != null && strokePaint != null) {
			
			float screenStrokeWidth = normalStrokeWidth * scale;

			if (Printer.isPrinting() || screenStrokeWidth >= minimumScreenStrokeWidth) {
				g2.setPaint(strokePaint);
				if (Printer.isPrinting())
				    g2.setStroke(GraphicsConstants.NULL_WIDTH_STROKE);
				else
				    g2.setStroke(getStroke());
				g2.draw(getPathReference());
			} else {
				if (Printer.isPrinting() || minimumScreenStrokeWidth == 1 && paintContext.getRenderQuality() == PPaintContext.LOW_QUALITY_RENDERING) {
					// Special case: if no antialiasing and stroke width of 1, we can use a stroke
					// width of zero.
					g2.setPaint(strokePaint);
					g2.setStroke(GraphicsConstants.NULL_WIDTH_STROKE);
					g2.draw(getPathReference());
				} else {
					g2.setPaint(strokePaint);
					g2.setStroke(getStrokeWithWidth(minimumScreenStrokeWidth / scale));
					g2.draw(getPathReference());
				}
			}
		}		
	}
	
	private static Stroke getStrokeWithWidth(float width) {
		if (lastStrokeWidth == width)
			return lastStroke;
		lastStroke = new BasicStroke(width);
		return lastStroke;
	}
	
	/**
	 * 
	 * @return the scale for larger strokes
	 */
	public float getScaleForLargerStrokes() {
		return minimumScreenStrokeWidth / normalStrokeWidth;
	}
	
}
