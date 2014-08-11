/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.data;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.nodes.PFam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class Fam
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class Fam extends Vertex {
    private ArrayList<String> chil;
    
    /**
     * Create a Fam.
     */
    public Fam() {
    }
    
    /**
     * @return the label to use
     */
    public String getLabel() {
//        return UNICODE_MARIAGE;//FIXME
        //return " "+UNICODE_MALE+UNICODE_FEMALE+" ";
        return " F ";
    }
    
    /**
     * {@inheritDoc}
     */
    protected PNode createNode() {
        return new PFam(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Fam["+getId()+"]";
    }

    /**
     * @return the wife
     */
    public String getWife() {
        return (String)getProperty("WIFE");
    }

    /**
     * @param wife the wife to set
     */
    public void setWife(String wife) {
        setProperty("WIFE", wife);
    }

    /**
     * @return the husb
     */
    public String getHusb() {
        return (String)getProperty("HUSB");
    }

    /**
     * @param husb the husb to set
     */
    public void setHusb(String husb) {
        setProperty("HUSB", husb);
    }

    /**
     * @return the chil
     */
    public List<String> getChil() {
        if (chil == null)
            return Collections.EMPTY_LIST;
        return chil;
    }

    /**
     * @param chil the chil to set
     */
    public void setChil(ArrayList<String> chil) {
        this.chil = chil;
    }
    
    /**
     * Adds a child
     * @param c the child
     */
    public void addChil(String c) {
        if (c == null) return;
        if (chil == null) {
            chil = new ArrayList<String>();
        }
        if (! chil.contains(c))
            chil.add(c);
    }
    
    /**
     * @return the marriage date or null
     */
    public DateRange getMarriage() {
        return (DateRange)getProperty("MARR.DATE");
    }
    
    /**
     * Sets the marriage date.
     * @param date the date 
     */
    public void setMarriage(DateRange date) {
        setProperty("MARR.DATE", date);
    }

    /**
     * @return the marriage date as a date, never null, maybe invalid
     */
    public DateRange findMarriage() {
        DateRange d = getMarriage();
        if (d == null) {
            d = new DateRange();
            d.clear();
            setMarriage(d);
        }
        return d;
    }
}
