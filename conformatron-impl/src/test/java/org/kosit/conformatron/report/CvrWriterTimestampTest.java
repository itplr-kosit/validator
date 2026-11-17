package org.kosit.conformatron.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/**
 * Tests the timestamp format of {@link CvrWriter}. The serialization of whole runs is covered by {@code CvrWriterTest}
 * in {@code validator-core}, which drives the complete pipeline including scenario detection and selection.
 */
public class CvrWriterTimestampTest {

    @Test
    public void timestampAlwaysCarriesSeconds() {
        // the bug this guards against only appears when the seconds happen to be zero, so it needs a fixed moment:
        // OffsetDateTime.toString() drops them, and xs:dateTime rejects the result
        final OffsetDateTime onTheMinute = OffsetDateTime.of(2026, 8, 31, 15, 48, 0, 0, ZoneOffset.ofHours(2));

        assertThat(CvrWriter.timestamp(onTheMinute)).isEqualTo("2026-08-31T15:48:00+02:00");
        assertThat(CvrWriter.timestamp(onTheMinute.withSecond(7))).isEqualTo("2026-08-31T15:48:07+02:00");
    }
}
