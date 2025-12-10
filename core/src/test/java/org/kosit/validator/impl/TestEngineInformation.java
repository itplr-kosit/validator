package org.kosit.validator.impl;

public class TestEngineInformation implements EngineInformation {

    @Override
    public String getVersion() {
        return "TestVersion";
    }

    @Override
    public String getName() {
        return "TestEngine";
    }

    @Override
    public String getFrameworkVersion() {
        return "TestFramework";
    }

    @Override
    public String getBuild() {
        return "TestBuildnumber";
    }
}
