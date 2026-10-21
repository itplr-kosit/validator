package org.kosit.validator.impl;

/**
 * A fixed engine identity for tests. Reports the engine names itself, and its version changes with every release, so a
 * test whose expected output is committed pins the identity instead of letting the build decide it.
 */
public class TestEngineInformation implements EngineInformation {

    @Override
    public String getVersion() {
        return "TestVersion";
    }

    @Override
    public String getName() {
        return "TestEngine";
    }
}
