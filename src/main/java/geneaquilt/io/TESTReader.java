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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 * Class TESTReader
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class TESTReader {
    private HashMap<String,Vertex> vertex;
    private Network net;

    /**
     * Creates a TESTReader
     */
    public TESTReader() {
    }
    
    /**
     * Load the file.
     * @param filename the filename
     * @return the network or null
     */
    public Network load(String filename) {
        try {
            URL url = GEDReader.class.getClassLoader().getResource(filename);
            InputStream bin = url.openStream();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(bin, "UTF-8"));
            
            net = new Network();
            vertex = new HashMap<String, Vertex>();
            
            String line;
            while ((line = in.readLine())!= null) {
                String[] fields = line.split(" ");
                Vertex v = findVertex(fields[0]);
                for (int i = 1; i < fields.length; i++) {
                    Vertex other = findVertex(fields[i]);
                    Edge e = new Edge(other.getId(), v.getId());
                    e.setFromVertex(other);
                    e.setToVertex(v);
                    net.addEdge(e, other, v);
                }
            }
            return net;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Vertex findVertex(String id) {
        Vertex v = vertex.get(id);
        if (v == null) {
            if (id.startsWith("F")) {
                v = new Fam();
            }
            else {
                Indi i = new Indi();
                i.setName(id);
                v = i;
            }
            v.setId(id);
            vertex.put(id, v);
            net.addVertex(v);
        }
        return v;
    }
    
    
}
