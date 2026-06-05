package org.kosit.validator.impl;

/**
 * Holds static information about this validator.
 *
 * @author Andreas Penski
 */
public interface EngineInformation {

    /**
     * Returns the version number of the validator.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Returns the name of the engine.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the version number of the framework used. This is relevant to align scenario configuration and validator
     * versions with each other.
     *
     * @return the framework version
     */
    String getFrameworkVersion();

    String getBuild();

}
