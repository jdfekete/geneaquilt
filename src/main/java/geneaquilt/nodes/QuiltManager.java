/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.nodes;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import geneaquilt.algorithms.GenerationRank;
import geneaquilt.data.DateRange;
import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.hull.Hull;
import geneaquilt.io.DOTWriter;
import geneaquilt.selection.Selection;
import geneaquilt.selection.SelectionManager;
import geneaquilt.utils.PiccoloUtils;

/**
 * Class Generation
 * 
 * @author Jean-Daniel Fekete
 * TODO Fix the layers, Fam/Indi levels get mixed-up
 */
public class QuiltManager extends PNode {
    private static final Logger LOG = Logger.getLogger(QuiltManager.class); 
    private Network network;
    private List<Indi>[] individual;
    private List<Fam>[] family;
    private IndiGeneration[] indiGeneration;
    private FamGeneration[] famGeneration;
    private int layerCount;
    private double xPad = 10;
    private double yPad = 10;
    private PNode grids;
    private PNode edges;
    private SelectionManager selectionManager;
    private boolean familyFirst;
    private Hull hull;

    /**
     * Creates a Quilt manager and checks for sanity
     * @param network the network
     * @param bg the background node
     * @param fg the foreground node
     */
    public QuiltManager(Network network) {
        this.network = network;
//        network.breakCycles();
        assignLayers();
        if (network.isLayerComputed()) {
            LOG.info("Layers already assigned");
            updateSortedLayers();
        }
        else {
            sortLayers();
        }
        
        addChildren();
        
        // The following line is optional (only for debug)
        //addChild(hull);        
    }
    
    /**
     * Sets the selection manager
     * @param selectionManager the new selection manager
     */
    public void setSelectionManager(SelectionManager selectionManager) {
    	this.selectionManager = selectionManager;
    }
    
    protected void addChildren() {
        grids = new PNode();
        addChild(grids);
        createGenerations();
        
        edges = new PNode();
        for (Edge edge : network.getEdges()) {
            edges.addChild(edge.getNode());
        }
        addChild(edges);
        
        hull = new Hull(this);
        hull.createBins();
    }

    /**
     * Cleanup and rebuild the network
     */
    public void rebuild() {
    	for (Vertex v : network.getVertices())
    		v.deleteNode();
    	for (Edge e : network.getEdges())
    		e.deleteNode();
    	removeAllChildren();
    	addChildren();
    }
    
    /**
     * @return the network
     */
    public Network getNetwork() {
    	return network;
    }
    
    /**
     * @return the individual generations
     */
    public IndiGeneration[] getIndiGenerations() {
    	return indiGeneration;
    }
    
    /**
     * @return the family generations
     */
    public FamGeneration[] getFamGenerations() {
    	return famGeneration;
    }
    
    /**
     * @return the selection manager
     */
    public SelectionManager getSelectionManager() {
    	return selectionManager;
    }
    
    /**
     * Selects the list of vertices
     * @param vertices the list
     */
    public void select(Collection<Vertex> vertices) {
        SelectionManager selManager = getSelectionManager(); 
        int colorindex = selManager.getNextSelectionColorIndex();
        for (Vertex v : vertices) {
            selManager.setNextSelectionColorIndex(colorindex);
            Selection currentSelection = selManager.select(v.getNode());
            if (currentSelection != null)
                currentSelection.setHighlightMode(
                        Selection.HighlightMode.HIGHLIGHT_NONE);
        }
    }
    

    /**
     * 
     * @return the hull
     */
    public Hull getHull() {
    	return hull;
    }
    
    private void createGenerations() {
        indiGeneration = new IndiGeneration[individual.length];
        famGeneration = new FamGeneration[family.length];
        for (int g = 0; g < individual.length; g++) {
            List<Indi> layer = individual[g];
            if (layer != null) {
                IndiGeneration gen = new IndiGeneration(layer);
                indiGeneration[g] = gen;
                addChild(gen);
            }
        }
        for (int g = 0; g < family.length; g++) {
            List<Fam> layer = family[g];
            if (layer != null) {
                FamGeneration gen = new FamGeneration(layer);
                famGeneration[g] = gen;
                addChild(gen);
          
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void layoutChildren() {
        double x = 0;
        double y = 0;
        if (familyFirst) {
            for (int g = 0; g < famGeneration.length; g++) {
                FamGeneration fam = famGeneration[g];
                if (fam != null) {
                	PiccoloUtils.setLocation(fam, x, y, true);
                    x += fam.getFullBoundsReference().getWidth() + xPad*0.5;
                    y += fam.getFullBoundsReference().getHeight() + yPad;
                }
                IndiGeneration indi = indiGeneration[g];
                if (indi != null) {
                	PiccoloUtils.setLocation(indi, x, y, true);
                    x += indi.getWidth() + xPad*0.5;
                    y += indi.getFullBoundsReference().getHeight() + yPad;
                }
            }
        }
        else {
            for (int g = 0; g < famGeneration.length; g++) {
                IndiGeneration indi = indiGeneration[g];
                if (indi != null) {
                    x -= xPad*0.5;
                    y -= yPad*1;
                	PiccoloUtils.setLocation(indi, x, y, true);
                    y += indi.getFullBoundsReference().getHeight() + xPad*0.5;
                    x += indi.getFullBoundsReference().getWidth() + yPad*0.5;
                }
                FamGeneration fam = famGeneration[g];
                if (fam != null) {
                    y -= yPad*0.5;
                	PiccoloUtils.setLocation(fam, x, y, true);
                    x += fam.getFullBoundsReference().getWidth() + xPad;
                    y += fam.getFullBoundsReference().getHeight() + yPad;
                }
            }
        }
        
        for (Edge edge : network.getEdges()) {
            edge.getNode().updateBounds();
        }
        grids.removeAllChildren();
        for (int g = 0; g < indiGeneration.length; g++) {
            PNode grid = createIndiLines(g);
            if (grid != null)
                grids.addChild(grid);
        }
        for (int g = 0; g < famGeneration.length; g++) {
            PNode grid = createFamLines(g);
            if (grid != null)
                grids.addChild(grid);
        }
        
        setBounds(0, 0, x, y);
        
        hull.updateShape();
        hull.setBounds(0, 0, x, y); // optional -- only needed if hull is added to the scenegraph for debugging
    }
    
    private void assignLayers() { 
        LOG.debug("Entering assignLayers");
        if (! network.isLayerComputed()) {
            GenerationRank rank = new GenerationRank(network);
            rank.compute();
        }
//            LayerRank layer = new LayerRank(network);
//            layer.computeRanks();
//            for (Vertex v : network.getVertices()) {
//                network.setVertexLayer(v, layer.getRank(v));
//                if (v.getLayer() < 0)
//                    LOG.error("Vertex "+v+" has no layer");
//            }
//        }
        
        layerCount = network.getMaxLayer()+1;
        fixLayers();
//        fixLayersByDate();
        this.individual = new ArrayList[(layerCount+1)/2];
        this.family = new ArrayList[(layerCount+1)/2];
        for (Vertex v : network.getVertices()) {
            int l = v.getLayer()/2;
            if (l < 0) {
                LOG.error("Negative layer");
                l = 0;
            }
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
                if (l >= individual.length) {
                    List<Indi>[] n = new ArrayList[l+1];
                    System.arraycopy(individual, 0, n, 0, individual.length);
                    individual = n;
                }
                List<Indi> i = individual[l]; 
                    
                if (i == null) {
                    i = new ArrayList<Indi>();
                    individual[l] = i;
                    if (v.getLayer() == 0) {
                        familyFirst = false;
                    }
                }
                i.add(indi);
            }
            else if (v instanceof Fam) {
                Fam fam = (Fam) v;
                if (l >= family.length) {
                    List<Fam>[] n = new ArrayList[l+1];
                    System.arraycopy(family, 0, n, 0, family.length);
                    family = n;
                }
                List<Fam> f = family[l];
                if (f == null) {
                    f = new ArrayList<Fam>();
                    family[l] = f;
                    if (v.getLayer() == 0) {
                        familyFirst = true;
                    }
                }
                f.add(fam);
            }
            else {
                LOG.error("Unexpected type of vertex "+v);
            }
        }
        LOG.debug("Leaving assignLayers");
    }

    private void fixLayers() {
        int compCount = getComponentCount();
        if (compCount < 2)
            return;
        LOG.debug("Fixing layers");
        int[] depth = new int[compCount];
        Vertex[] last = new Vertex[compCount];
        int deepest = -1;
        int maxDepth = 0;
        for (int c = 0; c < compCount; c++) {
            int max = 0;
            int min = layerCount;
            for (Vertex v : getComponent(c)) {
                int layer = v.getLayer(); 
                if (layer > max) {
                    max = v.getLayer();
                    last[c] = v;
                }
                else if (layer < min) {
                    min = layer;
                }
            }
            depth[c] = max - min + 1;
            if (depth[c] > maxDepth) {
                deepest = c;
                maxDepth = depth[c];
            }
        }
        boolean lastIsFamily = (last[deepest] instanceof Fam); 
        for (int c = 0; c < compCount; c++) {
            if (c == deepest)
                continue;
            if (lastIsFamily != (last[c] instanceof Fam)) {
                for (Vertex v : getComponent(c)) {
                    v.setLayer(v.getLayer()-1);
                }
            }
        }
    }
    
    private void fixLayersByDate() {
        int compCount = getComponentCount();
        if (compCount < 2)
            return;
        LOG.debug("Fixing layers by date");
        List<SortedMap<Integer,DateRange>> layerDate = computeLayerDate(compCount);
        List<Set<Vertex>> layerVertex = new ArrayList<Set<Vertex>>(network.getComponents());
        // Filter out components with no date
//        for (int i = 0; i < layerDate.size(); i++) {
//            if (layerDate.get(i) == null) {
//                layerVertex.remove(i);
//                layerDate.remove(i);
//                LOG.debug("Removed layer "+i+" with no date");
//                i--;
//            }
//        }
        
        while (layerDate.size() > 1) {
            BestMatch bestMatch = computeBestMatch(layerDate);
            if (bestMatch == null)
                break; // no more improvements
            LOG.debug(
                    "Merging best match bewteen layers("+bestMatch.layer1+", "+bestMatch.layer2+") "
                    +"with delta "+bestMatch.delta);
            mergeLayers(layerDate, bestMatch, layerVertex);
        }

        LOG.debug("Ending with "+layerDate.size()+" disconnected components");
        fixMinMax(layerDate, layerVertex);
    }

    private void fixMinMax(
            List<SortedMap<Integer, DateRange>> layerDate, 
            List<Set<Vertex>> layerVertex) {
        LOG.debug("Fixing min and max layers");
        boolean bumpToMax = true;
        
        assert(layerDate.size()==layerVertex.size());
        int maxCount = layerCount;
        int[] min = new int[layerDate.size()];
        int[] max = new int[layerDate.size()];
        int i = 0;
        for (Set<Vertex> s : layerVertex) {
            min[i] = Integer.MAX_VALUE;
            max[i] = Integer.MIN_VALUE;
            for (Vertex v : s) {
                if (v.getProperty(DOTWriter.GENERATION_KEY) != null) {
                    min[i] = 0;
                    max[i] = maxCount-1;
                    break;
                }
                int l = v.getLayer();
                min[i] = Math.min(min[i], l);
                max[i] = Math.max(max[i], l);
            }
            int count = (max[i] - min[i]) + 1;
            if (count > maxCount) {
                maxCount = count;
            }
            if (min[i] < 0) {
                LOG.debug("Layer "+i+" has min="+min[i]);
            }
            i++;
        }
        LOG.debug("New layers count "+maxCount+" (was "+layerCount+")");
        for (i = 0; i < layerDate.size(); i++) {
            Set<Vertex> s = layerVertex.get(i);
            int offset;
            if (bumpToMax) {
                offset = maxCount - max[i] - 1; 
            }
            else {
                offset = -min[i];
            }
            if ((min[i]+offset) < 0) {
                offset = -min[i];
            }
            LOG.debug("Moving comp "+i+" by "+offset);
            if (offset != 0) {
                for (Vertex v : s) {
                    v.setLayer(v.getLayer()+offset);
                }
            }
        }
        layerCount = maxCount;
    }

    private List<SortedMap<Integer, DateRange>> computeLayerDate(int compCount) {
        ArrayList<SortedMap<Integer, DateRange>> layerDate = 
            new ArrayList<SortedMap<Integer,DateRange>>(compCount);
        for (int c = 0; c < compCount; c++) {
            SortedMap<Integer,DateRange> l = new TreeMap<Integer,DateRange>();
            Set<Vertex> comp = getComponent(c);
            for (Vertex v : comp) {
                if (v instanceof Fam) {
                    continue; // only align indi generations
                }
                if (v.getProperty(DOTWriter.GENERATION_KEY) != null) {
                    // don't move components containing nodes with a specified generation
                    l.clear();
                    break;
                }
                DateRange d = v.getDateRange();
                if (!d.isValid())
                    continue;
                Integer layer = new Integer(v.getLayer());
                DateRange dr = l.get(layer);
                if (dr == null) {
                    dr = new DateRange(d);
                    l.put(layer, dr);
                }
                else {
                    dr.union(d);
                }
            }
            if (l.isEmpty())
                layerDate.add(null);
            else
                layerDate.add(l);
        }
        return layerDate;
    }
    
    static class BestMatch {
        int layer1;
        int layer2;
        int delta;
    }
    
    private BestMatch computeBestMatch(List<SortedMap<Integer, DateRange>> layerDate) {
        LOG.debug("Computing best match for "+layerDate.size()+" layers");
        BestMatch bm = new BestMatch();
        int bestLayer1 = -1;
        int bestLayer2 = -1;
        int bestDelta = 0;
        long bestDist = Long.MAX_VALUE;
        
        int n = layerDate.size();
        
        for (int layer1 = 0; layer1 < n && bestDist != 0;layer1++) {
            for (int layer2 = layer1+1; layer2 < n; layer2++) {
//                LOG.debug("computing distance between ("+layer1+", "+layer2+")");
                long dist = computeDistance(layerDate.get(layer1), layerDate.get(layer2), bm);
//                LOG.debug(" distance="+dist);
                if (dist < bestDist) {
//                    LOG.debug(" best so far");
                    bestDist = dist;
                    bestLayer1 = layer1;
                    bestLayer2 = layer2;
                    bestDelta = bm.delta;
                    if (bestDist == 0)
                        break;
                }
            }
        }
        if (bestLayer1 == -1)
            return null;
        bm.layer1 = bestLayer1;
        bm.layer2 = bestLayer2;
        bm.delta = bestDelta;
//        LOG.debug("Best distance is "+bestDist);
        return bm;
    }
    
    private long computeDistance(SortedMap<Integer, DateRange> l1, SortedMap<Integer, DateRange> l2, BestMatch bm) {
        long bestDist = Long.MAX_VALUE;
        if (l1 == null || l2 == null) {
            bm.delta = 0;
            return bestDist;
        }
        
        Iterator<Entry<Integer,DateRange>> 
            iter1 = l1.entrySet().iterator(),
            iter2 = l2.entrySet().iterator();
        Entry<Integer,DateRange> e1 = null, e2 = null;
        boolean need1 = true, need2 = true;
        while(true) {
            if (need1) {
                if (iter1.hasNext()) {
                    e1 = iter1.next();
                    need1 = false;
                }
                else {
                    break;
                }
            }
            if (need2) {
                if (iter2.hasNext()) {
                    e2 = iter2.next();
                    need2 = false;
                }
                else {
                    break;
                }
            }
            int layer1 = e1.getKey().intValue();
            DateRange dr1 = e1.getValue();
            int layer2 = e2.getKey().intValue();
            DateRange dr2 = e2.getValue();

            if (!dr1.isValid()) {
                need1 = true;
            }
            else if (!dr2.isValid()) {
                need2 = true;
            }
            else {
                long rel = dr1.getCenter() - dr2.getCenter();
                long dist = Math.abs(rel);
                if (dist < bestDist) {
                    bestDist = dist;
                    bm.delta = layer1-layer2;
                    if (dist == 0) 
                        break;
                }
                if (rel < 0) {
                    need1 = true;
                }
                else {
                    need2 = true;
                }
            }
        }
        return bestDist;
    }
    
    private void mergeLayers(
            List<SortedMap<Integer, DateRange>> layerDate,
            BestMatch bm,
            List<Set<Vertex>> layerVertex) {
        SortedMap<Integer, DateRange> l = layerDate.get(bm.layer1);
        for (Entry<Integer, DateRange> e : layerDate.get(bm.layer2).entrySet()) {
            Integer nl = new Integer(e.getKey().intValue()+bm.delta);
            assert(nl.intValue() >= 0);
            DateRange dr = l.get(nl);
            if (dr == null)
                l.put(nl, e.getValue());
            else {
                dr.union(e.getValue()); // merge
            }
        }
        layerDate.remove(bm.layer2);

        // update the vertex layers
        Set<Vertex> comp1 = new HashSet<Vertex>(layerVertex.get(bm.layer1));
        for (Vertex v : layerVertex.get(bm.layer2)) {
            int layer = v.getLayer()+bm.delta;
            v.setLayer(layer);
            comp1.add(v);
        }
        layerVertex.remove(bm.layer2);
    }
    
    double distance(DateRange d1, DateRange d2) {
        if (d1 == null || d2 == null)
            return Double.POSITIVE_INFINITY;
        return d1.distanceToCenter(d2);
    }
    
    
    private int getComponentCount() {
        return network.getComponentCount();
    }
    
    private Set<Vertex> getComponent(int c) {
        return network.getComponents().get(c);
    }
        
    private static class VertexComparator implements Comparator<Vertex> {
        public boolean changed = false;
        public int compare(Vertex o1, Vertex o2) {
            int ret = o1.getComponent()-o2.getComponent();
            if (ret != 0)
                return ret;
            ret = (int)Math.signum(o1.getX()-o2.getX());
            if (ret > 0)
                changed = true;
            return ret;
        }
    }
    /** The comparator used to order the vertices in Y */
    public final static VertexComparator COMPARATOR = new VertexComparator();

    private void sortLayers() {
        Vertex[][] layers = new Vertex[layerCount][];
        int famOffset = (familyFirst) ? 0 : 1;
        
        for (int g = 0; g < family.length; g++) {
            if (family[g] == null) continue;
            Vertex[] layer = new Vertex[family[g].size()];
            family[g].toArray(layer);
            layers[2*g+famOffset] = layer;
        }
        
        for (int g = 0; g < individual.length; g++) {
            if (individual[g]==null) continue;
            Vertex[] layer = new Vertex[individual[g].size()];
            individual[g].toArray(layer);
            layers[2*g+1-famOffset] = layer;
        }
        
        // initialize the positions
        initPositions(layers);
        boolean changed = true;
        int maxIter = 100;
        while (changed & maxIter-- > 0) {
            for (int g = 0; g < (layerCount-1); g++) {
                updateBarycenterUp(layers[g]);
            }
            for (int g = layerCount-1; g > 1; g--) {
                updateBarycenterDown(layers[g]);
            }
            COMPARATOR.changed = false;
            for (int g = 0; g < layerCount; g++) {
                Arrays.sort(layers[g], COMPARATOR);
            }
            changed = COMPARATOR.changed;
            initPositions(layers);
        }
        
//        for (int g = 0; g < family.length; g++) {
//            if (family[g] == null) continue;
//            Vertex[] layer = layers[2*g+famOffset];
//            List<Fam> fam = family[g];
//            for (int i = 0; i < layer.length; i++) {
//                fam.set(i, (Fam)layer[i]);
//            }
//        }
//        
//        for (int g = 0; g < individual.length; g++) {
//            if (individual[g]==null) continue;
//            Vertex[] layer = layers[2*g+1-famOffset];
//            List<Indi> indi = individual[g];
//            for (int i = 0; i < layer.length; i++) {
//                indi.set(i, (Indi)layer[i]);
//            }
//        }
        updateSortedLayers();
    }
    
    /**
     * @return the layerCount
     */
    public int getLayerCount() {
        return layerCount;
    }
    
    private void updateSortedLayers() {
        for (int g = 0; g < family.length; g++) {
            if (family[g] == null) continue;
            List<Fam> fam = family[g];
            Collections.sort(fam, COMPARATOR);
        }
        
        for (int g = 0; g < individual.length; g++) {
            if (individual[g]==null) continue;
            List<Indi> indi = individual[g];
            Collections.sort(indi, COMPARATOR);
        }
        
    }
    
    private void updateBarycenterUp(Vertex[] layer) {
        for (int i = 0; i < layer.length; i++) {
            Vertex v = layer[i];
            double barycenter = barycenter(network.getPredecessors(v));
            if (barycenter != Double.NaN)
                v.setX(barycenter);
        }
    }
    
    private void updateBarycenterDown(Vertex[] layer) {
        for (int i = 0; i < layer.length; i++) {
            Vertex v = layer[i];
            double barycenter = barycenter(network.getSuccessors(v));
            if (barycenter != Double.NaN)
                v.setX(barycenter);
        }
    }
    
    private static double barycenter(Collection<Vertex> vertices) {
        double d = 0;
        int n = 0;
        for (Vertex v : vertices) {
            d += v.getX();
            n++;
        }
        if (n == 0) 
            return Double.NaN;
        return d/n;
    }

    private void initPositions(Vertex[][] layers) {
        for (int g = 0; g < layerCount; g++) {
            Vertex[] layer = layers[g];
            for (int i = 0; i < layer.length; i++) {
                layer[i].setX(i);
            }
        }
    }
    
    private PNode createIndiLines(int g) {
    	last_line.setLine(0, 0, 0, 0);
    	double line_spacing_offset = (1 - GraphicsConstants.INDI_LINE_SPACING) * 0.5;
        IndiGeneration gen = indiGeneration[g];
        if (gen == null)
            return null;
        PBounds bounds = gen.getFullBounds();
        HashMap<PNode,PBounds> indiBounds = new HashMap<PNode, PBounds>();
        PBounds prevB = null; 
        for (Object o : gen.getChildrenReference()) {
            PIndi pindi = (PIndi)o;
            if (pindi.getVisible()) {
	            PBounds b = pindi.getFullBounds();
	            Indi indi = pindi.getIndi();
	            for (Edge edge : network.getIncidentEdges(indi)) {
	                PBounds edgeBounds = edge.getNode().getFullBoundsReference();
	                b.add(edgeBounds);
	                bounds.add(edgeBounds);
	            }
	            indiBounds.put(pindi, b);
	            if (prevB == null)
	                prevB = b;
            }
        }
        if (prevB == null) 
            return null;
        PNode lines = new PNode();
        Line2D.Double line = new Line2D.Double(
                prevB.getMinX(), prevB.getMinY() + line_spacing_offset * prevB.getHeight(),
                prevB.getMaxX(), prevB.getMinY() + line_spacing_offset * prevB.getHeight());
        addGridLine(lines, line);
        for (int i = 1; i < gen.getChildrenCount(); i++) {
            PIndi pindi = (PIndi)gen.getChild(i);
            if (pindi.getVisible()) {
	            PBounds b = indiBounds.get(pindi);
	            line.x1 = Math.min(prevB.getMinX(), b.getMinX());
	            line.x2 = Math.max(prevB.getMaxX(), b.getMaxX());
	            line.y1 = line.y2 = b.getMinY() + line_spacing_offset * b.getHeight();//(prevB.getCenterY() + b.getCenterY())/2;
	            addGridLine(lines, line);
	            prevB = b;
            }
        }
        line.setLine(
                prevB.getMinX(),
                prevB.getMaxY() - line_spacing_offset * prevB.getHeight(),
                prevB.getMaxX(),
                prevB.getMaxY() - line_spacing_offset * prevB.getHeight());
        addGridLine(lines, line);
        return lines;
    }
    
    private PNode createFamLines(int g) {
    	last_line.setLine(0, 0, 0, 0);
    	double line_spacing_offset = (1 - GraphicsConstants.INDI_LINE_SPACING) * 0.5;
        FamGeneration gen = famGeneration[g];
        if (gen == null)
            return null;
    //    PBounds bounds = gen.getFullBounds();
        HashMap<PNode,PBounds> famBounds = new HashMap<PNode, PBounds>();
        PBounds prevB = null; 
        for (Object o : gen.getChildrenReference()) {
            PFam pfam= (PFam)o;
            if (pfam.getVisible()) {
	            PBounds b = pfam.getFullBounds();
	            Fam fam = pfam.getFam();
	            for (Edge edge : network.getIncidentEdges(fam)) {
	                PBounds edgeBounds = edge.getNode().getFullBoundsReference();
	                double offset = line_spacing_offset * edgeBounds.getHeight();
	          //      bounds.add(edgeBounds.x, edgeBounds.y + offset);
	          //      bounds.add(edgeBounds.x + edgeBounds.width, edgeBounds.y + edgeBounds.height + offset);
	                b.add(edgeBounds.x, edgeBounds.y + offset);
	                b.add(edgeBounds.x + edgeBounds.width, edgeBounds.y + edgeBounds.height - offset);
	            }
	            famBounds.put(pfam, b);
	            if (prevB == null)
	                prevB = b;
            }
        }
        if (prevB == null)
            return null;
        PNode lines = new PNode();
        Line2D.Double line = new Line2D.Double(
                prevB.getMinX(), prevB.getMinY(),
                prevB.getMinX(), prevB.getMaxY());
        addGridLine(lines, line);

        for (int i = 1; i < gen.getChildrenCount(); i++) {
            PFam pfam = (PFam)gen.getChild(i);
            if (pfam.getVisible()) {
	            PBounds b = famBounds.get(pfam);
	            line.y1 = Math.min(prevB.getMinY(), b.getMinY());
	            line.y2 = Math.max(prevB.getMaxY(), b.getMaxY());
	            line.x1 = line.x2 = b.getMinX();//(prevB.getCenterX() + b.getCenterX())/2;
	            addGridLine(lines, line);
	            prevB = b;
            }
        }
        line.setLine(
                prevB.getMaxX(),
                prevB.getMinY(),
                prevB.getMaxX(),
                prevB.getMaxY());
        addGridLine(lines, line);
        return lines;
    }
       
    protected void addGridLine(PNode parent, Line2D line) {
    	PSemanticPath path = newGridLine(line);
    	if (path != null)
    		parent.addChild(path);
    }
    
    Line2D last_line = new Line2D.Double();

    protected PSemanticPath newGridLine(Line2D line) {
    	if (line.equals(last_line))
    		return null;
    	last_line.setLine(line);
    	PSemanticPath gridline = new PSemanticPath(line);
    	gridline.setMinimumScreenStrokeWidth(1);
    	gridline.setStroke(GraphicsConstants.instance.gridStroke());
    	gridline.setStrokePaint(GraphicsConstants.instance.gridColor());
    	gridline.setSmallStrokePaint(GraphicsConstants.GRID_COLOR_SMALL, 0.4f);
    	return gridline;
    }

    /**
     * {@inheritDoc}
     */
    public void paint(PPaintContext aPaintContext) {
        super.paint(aPaintContext);
    }
}
