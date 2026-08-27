package org.kosit.validator.api.xmlerror;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;

public enum XmlSeverity {

    WARNING("[WARN]", "SEVERITY_WARNING", CTStandardSeverity.WARNING), ERROR("[ERROR]", "SEVERITY_ERROR",
            CTStandardSeverity.ERROR), FATAL_ERROR("[FATAL]", "SEVERITY_FATAL_ERROR", CTStandardSeverity.ERROR);

    private final String logPrefix;

    // This is only needed if we ever stumble upon a specific serialization
    private final String oldStyleName;

    private final CTStandardSeverity ctSeverity;

    private XmlSeverity(final @NonNull String logPrefix, final @NonNull String oldStyleName, final @NonNull CTStandardSeverity ctSeverity) {
        this.logPrefix = logPrefix;
        this.oldStyleName = oldStyleName;
        this.ctSeverity = ctSeverity;
    }

    public @NonNull String getLogPrefix() {
        return this.logPrefix;
    }

    public boolean isError() {
        return this != WARNING;
    }

    public @NonNull CTStandardSeverity getConformatronSeverity() {
        return ctSeverity;
    }
}