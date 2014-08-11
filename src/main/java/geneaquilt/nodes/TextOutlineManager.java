package geneaquilt.nodes;

import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This component redraws Indi labels that need to be redrawn on top
 * of selections, with a white outline so they are more readable. 
 * 
 * @author dragice
 *
 */
public class TextOutlineManager extends PNode {

	QuiltManager quilt;
	boolean enabled = false;
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public TextOutlineManager(QuiltManager quilt) {
		this.quilt = quilt;
	}
	
	public PBounds getFullBoundsReference() {
		return quilt.getFullBoundsReference();
	}

	public PBounds getBoundsReference() {
		return quilt.getBoundsReference();
	}
	

	/**
	 * TODO: comment.
	 */
	protected void paint(PPaintContext paintContext) {
		
		if (!enabled)
			return;
		
		if (paintContext.getRenderQuality() == PPaintContext.HIGH_QUALITY_RENDERING && quilt.getSelectionManager().getSelections().size() > 0) {
			Rectangle2D paintclip = paintContext.getLocalClip();
			PBounds selectionbounds = quilt.getSelectionManager().getHighlightManager().getFullBoundsReference();
			if (selectionbounds.intersects(paintclip)) {			
				paintContext.pushTransform(getTransform());
				IndiGeneration generation;
				for (int g=0; g<quilt.getIndiGenerations().length; g++) {
					generation = quilt.getIndiGenerations()[g];
					if (generation.getFullBoundsReference().createIntersection(selectionbounds).intersects(paintclip)) {
						PIndi indi;
				        for (Object o : generation.getChildrenReference()) {
				            if (o instanceof PIndi) {
				            	indi = (PIndi)o;
				            	// FIXME: Don't draw if there is no highlight on top of the indi
				        		paintContext.pushTransform(indi.getTransform());
				            	indi.paintWithOutline(paintContext);
				        		paintContext.popTransform(indi.getTransform());
				            }
				        }
					}
					
				}
				paintContext.popTransform(getTransform());
			}
		}
	}
}
