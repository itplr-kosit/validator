package org.kosit.validator.api.compact;

/**
 * Contains information about the validator used (name and version).
 */
public class ValidatorEngineInformation {

    private String name;

    private String version;

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public ValidatorEngineInformation() {
    }

    public ValidatorEngineInformation(final String name, final String version) {
        this.name = name;
        this.version = version;
    }
}
