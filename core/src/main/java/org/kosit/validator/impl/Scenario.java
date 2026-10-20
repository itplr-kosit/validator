package org.kosit.validator.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;
import org.kosit.base.uri.UriHelper;
import org.kosit.jaxb.adapter.StringTrimAdapter;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.StrictRelativeResolvingStrategy;
import org.kosit.validator.impl.conformatron.util.ScenarioXml;
import org.kosit.validator.scenario.generic.EScenarioKind;
import org.kosit.validator.scenario.generic.ScenarioDescription;
import org.kosit.validator.scenario.generic.ScenarioNamespace;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioSchematron;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;

/**
 * A scenario as the engine runs it: its declaration ({@code scenarios-v1.xsd}), the artifact repository the declaration
 * refers to, and the compiled match expression - nothing else.
 * <p>
 * The validation artifacts (XML Schema, Schematron) are deliberately <b>not</b> resolved or compiled here: step 5
 * ({@code RETRIEVE_ARTIFACTS}) resolves them confined to the repository and step 6 ({@code PREPARE_RULES}) compiles
 * them, so a missing or broken artifact is a finding in the report and not an exception while loading the
 * configuration. The only thing compiled up front is the match expression, because a scenario that cannot be matched is
 * a configuration error, not a property of any document. Artifacts a caller already holds compiled (builder API) travel
 * along as {@link #getPrecompiled() precompiled} and are passed through by step 5.
 * </p>
 * <p>
 * A scenario without a match expression <b>applies unconditionally</b>: it is a candidate for every document. That is
 * the shape of a scenario assembled at runtime from validation artifacts - "run this Schematron against this file" -
 * and it is allowed for a declared scenario as well.
 * </p>
 *
 * @author Andreas Penski
 * @author Andreas Schmitz
 */
public final class Scenario {

    private final org.kosit.validator.scenario.generic.Scenario declaration;

    private final byte @Nullable [] declarationXml;

    private final ContentRepository repository;

    private final @Nullable XPathExecutable match;

    private final @Nullable String definitionFile;

    private final Map<URI, CTCompiledValidationArtifact<?>> precompiled;

    /**
     * @param declaration the declaration of the scenario, in the version independent model
     * @param declarationXml the declaration as the configuration wrote it, embedded into the report by step
     *            {@code select-scenario}; {@code null} for a scenario no configuration document stands behind
     * @param repository the artifact repository of the scenario: where its artifacts are resolved, compiled and cached
     * @param match the compiled match expression, {@code null} for a scenario that applies unconditionally
     * @param definitionFile where the scenario was read from, for the report; {@code null} if it was assembled in code
     * @param precompiled artifacts handed over compiled, keyed by the reference declared for them; may be empty
     */
    public Scenario(final org.kosit.validator.scenario.generic.Scenario declaration, final byte @Nullable [] declarationXml,
            final ContentRepository repository, final @Nullable XPathExecutable match, final @Nullable String definitionFile,
            final Map<URI, CTCompiledValidationArtifact<?>> precompiled) {
        this.declaration = Objects.requireNonNull(declaration, "declaration may not be null");
        this.declarationXml = declarationXml;
        this.repository = Objects.requireNonNull(repository, "repository may not be null");
        this.match = match;
        this.definitionFile = definitionFile;
        this.precompiled = Map.copyOf(precompiled);
    }

    /**
     * Creates the scenario for a declaration, compiling its match expression if it has one.
     *
     * @param declaration the declaration of the scenario, in the version independent model
     * @param declarationXml the declaration as the configuration wrote it; may be {@code null}
     * @param repository the artifact repository of the scenario
     * @param definitionFile where the scenario was read from, for the report; may be {@code null}
     * @return the scenario
     * @throws IllegalStateException if the match expression does not compile
     */
    public static Scenario of(final org.kosit.validator.scenario.generic.Scenario declaration, final byte @Nullable [] declarationXml,
            final ContentRepository repository, final @Nullable String definitionFile) {
        final XPathExecutable match = StringHelper.isBlank(declaration.getMatch()) ? null
                : repository.createXPath(declaration.getMatch(), namespaces(declaration));
        return new Scenario(declaration, declarationXml, repository, match, definitionFile, Map.of());
    }

    /**
     * A scenario assembled at runtime from a single Schematron: "run this rule set against this document". It applies
     * unconditionally, validates with that rule set alone, and its artifact repository is the directory of the
     * Schematron — relative includes of the rule set resolve there and nowhere else. This is what replaces a separate
     * ad hoc engine: the same pipeline, the same report.
     *
     * @param processor the Saxon processor
     * @param schematron URI of the Schematron ({@code .sch}, or a precompiled {@code .xsl})
     * @param resolveInArchive {@code true} if the Schematron lives inside an archive
     *            ({@code jar:file:/some.jar!/rules/simple.sch})
     * @return the scenario
     * @throws IllegalArgumentException if no repository can be derived from the URI: it is relative, or it addresses an
     *             archive that may not be resolved in
     */
    public static Scenario adHoc(final Processor processor, final URI schematron, final boolean resolveInArchive) {
        return adHoc(processor, List.of(schematron), null, resolveInArchive);
    }

    /**
     * A scenario assembled at runtime from a set of validation artifacts: XML Schemas ({@code .xsd}), Schematrons
     * ({@code .sch}) and precompiled Schematron XSLTs ({@code .xsl}, {@code .xslt}), applied in the given order. The
     * scenario applies unconditionally. Its artifact repository is the given root, or - when none is given - the common
     * parent directory of all artifacts; every artifact must lie beneath it, and includes and imports of the artifacts
     * resolve there and nowhere else.
     *
     * @param processor the Saxon processor
     * @param artifacts URIs of the artifacts, at least one; the kind is read from the file extension
     * @param repository the artifact repository, or {@code null} for the common parent directory of the artifacts
     * @param resolveInArchive {@code true} if the artifacts live inside an archive
     * @return the scenario
     * @throws IllegalArgumentException if no repository can be derived, an artifact lies outside the repository, or an
     *             artifact has an unsupported kind
     */
    public static Scenario adHoc(final Processor processor, final List<URI> artifacts, final @Nullable URI repository,
            final boolean resolveInArchive) {
        Objects.requireNonNull(processor, "processor may not be null");
        // not List.contains(null): the immutable lists of List.of refuse that question with an exception
        if (artifacts == null || artifacts.isEmpty() || artifacts.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("At least one artifact is required");
        }
        // one spelling for every URI, so that file:/C:/x and file:///C:/x compare as the same place
        final List<URI> canonical = artifacts.stream().map(Scenario::canonical).toList();
        final URI base = repository != null ? canonical(repository) : commonParent(canonical, resolveInArchive);
        if (!base.isAbsolute()) {
            throw new IllegalArgumentException("Can not derive an artifact repository from " + artifacts
                    + (artifacts.stream().anyMatch(UriHelper::isArchiveUri) ? ", because resolving inside an archive is not enabled" : ""));
        }
        final String root = base.toASCIIString().endsWith("/") ? base.toASCIIString() : base.toASCIIString() + "/";

        final List<String> names = new ArrayList<>();
        final List<ScenarioResource> schemas = new ArrayList<>();
        final List<ScenarioResource> ruleSets = new ArrayList<>();
        for (final URI artifact : canonical) {
            if (!artifact.toASCIIString().startsWith(root)) {
                throw new IllegalArgumentException("The artifact '" + artifact + "' lies outside the repository '" + base + "'");
            }
            final String reference = artifact.toASCIIString().substring(root.length());
            final String name = reference.substring(reference.lastIndexOf('/') + 1);
            names.add(name);
            final ScenarioResource resource = new ScenarioResource(name).setLocation(reference);
            final String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".xsd")) {
                schemas.add(resource);
            } else if (lower.endsWith(".sch") || lower.endsWith(".xsl") || lower.endsWith(".xslt")) {
                ruleSets.add(resource);
            } else {
                throw new IllegalArgumentException("Unsupported artifact '" + name + "': expected .xsd, .sch, .xsl or .xslt");
            }
        }
        final String name = String.join(", ", names);
        final org.kosit.validator.scenario.generic.Scenario declaration = new org.kosit.validator.scenario.generic.Scenario(
                EScenarioKind.XML, name);
        declaration.setDescription(ScenarioDescription.ofText("Ad hoc validation against " + name));
        schemas.forEach(declaration::addXmlSchema);
        ruleSets.forEach(r -> declaration.addSchematron(new ScenarioSchematron(r)));

        // the strategy of ResolvingMode.STRICT_RELATIVE, but with the archive permission of this scenario
        final ContentRepository contentRepository = new ContentRepository(processor, new StrictRelativeResolvingStrategy(resolveInArchive),
                base);
        return new Scenario(declaration, ScenarioXml.toXmlBytes(declaration), contentRepository, null, null, Map.of());
    }

    /**
     * A file URI in the spelling {@link java.nio.file.Path#toUri()} produces ({@code file:///C:/…}), so that the URIs
     * of the artifacts and of a repository given by the caller compare as strings; any other URI as it is.
     */
    private static URI canonical(final URI uri) {
        if (uri.isAbsolute() && "file".equalsIgnoreCase(uri.getScheme()) && uri.getPath() != null) {
            final boolean directory = uri.getPath().endsWith("/");
            final URI file = java.nio.file.Paths.get(uri).toUri();
            return directory && !file.toASCIIString().endsWith("/") ? URI.create(file.toASCIIString() + "/") : file;
        }
        return uri;
    }

    /** The longest common directory of the artifacts: the parent of one artifact, or {@code /} of the URI at worst. */
    private static URI commonParent(final List<URI> artifacts, final boolean resolveInArchive) {
        URI common = canonical(UriHelper.resolve(artifacts.get(0), ".", resolveInArchive));
        for (final URI artifact : artifacts) {
            final String candidate = artifact.toASCIIString();
            while (common.isAbsolute() && !candidate.startsWith(common.toASCIIString())) {
                final URI up = canonical(UriHelper.resolve(common, "..", resolveInArchive));
                if (up.toASCIIString().equals(common.toASCIIString())) {
                    return URI.create("");
                }
                common = up;
            }
        }
        return common;
    }

    /**
     * @param configuration a scenario declaration
     * @return the namespace bindings it declares for its XPath expressions, prefix to URI
     */
    public static Map<String, String> namespaces(final org.kosit.validator.scenario.generic.Scenario declaration) {
        return declaration.getNamespaces().stream()
                .collect(Collectors.toMap(ScenarioNamespace::getPrefix, ns -> StringTrimAdapter.trim(ns.getNamespaceURI())));
    }

    public String getName() {
        return this.declaration.getName();
    }

    /** @return the declaration of the scenario, in the version independent model */
    public org.kosit.validator.scenario.generic.Scenario getDeclaration() {
        return this.declaration;
    }

    /**
     * The declaration as the configuration wrote it, which step {@code select-scenario} embeds into the report. It is
     * carried rather than written back from the model, so that the report shows what was applied and not a
     * normalisation of it.
     *
     * @return the declaration as XML, or {@code null} for a scenario no configuration document stands behind
     */
    public byte @Nullable [] getDeclarationXml() {
        return this.declarationXml;
    }

    /**
     * @return the coordinate the configuration declares for this scenario, as one string, or {@code null} when it
     *         declares none. This is what the report identifies the scenario by where it is set.
     */
    public @Nullable String getDeclaredId() {
        return this.declaration.hasCoordinate() ? this.declaration.getCoordinate().getAsSingleID() : null;
    }

    /** @return the artifact repository of the scenario */
    public ContentRepository getRepository() {
        return this.repository;
    }

    /** @return where the scenario was read from, for the report; {@code null} if it was assembled in code */
    public @Nullable String getDefinitionFile() {
        return this.definitionFile;
    }

    /** @return the compiled match expression, {@code null} for a scenario that applies unconditionally */
    public @Nullable XPathExecutable getMatchExecutable() {
        return this.match;
    }

    /** @return {@code true} if the scenario has no match expression and therefore applies to every document */
    public boolean isUnconditional() {
        return this.match == null;
    }

    /** @return the artifacts handed over compiled, keyed by the reference declared for them */
    public Map<URI, CTCompiledValidationArtifact<?>> getPrecompiled() {
        return this.precompiled;
    }

    /**
     * @param reference an artifact reference as declared by this scenario
     * @return the compilation handed over for it, if any
     */
    public Optional<CTCompiledValidationArtifact<?>> precompiled(final URI reference) {
        return Optional.ofNullable(this.precompiled.get(reference));
    }

    /**
     * Whether the scenario applies to the document: unconditionally, or because its match expression is true.
     * <p>
     * An expression that can not be evaluated over this document is <b>not</b> answered as {@code false}. "I could not
     * tell" and "it does not apply" are different statements, and turning the first into the second lets the document
     * run against another scenario, or against none, without anything saying so - a wrong verdict nobody sees. Step 3
     * reports the failure and cancels the run instead.
     * </p>
     *
     * @param document the parsed document
     * @return {@code true} if the scenario is a candidate for the document
     * @throws SaxonApiException if the match expression can not be evaluated over this document. The expression itself
     *             compiles - it is checked when the configuration is loaded - so this is a dynamic error raised by the
     *             shape of the document, e.g. a cast that fails on its content
     */
    public boolean matches(final XdmNode document) throws SaxonApiException {
        if (this.match == null) {
            return true;
        }
        final XPathSelector selector = this.match.load();
        selector.setContextItem(document);
        return selector.effectiveBooleanValue();
    }

    @Override
    public String toString() {
        return "Scenario[" + getName() + (isUnconditional() ? ", unconditional" : "") + "]";
    }
}
