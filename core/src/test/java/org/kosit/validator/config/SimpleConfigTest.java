package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;

import org.conformatron.api.model.conformance.CTDecision;
import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.testdata.TestResources;

/**
 * A configuration assembled through the builder API drives the engine like a loaded one.
 *
 * @author Andreas Penski
 */
public class SimpleConfigTest {

    @Test
    public void testSimpleWithApi() {
        final VConfiguration config = createSimpleConfiguration().build(TestHelper.getTestProcessor());
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        final ConformanceValidation engine = new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true,
                config);

        final ConformanceValidationResult result = engine.validate(TestHelper.read(TestResources.Simple.SIMPLE_VALID));

        assertThat(result).isNotNull();
        assertThat(result.isCompleted())
                .as("cancelled at %s: %s / %s", result.getCancelledAt(), result.getRationale(), result.getProcessingErrors()).isTrue();
        assertThat(result.getDecision()).isEqualTo(CTDecision.ACCEPT);
    }
}
