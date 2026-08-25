package org.kosit.validator.impl.conformatron.source;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.impl.input.StreamHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.GuardedBy;

/**
 * A resource manager that keeps track of temporary files and other closables that will be closed when this manager is
 * closed. When calling {@link #createTempFile()} a new filename is created and added to the list. When using
 * {@link #addCloseable(Closeable)} the Closable is added for postponed closing.
 *
 * @author Philip Helger
 */
public class ResourceHelper implements Closeable {

    private static final String TEMP_FILE_PREFIX = "validator-res-";

    private static final String TEMP_FILE_SUFFIX = ".tmp";

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelper.class);

    private static File s_aTempDir;

    /**
     * @return The temp file directory to use, or <code>null</code> for the system default.
     */
    @Nullable
    public static File getTempDir() {
        return s_aTempDir;
    }

    /**
     * Set a temporary directory to use.
     *
     * @param aTempDir The directory to use. It must be an existing directory. May be <code>null</code> to use the
     *            system default.
     * @throws IllegalArgumentException If the directory does not exist
     */
    public static void setTempDir(@Nullable final File aTempDir) {
        if (aTempDir != null)
            if (!aTempDir.isDirectory())
                throw new IllegalArgumentException("Temporary directory '" + aTempDir.getAbsolutePath() + "' is not a directory");
        s_aTempDir = aTempDir;
    }

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final AtomicBoolean m_aInClose = new AtomicBoolean(false);

    @GuardedBy("m_aRWLock")
    private final List<File> tempFiles = new ArrayList<>();

    @GuardedBy("m_aRWLock")
    private final List<Closeable> m_aCloseables = new ArrayList<>();

    public ResourceHelper() {
    }

    /**
     * @return A new temporary {@link File} that will be deleted when {@link #close()} is called on this instance.
     * @throws IOException When temp file creation fails.
     * @throws IllegalStateException If {@link #close()} was already called before
     */
    @NonNull
    public File createTempFile() throws IOException {
        if (m_aInClose.get())
            throw new IllegalStateException("ResourceManager is already closing/closed!");

        // Create
        final File ret = s_aTempDir != null ? Files.createTempFile(s_aTempDir.toPath(), TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile()
                : Files.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX).toFile();

        // And remember
        rwLock.writeLock().lock();
        try {
            tempFiles.add(ret);
        } finally {
            rwLock.writeLock().unlock();
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("ResourceManager created temporary file '" + ret.getAbsolutePath() + "'");

        return ret;
    }

    /**
     * @return A list of all known temp files. Never <code>null</code> but maybe empty.
     */
    @NonNull
    public List<File> getAllTempFiles() {
        rwLock.readLock().lock();
        try {
            // Create a copy
            return new ArrayList<>(tempFiles);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Add a new closable for later closing.
     *
     * @param aCloseable The closable to be closed later. May not be <code>null</code>.
     * @throws IllegalStateException If {@link #close()} was already called before
     */
    public void addCloseable(@NonNull final Closeable aCloseable) {
        Objects.requireNonNull(aCloseable);

        if (m_aInClose.get())
            throw new IllegalStateException("ResourceManager is already closing/closed!");

        m_aCloseables.add(aCloseable);
    }

    /**
     * @return A list of all known closables. Never <code>null</code> but maybe empty.
     */
    @NonNull
    public List<Closeable> getAllCloseables() {
        rwLock.readLock().lock();
        try {
            // Create a copy
            return new ArrayList<>(m_aCloseables);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void close() {
        // Avoid taking new objects
        // close only once
        if (!m_aInClose.getAndSet(true)) {
            // Close all closeables before deleting files, because the closables might
            // be the files to be deleted :)
            final List<Closeable> aCloseables;
            rwLock.writeLock().lock();
            try {
                // Create a copy
                aCloseables = new ArrayList<>(m_aCloseables);
                m_aCloseables.clear();
            } finally {
                rwLock.writeLock().unlock();
            }
            if (!aCloseables.isEmpty()) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Closing " + aCloseables.size() + " stream handles");

                for (final Closeable aCloseable : aCloseables) {
                    StreamHelper.close(aCloseable);
                }
            }

            // Get and delete all temp files
            final List<File> aTempFiles;
            rwLock.writeLock().lock();
            try {
                aTempFiles = new ArrayList<>(tempFiles);
                tempFiles.clear();
            } finally {
                rwLock.writeLock().unlock();
            }
            if (!aTempFiles.isEmpty()) {
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("Deleting " + aTempFiles.size() + " temporary files");

                for (final File aFile : aTempFiles) {
                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("Deleting temporary file '" + aFile.getAbsolutePath() + "'");

                    if (aFile.exists())
                        if (!aFile.delete())
                            LOGGER.warn("  Failed to delete temporary file " + aFile.getAbsolutePath());
                }
            }
        }
    }
}
