package org.kosit.base.io.mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kosit.base.io.ResourceHelper;

public class ResourceHelperExtensionTest {

    @RegisterExtension
    private final ResourceHelperExtension resHelper = new ResourceHelperExtension();

    @Test
    public void theRegisteredExtensionProvidesAHelper() throws IOException {
        final ResourceHelper helper = resHelper.get();
        assertThat(helper).isNotNull();
        // the same helper is handed out for the whole test method
        assertThat(resHelper.get()).isSameAs(helper);
        assertThat(helper.createTempFile()).exists();
    }

    @Test
    public void everyTestMethodGetsItsOwnHelper() {
        // the extension was reset by afterEach, so this can never be the helper of the previous test method
        assertThat(resHelper.get().getAllTempFiles()).isEmpty();
    }

    @Test
    public void getFailsOutsideOfATestMethod() {
        // the callbacks ignore the context, so it does not need to be a real one
        assertThatThrownBy(new ResourceHelperExtension()::get).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No ResourceHelper is available");
    }

    @Test
    public void theHelperIsClosedAfterEachTestMethod() throws IOException {
        final ResourceHelperExtension extension = new ResourceHelperExtension();
        extension.beforeEach(null);
        final ResourceHelper helper = extension.get();
        final File tempFile = helper.createTempFile();
        assertThat(tempFile).exists();

        extension.afterEach(null);
        assertThat(tempFile).doesNotExist();
        assertThatThrownBy(extension::get).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void everyBeforeEachCreatesANewHelper() {
        final ResourceHelperExtension extension = new ResourceHelperExtension();
        extension.beforeEach(null);
        final ResourceHelper first = extension.get();
        extension.afterEach(null);

        extension.beforeEach(null);
        assertThat(extension.get()).isNotSameAs(first);
        extension.afterEach(null);
    }

    @Test
    public void afterEachWithoutBeforeEachIsHarmless() {
        final ResourceHelperExtension extension = new ResourceHelperExtension();
        extension.afterEach(null);
        assertThatThrownBy(extension::get).isInstanceOf(IllegalStateException.class);
    }
}
