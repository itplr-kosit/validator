package de.kosit.validationtool.impl.model;

import java.util.Objects;

import org.oclc.purl.dsdl.svrl.FailedAssert;

import de.kosit.validationtool.model.scenarios.ErrorLevelType;

/**
 * This class contains a single Schematron failed assertion that has a custom error level.
 * 
 * @since 1.6.0
 */
public class CustomFailedAssert {

    private final FailedAssert originalFailedAssert;

    private final ErrorLevelType customLevelFlag;

    /**
     * Constructor
     * 
     * @param failedAssert The failed assert from Schematron. May not be <code>null</code>.
     * @param customLevelFlag The custom error level. May not be <code>null</code>.
     */
    public CustomFailedAssert(final FailedAssert failedAssert, final ErrorLevelType customLevelFlag) {
        Objects.requireNonNull(failedAssert);
        Objects.requireNonNull(customLevelFlag);
        this.originalFailedAssert = failedAssert;
        this.customLevelFlag = customLevelFlag;
    }

    public FailedAssert getFailedAssert() {
        return this.originalFailedAssert;
    }

    public ErrorLevelType getCustomLevelFlag() {
        return this.customLevelFlag;
    }

}
