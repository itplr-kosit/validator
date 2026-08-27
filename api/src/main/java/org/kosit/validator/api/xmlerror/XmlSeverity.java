package org.kosit.validator.api.xmlerror;

public enum XmlSeverity {

    SEVERITY_WARNING("[WARN]", "SEVERITY_WARNING"), SEVERITY_ERROR("[ERROR]", "SEVERITY_ERROR"), SEVERITY_FATAL_ERROR("[FATAL]",
            "SEVERITY_FATAL_ERROR");

    private final String logPrefix;

    // This is only needed if we ever stumble upon a specific serialization
    private final String oldStyleName;

    private XmlSeverity(final String logPrefix, final String oldStyleName) {
        this.logPrefix = logPrefix;
        this.oldStyleName = oldStyleName;
    }

    public String getLogPrefix() {
        return this.logPrefix;
    }

    public boolean isError() {
        return this != SEVERITY_WARNING;
    }
}