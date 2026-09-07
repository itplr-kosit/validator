package org.kost.validator.api.saxon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.junit.jupiter.api.Test;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.s9api.Processor;

public class ProcessorProviderTest {

    @Test
    public void processorIsASingleton() {
        final Processor processor = ProcessorProvider.getProcessor();
        assertThat(processor).isNotNull();
        assertThat(ProcessorProvider.getProcessor()).isSameAs(processor);
    }

    @Test
    public void securityRelevantFeaturesAreDisabled() {
        final Processor processor = ProcessorProvider.getProcessor();
        assertThat(processor.getConfigurationProperty(Feature.DTD_VALIDATION)).isFalse();
        assertThat(processor.getConfigurationProperty(Feature.XINCLUDE)).isFalse();
        assertThat(processor.getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)).isFalse();
    }

    @Test
    public void collectionResolvingIsDisabled() {
        final Configuration config = ProcessorProvider.getProcessor().getUnderlyingConfiguration();
        assertThatThrownBy(() -> config.getCollectionFinder().findCollection(null, "http://example.org/collection"))
                .isInstanceOf(IllegalStateException.class).hasMessage(ProcessorProvider.SecureUriResolver.MESSAGE);
    }

    @Test
    public void unparsedTextResolvingIsDisabled() {
        final Configuration config = ProcessorProvider.getProcessor().getUnderlyingConfiguration();
        assertThatThrownBy(() -> config.getUnparsedTextURIResolver().resolve(URI.create("http://example.org/text.txt"), "UTF-8", config))
                .isInstanceOf(IllegalStateException.class).hasMessage(ProcessorProvider.SecureUriResolver.MESSAGE);
    }

    @Test
    public void outputResolvingIsDisabled() {
        final Configuration config = ProcessorProvider.getProcessor().getUnderlyingConfiguration();
        final OutputURIResolver resolver = config.getDefaultXsltCompilerInfo().getOutputURIResolver();
        assertThat(resolver).isNotNull();
        assertThatThrownBy(() -> resolver.newInstance()).isInstanceOf(IllegalStateException.class)
                .hasMessage(ProcessorProvider.SecureUriResolver.MESSAGE);
        assertThatThrownBy(() -> resolver.resolve("result.xml", "file:/tmp/")).isInstanceOf(IllegalStateException.class)
                .hasMessage(ProcessorProvider.SecureUriResolver.MESSAGE);
        assertThatThrownBy(() -> resolver.close(null)).isInstanceOf(IllegalStateException.class)
                .hasMessage(ProcessorProvider.SecureUriResolver.MESSAGE);
    }
}
