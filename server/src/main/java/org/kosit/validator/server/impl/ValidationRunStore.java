package org.kosit.validator.server.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.kosit.validator.server.config.ValidationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Keeps the results of validation runs so that a client can fetch them after creating the run — the resource behind
 * {@code GET /api/validation/result/{id}}.
 * <p>
 * A result is kept as the serialized CVR, not as the run object: the run holds the parsed source document, the bytes
 * are what a client receives, and the bytes are what a server can afford to keep. Two limits bound the store, both from
 * the configuration: how long a result is kept ({@code validator.results.retention}) and how many at most
 * ({@code validator.results.capacity}). Expired results go on the next write; when the store is still full after that,
 * the oldest result goes. There is no scheduler — eviction rides on the writes, which is enough for a bound and spares
 * a dependency.
 * </p>
 * <p>
 * The store is per instance. Two instances behind a load balancer do not see each other's results; a deployment that
 * needs that puts a shared store behind this interface.
 * </p>
 */
@ApplicationScoped
public class ValidationRunStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRunStore.class);

    /**
     * One kept result: when it was created, its place in the order of creation, and the CVR. The sequence exists
     * because several results can be created within the same millisecond — "oldest" has to mean something then.
     */
    private record Entry(Instant created, long sequence, byte[] cvr) {
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    private final AtomicLong sequence = new AtomicLong();

    private final Duration retention;

    private final int capacity;

    /** The bean constructor — CDI needs it marked, because the class has a second constructor for tests. */
    @Inject
    public ValidationRunStore(final ValidationConfig config) {
        this(config.results().retention(), config.results().capacity());
    }

    ValidationRunStore(final Duration retention, final int capacity) {
        if (retention == null || retention.isNegative() || retention.isZero()) {
            throw new IllegalArgumentException("retention must be a positive duration");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be at least 1");
        }
        this.retention = retention;
        this.capacity = capacity;
    }

    /**
     * Keeps a result and hands back the identifier a client fetches it by.
     *
     * @param cvr the serialized report
     * @return the identifier of the run
     */
    public UUID put(final byte[] cvr) {
        if (cvr == null) {
            throw new IllegalArgumentException("cvr may not be null");
        }
        evict();
        final UUID id = UUID.randomUUID();
        this.entries.put(id, new Entry(Instant.now(), this.sequence.incrementAndGet(), cvr));
        return id;
    }

    /**
     * @param id the identifier of a run
     * @return its report, if it is known and has not expired
     */
    public Optional<byte[]> get(final UUID id) {
        final Entry entry = id == null ? null : this.entries.get(id);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry, Instant.now())) {
            this.entries.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.cvr());
    }

    /** @return how many results are kept right now */
    public int size() {
        return this.entries.size();
    }

    private boolean isExpired(final Entry entry, final Instant now) {
        return entry.created().plus(this.retention).isBefore(now);
    }

    /** Drops what has expired, then what exceeds the capacity — oldest first. */
    private void evict() {
        final Instant now = Instant.now();
        this.entries.entrySet().removeIf(e -> isExpired(e.getValue(), now));
        while (this.entries.size() >= this.capacity) {
            final UUID oldest = this.entries.entrySet().stream().min(Comparator.comparingLong(e -> e.getValue().sequence()))
                    .map(Map.Entry::getKey).orElse(null);
            if (oldest == null || this.entries.remove(oldest) == null) {
                break;
            }
            LOGGER.debug("Evicted result {} to stay within the capacity of {}", oldest, this.capacity);
        }
    }
}
