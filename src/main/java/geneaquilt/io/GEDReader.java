package geneaquilt.io;

import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Network;
import geneaquilt.data.Vertex;
import org.gedcom4j.model.Family;
import org.gedcom4j.model.FamilyChild;
import org.gedcom4j.model.FamilySpouse;
import org.gedcom4j.model.Individual;
import org.gedcom4j.model.IndividualAttribute;
import org.gedcom4j.model.IndividualEvent;
import org.gedcom4j.model.PersonalName;
import org.gedcom4j.model.StringWithCustomTags;
import org.gedcom4j.parser.GedcomParser;


/**
 * Class GEDReader allows reading from a GEDCOM file.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class GEDReader  {
    private Network network;

    /**
     * Loads a GEDCOM file
     * @param filename the file name
     * @return a network or null
     */
    public Network load(String filename) {
        GedcomParser gp = new GedcomParser();
        network = new Network();
        try {
            gp.load(filename);
            
            for (Individual i: gp.gedcom.individuals.values()) {
                Indi indi = new Indi();
                indi.setId(i.xref);
                network.addVertex(indi);
                for (PersonalName p : i.names) {
                    indi.setProperty("NAME", p.basic);
                    if (p.givenName != null)
                        indi.setProperty("NAME.GIVN", p.givenName);
                    if (p.surname != null)
                        indi.setProperty("NAME.SURN", p.surname);
                    if (p.nickname != null)
                        indi.setProperty("NAME.NICK", p.surname);
                }
                for (StringWithCustomTags s : i.aliases) {
                    indi.setProperty("NAME.ALIAS", s.trim());
                }
                for (FamilyChild fc : i.familiesWhereChild) {
                    indi.setFamc(fc.family.xref); // should take the fist?
                }
                for (FamilySpouse fs : i.familiesWhereSpouse) {
                    indi.addFams(fs.family.xref);
                }
                if (i.sex != null)
                    indi.setSex(i.sex.trim());
                for (IndividualAttribute ia : i.attributes) {
                    indi.setProperty(ia.type.tag, ia.description.value);
                }
                for (IndividualEvent ee : i.events) {
                    String tag = ee.type.tag;
                    if (ee.address != null) {
                        StringBuffer sb = new StringBuffer();
                        for (String s : ee.address.lines) sb.append(s);
                        indi.setProperty(tag+".ADDR", sb.toString());
                    }
                    if (ee.age != null)
                        indi.setProperty(tag+".AGE", ee.age.trim());

                    if (ee.cause != null)
                        indi.setProperty(tag+".CAUSE", ee.cause.trim());
                    
                    if (ee.date != null) 
                        indi.setDate(tag+".DATE", ee.date.trim());
                    
                    if (ee.description != null)
                        indi.setProperty(tag+".DESC", ee.description.trim());
                    
                }
            }
            for (Family f : gp.gedcom.families.values()) {
                Fam fam = new Fam();
                fam.setId(f.xref);
                network.addVertex(fam);
                if (f.husband != null)
                    fam.setHusb(f.husband.xref);
                if (f.wife != null)
                    fam.setWife(f.wife.xref);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
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
