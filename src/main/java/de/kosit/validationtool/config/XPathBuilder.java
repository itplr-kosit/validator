package de.kosit.validationtool.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.ArrayUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.model.Result;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Internal class to represent xpath configuration.
 * 
 * @author Andreas Penski
 */
@Data
class XPathBuilder implements Builder<XPathExecutable> {

    private static final String[] IGNORED_PREFIXES = new String[] { "xsd" };

    private String xpath;

    private XPathExecutable executable;

    @Setter(AccessLevel.PACKAGE)
    @Getter(AccessLevel.PACKAGE)
    private Map<String, String> namespaces;

    /**
     * Returns the xpath expression.
     * 
     * @return xpath expression
     */
    public String getXPath() {
        return this.xpath == null && this.executable != null ? this.executable.getUnderlyingExpression().getInternalExpression().toString()
                : this.xpath;

    }

    @Override
    public Result<XPathExecutable, String> build(final ContentRepository repository) {
        if (this.executable == null && this.xpath == null) {
            return createError("No configuration for xpath expression found");
        }
        if (this.executable == null) {
            this.executable = repository.createXPath(this.xpath, this.namespaces);
        } else {
            this.xpath = extractExpression();
            this.namespaces = extractNamespaces();
        }
        return new Result<>(this.executable);
    }

    private Map<String, String> extractNamespaces() {
        final Map<String, String> ns = new HashMap<>();
        final Iterator<String> iterator = this.executable.getUnderlyingExpression().getInternalExpression().getRetainedStaticContext()
                .iteratePrefixes();
        final Iterable<String> iterable = () -> iterator;
        StreamSupport.stream(iterable.spliterator(), false).filter(e -> !ArrayUtils.contains(IGNORED_PREFIXES, e)).forEach(e -> {
            ns.put(e,
                    this.executable.getUnderlyingExpression().getInternalExpression().getRetainedStaticContext().getURIForPrefix(e, false));
        });
        return ns;
    }

    private String extractExpression() {
        return this.executable.getUnderlyingExpression().getInternalExpression().toString();
    }

    private static Result<XPathExecutable, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
    }
}
