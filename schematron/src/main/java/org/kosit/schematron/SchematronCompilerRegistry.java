package org.kosit.schematron;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.kosit.schematron.compiler.IsoSchematronCompiler;
import org.kosit.schematron.compiler.SchXslt2Compiler;
import org.kosit.schematron.compiler.SchXsltCompiler;
import org.kosit.schematron.compiler.SchematronCompiler;

import net.sf.saxon.s9api.Processor;

public final class SchematronCompilerRegistry {

    // TODO this should be SchXslt2 but will fail main tests
    public static final String FALLBACK_COMPILER_ID = SchXsltCompiler.COMPILER_ID;

    private final Map<String, SchematronCompiler> byId;

    public static SchematronCompilerRegistry defaultSchematronCompilerRegistry(final Processor processor) {
        return new SchematronCompilerRegistry(List.of(new SchXsltCompiler(), new SchXslt2Compiler(), new IsoSchematronCompiler(processor)));
    }

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
