package geneaquilt.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.Printer;
import geneaquilt.data.Fam;
import geneaquilt.data.Vertex;
import geneaquilt.hull.HullBin;

/**
 * <b>PFam</b> is a Piccolo object representing a Family.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class PFam extends PText implements PVertex, HullBin {
    Fam fam;
    Color bordercolor;
    int hullBinIndex;
    static final BasicStroke STROKE = new BasicStroke(1); // FIXME

    
    /**
     * Creates a PFam from a specified Fam.
     * @param fam the Fam
     */
    public PFam(Fam fam) {
        super(fam.getLabel());
        this.fam = fam;
        setTextPaint(GraphicsConstants.FAM_COLOR);
        setFont(GraphicsConstants.FAM_FONT);
        setStrokePaint(GraphicsConstants.instance.famBorderColor());
    }
    
    /**
     * @return the fam
     */
    public Fam getFam() {
        return fam;
    }
    
    /**
     * {@inheritDoc}
     */
    public Vertex getVertex() {
        return fam;
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
    
    /**
     * Sets the stroke paint
     * @param bordercolor color
     */
    public void setStrokePaint(Color bordercolor) {
    	this.bordercolor = bordercolor;
    }
    
    @Override
    protected void paint(PPaintContext paintContext) {
        double save = getGreekThreshold();
        try {
        if (Printer.isPrinting())
            setGreekThreshold(0);
    	super.paint(paintContext);
        if (bordercolor != null) {
        	Graphics2D g = paintContext.getGraphics();
        	g.setColor(bordercolor);
        	g.setStroke(STROKE);
        	g.draw(getBoundsReference());
        }
        }
        finally {
        if (Printer.isPrinting())
            setGreekThreshold(save);
        }
    }
}
