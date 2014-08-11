/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.io;

import geneaquilt.data.Edge;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Class DOTWriter
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class DOTWriter {
    Network network;
    boolean bare;
    TreeMap<Object, Collection<Vertex>> ranks;
    /** Key for generation data */
    static public String GENERATION_KEY = "GENERATION";
    
    /**
     * Creates a writer from a network
     * 
     * @param network
     *            the network
     */
    public DOTWriter(Network network) {
        this.network = network;
    }

    /**
     * Writes in the specified file
     * @param filename the file name
     * @throws IOException
     */
    public void write(String filename) throws IOException {
        PrintWriter out = new PrintWriter(filename);
        try {
            write(out);
        }
        finally {
            out.close();
        }
    }
    
    /**
     * Writes in the specified printwriter.
     * @param out the print writer
     * @throws IOException
     */
    public void write(PrintWriter out) throws IOException {
        ranks = new TreeMap<Object, Collection<Vertex>>();
        out.println("digraph genealogy {");
        if (bare) {
            out.println("ranksep=1;");
            out.println("node [shape=point,width=0,height=0,label=\"\"];");
            out.println("edge [style=invis];");
        }
        else {
            out.println("node [shape=box];");
        }
        HashSet<Vertex> vertices = new HashSet<Vertex>(network.getVertices());
        if (! bare) {
            for (Vertex v : vertices) {
                if (v instanceof Indi) {
                    Indi indi = (Indi) v;
                    out.println(v.getId() + " [label=\"" + indi.getName() + "\"];");
                }
            }
            vertices.clear();
        }
        for (Edge e : network.getEdges()) {
            Vertex source = network.getSource(e);
            Vertex dest = network.getDest(e);
            
            addRank(source);
            addRank(dest);
            vertices.remove(source);
            vertices.remove(dest);
            out.print('"');
            out.print(source.getId());
            out.print('"');
            out.print("->");
            out.print('"');
            out.print(dest.getId());
            out.print('"');
            out.print(';');
            out.println();
        }
        for (Vertex v : vertices) {
            addRank(v);
            out.print('"');
            out.print(v.getId());
            out.print('"');
            out.print(';');
            out.println();            
        }
        
        for (Collection<Vertex> sameRank : ranks.values()) {
            if (sameRank.size() != 1) {
                out.print("{ rank=same;");
                for (Vertex v : sameRank) {
                    out.print(' ');
                    out.print('"');
                    out.print(v.getId());
                    out.print('"');
                }
                out.println(";}");
            }
        }
        
        out.println('}');
        out.close();
        ranks = null;
    }

    private void addRank(Vertex source) {
        Object gen = source.getProperty(GENERATION_KEY); 
        if (gen != null) {
            Collection<Vertex> sameRank = ranks.get(gen);
            if (sameRank == null) {
                sameRank = new HashSet<Vertex>();
                ranks.put(gen, sameRank);
            }
            sameRank.add(source);
        }
    }

    /**
     * @return the bare
     */
    public boolean isBare() {
        return bare;
    }

    /**
     * @param bare the bare to set
     */
    public void setBare(boolean bare) {
        this.bare = bare;
    }

    /**
     * @return the generationKey
     */
    public String getGenerationKey() {
        return GENERATION_KEY;
    }

    /**
     * @param generationKey the generationKey to set
     */
    public static void setGenerationKey(String generationKey) {
        GENERATION_KEY = generationKey;
    }
    
}
