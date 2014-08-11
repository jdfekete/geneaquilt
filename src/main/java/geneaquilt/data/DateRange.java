/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.data;

import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * <b>DateRange</b> is the implementation of a
 * GEDCOM date with a precision unit of one second. 
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public class DateRange implements Comparable<DateRange> {
    /** Number of unit per second. */
    public static final long  SECOND = 1;
    /** Number of unit per minute. */
    public static final long  MINUTE = SECOND*60;
    /** Number of unit per hour. */
    public static final long  HOUR = MINUTE*60;
    /** Number of unit per day. */
    public static final long  DAY = HOUR*24;
    /** Number of unit per hour. */
    public static final long  YEAR = 31558150L;
    
    /** Minimum date */
    public static final long MIN_INF = Long.MIN_VALUE/2;
    /** Maximum date */
    public static final long MAX_INF = Long.MAX_VALUE/2;
    private String text;
    private String mode;
    private long start;
    private long end;
    private boolean approximated;
    private boolean calculated;
    private boolean estimated;
    private boolean interpolated;

    static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    static final HashMap<String, Integer> MONTH = new HashMap<String, Integer>();
    static final String[] MONTHS = {
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN", 
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };
    static {
        calendar.setLenient(true);
        for (int i = 0; i < MONTHS.length; i++) {
            MONTH.put(MONTHS[i], new Integer(i));
        }
    }
    
    /**
     * Creates an invalid date. 
     */
    public DateRange() {
        setInvalid();
    }
    
    /**
     * Creates a new copy of the specified date.
     * @param other the other date
     */
    public DateRange(DateRange other) {
        this.text = other.text;
        this.mode = other.mode;
        this.start = other.start;
        this.end = other.end;
    }
    
    /**
     * Creates a date from a GEDCom syntax
     * @param dateString the string specifying the date
     * @throws ParseException if the syntax is not recognized
     */
    public DateRange(String dateString) {
        try {
            parse(dateString);
        }
        catch(ParseException e) {
            System.err.println("Couldn't parse date "+dateString);
//            e.printStackTrace();
        }
    }

    /**
     * Creates a precisely date
     * @param value the precise value
     */
    public DateRange(long value) {
        start = end = value;
    }    

    /**
     * Creates a range.
     * @param start start range
     * @param end end range
     */
    public DateRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Clears the date, making as long as possible.
     */
    public void clear()  {
        start = MIN_INF;
        end = MAX_INF;
        text = null;
    }
    
    /**
     * Returns the Year/Month/Day of the specified date value
     * in seconds.
     * @param value the value
     * @param fields optional array of at least 3 ints
     * @return the fields filled with the values 
     */
    public static int[] getYMD(long value, int[] fields) {
        if (fields == null) {
            fields = new int[3];
        }
        calendar.setTimeInMillis(value*1000);
        fields[0] = calendar.get(Calendar.YEAR);
        fields[1] = calendar.get(Calendar.MONDAY);
        fields[2] = calendar.get(Calendar.DAY_OF_MONTH);
        return fields;
    }

    /**
     * Returns the date of the starting year day for the
     * specified date value
     * @param value the value
     * @return the start of the year
     */
    public static long yearFloor(long value) {
        calendar.setTimeInMillis(value*1000);
        int year = calendar.get(Calendar.YEAR);
        return yearFloor(year);
    }
    
    /**
     * Returns the date value for January first of the specified year
     * @param year the year
     * @return the date value for January first
     */
    public static long yearFloor(int year) {
        calendar.set(year, 0, 1);
        return calendar.getTimeInMillis()/1000;        
    }
    
    /**
     * Returns the date of the ending year day for the
     * specified date value
     * @param value the value
     * @return the end of the year
     */
    public static long yearCeil(long value) {
        calendar.setTimeInMillis(value*1000);
        int year = calendar.get(Calendar.YEAR);
        calendar.set(year+1, 0, 1);
        return calendar.getTimeInMillis()/1000;
    }
    
//    public static DateRange createGregorian(int year) {
//        return new DateRange(yearFloor(year), yearCeil(year));
//    }
    
//    public static DateRange createGregorian(int month, int year) {
//        calendar.set(year, month, 0);
//        long floor = calendar.getTimeInMillis()/1000;
//        calendar.set(year, month, 30);
//        long ceil = calendar.getTimeInMillis()/1000;
//        return new DateRange(floor, ceil);
//    }

//    public static DateRange createGregorian(int day, int month, int year) {
//        calendar.set(year, month, day);
//        long floor = calendar.getTimeInMillis()/1000;
//        calendar.set(year, month, day+1);
//        long ceil = calendar.getTimeInMillis()/1000;
//        return new DateRange(floor, ceil);
//    }
    
    /**
     * @return true if the range is maximal
     */
    public boolean isMax() {
        return start == MIN_INF && end == MAX_INF;
    }
    
    /**
     * Sets the date to invalid with
     * end = MIN_INF and start = MAX_INF.
     */
    public void setInvalid() {
        start = MAX_INF;
        end = MIN_INF;
        text = null;
        
    }
    
    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }
    
    /**
     * @param start the start to set
     */
    public void setStart(long start) {
        this.start = start;
        text = null;
    }
    
    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }
    
    /**
     * @param end the end to set
     */
    public void setEnd(long end) {
        this.end = end;
        text = null;
    }

    /**
     * @return true of the date range is valid
     */
    public boolean isValid() {
        return start <= end;
    }
    
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }
    
    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }
    
    /**
     * Computes the union of this date with the specified one
     * @param other the other date
     */
    public void union(DateRange other) {
        if (other.isValid()) {
            this.start = Math.min(other.getStart(), this.start);
            this.end = Math.max(other.getEnd(), this.end);
            text = null;
        }
    }
    
    /**
     * Computes the intersection of this date with the specified one
     * @param other the other date
     */
    public void intersection(DateRange other) {
        if (other.isValid()) {
            this.start = Math.max(other.getStart(), this.start);
            this.end = Math.min(other.getEnd(), this.end);
            text = null;
        }
        
    }
    
    /**
     * Sets the year as a plus/minus 6 month range around jan 1st
     * @param year the year
     */
    public void setYear(int year) {
        calendar.set(year, 6, 15);
        end = calendar.getTimeInMillis()/1000;
        calendar.roll(Calendar.YEAR, false);
        start = calendar.getTimeInMillis()/1000;
        text = null;
    }
    
    /**
     * 
     * @return the center of the interval
     * except when it is unbound in one direction
     */
    public long getCenter() {
        if (! isValid())
            return MIN_INF;
        if (start == MIN_INF)
            return end;
        if (end == MAX_INF)
            return start;
        return(start + end)/2;
    }
        
    static int parseMonth(String m) {
        Integer i = MONTH.get(m);
        if  (i == null)
            return -1;
        return i.intValue();
    }
    
    static int[] parseInt(String y) {
        int s = y.indexOf('/');
        try {
            if (s != -1) {
                int[] ret = new int[2];
                String y1 = y.substring(0, s);
                String y2 = y.substring(s+1);
                ret[0] = Integer.parseInt(y1);
                ret[1]= Integer.parseInt(y2);
                return ret;
            }
            else {
                int[] ret = new int[1];
                ret[0] = Integer.parseInt(y);
                return ret;
            }
        }
        catch(NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Parses a date formated string
     * @param dateString the string specifying the date
     * @throws ParseException if the syntax is not recognized
     */
    public void parse(String dateString) throws ParseException {
        this.text = dateString;
        mode = null;
        
        String[] field = dateString.split(" +");
        int f;
        for (f = 0; f < field.length; f++) {
            field[f] = field[f].toUpperCase(); 
        }
        f = 0;
        if ("ABT".equals(field[f])
                || "about".equals(field[f])
                || "EST".equals(field[f])
                || "BEF".equals(field[f])
                || "AFT".equals(field[f])
                || "BET".equals(field[f])) {
            mode = field[f];
            f++;
            if ("BET".equals(mode)) {
                for (int i = f; i < field.length; i++) {
                    if ("AND".equals(field[i])) {
                        parse(field, f, i-f);
                        //TODO parse(field, i+1, field.length-i);
                        return;
                    }
                }
            }
        }
        int n = field.length-f;
        parse(field, f, n);
    }

    private void parse(String[] field, int f, int n) throws ParseException {
        try {
        if (n == 3) {
            // day month year
            int month = parseMonth(field[f+1]);
            int[] day = null;
            if (month < 0) {
                month = parseMonth(field[f]);
                if (month >= 0) {
                    day = parseInt(field[f+1]); 
                }
            }
            else {
                day = parseInt(field[f]);
            }
            int[] y= parseInt(field[f+2]);
            if (month >=0 && y != null && day != null) {
                if (y.length == 1) {
                    int year = y[0];
                    calendar.set(year, month, day[0]);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year, month, day[0]);
                    start = calendar.getTimeInMillis()/1000;
                }
                else {
                    int year1 = y[0];
                    int year2 = y[1];
                    calendar.set(year2, month, day[0]);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year1, month, day[0]);
                    start = calendar.getTimeInMillis()/1000;
                }
            }
            else {
                throw new ParseException("Invalid date format", 0);
            }
        }
        else if (n == 2) {
            int month = parseMonth(field[f]);
            int[] y = parseInt(field[f+1]);
            if (month != -1 && y != null) {
                if (y.length == 1) {
                    int year = y[0];
                    calendar.set(year, month+1, 0);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year, month, 1); // calendar should be lenient for month-1
                    start = calendar.getTimeInMillis()/1000;
                }
                else {
                    int year1 = y[0];
                    int year2 = y[1];
                    calendar.set(year2, month+1, 0);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year1, month, 1);
                    start = calendar.getTimeInMillis()/1000;
                }
            }
            else {
                throw new ParseException("Invalid date format", 0);
            }
        }
        else if (n == 1) {
            int[] y = parseInt(field[f]);
            if (y != null) {
                if (y.length == 2) {
                    int year1 = y[0];
                    int year2 = y[1];
                    calendar.set(year2, 11, 31);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year1, 0, 1);
                    start = calendar.getTimeInMillis()/1000;
                }
                else {
                    int year = y[0];
                    calendar.set(year, 11, 31);
                    end = calendar.getTimeInMillis()/1000;
                    calendar.set(year, 0, 1);
                    start = calendar.getTimeInMillis()/1000;                
                }
            }
            else {
                throw new ParseException("Invalid date format", 0);
            }
        }
        }
        catch(NumberFormatException e) {
            throw new ParseException("Invalid format", 0);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (text != null) {
            if (isValid())
                return text;
            else
                return "??"+text;
        }
        String ret = formatStart();
        if (start == end)
            return ret;
        ret = "["+ret+", "+formatEnd()+"]";
        return ret;
    }
    
    /**
     * @param timeInSeconds the time in seconds
     * @return the time in secodns formated.
     * 
     */
    public static String format(long timeInSeconds) {
        if (timeInSeconds <= MIN_INF)
            return "-INF";
        else if (timeInSeconds >= MAX_INF)
            return "INF";
        calendar.setTimeInMillis(timeInSeconds*1000);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String ret = Integer.toString(day) + " " 
            + MONTHS[month] + " "
            + Integer.toString(year);
        return ret;
    }

    /**
     * @return the start time formated.
     */
    public String formatStart() {
        return format(start);
    }

    /**
     * @return the end time formated.
     */
    public String formatEnd() {
        return format(end);
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(DateRange o) {
        if (! isValid()) {
            if (! o.isValid())
                return 0;
            return 1;
        }
        else if (! o.isValid())
            return -1;
        
        if (o.getStart() > getEnd())
            return 1;
        if (getStart() > o.getEnd())
            return -1;
        if (getStart() == o.getStart() 
                && getEnd() == o.getEnd())
            return 0;
        return (int)Math.signum(o.getCenter()-getCenter());
    }
    
    /**
     * Computes the distance between this date and the specifid
     * date.
     * @param o the date
     * @return a distance, computed as the cartesian distance
     * between the starts and the ends.
     */
    public double distance(DateRange o) {
        if (o == null || !o.isValid() || !isValid()) {
            return Double.POSITIVE_INFINITY;
        }
        long ds = start - o.start;
        long de = end - o.end;
        return Math.hypot(ds, de);
    }
    
    /**
     * Computes the distance between this date and the specifid
     * date.
     * @param o the date
     * @return a distance, computed as the cartesian distance
     * between the starts and the ends.
     */
    public double distanceToCenter(DateRange o) {
        if (o == null || !o.isValid() || !isValid()) {
            return Double.POSITIVE_INFINITY;
        }
        long dist =  Math.abs(getCenter()-o.getCenter());
        return dist;
    }

    /**
     * @return the approximated
     */
    public boolean isApproximated() {
        return approximated;
    }

    /**
     * @param approximated the approximated to set
     */
    public void setApproximated(boolean approximated) {
        this.approximated = approximated;
    }

    /**
     * @return the calculated
     */
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * @param calculated the calculated to set
     */
    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    /**
     * @return the estimated
     */
    public boolean isEstimated() {
        return estimated;
    }

    /**
     * @param estimated the estimated to set
     */
    public void setEstimated(boolean estimated) {
        this.estimated = estimated;
    }

    /**
     * @return the interpolated
     */
    public boolean isInterpolated() {
        return interpolated;
    }

    /**
     * @param interpolated the interpolated to set
     */
    public void setInterpolated(boolean interpolated) {
        this.interpolated = interpolated;
    }

}
