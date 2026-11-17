package org.kosit.validator.impl;

/**
 * Who validated: the name and version a report states for the engine that produced it.
 * <p>
 * The engine knows this about itself - {@link ConformanceValidation} implements this interface and answers for the
 * validator it is part of, so nobody has to tell it who it is. An application that embeds the engine and wants the
 * report to name itself instead passes its own implementation to
 * {@link ConformanceValidation#withEngineInformation(EngineInformation)}.
 * </p>
 *
 * @author Andreas Penski
 */
public interface EngineInformation {

    /**
     * @return the name of the engine, as the report states it
     */
    String getName();

    /**
     * @return the version of the engine, as the report states it
     */
    String getVersion();

    /**
     * @return what this validator says about itself, read from the build. Never {@code null}.
     */
    static EngineInformation ofThisEngine() {
        return ThisEngine.INSTANCE;
    }
}
