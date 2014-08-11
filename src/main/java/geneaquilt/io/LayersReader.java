package geneaquilt.io;

import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * Class LayersReader
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class LayersReader {
    private static final Logger LOG = Logger.getLogger(LayersReader.class);
    
    /**
     * Returns true if the layer file exists.
     * @param basefile
     * @return true if the layer file exists
     */
    public boolean layerFileExists(String basefile) {
        int index = basefile.lastIndexOf('.');
        if (index == -1) {
            LOG.warn("No file extension");
            return false;
        }
        String layername = basefile.substring(0, index)+".lyr";
        return (new File(layername)).exists();
    }
    
    /**
     * Reads a layer file when it exists, associating a layer
     * and X position to each vertex.
     * @param basefile the GED file name
     * @param network the network
     * @return true if it has been loaded, false otherwise
     */
    public boolean load(String basefile, Network network) {
        int index = basefile.lastIndexOf('.');
        if (index == -1) {
            LOG.warn("No file extension");
            return false;
        }
        String layername = basefile.substring(0, index)+".lyr";
        FileReader fin = null;
        BufferedReader in = null;
        try {
            fin = new FileReader(layername);
            in = new BufferedReader(fin);

            String line;
            TreeSet<Double> ranks = new TreeSet<Double>();
            HashMap<Vertex,Double> vertexY = new HashMap<Vertex, Double>();
            while ((line = in.readLine()) != null) {
                String fields[] = line.split(" ");
                String id = fields[0];
                if (id.startsWith("\"") && id.endsWith("\"")) {
                    id = id.substring(1, id.length()-1);
                }
                double x = Double.parseDouble(fields[1]);
                Double y = Double.valueOf(fields[2]);
                Vertex v = network.getVertex(id);
                if (v != null) {
                    v.setX(x);
                    vertexY.put(v, y);
                    ranks.add(y);
                }
                else {
                    LOG.warn("Cannot find vertex with id="+id);
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
                    LOG.error("Unexpected layer not found for "+e.getValue());
                    l = -l+1;
                }
                network.setVertexLayer(e.getKey(), l);
            }
//            network.fixLayers();
            in.close();
            fin.close();
        }
        catch (FileNotFoundException e) {
        	LOG.warn("Layer file does not exist");
        	return false;
        } catch(Exception e) {
            LOG.info("Cannot open layer file", e);
            return false;
        }
        return true;
    }
}
