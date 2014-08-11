package geneaquilt.io;

import geneaquilt.data.Edge;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class JSONWriter
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class JSONWriter {
    Network network;
    /** Key for generation data */
    static public String GENERATION_KEY = "GENERATION";
    static private Pattern INTEGER = Pattern.compile("[0-9]+");
    static private Pattern JSON_CONTROLS = Pattern.compile("[\"|\\|\b|\n|\r|\t]");
    
    /**
     * Creates a writer from a network
     * 
     * @param network
     *            the network
     */
    public JSONWriter(Network network) {
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
        HashMap<Vertex,Integer> nodeIndex = new HashMap<Vertex, Integer>(network.getVertexCount());
        out.print("{");
        
        printTag(out, "nodes");
        out.print('[');
        
        boolean first = true;
        for (Vertex v : network.getVertices()) {
            if (first) {
                first = false;
            }
            else {
                out.print(',');                
            }
            out.print('{');
            nodeIndex.put(v, new Integer(nodeIndex.size()));
            if (v instanceof Indi) {
                Indi indi = (Indi) v;
                printTag(out, "type");
                printName(out, "indi");
                String f = indi.getFamc();
                if (f != null) {
                    out.print(',');
                    printTag(out, "famc");
                    printName(out, f);
                }
                if (indi.getFams() != null && ! indi.getFams().isEmpty()) {
                    out.print(',');
                    printTag(out, "fams");
                    out.print('[');
                    boolean first2 = true;
                    for (String o : indi.getFams()) {
                        if (first2) first2 = false;
                        else out.print(','); 
                        printName(out, o);
                    }
                    out.print(']');
                }
//                printTag(out, "id");
//                printName(out, indi.getId());
//                out.print(',');
//                printTag(out, "name");
//                printName(out, indi.getName());
            }
            else {
//                Fam fam = (Fam) v;
                printTag(out, "type");
                printName(out, "fam");
                out.print(',');
                printTag(out, "ID");
                printName(out, v.getId());
            }
            out.print(',');
            printTag(out, "x");
            out.print(v.getX());
            out.print(',');
            printTag(out, "y");
            out.print(v.getLayer());
            
            for (Map.Entry<String,Object> e : v.getProps().entrySet()) {
                out.print(',');
                printTag(out, e.getKey());
                print(out, e.getValue());
            }
            out.print('}');
        }
        out.print(']');
        out.print(',');
        printTag(out, "links");
        out.print('[');

        first = true;
        for (Edge e : network.getEdges()) {
            Vertex source = network.getSource(e);
            Vertex dest = network.getDest(e);
            
            if (first) {
                first = false;
            }
            else {
                out.print(',');                
            }
            out.print('{');
            printTag(out, "source");
            out.print(nodeIndex.get(source));
            out.print(',');
            printTag(out, "target");
            out.print(nodeIndex.get(dest));
            out.print('}');
        }
        
        out.println("]}");
        out.close();
    }
    
    private void print(PrintWriter out, Object o) throws IOException {
        if (o == null) {
            out.print("null");
        }
        else if (o instanceof String) {
            String s = (String)o;
            Matcher m = INTEGER.matcher(s);
            if (m.matches()) {
                out.print(s);
            }
            else {
                printName(out, s);
            }
        }
        else {
            out.print(o.toString());
        }
    }
    
    private void printName(PrintWriter out, String name) throws IOException {
        int last = 0;
        out.print('"');
        Matcher m = JSON_CONTROLS.matcher(name);

        while (m.find()) {
            String pre = name.substring(last, m.end()-1);
            out.print(pre);
            out.print('\\');
            last = m.end()-1;
        }
        out.print(name.substring(last));
        out.print('"');
    }
    
    private void printTag(PrintWriter out, String name) throws IOException {
        printName(out, name);
        out.print(':');
    }

}
