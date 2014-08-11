package geneaquilt;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDebug;
import geneaquilt.data.DateRange;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.event.DisabablePanEventHandler;
import geneaquilt.event.MouseWheelZoomController;
import geneaquilt.io.DOTLayersReader;
import geneaquilt.io.DOTWriter;
import geneaquilt.io.GEDReader;
import geneaquilt.io.JSONWriter;
import geneaquilt.io.LayerWriter;
import geneaquilt.io.LayersReader;
import geneaquilt.io.PEDReader;
import geneaquilt.io.TIPReader;
import geneaquilt.nodes.GraphicsConstants;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.nodes.PVertex;
import geneaquilt.nodes.QuiltManager;
import geneaquilt.nodes.TextOutlineManager;
import geneaquilt.nodes.TimeLine;
import geneaquilt.selection.DOIManager;
import geneaquilt.selection.SelectionManager;
import geneaquilt.selection.SlidingController;
import geneaquilt.utils.GUIUtils;
import geneaquilt.utils.PrintConstants;
import geneaquilt.utils.PrintUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Class GeneaQuilt
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class GeneaQuilt implements PFilterAnimation.Target {
    private static final Logger LOG = Logger.getLogger(GeneaQuilt.class);
	// NOTE: These fields all have default access to allow for performance profiling.

    private static final String TITLE = "Genealogy Quilt";
    private static JFrame frame;
    private static JFileChooser jfile = null;
    private static JFileChooser jexportfile = null;
    private static GeneaQuilt quilt = null;
    
    private JSplitPane viewControlSplit, timelineSplit;
    private DetailsTable detailsTable;
    private JLabel searchLabel;
    private JTextField searchField;
    private JCheckBox literalFlag;
    private JCheckBox caseSensitiveFlag;
    private int searchFlags = Pattern.LITERAL | Pattern.CASE_INSENSITIVE;
    private JComboBox fieldList;
    private Network network;
    private BirdsEyeView bev;
    private QuiltManager quiltManager;
    private TextOutlineManager outlineManager;
    private PCanvas canvas;
    private TimeLine timeLine;
    private PCanvas timeLineCanvas;
    private DOIManager doiManager;
    private JToggleButton filterBox;
//    private PLayer bgLayer;
    private PLayer mainLayer;
//    private PLayer fgLayer;
    private boolean filteringAnimationEnabled = true; // Set this to false to turn filtering animation off
    private float filteringParameter = 0;
    PNode centerNode;
    Point2D centerNodePreviousPosition;
    String filename;
        
    /**
     * The main program
     * @param args
     */
    public static void main(String[] args) {
    	
    	Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
  	      public void uncaughtException(Thread t, Throwable e) {
  	    	  String message;
  	    	  if (e instanceof OutOfMemoryError) {
  	    		  message = "Java ran out of memory!\n\nTo fix this problem, run GeneaQuilts from the command line:\njava -jar -Xms256m geneaquilt-x.x.x.jar\n\nIf you still get the same error, increase the value 256 above.";
  	    	  } else {
  	    		  message = "An error occured: " + e.getClass() + "("+ e.getMessage() + ")";
  	    	  }
  	    	  
  	    	  JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
  	    	  e.printStackTrace();
  	      }
  	    });
    	
    	doMain(args);
    }
    
    protected static void doMain(String[] args) {
        URL loggerConfig = GeneaQuilt.class.getClassLoader().getResource("log4j.properties");
        if (loggerConfig != null)
            PropertyConfigurator.configure(loggerConfig);
        else {
            BasicConfigurator.configure();
        }

        frame = new JFrame(TITLE);
        JMenuBar mb = new JMenuBar();
        frame.setJMenuBar(mb);
        JMenu fileMenu = new JMenu("File");
        mb.add(fileMenu);
        JMenuItem openMenu = new JMenuItem("Open...");
        openMenu.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		showFileChooser();
        	}
        });
        fileMenu.add(openMenu);       
        fileMenu.addSeparator();

//        JMenuItem layerMenu = new JMenuItem("Save Layers");
//        layerMenu.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                if (getQuilt()!=null)
//                    getQuilt().saveLayer();
//            }
//        });
//        fileMenu.add(layerMenu);

        JMenuItem exportMenu = new JMenuItem("Export to JSON...");
        exportMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getQuilt()!=null)
                    getQuilt().saveAsJSON();
            }
        });
        fileMenu.add(exportMenu);

        exportMenu = new JMenuItem("Export to DOT...");
        exportMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getQuilt()!=null)
                    getQuilt().saveAsDOT();
            }
        });
        fileMenu.add(exportMenu);
        
        JMenuItem exportSelectionMenu = new JMenuItem("Export selection to DOT...");
        exportSelectionMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getQuilt()!=null)
                    getQuilt().saveSelectionAsDOT();
            }
        });
        fileMenu.add(exportSelectionMenu);
        
        fileMenu.add(Printer.createExportMenu());
        
//        JMenuItem printMenu = new JMenuItem("Print...");
//        printMenu.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                getQuilt().simplePrint();
//            }
//        });
//        fileMenu.add(printMenu);
        
        JMenuItem statsMenu = new JMenuItem("Stats...");
        statsMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, getQuilt().getStats(), "Stats", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        fileMenu.add(statsMenu);
        
        fileMenu.addSeparator();
        JMenuItem quitMenu = new JMenuItem("Quit");
        quitMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(
                        frame, 
                        "Confirm quit?", 
                        "Quit", 
                        JOptionPane.YES_NO_OPTION)
                        == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
        fileMenu.add(quitMenu);
        
        JMenu viewMenu = new JMenu("View");
        mb.add(viewMenu);
        JMenuItem viewAll = new JMenuItem("View All");
        viewAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (getQuilt() != null)
                    getQuilt().viewAll();
            }
        });
        viewMenu.add(viewAll);
        
        ButtonGroup labelBy = new ButtonGroup();
        JRadioButtonMenuItem labelByName = new JRadioButtonMenuItem("Label by Name", true);
        labelByName.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getQuilt().setLabelBy("NAME");
                }
            }
        });
        labelBy.add(labelByName);
        viewMenu.add(labelByName);
        
        JRadioButtonMenuItem labelBySurname = new JRadioButtonMenuItem("Label by Surname", false);
        labelBySurname.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getQuilt().setLabelBy("NAME.SURN");
                }
            }
        });
        labelBy.add(labelBySurname);
        viewMenu.add(labelBySurname);
        
        JRadioButtonMenuItem labelByGiven = new JRadioButtonMenuItem("Label by Given Name", false);
        labelByGiven.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getQuilt().setLabelBy("NAME.GIVN");
                }
            }
        });
        labelBy.add(labelByGiven);
        viewMenu.add(labelByGiven);
        
        viewMenu.addSeparator();
        
        JCheckBoxMenuItem dotDebug = new JCheckBoxMenuItem(
                "Keep DOT file",
                DOTLayersReader.isDebug());
        viewMenu.add(dotDebug);
        dotDebug.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                DOTLayersReader.setDebug(
                        ev.getStateChange()==ItemEvent.SELECTED);
            }
        });
        
        JCheckBoxMenuItem viewDebug = new JCheckBoxMenuItem("Frame Rate");
        viewMenu.add(viewDebug);
        viewDebug.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                PDebug.debugPrintFrameRate = 
                    ev.getStateChange()==ItemEvent.SELECTED;
            }
        });
        viewDebug.setSelected(PDebug.debugPrintFrameRate);

        JCheckBoxMenuItem threadDebug = new JCheckBoxMenuItem("Thread Bugs");
        viewMenu.add(threadDebug);
        threadDebug.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                PDebug.debugThreads = 
                    ev.getStateChange()==ItemEvent.SELECTED;
            }
        });
        viewDebug.setSelected(PDebug.debugThreads);
        
        final JCheckBoxMenuItem textOutlines = new JCheckBoxMenuItem("Text outlines");
        viewMenu.add(textOutlines);
        textOutlines.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
            	getQuilt().outlineManager.setEnabled(textOutlines.isSelected());
            	getQuilt().getCanvas().repaint();
            }
        });
        textOutlines.setSelected(false);//getQuilt().outlineManager.isEnabled());

        JMenu editMenu = new JMenu("Edit");
        mb.add(editMenu);
//        JMenuItem estimateDates = new JMenuItem("Estimate Dates");
//        editMenu.add(estimateDates);
//        estimateDates.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent ev) {
//                if (getQuilt() != null)
//                    getQuilt().estimateDates();
//            }
//        });
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        resizeAndCenterWindow(frame);
        frame.setVisible(true);
        
        if (args.length == 0) {
        	showFileChooser();
        } else {
        	if (args.length > 1) {
        		LOG.warn("WARNING: Only one genealogy file can be loaded at a time. Loading the first one...");
        	}
            File file = new File(args[0]);
            if (!file.exists()) {
                LOG.error("The file \"" + args[0] + "\" does not exist. Quitting the application.");
        		System.exit(0);
            }
            quilt = new GeneaQuilt(file.getAbsolutePath());
        }
    }
    
    private static void showFileChooser() {
    	
//    	if (quilt != null && quilt.bird != null)
//    		quilt.bird.setVisible(false);
    	
    	if (jfile == null) {
    		jfile = new JFileChooser("data/");
	        jfile.addChoosableFileFilter(new FileFilter() {
	            public String getDescription() {
	                return "Choose a genealogical file";
	            }
	            public boolean accept(File f) {
	                if (f.isDirectory()) {
	                    return true;
	                }
	                String name = f.getName().toLowerCase();
	                return name.endsWith(".ged")
	                    || name.endsWith(".tip")
	                    || name.endsWith(".ped");
	            }
	        });
    	}
        int ret = jfile.showOpenDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = jfile.getSelectedFile();
            if (quilt == null) {
                quilt = new GeneaQuilt(file.getAbsolutePath());
            }
            else {
                quilt.clear();
                quilt = new GeneaQuilt(file.getAbsolutePath());
            }
        }
    }
    
    /**
     * Creates a GeneaQuilt with a specified PCanvas to draw to
     * @param filename the file name
     */
    public GeneaQuilt(final String filename) {
    	Thread loadingThread = new Thread(new Runnable() {
    		public void run() {
    			load(filename);
    		}
    	});
    	loadingThread.start();
    }
    
    void viewAll() {
        canvas.getCamera().setViewBounds(quiltManager.getFullBoundsReference());
    }

    /**
     * Computes statistics about the genealogy.
     * @return the stats.
     */
    public String getStats() {
    	String s = "";
    	s += "Stats for " + filename + ":\n\n";
    	int individuals = 0;
    	int families = 0;
    	for (Vertex v : network.getVertices()) {
    		PNode n = v.getNode();
    		if (n instanceof PIndi)
    			individuals++;
    		else if (n instanceof PFam)
    			families++;
    	}
    	int edges = network.getEdgeCount();
    	int generations = quiltManager.getIndiGenerations().length;
    	
    	s += generations + " generations\n";
    	s += families + " families\n";
    	s += individuals + " individuals\n";
    	s += edges + " edges\n";
    	
    	return s;
    }
    
    private void load(String filename) {
    	GUIUtils.beginLongComputation(frame, "Loading file...");

    	this.filename = filename;
    	filename = filename.toLowerCase();
    	if (filename.endsWith(".tip")) {
            TIPReader reader = new TIPReader();
            network = reader.load(this.filename);
        }
        else if (filename.endsWith(".ped")) {
            PEDReader reader = new PEDReader();
            network = reader.load(this.filename);
        }
        else { //  if (filename.endsWith(".ged")) { // defaults to GED
            GEDReader reader = new GEDReader();
            network = reader.load(this.filename);
        }
        if (network == null) {
            LOG.error("Couldn't read the network");
            JOptionPane.showMessageDialog(null, "Could not read the file. Make sure its format is correct.", "Error", JOptionPane.ERROR_MESSAGE);
            GUIUtils.endLongComputation(frame);
            return;
            //System.exit(1);
        }
        frame.setTitle(TITLE+": "+filename);
        
        final LayersReader layers = new LayersReader();
        GUIUtils.updateComputationMessage("Loading layers...");        
        if (!layers.load(filename, network)) {
            DOTLayersReader dlr = new DOTLayersReader();
            dlr.load(network);
        }

        GUIUtils.updateComputationMessage("Creating visualization...");
        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
        		createCanvas();
        		if (!layers.layerFileExists(GeneaQuilt.this.filename)) {
                    GUIUtils.updateComputationMessage("Saving layers...");
        			saveLayer();
        		}
        	}
        });
    }
    
    private Set<String> getProperties() {
        Set<String> propertySet = new TreeSet<String>();
        for (Vertex v : network.getVertices()) {
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
                for (String key : indi.getProps().keySet()) {
                    propertySet.add(key);
                }
            }
        }
        return propertySet;
    }
    
    void setLabelBy(String attr) {
        try {
            saveCenter();
    
            for (Vertex v : network.getVertices()) {
                if (v instanceof Indi) {
                    Indi indi = (Indi) v;
                    indi.setLabelBy(attr);
                }
            }
        }
        finally {
            restoreCenter();
        }
    }

    private void restoreCenter() {
        // Force layout
        quiltManager.getFullBoundsReference();
        // Update highlights
        getSelectionManager().getHighlightManager().updateHighlightShapes();
        
        // Re-center camera
        if (centerNode != null) {
            PBounds cb = centerNode.getFullBoundsReference();
            double dx = cb.getCenterX() - centerNodePreviousPosition.getX();
            double dy = cb.getCenterY() - centerNodePreviousPosition.getY();
            PBounds vb = canvas.getCamera().getViewBounds();
            vb.setFrame(vb.getX() + dx, vb.getY() + dy, vb.getWidth(), vb.getHeight());
            canvas.getCamera().setViewBounds(vb);
            centerNodePreviousPosition.setLocation(cb.getCenterX(), cb.getCenterY());
        }
    }
    
    private void saveCenter() {
        centerNode = getCenterNode();
        if (centerNode != null) {
            PBounds b = centerNode.getFullBoundsReference();
            centerNodePreviousPosition = new Point2D.Double(b.getCenterX(), b.getCenterY());
        }
    }
    
    void saveLayer() {
        if (quilt == null)
            return;
        int last = filename.lastIndexOf('.');
        String lyerfile;
        if (last == -1) {
            lyerfile = filename + ".lyr";
        }
        else {
            lyerfile = filename.substring(0, last) + ".lyr";
        }
        File f = new File(lyerfile);
        if (f.exists()) {
            if (JOptionPane.showConfirmDialog(
                    frame, 
                    "File "+filename+" already exists.",
                    "Replace", 
                    JOptionPane.YES_NO_OPTION)
                    == JOptionPane.NO_OPTION) {
                return;
            }
        }
        LayerWriter writer = new LayerWriter(network);
        try {
            writer.write(lyerfile);
        }
        catch(IOException e) {
            LOG.error("Cannot write layer file", e);
        }
    }

    void saveAsJSON() {
        saveAsJSON(network);
    }
    
    void saveAsDOT() {
    	saveAsDOT(network);
    }
    
    void saveSelectionAsDOT() {
    	saveAsDOT(quiltManager.getSelectionManager().getSelectedNetwork());
    }
    
    void saveAsDOT(Network network) {
        if (quilt == null)
            return;
        if (jexportfile == null) {
            File f = new File(filename);
            jexportfile = new JFileChooser(f.getParentFile());
        }
        else {
            jexportfile.resetChoosableFileFilters();
        }
        jexportfile.addChoosableFileFilter(new FileFilter() {
            public String getDescription() {
                return "Choose a DOT filename";
            }
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().endsWith(".dot");
            }
        });
        File f = new File(filename);
        String defaultFilename = f.getAbsolutePath() + File.separator + f.getName().substring(0, f.getName().length() - 4) + ".dot";
        jexportfile.setSelectedFile(new File(defaultFilename));
        int ret = jexportfile.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = jexportfile.getSelectedFile().getAbsolutePath();
            DOTWriter writer = new DOTWriter(network);
            //writer.setBare(true);
            try {
                writer.write(filename);
            }
            catch(Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
            }
        }
    }
    
    void saveAsJSON(Network network) {
        if (quilt == null)
            return;
        if (jexportfile == null) {
            File f = new File(filename);
            jexportfile = new JFileChooser(f.getParentFile());
        }
        else {
            jexportfile.resetChoosableFileFilters();
        }
        jexportfile.addChoosableFileFilter(new FileFilter() {
            public String getDescription() {
                return "Choose a JSON filename";
            }
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().endsWith(".json");
            }
        });
        File f = new File(filename);
        String defaultFilename = 
            f.getAbsolutePath() 
            + File.separator 
            + f.getName().substring(0, f.getName().length() - 4) 
            + ".json";
        jexportfile.setSelectedFile(new File(defaultFilename));
        int ret = jexportfile.showSaveDialog(frame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            String filename = jexportfile.getSelectedFile().getAbsolutePath();
            JSONWriter writer = new JSONWriter(network);
            //writer.setBare(true);
            try {
                writer.write(filename);
            }
            catch(Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
            }
        }
    }
    
    void createCanvas() {
        canvas = new PCanvas() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void print(Graphics g) {
                try {
                    Printer.setPrinting(true);
                    super.print(g);
                }
                finally {
                    Printer.setPrinting(false);
                    g.dispose();
                }
            }
        };
        canvas.requestFocus();
        
        Box controls = Box.createVerticalBox();
            
        mainLayer = canvas.getLayer();
        
        // Quilt
        quiltManager = new QuiltManager(network);
        mainLayer.addChild(quiltManager);
        
        // Selections
        PNode selectionLayer = new PNode();
        mainLayer.addChild(selectionLayer);
        SelectionManager selectionManager = new SelectionManager(quiltManager, selectionLayer);
        quiltManager.setSelectionManager(selectionManager);
        
        // Text outlines
        outlineManager = new TextOutlineManager(quiltManager);
        mainLayer.addChild(outlineManager);
        
        ConstraintViewport cvp = new ConstraintViewport();
        cvp.setEnabled(false);
        cvp.connect(canvas, quiltManager);
        
        canvas.setZoomEventHandler(null);
        canvas.addInputEventListener(new MouseWheelZoomController());
        DisabablePanEventHandler panHandler = new DisabablePanEventHandler();
//        canvas.addInputEventListener(new MouseSelectionController(manager, panHandler));
//        canvas.addInputEventListener(new LinkSlidingController(manager, panHandler));
        canvas.addInputEventListener(new SlidingController(quiltManager, panHandler));
        canvas.setPanEventHandler(panHandler);
        
        bev = new BirdsEyeView(quiltManager.getHull());
        PLayer[] bevLayers = new PLayer[] { mainLayer }; //, bgLayer, fgLayer};
        bev.connect(canvas, bevLayers);
        bev.setPreferredSize(new Dimension(150, 150));
        //bird = new JDialog(frame, "Overview");
        //bird.setAlwaysOnTop(true);
        //bird.getContentPane().add(bev);
        //bird.pack();
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
//        Rectangle frameBounds = frame.getBounds();
//        int w = (int)(frameBounds.width * GraphicsConstants.BIRDSEYE_VIEW_SIZE);
//        int h = (int)(frameBounds.height * GraphicsConstants.BIRDSEYE_VIEW_SIZE);
//        bird.setBounds(
//        	frameBounds.x + frameBounds.width - w - 10,
//        	frameBounds.y + 10,
//        	w,
//        	h);
//        bird.setVisible(true);
      //details = new JDialog(frame, "Details");
//        Box dateBox=  new Box(BoxLayout.X_AXIS) {
//            public Dimension getMaximumSize() {
//                Dimension size = getPreferredSize();
//                size.width = Short.MAX_VALUE;
//                return size;
//            }
//        };
        timeLine = new TimeLine(quiltManager);
        DateRange fullRange = timeLine.getFullRange();
        if (fullRange == null || !fullRange.isValid()) {
            timeLine = null;
        }
        else {
//            canvas.getCamera().addChild(timeLine);
//            timeLine.connect(canvas);

//            JLabel minDateLabel = new JLabel("Min:");
//            dateBox.add(minDateLabel);
//            final JTextField minDate = new JTextField(10);
//            dateBox.add(minDate);
//            minDateLabel.setLabelFor(minDate);
//            
//            JLabel maxDateLabel = new JLabel("Max:");
//            dateBox.add(maxDateLabel);
//            final JTextField maxDate = new JTextField(10);
//            dateBox.add(maxDate);
//            maxDateLabel.setLabelFor(maxDate);
//            controls.add(dateBox);
//            timeLine.addPropertyChangeListener(
//                    TimeLine.PROP_VISIBLE_RANGE, 
//                    new PropertyChangeListener() {
//                public void propertyChange(PropertyChangeEvent ev) {
//                    DateRange dr = timeLine.getVisibleRange();
//                    if (dr != null) {
//                        minDate.setText(dr.formatStart());
//                        maxDate.setText(dr.formatEnd());
//                    }
//                }
//            });
        }
        
        Box searchBox=  new Box(BoxLayout.X_AXIS) {
            public Dimension getMaximumSize() {
                Dimension size = getPreferredSize();
                size.width = Short.MAX_VALUE;
                return size;
            }
        };
        
        searchLabel = new JLabel("Search");
        searchLabel.setPreferredSize(new Dimension(90, (int)searchLabel.getPreferredSize().getHeight()));
        searchBox.add(searchLabel);
        searchField = new JTextField(20);
        searchLabel.setLabelFor(searchField);
        searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectSearch(
                        searchField.getText(), 
                        (String)fieldList.getSelectedItem(),
                        searchFlags);
                searchField.setText("");
            }
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void removeUpdate(DocumentEvent e) { search(); }
            public void insertUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
        });
        searchBox.add(searchField);
        literalFlag = new JCheckBox("Literal");
        literalFlag.setSelected((searchFlags&Pattern.LITERAL)!=0);
        literalFlag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                if (literalFlag.isSelected()) {
                    searchFlags |= Pattern.LITERAL;
                }
                else {
                    searchFlags &= (~Pattern.LITERAL);
                }
                search();
            }
        });
        searchBox.add(literalFlag);
        caseSensitiveFlag = new JCheckBox("NoCase");
        caseSensitiveFlag.setSelected((searchFlags&Pattern.CASE_INSENSITIVE)!=0);
        caseSensitiveFlag.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                if (caseSensitiveFlag.isSelected()) {
                    searchFlags |= Pattern.CASE_INSENSITIVE;
                }
                else {
                    searchFlags &= (~Pattern.CASE_INSENSITIVE);
                }
                search();
            }
        });
        searchBox.add(caseSensitiveFlag);
        controls.add(searchBox);
        
        Set<String> propertySet = getProperties();
        propertySet.add("*");
        fieldList = new JComboBox(propertySet.toArray()) {
            public Dimension getMaximumSize() {
              Dimension size = getPreferredSize();
              size.width = Short.MAX_VALUE;
              return size;
          }
        };
        controls.add(fieldList);
//        details.getContentPane().add(searchBox, BorderLayout.NORTH);
        detailsTable = new DetailsTable(getSelectionManager());
        detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailsTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = detailsTable.getSelectedRow();
                if (row < 0) return;
                String field = (String)detailsTable.getValueAt(row, 0);
                if ("ID".equals(field)) {
                    String id = (String)detailsTable.getValueAt(row, 1);
                    showNodeId(id);
                }
            }
        });
        controls.add(detailsTable.getScrollPane());
        
        filterBox = new JToggleButton("Filter") {
            public Dimension getMaximumSize() {
                Dimension size = getPreferredSize();
                size.width = Short.MAX_VALUE;
                return size;
            }
          };
        filterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                filterItems(ev.getStateChange() == ItemEvent.SELECTED);
            }
        });
        controls.add(filterBox);

        JSplitPane overviewSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                bev, controls);
        overviewSplit.setDividerLocation(frame.getHeight() / 4);
//        overviewSplit.setResizeWeight(1);
        overviewSplit.setDividerSize(6);

        
        viewControlSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                canvas, overviewSplit);
        viewControlSplit.setDividerLocation(frame.getWidth()-250);
        viewControlSplit.setResizeWeight(0.8);
        viewControlSplit.setDividerSize(6);

        if (timeLine != null) {
            timeLineCanvas = new PCanvas();
            timeLineCanvas.setZoomEventHandler(null);
            timeLineCanvas.setPanEventHandler(null);
            timeLineCanvas.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
            timeLine.connect(canvas, timeLineCanvas);
            quiltManager.getSelectionManager().addChangeListener(timeLine);
            timeLineCanvas.getLayer().addChild(timeLine);
            
            timelineSplit = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    timeLineCanvas, viewControlSplit);
            timelineSplit.setDividerLocation((int)timeLineCanvas.getPreferredSize().getHeight());
            //timelineSplit.setResizeWeight(0.3);
            frame.getContentPane().add(timelineSplit, BorderLayout.CENTER);
            timelineSplit.setDividerSize(6);
        }
        else {
            frame.getContentPane().add(viewControlSplit);
        	if (timeLineCanvas != null) {
	            frame.getContentPane().add(viewControlSplit);
	            frame.getContentPane().remove(timeLineCanvas);
	            timeLine.disconnect();
	            timeLineCanvas = null;
	            timeLine = null;
        	}
        }
        frame.validate();
        // FIXME
        SwingUtilities.invokeLater(new Runnable() {
        	public void run() {
                bev.autoScale();
                bev.updateFromViewed();
            	GUIUtils.endLongComputation(frame);
        	}
        });
    }
    
    private PNode getCenterNode() {
        ArrayList<PNode> list = new ArrayList<PNode>();
        PBounds vb = canvas.getCamera().getViewBounds();
        quiltManager.findIntersectingNodes(
                vb,
                list);
        PNode best = null;
        double dist = Double.MAX_VALUE;
        for (PNode n : list) {
            if (n instanceof PVertex 
                    || n instanceof PEdge) {
                PBounds b = n.getFullBoundsReference();
                double d =
                        Math.hypot(
                                vb.getCenterX()-b.getCenterX(), 
                                vb.getCenterY()-b.getCenterY());
                if (d < dist) {
                    dist = d;
                    best = n;
                }
            }
        }
        return best;
    }
    
    /**
     * Turns filtering on or off.
     * @param filter
     */
    public void filterItems(boolean filter) {
    	
        if (getSelectionManager().isEmpty()) {
            filterBox.setSelected(false);
            //return;
        }
        
        float destFilteringParameter = filter ? 1 : 0;
        if (filteringParameter == destFilteringParameter)
        	return;
        
        if (doiManager == null) {
            doiManager = new DOIManager(getManager());
        }
        
        //
        
    	if (!filteringAnimationEnabled) {
       	 	startFiltering(destFilteringParameter);
	    	setFilteringParameter(destFilteringParameter);
	    	endFiltering();
    	} else {
    		PFilterAnimation activity = new PFilterAnimation(500, 50, this, destFilteringParameter);
    		canvas.getRoot().addActivity(activity);
    	}
    }
    
    /**
     * Called by PFilterAnimation. Don't call this yourself.
     */
    public void startFiltering(float destFilterParameter) {
        
    	if (destFilterParameter == 1) {
            doiManager.computeDOI();
    	}
    	
		saveCenter();
    }

    /**
     * Called by PFilterAnimation. Don't call this yourself.
     * @param filter if = 0, don't filter. If = 1, filter.
     */
    public void setFilteringParameter(float filter) {
    	if (filter == filteringParameter)
    		return;
    	
    	filteringParameter = filter;

        final double TINY_SCALE = .01;
        final double SMALL_SCALE = .5;
        final double MEDIUM_SCALE = .8;
        final double FULL_SCALE = 1;
        for (Vertex v : network.getVertices()) {
            double doi = v.getDOI();
            if (doi > 5) {
                setScale(v.getNode(), TINY_SCALE + (1 - TINY_SCALE) * (1 - filteringParameter));
            }
            else if (doi > 3) { 
                setScale(v.getNode(), SMALL_SCALE + (1 - SMALL_SCALE) * (1 - filteringParameter));
            }
            else if (doi > 1) { 
                setScale(v.getNode(), MEDIUM_SCALE + (1 - MEDIUM_SCALE) * (1 - filteringParameter));
            }
            else {
                setScale(v.getNode(), FULL_SCALE + (1 - FULL_SCALE) * (1 - filteringParameter));
            }
        }
            
        restoreCenter();
     }
    
    
    private static void setScale(PNode node, double scale) {
    	if (scale == 0) {
    		node.setVisible(false);
    	} else {
    		if (!node.getVisible())
    			node.setVisible(true);
    		if (node instanceof PIndi || node instanceof PFam || node instanceof PEdge)
    		node.setScale(scale);
    		//PBounds b = node.getFullBoundsReference();
    		//node.setBounds(b.x, b.y, b.width*scale, b.height*scale);
    	}
    }
    
    /**
     * Called by PFilterAnimation. Don't call this yourself.
     */
    public float getFilteringParameter() {
    	return filteringParameter;
    }
    
    /**
     * Called by PFilterAnimation. Don't call this yourself.
     */
    public void endFiltering() {
//       bev.autoScale();
       // if (centerNode != null)
        //    canvas.getCamera().animateViewToCenterBounds(centerNode.getFullBoundsReference(), false, 0);
    }
    
    void showNodeId(String id) {
        Vertex v = network.getVertex(id);
        if (v == null)
            return;
        PNode node = v.getNode(); 
        canvas.getCamera().animateViewToCenterBounds(node.getFullBounds(), false, 200);
    }
    
   
    /**
     * Search and select the specified string.
     * @param text the string to search
     * @param field the field to search into or null or "*" for all
     * @param flags the Pattern.compile flags.
     */
    public void selectSearch(String text, String field, int flags) {
        quiltManager.select(search(text, field, flags));
    }

    /**
     * Search and higligh nodes containing the specified text in
     * the text field.
     **/
    public void search() {
        search(searchField.getText(), (String)fieldList.getSelectedItem(), searchFlags);
    }
    
    /**
     * Search and higligh nodes containing the specified text in
     * the specified field. 
     * @param text the text to search, empty means reset
     * @param field the field to search in or null or "*" to mean all.
     * @param flags Pattern compilation flags
     * @return a collection of matching vertices
     */
    public Collection<Vertex> search(String text, String field, int flags) {
        SelectionManager selManager = getSelectionManager(); 
        if ("*".equals(field)) {
            field = null;
        }
        int found = 0;
        ArrayList<Vertex> selection = new ArrayList<Vertex>();
        Pattern p;
        if (text == null || text.length()==0) {
            p = null;
        }
        else {
            p = Pattern.compile(text, flags);
        }
        
        Color selColor = GUIUtils.multiplyAlpha(selManager.getNextSelectionColor(), 0.7f);
        
        for (Vertex v : network.getVertices()) {
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
//                if (indi.search(text, field)) {
                if (indi.matches(p, field)) {
                    found++;
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
        if (found == 0) {
            searchLabel.setText("Search");
//            if (savedSearchBounds != null) {
//                canvas.getCamera().animateViewToPanToBounds(
//                        savedSearchBounds,
//                        500);
//                savedSearchBounds = null;
//            }
        }
        else {
            searchLabel.setText("Search ("+found+")");
//            if (text.length()==1 && savedSearchBounds == null) {
//                savedSearchBounds = canvas.getCamera().getViewBounds();
//            }
            Vertex first = selection.get(0);
            canvas.getCamera().animateViewToPanToBounds(first.getNode().getFullBounds(), 200);
        }
        return selection;

    }
    
    private void clear() {
        if (quiltManager != null) {
    		getSelectionManager().clearSelections();
    		quiltManager.removeAllChildren();
    //		bgLayer.removeAllChildren();
    		mainLayer.removeAllChildren();
    //		fgLayer.removeAllChildren();
        }
        if (viewControlSplit != null) {
            frame.remove(viewControlSplit);
        }
        if (timelineSplit != null) {
        	frame.remove(timelineSplit);
        }
        if (timeLineCanvas != null) {
            timeLine = null;
            frame.getContentPane().remove(timeLineCanvas);
            timeLineCanvas = null;
        }
        frame.setTitle(TITLE);
//		bird.removeAll();
//		bird.setVisible(false);
//		bird.dispose();
		System.gc();
    }
    
    private static void resizeAndCenterWindow(JFrame window) {
    	
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	int w = (int)(screenSize.width * GraphicsConstants.MAIN_WINDOW_SIZE);
    	int h = (int)(screenSize.height * GraphicsConstants.MAIN_WINDOW_SIZE);
    	
    	if (w > GraphicsConstants.MAIN_WINDOW_MAX_WIDTH)
    		w = GraphicsConstants.MAIN_WINDOW_MAX_WIDTH;
    	if (h > GraphicsConstants.MAIN_WINDOW_MAX_HEIGHT)
    		h = GraphicsConstants.MAIN_WINDOW_MAX_HEIGHT;
    	
        window.setBounds(
        	(screenSize.width - w) / 2,
        	(screenSize.height - h) / 2,
        	w,
        	h
        );
    }
    
    // Note: these fields have been added to allow for benchmarking
    
    /**
     * @return the Framce 
     */
    public static JFrame getFrame() {
		return frame;
	}

    /**
     * @return the GeneaQuilt
     */
	public static GeneaQuilt getQuilt() {
		return quilt;
	}

	/**
	 * @return the QuiltManager
	 */
	public QuiltManager getManager() {
		return quiltManager;
	}

	/**
	 * @return the PCanvas
	 */
	public PCanvas getCanvas() {
		return canvas;
	}

	/**
	 * @return the BirdsEyeView
	 */
	public BirdsEyeView getBev() {
		return bev;
	}
	
	/**
	 * @return the SelectionManager
	 */
    public SelectionManager getSelectionManager() {
        return getManager().getSelectionManager();
    }
    
    /**
     * Prints.
     */
    public void simplePrint() {
    	GraphicsConstants.instance = new PrintConstants();
    	quiltManager.rebuild();
    	PrintUtilities.printComponent(canvas);
    	GraphicsConstants.instance = new GraphicsConstants();
    	quiltManager.rebuild();
    }

}
