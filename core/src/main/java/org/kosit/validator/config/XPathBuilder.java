package org.kosit.validator.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Internal class to represent xpath configuration.
 * 
 * @author Andreas Penski
 */
class XPathBuilder implements Builder<XPathExecutable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(XPathBuilder.class);

    private static final String[] IGNORED_PREFIXES = new String[] { "xsd", "saxon", "xsl", "xs", "xml" };

    private final String name;

    private String xpath;

    private XPathExecutable executable;

    private Map<String, String> namespaces;

    private static SingleProcessingResult<XPathExecutable, String> createError(final String msg) {
        return new SingleProcessingResult<>(null, Collections.singletonList(msg));
    }

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
    public SingleProcessingResult<XPathExecutable, String> build(final ContentRepository repository) {
        if (!isAvailable()) {
            return createError("No configuration for " + this.name + " xpath  expression found");
        }
        try {
            if (this.executable == null) {
                this.executable = repository.createXPath(this.xpath, getNamespaces());
            } else {
                this.xpath = extractExpression();
                extractNamespaces();
            }
        } catch (final IllegalStateException e) {
            final String msg = "Error creating " + this.name + " xpath: " + e.getMessage();
            LOGGER.error(msg, e);
            return new SingleProcessingResult<>(Collections.singletonList(msg));
        }
        return new SingleProcessingResult<>(this.executable);
    }

    private void extractNamespaces() {
        final Map<String, String> ns = new HashMap<>();
        final Iterator<String> iterator = this.executable.getUnderlyingExpression().getInternalExpression().getRetainedStaticContext()
                .iteratePrefixes();
        final Iterable<String> iterable = () -> iterator;
        StreamSupport.stream(iterable.spliterator(), false).filter(e -> !ArrayUtils.contains(IGNORED_PREFIXES, e))
                .filter(StringUtils::isNotBlank).forEach(e -> ns.put(e, this.executable.getUnderlyingExpression().getInternalExpression()
                        .getRetainedStaticContext().getURIForPrefix(e, false).toString()));
        getNamespaces().putAll(ns);
    }

    private String extractExpression() {
        return this.executable.getUnderlyingExpression().getInternalExpression().toString();
    }

    public XPathBuilder(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public XPathExecutable getExecutable() {
        return this.executable;
    }

    public void setXpath(final String xpath) {
        this.xpath = xpath;
    }

    public void setExecutable(final XPathExecutable executable) {
        this.executable = executable;
    }

    void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
}
