package org.kosit.validator.testdata;

import java.net.URI;

public final class TestResources {

    public static class Invalid {

        public static final URI ROOT = TestData.dir("examples/invalid/");

        public static final URI SCENARIOS = TestData.file("examples/invalid/scenarios.xml");

        public static final URI SCENARIOS_ILLFORMED = TestData.file("examples/invalid/scenarios-illformed.xml");
    }

    public static class Resolving {

        public static final URI ROOT = TestData.dir("examples/resolving/");

        public static final URI SCHEMA_WITH_REFERENCE = TestData.file("examples/resolving/main.xsd");

        public static final URI SCHEMA_WITH_REMOTE_REFERENCE = TestData.file("examples/resolving/withRemote.xsd");
    }

    public static class Simple {

        public static final URI ROOT = TestData.dir("examples/simple/");

        public static final URI EXAMPLES = TestData.dir("examples/simple/input/");

        public static final URI SIMPLE_VALID = TestData.file("examples/simple/input/simple.xml");

        public static final URI SIMPLE_ISO_VALID = TestData.file("examples/simple/input/simple-iso.xml");

        /**
         * Valid instance that is actually encoded in ISO-8859-1 (not UTF-8), for the base64 embedding path.
         */
        public static final URI SIMPLE_LATIN1 = TestData.file("examples/simple/input/simple-latin1.xml");

        public static final URI FOO = TestData.file("examples/simple/input/foo.xml");

        public static final URI FOO_SCHEMATRON_INVALID = TestData.file("examples/simple/input/foo-schematron-invalid.xml");

        public static final URI REJECTED = TestData.file("examples/simple/input/withManualReject.xml");

        public static final URI SCENARIOS = TestData.file("examples/simple/scenarios.xml");

        public static final URI SCENARIOS_WITH_SCH = TestData.file("examples/simple/scenarios-with-sch.xml");

        /** compiler at the rule set, customLevel below validateWithSchematron, no createReport, empty acceptMatch */
        public static final URI SCENARIOS_WRITTEN_FOR_2_0 = TestData.file("examples/simple/scenarios-accept-match-empty.xml");

        /** a scenario matched by expression plus a scenario without match, which applies unconditionally */
        public static final URI SCENARIOS_WITH_UNCONDITIONAL = TestData.file("examples/simple/scenarios-with-unconditional.xml");

        /** Configuration with two scenarios matching the same document, for the ambiguity path. */
        public static final URI SCENARIOS_AMBIGUOUS = TestData.file("examples/simple/scenarios-ambiguous.xml");

        /**
         * Configuration whose scenario references a rule set that does not resolve (step 5 failure).
         */
        public static final URI SCENARIOS_ARTIFACT_MISSING = TestData.file("examples/simple/scenarios-artifact-missing.xml");

        /**
         * Configuration whose scenario references a rule set that resolves but does not compile (step 6 failure).
         */
        public static final URI SCENARIOS_RULES_BROKEN = TestData.file("examples/simple/scenarios-rules-broken.xml");

        /**
         * Configuration whose scenario references a rule set that fails while running (step 7 failure).
         */
        public static final URI SCENARIOS_ENGINE_ERROR = TestData.file("examples/simple/scenarios-engine-error.xml");

        public static final URI SCENARIOS_WITH_RELATIVE_PATHS = TestData.file("examples/simple/scenarios-with-relative-paths.xml");

        public static final URI OTHER_SCENARIOS = TestData.file("examples/simple/otherScenarios.xml");

        public static final URI SCENARIOS_WITH_MANY_CONFIGS = TestData.file("examples/simple/scenarios-with-many-configs.xml");

        public static final URI ERROR_SCENARIOS = TestData.file("examples/simple/scenarios-with-errors.xml");

        public static final URI REPOSITORY_URI = TestData.dir("examples/simple/repository/");

        public static final URI SCHEMA_INVALID = TestData.file("examples/simple/input/simple-schema-invalid.xml");

        public static final URI SCHEMATRON_INVALID = TestData.file("examples/simple/input/simple-schematron-invalid.xml");

        public static final URI NOT_WELLFORMED = TestData.file("examples/simple/input/simple-not-wellformed.xml");

        public static final URI UNKNOWN = TestData.file("examples/simple/input/unknown.xml");

        public static final URI GARBAGE = TestData.file("examples/simple/input/no-xml.file");

        public static final URI NOT_EXISTING = TestData.missing("examples/", "doesnotexist");

        public static final URI REPORT_XSL = TestData.file("examples/simple/repository/report.xsl");

        public static final URI SCHEMA = TestData.file("examples/simple/repository/simple.xsd");

        public static final URI SCHEMATRON = TestData.file("examples/simple/repository/simple-schematron-error.xsl");
    }

    /**
     * Repository that lives inside an archive instead of an unpacked directory, for the tests covering that code path.
     *
     * @return the URI of the packaged repository, never {@code null}
     */
    public static URI getJarRepository() {
        return TestData.inArchive("simple/packaged/repository/");
    }

    private TestResources() {
    }
}
