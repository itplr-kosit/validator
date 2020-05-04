package de.kosit.validationtool.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.model.Result;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Internal class to represent xpath configuration.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Data
@Slf4j
class XPathBuilder implements Builder<XPathExecutable> {

    private static final String[] IGNORED_PREFIXES = new String[] { "xsd", "saxon", "xsl", "xs" };

    private final String name;

    private String xpath;

    private XPathExecutable executable;

    @Setter(AccessLevel.PACKAGE)
    private Map<String, String> namespaces;

    Map<String, String> getNamespaces() {
        if (this.namespaces == null) {
            this.namespaces = new HashMap<>();
        }
        return this.namespaces;
    }

    /**
     * Returns the xpath expression.
     * 
     * @return xpath expression
     */
    public String getXPath() {
        return this.xpath == null && this.executable != null ? this.executable.getUnderlyingExpression().getInternalExpression().toString()
                : this.xpath;
    }

    public boolean isAvailable() {
        return this.executable != null || isNotEmpty(this.xpath);
    }

    @Override
    public Result<XPathExecutable, String> build(final ContentRepository repository) {
        if (!isAvailable()) {
            return createError(String.format("No configuration for %s xpath  expression found", this.name));
        }
        try {
            if (this.executable == null) {
                this.executable = repository.createXPath(this.xpath, getNamespaces());
            } else {
                this.xpath = extractExpression();
                extractNamespaces();
            }
        } catch (final IllegalStateException e) {
            final String msg = String.format("Error creating %s xpath: %s", this.name, e.getMessage());
            log.error(msg, e);
            return new Result<>(Collections.singletonList(msg));

        }
        return new Result<>(this.executable);
    }

    private void extractNamespaces() {

        final Map<String, String> ns = new HashMap<>();
        final Iterator<String> iterator = this.executable.getUnderlyingExpression().getInternalExpression().getRetainedStaticContext()
                .iteratePrefixes();
        final Iterable<String> iterable = () -> iterator;
        StreamSupport.stream(iterable.spliterator(), false).filter(e -> !ArrayUtils.contains(IGNORED_PREFIXES, e))
                .filter(StringUtils::isNotBlank).forEach(e -> ns.put(e, this.executable.getUnderlyingExpression().getInternalExpression()
                        .getRetainedStaticContext().getURIForPrefix(e, false)));
        getNamespaces().putAll(ns);

    }

    private String extractExpression() {
        return this.executable.getUnderlyingExpression().getInternalExpression().toString();
    }

    private static Result<XPathExecutable, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
    }
}
