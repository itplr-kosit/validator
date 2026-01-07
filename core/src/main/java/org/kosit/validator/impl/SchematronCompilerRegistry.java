package org.kosit.validator.impl;

import org.kosit.validator.api.SchematronCompiler;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public final class SchematronCompilerRegistry {

    private final Map<String, SchematronCompiler> byId;

    public SchematronCompilerRegistry(Collection<SchematronCompiler> compilers) {
        this.byId = compilers.stream().collect(Collectors.toMap(SchematronCompiler::getId, c -> c));
    }

    public SchematronCompiler get(String id) {
        SchematronCompiler c = byId.get(id);
        if (c == null) {
            throw new IllegalArgumentException("Unknown Schematron compiler: " + id);
        }
        return c;
    }
}
