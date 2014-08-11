package geneaquilt.io;

import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Class TIPReader
 * 
 * See http://www.kintip.net/content/view/74/
 * and http://www.kintip.net/content/view/76/21/
 * 
 * @author Jean-Daniel Fekete
 */
public class TIPReader {
    private Network network;
    
    /**
     * Creates a TIP Reader.
     */
    public TIPReader() {
    }
    
    /**
     * Load the file.
     * @param filename the filename
     * @return the network or null
     */
    public Network load(String filename) {
        try {
            FileReader fin = new FileReader(filename);
            BufferedReader in = new BufferedReader(fin);
            network = new Network();
            
            String line;
            int lineNum = 0;
            while ((line = in.readLine()) != null) try {
                lineNum++;
                if (line.startsWith("*")) { 
                    // comment, ignore for no
                    continue;
                }
                String[] field = line.split("\t");
                if (field.length < 4) {
                    System.err.println("Invalid line :"+line);
                    continue;
                }
                int type = Integer.parseInt(field[0]);
                String id = field[1].intern();
                Indi indi;
                switch(type) {
                case 0:
                    indi = new Indi();
                    indi.setId(id);
                    if ("0".equals(field[2])) {
                        indi.setSex("M");
                    }
                    else if ("1".equals(field[2])) {
                        indi.setSex("F");
                    }
                    // 2 is unkown
                    indi.setName(field[3]);
                    network.addVertex(indi);
                    break;
                case 1:
                    indi = (Indi)network.getVertex(id);
                    if (indi == null) {
                        System.err.println("Invalid id at line "+lineNum+line);
                        continue;
                    }
                    String other = field[2].intern();
                    int rel = Integer.parseInt(field[3]);
                    switch(rel) {
                    case 0:
                        indi.setProperty("FATHER", other);
                        break;
                    case 1:
                        indi.setProperty("MOTHER", other);
                        break;
                    case 2:
                        indi.setProperty("SPOUSE", other);
                        break;
                    default:
                        System.err.println("Invalid relation at line "+lineNum+line);
                    }
                    break;
                case 2:
                    // parse property
                    String prop = field[2];
                    String val = field[3];
                    if ("GENERATION".equals(prop)) {
                        int gen = Integer.parseInt(val.trim());
                        network.getVertex(id).setLayer(2*gen);
                    }
                    //TODO
                    break;
                default:
                    System.err.println("Invalid type at line "+lineNum+line);
                }
            }
            catch(NumberFormatException e) {
                System.err.println("Invalid int at line "+lineNum+line);
            }
            in.close();
            fin.close();

            ArrayList<Vertex> vertices = new ArrayList<Vertex>(network.getVertices());
            
            for (Vertex v : vertices) {
                if (v instanceof Indi) {
                    Indi indi = (Indi)v;
                    String father = (String)indi.getProperty("FATHER");
                    String mother = (String)indi.getProperty("MOTHER");
                    if (father != null || mother != null) {
                        Fam fam = getFam(mother, father);
                        fam.addChil(indi.getId());
                        indi.setFamc(fam.getId());
                        Edge edge = new Edge(indi.getId(), fam.getId());
                        if (! network.containsEdge(edge)) {
                            network.addEdge(edge, indi, fam);
                            edge.setFromVertex(indi);
                            edge.setToVertex(fam);
                        }
                    }
                    
                    String spouse = (String)indi.getProperty("SPOUSE");
                    if (spouse != null) {
                        Indi spouseIndi = (Indi)network.getVertex(spouse);
                        assert(spouseIndi != null);
                        Fam fam = getFam(spouse, indi.getId());
                        Edge edge = new Edge(fam.getId(), spouse);
                        if (! network.containsEdge(edge)) {
                            network.addEdge(edge, fam, spouseIndi);
                            spouseIndi.addFams(fam.getId());
                            edge.setFromVertex(fam);
                            edge.setToVertex(spouseIndi);
                        }
                        edge = new Edge(fam.getId(), indi.getId());
                        if (! network.containsEdge(edge)) {
                            network.addEdge(edge, fam, indi);
                            indi.addFams(fam.getId());
                            edge.setFromVertex(fam);
                            edge.setToVertex(indi);
                        }
                    }
                }
            }

//          if (network != null) {
//              DOTWriter writer = new DOTWriter(network);
//              writer.setBare(true);
//              try {
//                  writer.write("debug.dot");
//              }
//              catch(Exception e) {
//                  e.printStackTrace();
//              }
//          }
            return network;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private Fam getFam(String wife, String husb) {
        String id = "F"+(wife==null?"unknown":wife)+"_"+(husb==null?"unkown":husb);
        Fam fam = (Fam)network.getVertex(id);
        if (fam == null) {
            fam = new Fam();
            fam.setId(id);
            fam.setHusb(husb);
            fam.setWife(wife);
            network.addVertex(fam);
            if (husb!=null) {
                Edge edge = new Edge(id, husb);
                if (! network.containsEdge(edge)) {
                    Indi husbIndi = (Indi)network.getVertex(husb);
                    network.addEdge(edge, fam, husbIndi);
                    husbIndi.addFams(id);
                    edge.setFromVertex(fam);
                    edge.setToVertex(husbIndi);
                }
            }
            if (wife != null) {
                Edge edge = new Edge(id, wife);
                if (! network.containsEdge(edge)) {
                    Indi wifeIndi = (Indi)network.getVertex(wife);
                    if (wifeIndi == null) {
                        wifeIndi = new Indi();
                        wifeIndi.setId(wife);
                        network.addVertex(wifeIndi);
                    }
                    network.addEdge(edge, fam, wifeIndi);
                    wifeIndi.addFams(id);
                    edge.setFromVertex(fam);
                    edge.setToVertex(wifeIndi);
                }
            }
        }
        return fam;
    }
}
