package org.kosit.validator.impl.tasks;

import org.kosit.xvrl.model.XVRLReport;

import net.sf.saxon.s9api.XdmNode;

/**
 * Result object for business report e.g. user defined transformation output.
 * 
 * @author apenski
 */
public class BusinessReport {

    private String name;

    private XdmNode content;

    private XVRLReport report;

    public String getName() {
        return this.name;
    }

    public XdmNode getContent() {
        return this.content;
    }

    public XVRLReport getReport() {
        return this.report;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setContent(final XdmNode content) {
        this.content = content;
    }

    public void setReport(final XVRLReport report) {
        this.report = report;
    }
}
