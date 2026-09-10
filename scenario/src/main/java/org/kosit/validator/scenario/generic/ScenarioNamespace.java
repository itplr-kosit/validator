package org.kosit.validator.scenario.generic;

import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.string.StringHelper;

/**
 * A single XML namespace prefix mapping of a {@link Scenario}, used to evaluate the XPath expressions of
 * {@link Scenario#getMatch()} and {@link Scenario#getAcceptMatch()}. This class is immutable.
 *
 * @author Philip Helger
 */
public final class ScenarioNamespace {

    private final String prefix;

    private final String namespaceURI;

    private ScenarioNamespace(@NonNull @Nonempty final String prefix, @NonNull @Nonempty final String namespaceURI) {
        if (StringHelper.isEmpty(prefix)) {
            throw new IllegalArgumentException("Prefix must not be empty");
        }
        if (StringHelper.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException("Namespace URI must not be empty");
        }
        this.prefix = prefix;
        this.namespaceURI = namespaceURI;
    }

    /**
     * @return the namespace prefix. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getPrefix() {
        return this.prefix;
    }

    /**
     * @return the namespace URI. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getNamespaceURI() {
        return this.namespaceURI;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ScenarioNamespace rhs = (ScenarioNamespace) o;
        return this.prefix.equals(rhs.prefix) && this.namespaceURI.equals(rhs.namespaceURI);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.prefix, this.namespaceURI);
    }

    @Override
    public String toString() {
        return "ScenarioNamespace[prefix=" + this.prefix + "; namespaceURI=" + this.namespaceURI + "]";
    }

    /**
     * Factory method.
     *
     * @param prefix the namespace prefix. May neither be <code>null</code> nor empty.
     * @param namespaceURI the namespace URI. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioNamespace of(@NonNull @Nonempty final String prefix, @NonNull @Nonempty final String namespaceURI) {
        return new ScenarioNamespace(prefix, namespaceURI);
    }
}
