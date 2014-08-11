package geneaquilt.nodes;

import java.awt.Graphics2D;
import java.util.List;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.Printer;
import geneaquilt.data.Indi;
import geneaquilt.hull.HullBin;
import geneaquilt.utils.PiccoloUtils;
import geneaquilt.utils.PrintConstants;

/**
 * An <b>IndiGeneration</b> is a container that lays out its
 * children (Indi) in a vertical fashion and draws an horizontal
 * grid that spans over all the families
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class IndiGeneration extends PNode implements HullBin {
	
	int hullBinIndex;
	
    /**
     * Creates a Generation from a layer of individuals
     * @param layer the layer
     */
    public IndiGeneration(List<Indi> layer) {
        for (Indi i : layer) {
            addChild(i.getNode());
        }
        //setPaint(Color.LIGHT_GRAY);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
    	final double lineSpacing = GraphicsConstants.INDI_LINE_SPACING;
        double h = 0;
        double w = 0;
        for (Object o : getChildrenReference()) {
            PNode child = (PNode)o;
//            child.setGlobalTranslation(
//                    new Point2D.Double(
//                            getX(),
//                            getY()+h));
            PiccoloUtils.setLocation(child, getX(), getY()+h, true);
            if (child.getVisible()) {
            	PBounds b = child.getFullBoundsReference();
	            h += b.getHeight() * lineSpacing;
	            w = Math.max(w, b.getWidth());
	            if (child instanceof PSemanticText)
	            	((PSemanticText)child).recomputeLayout();
            }
        }
        setBounds(getX(), getY(), w/2, h/2);
    }
    
    /**
     * {@inheritDoc}
     */
	public void fullPaint(PPaintContext paintContext) {

		if (!getVisible() || !fullIntersects(paintContext.getLocalClip()))
			return;

		// -- Paint in normal scale
		
		if (Printer.isPrinting() || PrintConstants.instance.getScale(paintContext) > 0.1) {
			super.fullPaint(paintContext);
			return;
		}
		
		// -- Paint in small scale
		
//		paintContext.pushTransform(getTransform());
		paintContext.pushTransparency(getTransparency());

		Graphics2D g = paintContext.getGraphics();
		g.setColor(GraphicsConstants.INDI_GENERATION_COLOR);
		g.fill(getBoundsReference());
		
		// paint only highlighted labels (string search)
		for (Object o : getChildrenReference()) {
			PNode n = (PNode)o;
			if (n instanceof PSemanticText && ((PSemanticText)n).getPaint() != null) {
				n.fullPaint(paintContext);
			}
		}
		
		paintContext.popTransparency(getTransparency());
//		paintContext.popTransform(getTransform());
	}
	
	/**
	 * {@inheritDoc}
	 */
    public int getHullBinIndex() {
    	return hullBinIndex;
    }

    /**
     * {@inheritDoc}
     */
    public void setHullBinIndex(int index) {
    	hullBinIndex = index;
    }
}
