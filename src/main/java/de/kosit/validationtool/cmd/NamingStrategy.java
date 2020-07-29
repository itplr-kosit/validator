package de.kosit.validationtool.cmd;

/**
 * Strategy for creating names. This is used for generating the report result name.
 * 
 * @author Andreas Penski
 */
public interface NamingStrategy {

    /**
     * Create a name based on a base name
     * 
     * @param base the base name
     * @return the generated name
     */
    String createName(String base);
}
