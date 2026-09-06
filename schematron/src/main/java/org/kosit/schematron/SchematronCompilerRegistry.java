package org.kosit.schematron;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;

public final class SchematronCompilerRegistry {

    private final Map<String, SchematronCompiler> byId;

    public SchematronCompilerRegistry(final Collection<SchematronCompiler> compilers) {
        this.byId = compilers.stream().collect(Collectors.toMap(SchematronCompiler::getId, Function.identity()));
    }

    public @NonNull SchematronCompiler get(final String id) {
        final SchematronCompiler c = byId.get(id);
        if (c == null) {
            throw new IllegalArgumentException("Unknown Schematron compiler: " + id);
        }
        return c;
    }
}
