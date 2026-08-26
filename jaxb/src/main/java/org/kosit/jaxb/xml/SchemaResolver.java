package org.kosit.jaxb.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.jspecify.annotations.NonNull;
import org.xml.sax.SAXException;

/**
 * Provides the XML Schema from a source URL.
 */
public final class SchemaResolver {

    private SchemaResolver() {
    }

    public static @NonNull Source resolve(final @NonNull URL resource) {
        Objects.requireNonNull(resource);

        try {
            final String rawPath = resource.toURI().getRawPath();
            return new StreamSource(resource.openStream(), rawPath);
        } catch (final IOException | URISyntaxException e) {
            throw new IllegalStateException("Can not load schema for resource '" + resource.getPath() + "'", e);
        }
    }

    /**
     * Returns the parsed XML schema for the provided XSD.
     *
     * @param schemaUrl the schema URL to read
     * @return scenario schema
     */
    public static @NonNull Schema createParsedSchema(final @NonNull URL schemaUrl) {
        final Source source = resolve(schemaUrl);
        try {
            return XMLHelper.createSafeSchemaFactory().newSchema(source);
        } catch (final SAXException e) {
            throw new IllegalArgumentException("Can not load schema from source '" + source.getSystemId() + "'", e);
        }
    }
}
