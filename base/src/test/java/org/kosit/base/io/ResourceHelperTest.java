package org.kosit.base.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class ResourceHelperTest {

    private static final class CountingCloseable implements Closeable {

        private int closeCount;

        public void close() {
            this.closeCount++;
        }
    }

    @AfterEach
    public void resetTempDir() {
        ResourceHelper.setTempDir(null);
    }

    @SuppressWarnings("resource")
    @Test
    public void createTempFileRemembersTheFile() throws IOException {
        final ResourceHelper resHelper = new ResourceHelper();
        final File file = resHelper.createTempFile();

        assertThat(file).exists();
        assertThat(resHelper.getAllTempFiles()).containsExactly(file);

        resHelper.close();
        assertThat(file).doesNotExist();
    }

    @Test
    public void getAllTempFilesReturnsACopy() throws IOException {
        try ( final ResourceHelper resHelper = new ResourceHelper() ) {
            resHelper.createTempFile();

            resHelper.getAllTempFiles().clear();
            assertThat(resHelper.getAllTempFiles()).hasSize(1);
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void closeDeletesAllTempFiles() throws IOException {
        final ResourceHelper resHelper = new ResourceHelper();
        final File file1 = resHelper.createTempFile();
        final File file2 = resHelper.createTempFile();
        assertThat(resHelper.getAllTempFiles()).containsExactly(file1, file2);

        resHelper.close();
        assertThat(file1).doesNotExist();
        assertThat(file2).doesNotExist();
        assertThat(resHelper.getAllTempFiles()).isEmpty();
    }

    @Test
    public void closeIgnoresAnAlreadyDeletedTempFile() throws IOException {
        final File file;
        try ( final ResourceHelper resHelper = new ResourceHelper() ) {
            file = resHelper.createTempFile();
            Files.delete(file.toPath());
        }
        assertThat(file).doesNotExist();
    }

    @SuppressWarnings("resource")
    @Test
    public void closeClosesAllCloseables() {
        final ResourceHelper resHelper = new ResourceHelper();
        final CountingCloseable closeable1 = new CountingCloseable();
        final CountingCloseable closeable2 = new CountingCloseable();
        resHelper.addCloseable(closeable1);
        resHelper.addCloseable(closeable2);
        assertThat(resHelper.getAllCloseables()).containsExactly(closeable1, closeable2);

        resHelper.close();
        assertThat(closeable1.closeCount).isEqualTo(1);
        assertThat(closeable2.closeCount).isEqualTo(1);
        assertThat(resHelper.getAllCloseables()).isEmpty();
    }

    @SuppressWarnings("resource")
    @Test
    public void closeIsIdempotent() {
        final ResourceHelper resHelper = new ResourceHelper();
        final CountingCloseable closeable = new CountingCloseable();
        resHelper.addCloseable(closeable);

        resHelper.close();
        resHelper.close();
        assertThat(closeable.closeCount).isEqualTo(1);
    }

    @Test
    public void addCloseableRejectsNull() {
        try ( final ResourceHelper resHelper = new ResourceHelper() ) {
            assertThatThrownBy(() -> resHelper.addCloseable(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @SuppressWarnings("resource")
    @Test
    public void nothingCanBeAddedAfterClose() {
        final ResourceHelper resHelper = new ResourceHelper();
        resHelper.close();

        assertThatThrownBy(resHelper::createTempFile).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> resHelper.addCloseable(new CountingCloseable())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void tempFilesAreCreatedInTheConfiguredTempDir() throws IOException {
        final File tempDir = Files.createTempDirectory("validator-test-").toFile();
        try {
            ResourceHelper.setTempDir(tempDir);
            assertThat(ResourceHelper.getTempDir()).isEqualTo(tempDir);

            try ( final ResourceHelper resHelper = new ResourceHelper() ) {
                assertThat(resHelper.createTempFile().getParentFile()).isEqualTo(tempDir);
            }
        } finally {
            ResourceHelper.setTempDir(null);
            Files.delete(tempDir.toPath());
        }
    }

    @Test
    public void tempDirMustBeAnExistingDirectory() {
        assertThatThrownBy(() -> ResourceHelper.setTempDir(new File("does-not-exist"))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("is not a directory");
        assertThat(ResourceHelper.getTempDir()).isNull();
    }
}
