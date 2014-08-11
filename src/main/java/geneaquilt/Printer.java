/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsbase.util.export.ExportDialog;

/**
 * Class Printer
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class Printer {
    private static boolean printing = false;
    
    /**
     * @return the printing
     */
    public static boolean isPrinting() {
        return printing;
    }
    
    /**
     * @param printing the printing to set
     */
    public static void setPrinting(boolean printing) {
        Printer.printing = printing;
    }
    
    /**
     * Creates an export menu item
     * @param canvas the canvas to print
     * @return a JMenuItem
     */
    public static JMenuItem createExportMenu() {
        JMenuItem exportItem = new JMenuItem("Export Graphics to ...");
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        exportItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (GeneaQuilt.getQuilt()==null)
                    return;
                PCanvas panel = GeneaQuilt.getQuilt().getCanvas();
                if (panel == null)
                    return;
                ExportDialog export = new ExportDialog();
//                Properties prop = new Properties();
                // Should look into the sources to have this magic name
//                prop.setProperty(
//                        export.getClass().getName()+".SaveAsFile",  //$NON-NLS-1$
//                        filename);
//                export.setUserProperties(prop);
                export.showExportDialog(
                        panel,
                        "Export view as ...",
                        panel,
                        (new File(GeneaQuilt.getQuilt().filename)).getName());
//                ExportFileType t = new PDFExportFileType();
//                if (fileChooser.showDialog(null, "OK") 
//                        == JFileChooser.APPROVE_OPTION) {
//                    File f = t.adjustFilename(
//                            fileChooser.getSelectedFile(),
//                            null);
//                    if (f.exists())  {
//                        int ok = JOptionPane.showConfirmDialog(null,"Replace existing file?");
//                        if (ok != JOptionPane.OK_OPTION) return;
//                     }
//
//                    printPDF(f, t, panel, panel);
//                }
            }
        });
        return exportItem;
    }
    
//    private static void printPDF(
//            File file, 
//            ExportFileType t,
//            Component comp,
//            PCanvas canvas) {
//        try {
//            PDFGraphics2D pdfGraphics = new PDFGraphics2D(file, comp);
//            PCamera camera = canvas.getCamera();
//            final PBounds originalCameraBounds = camera.getBounds();
//            final AffineTransform originalTransform = canvas.getLayer().getChild(0).getTransform();
//            final PBounds layerBounds = camera.getUnionOfLayerFullBounds();
//            camera.setBounds(layerBounds);
//            Dimension size = PageConstants.getSize(
//                    pdfGraphics.getProperty(PDFGraphics2D.PAGE_SIZE),
//                    pdfGraphics.getProperty(PDFGraphics2D.ORIENTATION));
//
//            double vscale = size.height / layerBounds.getHeight();
//            double hscale = size.width / layerBounds.getWidth();
//            double scale;
//            if (hscale < vscale) {
//                scale = hscale;
//            }
//            else {
//                scale = vscale;
//            }
//            //scale = 0.5;
////            double r = scale * layerBounds.getWidth() / size.width;
////            double c = scale * layerBounds.getHeight() / size.height;
////            int rows = (int)Math.ceil(r);
////            int cols = (int)Math.ceil(c);
////            
//////            if (rows > 1 || cols > 1) {
////                pdfGraphics.setMultiPage(true);
//////            }
//            
////            ArrayList<PNode> list = new ArrayList<PNode>();
////            int rows = 1;
////            int cols = 1;
//            pdfGraphics.startExport();
//            for (int row = 0; row < rows; row++) {
//                for (int col = 0; col < cols; col++) {
//                    PBounds bounds = new PBounds(
//                            row*size.width/scale, 
//                            col*size.height/scale,
//                            size.width/scale, size.height/scale);
//                    list.clear();
//                    canvas.getLayer().findIntersectingNodes(bounds, list);
//                    if (list.size() < 2)
//                        continue;
//                    pdfGraphics.openPage(size, "Genealogy"); //"Page "+(col+1)+"x"+(row+1));
//                    PDFGraphics2D g = (PDFGraphics2D)pdfGraphics.create();
//                    g.translate(-row*size.width, -col*size.height);
//                    g.scale(scale, scale);
//                    final PPaintContext pc = new PPaintContext(g);
//                    final PPaintContext pc = new PPrintContext(g);
//                    
//                    pc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
                    //camera.setBounds(layerBounds);
//                    setPrinting(true);
//                    canvas.getCamera().fullPaint(pc);
//                    g.dispose();
//                    pdfGraphics.closePage();
//                }
//            }
//            pdfGraphics.endExport();
//            pdfGraphics.closeStream();
//            pdfGraphics.dispose();
//            canvas.getLayer().getChild(1).setTransform(originalTransform);
//            camera.setBounds(originalCameraBounds);
//        }
//        catch(Exception e) {
//            JOptionPane.showMessageDialog(comp, e.getMessage(), "Error writing file "+file, JOptionPane.ERROR_MESSAGE);
//        }
//        finally {
//            setPrinting(false);
//        }
//    }
//    
    static void printAll(PCanvas canvas, PDFGraphics2D g2) {
        final PBounds clippingRect = new PBounds(g2.getClipBounds());
        clippingRect.expandNearestIntegerDimensions();

        final PBounds originalCameraBounds = canvas.getCamera().getBounds();
        final PBounds layerBounds = canvas.getCamera().getUnionOfLayerFullBounds();
        canvas.getCamera().setBounds(layerBounds);

        final double clipRatio = clippingRect.getWidth() / clippingRect.getHeight();
        final double nodeRatio = ((double) canvas.getWidth()) / ((double) canvas.getHeight());
        final double scale;
        if (nodeRatio <= clipRatio) {
            scale = clippingRect.getHeight() / canvas.getCamera().getHeight();
        }
        else {
            scale = clippingRect.getWidth() / canvas.getCamera().getWidth();
        }
        g2.scale(scale, scale);
        g2.translate(-clippingRect.x, -clippingRect.y);

        final PPaintContext pc = new PPaintContext(g2);
        pc.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        canvas.getCamera().fullPaint(pc);

        canvas.getCamera().setBounds(originalCameraBounds);
    }
}
