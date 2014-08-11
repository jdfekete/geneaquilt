package geneaquilt.nodes;

import java.util.List;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import geneaquilt.data.Fam;
import geneaquilt.utils.PiccoloUtils;

/**
 * Class FamGeneration
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class FamGeneration extends PNode {
	
    /**
     * Creates a Generation from a layer of individuals
     * @param layer the layer
     */
    public FamGeneration(List<Fam> layer) {
        for (Fam f : layer) {
            addChild(f.getNode());
        }
        setPaint(GraphicsConstants.FAM_GENERATION_COLOR);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void layoutChildren() {
        double w = 0;
        double h = 0;
        for (Object o : getChildrenReference()) {
            PNode child = (PNode)o;
//            child.setGlobalTranslation(
//                    new Point2D.Double(
//                            getX()+w,
//                            getY()));
            PiccoloUtils.setLocation(child, getX()+w, getY(), true);
            if (child.getVisible()) {
            	PBounds b = child.getFullBoundsReference();
	            w += b.getWidth();
	            h = Math.max(h, b.getHeight());
            }
        }
        setBounds(getX(), getY(), w, h);
    }
}
