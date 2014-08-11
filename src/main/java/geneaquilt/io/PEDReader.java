/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.io;

import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Class PEDReader
 * 
 * See http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml
 *  
 * @author Jean-Daniel Fekete
 */
public class PEDReader {
    private Network network;
    private String[] tmpLine = new String[7];
    private static final String DELIMS = " \t";
    
    /**
     * Creates a PED Reader.
     */
    public PEDReader() { }
    
    private Indi findIndi(String fam, String id) {
        if (id.equals("0") || id.equals("."))
            return null;
        id = "I"+fam+"_"+id;
        Indi indi = (Indi)network.getVertex(id);
        if (indi == null) {
            indi = new Indi();
            indi.setId(id);
            network.addVertex(indi);
        }
        return indi;
    }
    
    private Fam findFam(String f, String wife, String husb) {
        Indi w = null;
        Indi h = null;
        
        if (wife.equals("0") || wife.equals(".")) {
            wife = null;
        }
        else {
            w = findIndi(f, wife);
        }
        if (husb.equals("0") || husb.equals(".")) {
            husb = null;
        }
        else {
            h = findIndi(f, husb);
        }
        
        if (wife == null && husb == null)
            return null;
        
        String id = "F"+f+"_"+(wife==null?"unknown":wife)+"_"+(husb==null?"unkown":husb);
        Fam fam = (Fam)network.getVertex(id);
        if (fam == null) {
            fam = new Fam();
            fam.setId(id);
            fam.setHusb(h.getId());
            h.addFams(fam.getId());
            fam.setWife(w.getId());
            w.addFams(fam.getId());
            network.addVertex(fam);
        }
        return fam;
    }
    
    private String getSex(String s) {
        if (s == null) return null;
        if (s.equals("1")) return "M";
        if (s.equals("2")) return "F";
        return null;
    }
    
    private static int nextDelim(String line, int offset) {
        int len = line.length();
        for (int i = offset; i < len; i++) {
            char c = line.charAt(i);
            if (DELIMS.indexOf(c) != -1)
                return i;
        }
        return -1;
    }
    
    private static int skipDelims(String line, int offset) {
        while (DELIMS.indexOf(line.charAt(offset)) != -1)
            offset++;
        return offset;
    }
    
    private String[] split(String line) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#") || line.equals("end"))
            return null;
        int last = skipDelims(line, 0);
        for (int i = 0; i < 6; i++) {
            int next = nextDelim(line, last);
            if (next == -1) {
                tmpLine[i++] = line.substring(last);
                while (i < 7) {
                    tmpLine[i++] = null;
                }
                return tmpLine;
            }
            else 
                tmpLine[i] = line.substring(last, next);
            last = skipDelims(line, next+1);
        }
        tmpLine[6] = line.substring(last);
        return tmpLine;
    }

    /**
     * Loads a network.
     * @param filename the file name
     * @return a network
     */
    public Network load(String filename) {
        try {
            FileReader fin = new FileReader(filename);
            BufferedReader in = new BufferedReader(fin);
            network = new Network();
            
            String line;
            int lineNum = 0;
            while ((line = in.readLine()) != null) {
                lineNum++;
                String[] field = split(line);
                if (field == null)
                    continue;
                String f = field[0];
                Indi indi = findIndi(f, field[1]);
                assert(indi.getFamc() == null);
                indi.setProperty("FAMILY", field[0]);
                indi.setName(field[1]);
                indi.setSex(getSex(field[4]));
                if (field[5] != null) {
                    indi.setProperty("PHENOTYPE", field[5]);
                }
                if (field[6] != null) {
                    indi.setProperty("DATA", field[6]);
                }
                Fam fam = findFam(f, field[3], field[2]);
                if (fam != null) {
                    indi.setFamc(fam.getId());
                    fam.addChil(indi.getId());
                }
            }
            in.close();
            fin.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        for (Vertex v : network.getVertices()) {
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
                if (indi.getFamc() != null) {
                    Vertex fam = network.getVertex(indi.getFamc());
                    if (fam != null) {
                        Edge edge = new Edge(v.getId(), indi.getFamc());
                        if (!network.containsEdge(edge)) {
                            network.addEdge(edge, indi, fam);
                            edge.setFromVertex(v);
                            edge.setToVertex(fam);
                        }
                    }
                }
                if (indi.getFams() != null) {
                    for (String fid : indi.getFams()) {
                        Vertex fam = network.getVertex(fid);
                        if (fam != null) {
                            Edge edge = new Edge(fam.getId(), v.getId());
                            if (!network.containsEdge(edge)) {
                                network.addEdge(edge, fam, indi);
                                edge.setFromVertex(fam);
                                edge.setToVertex(indi);
                            }
                        }
                    }
                }
            }
        }
        return network;
    }
}
