package org.kosit.validator.api;

import javax.xml.transform.Source;
import java.net.URI;
import java.util.function.Function;

public interface SchematronCompiler {

    String getId();

    /**
     * Compiles a Schematron (.sch) file into XSLT-Stylesheet, to create SVRL.
     *
     * @param schematronUri URI of the .sch file in repository
     * @param rawResolver Funktion, die eine URI auf das rohe Source (sch) auflöst
     * @return Source containing the generated XSLT
     */
    Source compileToXslt(URI schematronUri, Function<URI, Source> rawResolver);
}