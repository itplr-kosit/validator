package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.conformance.CTDecision;
import org.conformatron.api.model.detection.CTDetection;
import org.junit.jupiter.api.Test;
import org.kosit.validator.TestHelper;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kosit.validator.testdata.TestResources;

/**
 * One engine, many threads: a server builds a single {@link ConformanceValidation} at startup and runs every request
 * through it, so everything the engine and its repositories hold has to survive concurrent use.
 * <p>
 * The engine itself keeps one artifact retrieval per repository; below it a {@code ContentRepository} caches compiled
 * Schematron transforms and XML Schemas and builds a {@code SchemaFactory} per compilation, and the Saxon processor is
 * a singleton. Each of those was a shared, unsynchronised object at some point. What makes a defect there expensive is
 * that it does not throw: it produces a different verdict on the same document, which is exactly what a validator must
 * never do.
 * </p>
 * <p>
 * The test therefore establishes a single-threaded verdict per document first and then demands that every concurrent
 * run reproduces it exactly - decision, run state, cancelling step and the codes of all detections. Documents are mixed
 * so that the caches are hit for different keys at the same time, and the set covers a completed run, both kinds of
 * finding, and two cancel paths.
 * </p>
 */
public class ConformanceValidationConcurrencyTest {

    private static final int THREADS = 16;

    private static final int RUNS_PER_THREAD = 20;

    private static final List<URI> DOCUMENTS = List.of(TestResources.Simple.SIMPLE_VALID, TestResources.Simple.SCHEMA_INVALID,
            TestResources.Simple.SCHEMATRON_INVALID, TestResources.Simple.NOT_WELLFORMED, TestResources.Simple.UNKNOWN);

    /** What a run is expected to produce; everything a caller of the engine acts on. */
    private record Verdict(CTDecision decision, boolean completed, CTActionType cancelledAt, List<String> detectionCodes) {

        static Verdict of(final ConformanceValidationResult result) {
            return new Verdict(result.getDecision(), result.isCompleted(), result.getCancelledAt(),
                    result.getAllDetections().stream().map(CTDetection::getCode).sorted().toList());
        }
    }

    private static ConformanceValidation createEngine() {
        final ScenarioSet configuration = ScenarioSet.load(TestResources.Simple.SCENARIOS_WITH_SCH, TestResources.Simple.REPOSITORY_URI)
                .setResolvingStrategy(TestHelper.getTestResolvingStrategy()).build(TestHelper.getTestProcessor());
        // the shared test repository lives inside an archive, so this engine is allowed to resolve into one
        return new ConformanceValidation(new TestEngineInformation(), TestHelper.getTestProcessor(), true, configuration);
    }

    @Test
    public void theSameEngineGivesTheSameVerdictOnEveryThread() throws Exception {
        final ConformanceValidation engine = createEngine();

        // the baseline, taken one document at a time on this thread
        final Map<URI, Verdict> expected = new LinkedHashMap<>();
        for (final URI document : DOCUMENTS) {
            expected.put(document, Verdict.of(engine.validate(TestHelper.read(document))));
        }

        final ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            final CountDownLatch start = new CountDownLatch(1);
            final List<Callable<List<Verdict>>> work = new ArrayList<>();
            for (int thread = 0; thread < THREADS; thread++) {
                final int offset = thread;
                work.add(() -> {
                    start.await();
                    final List<Verdict> verdicts = new ArrayList<>();
                    for (int run = 0; run < RUNS_PER_THREAD; run++) {
                        // threads walk the documents out of step, so different cache keys are in flight together
                        final URI document = DOCUMENTS.get((offset + run) % DOCUMENTS.size());
                        verdicts.add(Verdict.of(engine.validate(TestHelper.read(document))));
                    }
                    return verdicts;
                });
            }
            final List<Future<List<Verdict>>> running = new ArrayList<>();
            work.forEach(task -> running.add(pool.submit(task)));
            start.countDown();

            int checked = 0;
            for (int thread = 0; thread < running.size(); thread++) {
                // get() rethrows whatever the run threw, so a crash under load fails the test rather than vanishing
                final List<Verdict> verdicts = running.get(thread).get(120, TimeUnit.SECONDS);
                assertThat(verdicts).hasSize(RUNS_PER_THREAD);
                for (int run = 0; run < verdicts.size(); run++) {
                    final URI document = DOCUMENTS.get((thread + run) % DOCUMENTS.size());
                    assertThat(verdicts.get(run)).as("run %d of thread %d over %s", run, thread, document)
                            .isEqualTo(expected.get(document));
                    checked++;
                }
            }
            assertThat(checked).isEqualTo(THREADS * RUNS_PER_THREAD);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void theDocumentSetCoversACompletedRunBothFindingKindsAndTwoCancelPaths() {
        // guards the test above: if the fixtures ever stop producing different outcomes, it would still pass while
        // proving much less
        final ConformanceValidation engine = createEngine();
        final Map<URI, Verdict> verdicts = new LinkedHashMap<>();
        for (final URI document : DOCUMENTS) {
            verdicts.put(document, Verdict.of(engine.validate(TestHelper.read(document))));
        }

        assertThat(verdicts.get(TestResources.Simple.SIMPLE_VALID).completed()).isTrue();
        assertThat(verdicts.get(TestResources.Simple.SIMPLE_VALID).decision()).isEqualTo(CTDecision.ACCEPT);
        assertThat(verdicts.get(TestResources.Simple.SCHEMA_INVALID).decision()).isEqualTo(CTDecision.REJECT);
        assertThat(verdicts.get(TestResources.Simple.SCHEMATRON_INVALID).decision()).isEqualTo(CTDecision.REJECT);
        assertThat(verdicts.get(TestResources.Simple.NOT_WELLFORMED).cancelledAt()).isEqualTo(CTActionType.PARSE_DOCUMENT);
        assertThat(verdicts.get(TestResources.Simple.UNKNOWN).cancelledAt()).isEqualTo(CTActionType.DETECT_SCENARIOS);
    }
}
