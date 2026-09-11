package org.kost.validator.api.saxon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void everyThreadGetsTheSameHardenedProcessor() throws Exception {
        // the processor is shared across request threads of the server; a lazily assigned field could hand one of them
        // a reference to a processor whose hardening is not visible yet
        final int threads = 16;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final CountDownLatch start = new CountDownLatch(1);
            final List<Future<Processor>> handed = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                handed.add(pool.submit(() -> {
                    start.await();
                    return ProcessorProvider.getProcessor();
                }));
            }
            start.countDown();

            final Processor expected = ProcessorProvider.getProcessor();
            for (final Future<Processor> future : handed) {
                final Processor processor = future.get(30, TimeUnit.SECONDS);
                assertThat(processor).isSameAs(expected);
                assertThat(processor.getConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS)).isFalse();
            }
        } finally {
            pool.shutdownNow();
        }
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
