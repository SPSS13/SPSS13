/*
 * A complexity class.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/problem/Complexity.java,v 2.3 2012/10/28 16:00:57 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.problem;


/**
 * Represents complexity classes like Linear, P, NPC, coNPC and Unknown.
 * Use only the defined comparison methods, the enum compareTo does not give a
 * complexity comparison.
 * The betterThan and betterOrEqual compare as LIN < P < GIC < NPC,NPH,CONPC.
 * betterThan with other complexities is undefined.
 */
public enum Complexity {
    /** Higher complexity, higher number */
    LINEAR ("Linear",        "Lin",   "Bounded"),
    P      ("Polynomial",    "P",     "Bounded"),
    GIC    ("GI-complete",   "GIC",   "Unbounded"),
    NPC    ("NP-complete",   "NPC",   "Unbounded"),
    NPH    ("NP-hard",       "NPh",   "Unbounded"),
    CONPC  ("coNP-complete", "coNPC", "Unbounded"),
    OPEN   ("Open",          "Open",  "Open"),
    UNKNOWN("Unknown",       "?",     "Unknown");


    /** Complexity class */
    protected String name;
    protected String abbrev;
    protected String widthName;

    /**
     * Creates a new complexity with the given value and names
     */
    private Complexity(String name, String abbrev, String widthName) {
        this.name = name;
        this.abbrev = abbrev;
        this.widthName = widthName;
    }


    public String getShortString() {
        return abbrev;
    }

    public String getComplexityString() {
        return name;
    }

    public String getWidthString() {
        return widthName;
    }

    public boolean betterThan(Complexity c) {
        return compareTo(c) < 0;
    }

    public boolean betterOrEqual(Complexity c) {
        return compareTo(c) <= 0;
    }

    public boolean isUnknown() {
        return this == UNKNOWN  ||  this == OPEN;
    }

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isNPC() {
        return this == NPC;
    }

    public boolean isCONPC() {
        return this == CONPC;
    }

    public boolean likelyNotP() {
        return this == CONPC  || this == NPC  ||  this == NPH  ||  this == GIC;
    }

    public String toString() {
        return name;
    }

    /**
     * Can a problem at the same time have this complexity and c's?
     */
    public boolean isCompatible(Complexity c) {
        return this == UNKNOWN  ||  c == UNKNOWN  ||
                (betterOrEqual(P)  &&  c.betterOrEqual(P))  ||
                equals(c);
    }

    /**
     * Assuming this complexity is assigned to some graphclasses, return true
     * iff this complexity also holds for subclasses.
     */
    boolean distributesDown() {
        return betterOrEqual(P);
    }

    /**
     * Assuming this complexity is assigned to some graphclasses, return true
     * iff this complexity also holds for superclasses.
     */
    boolean distributesUp() {
        return likelyNotP();
    }

    /**
     * Assuming this complexity is assigned to some graphclasses, return true
     * iff this complexity also holds for equivalent classes, but not
     * necessarily for super/sub classes.
     */
    boolean distributesEqual() {
        return isOpen();
    }


    /*
     * If a problem has both complexity this and c, return the resulting
     * complexity.
     */
    public Complexity distil(Complexity c) throws ComplexityClashException {
        if (!isCompatible(c))
            throw new ComplexityClashException(this, c);
        if (c.isUnknown())
            return this;
        if (c.betterThan(this)  ||  this.isUnknown())
            return c;
        return this;
    }


    /**
     * Return the complexity class represented by s.
     */
    public static Complexity getComplexity(String s) {
        for (Complexity c : Complexity.values())
            if (c.name.equals(s)  ||  c.abbrev.equals(s))
                return c;
        if (LINEAR.widthName.equals(s))
            return LINEAR;
        if (NPC.widthName.equals(s))
            return NPC;
        throw new IllegalArgumentException(s);
    }
}

/* EOF */
