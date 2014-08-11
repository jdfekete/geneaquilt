package geneaquilt.data;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.nodes.PIndi;
import geneaquilt.nodes.PSemanticText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class Indi
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class Indi extends Vertex {
    private ArrayList<String> fams;
    private String famc;
    /** Use that attribute for names */
    private transient String label;
    
    
    /**
     * Creates a new Indi.
     */
    public Indi() {
    }
    
    /**
     * {@inheritDoc}
     */
    public void setId(String id) {
        super.setId(id);
        setProperty("ID", id);
    }
    
    protected PNode createNode() {
        return new PIndi(this);
    }
    
    /**
     * @return a suitable label
     */
    public String getLabel() {
        if (label == null) {
            label = getSexString()+getName();
        }
        return label;
    }
    
    /**
     * Change the label
     * @param prop
     */
    public void setLabelBy(String prop) {
        String old = label;

        Object l = getProperty(prop);
        if (l == null) {
            l = getName();
        }
        if (l == null) {
            label = getSexString();
        }
        else {
            label = getSexString() + l.toString();
        }
        if (getNode() != null 
                && (old == null || ! old.equals(label))) {
            ((PSemanticText)getNode()).setText(label);
        }
    }
    
    /**
     * @return the unicode string related to the sex
     */
    public String getSexString() {
        if ("M".equalsIgnoreCase(getSex())) {
            return UNICODE_MALE + " ";
        }
        else if ("F".equalsIgnoreCase(getSex())) {
            return UNICODE_FEMALE + " ";
        }
        else
            return "    ";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Indi["+getId()+":"+getName()+"]";
    }
    /**
     * @return the name
     */
    public String getName() {
        return getStringProperty("NAME");
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        setProperty("NAME", name);
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return getStringProperty("NAME.SURN");
    }

    /**
     * @return the given name
     */
    public String getGiven() {
        return getStringProperty("NAME.GIVN");
    }


    /**
     * @return the fams
     */
    public List<String> getFams() {
        if (fams == null) 
            return Collections.EMPTY_LIST;
        return fams;
    }

    /**
     * @param fams the fams to set
     */
    public void setFams(ArrayList<String> fams) {
        this.fams = fams;
    }
    
    /**
     * Adds a fams
     * @param f the fams
     */
    public void addFams(String f) {
        if (fams == null) {
            fams = new ArrayList<String>();
        }
        else {
            if (fams.contains(f))
                return;
        }
        fams.add(f);
    }

    /**
     * @return the famc
     */
    public String getFamc() {
        return famc;
    }

    /**
     * @param famc the famc to set
     */
    public void setFamc(String famc) {
        this.famc = famc;
    }

    /**
     * @return the sex
     */
    public String getSex() {
        return getStringProperty("SEX");
    }

    /**
     * @param sex the sex to set
     */
    public void setSex(String sex) {
        setProperty("SEX", sex);
    }

    /**
     * @return the birth
     */
    public DateRange getBirth() {
        Object d = getProperty("BIRT.DATE");
        if (d == null)
            return null;
        return (DateRange)d;
    }

    /**
     * @return a birth date, maybe invalid, never null
     */
    public DateRange findBirth() {
        DateRange d = getBirth();
        if (d == null) {
            d = new DateRange();
            d.clear();
            setDate("BIRT.DATE", d);
        }
        return d;
    }

    /**
     * @param birth the birth to set
     */
    public void setBirth(String birth) {
        setDate("BIRT.DATE", birth);
    }

    /**
     * @return the death
     */
    public DateRange getDeath() {
        Object d = getProperty("DEAT.DATE");
        if (d == null)
            return null;
        return (DateRange)d;
    }
    
    /**
     * @return a death date, maybe invalid, never null
     */
    public DateRange findDeath() {
        DateRange d = getDeath();
        if (d == null) {
            d = new DateRange();
            d.clear();
            setDate("DEAT.DATE", d);
        }
        return d;
    }

    /**
     * @param death the death to set
     */
    public void setDeath(String death) {
        setDate("DEAT.DATE", death);
    }
    
    /**
     * @return the christening date
     */
    public DateRange getChr() {
        Object d = getProperty("CHR.DATE");
        if (d == null)
            return null;
        return (DateRange)d;
    }

    /**
     * @return a christening date, maybe invalid, never null
     */
    public DateRange findChr() {
        DateRange d = getChr();
        if (d == null) {
            d = new DateRange();
            d.clear();
            setDate("CHR.DATE", d);
        }
        return d;
    }

    /**
     * @return the burial date
     */
    public DateRange getBurial() {
        Object d = getProperty("BURI.DATE");
        if (d == null)
            return null;
        return (DateRange)d;
    }
    
    /**
     * @return a burial date, maybe invalid, never null
     */
    public DateRange findBurial() {
        DateRange d = getBurial();
        if (d == null) {
            d = new DateRange();
            d.clear();
            setDate("BURI.DATE", d);
        }
        return d;
    }
}
