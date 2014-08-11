package geneaquilt.io;

import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import geneaquilt.utils.GUIUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

/**
 * Class DOTLayersReader
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class DOTLayersReader {
    private static final Logger LOG = Logger.getLogger(DOTLayersReader.class);
    private static boolean debug = false;
    
    /**
     * Reads a layer file when it exists, associating a layer
     * and X position to each vertex.
     * @param network the network
     * @return true if it has been loaded, false otherwise
     */
    public boolean load(Network network) {
        try {
            DOTWriter writer = new DOTWriter(network);
            String msg = "Computing layers...";
            if (network.getEdgeCount() > 5000)
                msg += " (this may take a while!)";
            GUIUtils.updateComputationMessage(msg);
            
            writer.setBare(true);
            String arg;
            File tmp = null;
            if (debug) {
                tmp = File.createTempFile("quilt", ".dot");
                PrintWriter pw = new PrintWriter(tmp);
                writer.write(pw);
                pw.close();
                arg = "dot -Tplain "+tmp.getAbsolutePath() ;
            }
            else {
                arg = "dot -Tplain";            
            }
            
            Process proc;
            try {
            	proc = Runtime.getRuntime().exec(arg);
            }
        	catch(IOException e) {
                JOptionPane.showMessageDialog(null, "Cannot find the 'dot' program. The generations will not be correctly assigned.\n\nTo fix this problem, make sure GraphViz is installed and the dot executable is accessible from " + System.getProperty("user.dir") + "\nThen, delete the .lyr file and relaunch GeneaQuilts.", "Error", JOptionPane.ERROR_MESSAGE);
                LOG.warn("Cannot find the dot program");
                return false;
            }

            InputStreamReader isr = new InputStreamReader(proc.getInputStream());
            BufferedReader in = new BufferedReader(isr);
            if (! debug) {
                writer.write(new PrintWriter(proc.getOutputStream()));
            }

            if (tmp != null) {
                LOG.info("Generated DOT file available at "+tmp.getAbsolutePath());
                //tmp.delete();
            }

            String line;
            TreeSet<Double> ranks = new TreeSet<Double>();
            HashMap<Vertex,Double> vertexY = new HashMap<Vertex, Double>();
            while ((line = in.readLine()) != null) {
                String fields[] = line.split(" ");
                if (fields.length < 5 || ! fields[0].equals("node")) 
                    continue;
                String id = fields[1];
                if (id.startsWith("\"") && id.endsWith("\"")) {
                    id = id.substring(1, id.length()-1);
                }
                double x = Double.parseDouble(fields[2]);
                Double y = Double.valueOf(fields[3]);
                Vertex v = network.getVertex(id);
                if (v != null) {
                    v.setX(x);
                    vertexY.put(v, y);
                    ranks.add(y);
                }
                else {
                    LOG.warn("Invalid node id="+id);
                }
            }
            double[] r = new double[ranks.size()];
            int i = 0;
            for (Double d : ranks) {
                r[i++] = d.doubleValue();
            }
            for (Entry<Vertex,Double> e : vertexY.entrySet()) {
                int l = Arrays.binarySearch(r, e.getValue().doubleValue());
                if (l < 0) {
                    LOG.error("Unexpeced layer not found for "+e.getValue());
                    l = -l+1;
                }
                network.setVertexLayer(e.getKey(), l);
            }
//            network.fixLayers();
        }
        catch(Exception e) {
            LOG.debug("Cannot read layers", e);
            return false;
        }
        return true;
    }



    /**
     * @return the debug
     */
    public static boolean isDebug() {
        return debug;
    }



    /**
     * @param d the debug to set
     */
    public static void setDebug(boolean d) {
        debug = d;
    }
}
