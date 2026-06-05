package org.kosit.validator.impl;

/**
 * Some metadata for the action. Mainly used for creating the XVRL report.
 * 
 * @author apenski
 */
public class ActionMetadata {

    private String name;

    private String id;

    public String getName() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public ActionMetadata(final String name, final String id) {
        this.name = name;
        this.id = id;
    }
}
