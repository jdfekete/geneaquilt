/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.piccolo.PNode;

/**
 * Class Vertex
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public abstract class Vertex {
    /** Character for FEMALE */
    public final String UNICODE_FEMALE = "\u2640";
    /** Character for MALE */
    public final String UNICODE_MALE = "\u2642";
    /** Character for SEXLESS */
    public final String UNICODE_SEXLESS = "\u26AA";
    /** Character for DIVORCE */
    public final String UNICODE_DIVORCE = "\u26AE";
    /** Character for UNMARRIED */
    public final String UNICODE_UNMARRIED_PARTNERSHIP = "\u26AF";
    /** Character for MARRIED */
    public final String UNICODE_MARIAGE = "\u26AD";
    /** Character for HETERO */
    public final String UNICODE_HETERO = "\u26A4";
    
    private PNode node;
    private double x;
    private String id;
    private int layer = -1;
    protected DateRange dateRange;
    private boolean dateRangeInvalid = true;
    private Map<String,Object> props = new TreeMap<String, Object>();
    
    /**
     * Creates a vertex.
     */
    public Vertex() {
    }
    
    /**
     * @return a displayable label
     */
    public abstract String getLabel();
    
    /**
     * @return the node
     */
    public PNode getNode() {
        if (node == null) 
            node = createNode();
        return node;
    }
    
    protected abstract PNode createNode();
    
    /**
     * Unreference the pointed node
     */
    public void deleteNode() {
    	node = null;
    }
    
    /**
     * Searches for the specified text in the specified field.
     * @param text the text to search
     * @param field the property or null for all the properties
     * @return true if the text is found
     */
    public boolean search(String text, String field) {
        if (text == null || text.length()==0)
            return false;
        if (field == null) {
            for (Object v : props.values()) {
                if (v == null)
                    continue;
                String value = v.toString();
                if (value.contains(text))
                    return true;
            }
        }
        else {
            Object v = getProperty(field);
            if (v == null)
                return false;
            boolean f = v.toString().contains(text);
            if (f)
                return f;
            for (int cnt = 2; true; cnt++) {
                v = getProperty(field+"."+cnt);
                if (v == null)
                    return false;
                if (v.toString().contains(text))
                    return true;
            }
            
        }
        return false;
    }
    
    /**
     * Finds the specified pattern in the field or all the
     * fields if field is null
     * @param p the pattern
     * @param field the field name or null
     * @return true if the pattern matches
     */
    public boolean matches(Pattern p, String field) {
        if (p == null)
            return false;
        if (field == null) {
            for (Object v : props.values()) {
                if (v == null)
                    continue;
                String value = v.toString();
                Matcher m = p.matcher(value);
                if (m.find())
                    return true;
            }
        }
        else {
            Object v = getProperty(field);
            if (v == null)
                return false;
            Matcher m = p.matcher(v.toString());
            if (m.find())
               return true;
            for (int cnt = 2; true; cnt++) {
                v = getProperty(field+"."+cnt);
                if (v == null)
                    return false;
                m = p.matcher(v.toString());
                if (m.find())
                    return true;
            }
        }
        return false;
    }
    

    
    /**
     * Returns the property with the specified key as an object
     * @param key the key
     * @return the value
     */
    public Object getProperty(String key) {
        return props.get(key);
    }

    
    /**
     * Returns the property with the specified key as a string
     * @param key the key
     * @return the value
     */
    public String getStringProperty(String key) {
        Object o = props.get(key);
        if (o != null)
            return o.toString();
        return null;
    }

    /**
     * Returns the specified property as a double value
     * @param key the property name
     * @param def the default value if the property is undefined
     * @return the value
     */
    public double getDoubleProperty(String key, double def) {
        Double prop = (Double)getProperty(key);
        if (prop == null)
            return def;
        return prop.doubleValue();
    }

    /**
     * Returns the specified property as a date value
     * @param key the property name
     * @return the value
     */
    public DateRange getDatePropery(String key) {
        return (DateRange)getProperty(key);
    }
    /**
     * Sets the specified property, returning the old value or null
     * @param key the key
     * @param value the new value
     * @return the real key
     */
    public String setProperty(String key, Object value) {
        int count = 2;
        String k2 = key;
        while (props.get(k2) != null) {
            k2 = key+"."+count;
            count++;
        }
        props.put(k2, value);
        return k2;
    }
    
    /**
     * Set the nth property with the specified key
     * @param key the key
     * @param value the value
     * @param count the property number
     * @return the old property value
     */
    public Object setProperty(String key, Object value, int count) {
        if (count == 0)
            return props.put(key, value);
        else
            return props.put(key+"."+count,value);
    }

    /**
     * Return all the properties with the specified key.
     * @param key the key
     * @return a table of properties with the name
     */
    public List<Object> getAllProperties(String key) {
        Object v = props.get(key);
        if (v == null)
            return Collections.EMPTY_LIST;
        ArrayList<Object> ret = new ArrayList<Object>(1);
        ret.add(v);
        int count = 2;
        for (String k2 = key+"."+count;
            (v = props.get(k2)) != null;
            k2 = key+"."+count) {
            ret.add(v);
        }
        
        return ret;
    }
    
    /**
     * Removes the specified property.
     * @param key the property name
     * @return the old value
     */
    public Object removeProperty(String key) {
        Object o = props.remove(key);
        if (o != null && o instanceof DateRange)
            dateRangeInvalid = true;
        if (o != null) {
            int count = 2;
            for (String k2 = key+"."+count;
                (o = props.remove(k2)) != null;
                k2 = key+"."+count)
                ; // nothing
        }
        return o;
    }

    /**
     * Sets the date of the specified attribut (e.g. DEAT, BIRT)
     * @param attr name of the attribute
     * @param value date value
     */
    public void setDate(String attr, String value) {
        if ("CHAN".equals(attr)) return;
        DateRange date = new DateRange(value); 
        setProperty(attr, date);
        dateRangeInvalid = true;
    }
    
    /**
     * Sets the date of the specified attribut (e.g. DEAT, BIRT)
     * @param attr name of the attribute
     * @param date date value
     */
    public void setDate(String attr, DateRange date) {
        if ("CHAN".equals(attr)) return;
        setProperty(attr, date);
        dateRangeInvalid = true;
    }
    
    /**
     * @return the props
     */
    public Map<String, Object> getProps() {
        return props;
    }
    
    protected void updateMinMaxDate() {
        if (! dateRangeInvalid)
            return;
        if (dateRange == null) {
            dateRange = new DateRange();            
        }
        dateRange.setInvalid();
        for (Entry<String, Object> o : props.entrySet()) {
            if (o.getValue() instanceof DateRange) {
                DateRange date = (DateRange) o.getValue();
                dateRange.union(date);
            }
        }
        dateRangeInvalid = false;
    }
    
    /**
     * @return Returns the range of dates of this vertex.
     */
    public DateRange getDateRange() {
        updateMinMaxDate();
        return dateRange;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Vertex) {
            Vertex v = (Vertex) other;
            return v.getId().equals(id);
        }
        return false;
    }

    /**
     * @param node the node to set
     */
    public void setNode(PNode node) {
        this.node = node;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the layer
     */
    public int getLayer() {
        return layer;
    }
    /**
     * @param layer the layer to set
     */
    public void setLayer(int layer) {
        this.layer = layer;
        setProperty("LAYER", new Integer(layer), 0);
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }
    
    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }
    
    /**
     * @return the DOI
     */
    public double getDOI() {
        return getDoubleProperty("DOI", Double.POSITIVE_INFINITY);
    }

    /**
     * @param DOI the dOI to set
     */
    public void setDOI(double DOI) {
        setProperty("DOI", new Double(DOI), 0);
    }

    /**
     * @return the component
     */
    public int getComponent() {
        Integer i = (Integer)getProperty("COMP");
        if (i == null)
            return -1;
        return i.intValue();
    }
    
    /**
     * @param component the component to set
     */
    public void setComponent(int component) {
        setProperty("COMP", new Integer(component), 0);
    }
}
