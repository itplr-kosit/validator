package org.kosit.validator.server.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/** The two bounds of the store — time and count — and the identity of what goes in and comes out. */
class ValidationRunStoreTest {

    private static byte[] cvr(final String marker) {
        return ("<reports>" + marker + "</reports>").getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void testWhatGoesInComesOutUnchanged() {
        final ValidationRunStore store = new ValidationRunStore(Duration.ofMinutes(10), 10);

        final UUID id = store.put(cvr("a"));

        assertArrayEquals(cvr("a"), store.get(id).orElseThrow());
        assertArrayEquals(cvr("a"), store.get(id).orElseThrow(), "fetching does not consume");
    }

    @Test
    void testAnUnknownIdentifierIsEmpty() {
        final ValidationRunStore store = new ValidationRunStore(Duration.ofMinutes(10), 10);

        assertTrue(store.get(UUID.randomUUID()).isEmpty());
        assertTrue(store.get(null).isEmpty());
    }

    @Test
    void testTheCapacityEvictsTheOldest() {
        final ValidationRunStore store = new ValidationRunStore(Duration.ofMinutes(10), 2);

        final UUID first = store.put(cvr("1"));
        final UUID second = store.put(cvr("2"));
        final UUID third = store.put(cvr("3"));

        assertEquals(2, store.size());
        assertTrue(store.get(first).isEmpty(), "the oldest made room");
        assertTrue(store.get(second).isPresent());
        assertTrue(store.get(third).isPresent());
    }

    @Test
    void testAnExpiredResultIsGone() throws InterruptedException {
        final ValidationRunStore store = new ValidationRunStore(Duration.ofMillis(50), 10);

        final UUID id = store.put(cvr("x"));
        Thread.sleep(120);

        assertTrue(store.get(id).isEmpty());
        assertEquals(0, store.size(), "the read evicted it");
    }

    @Test
    void testTheBoundsMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new ValidationRunStore(Duration.ZERO, 10));
        assertThrows(IllegalArgumentException.class, () -> new ValidationRunStore(Duration.ofMinutes(1), 0));
    }
}
