package org.kosit.validator.api.xvrl.compact;

/**
 * Contains information about the validator used.
 * 
 * @param name validator name
 * @param version validator version
 */
public record ValidatorEngineInformation(String name, String version) {
}
