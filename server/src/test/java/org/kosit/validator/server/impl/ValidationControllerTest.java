package org.kosit.validator.server.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kosit.validator.testdata.TestData;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * The resource contract: creating a run answers 201 with a Location, fetching the result answers with the CVR, and a
 * document that can not be parsed still creates a run.
 */
@QuarkusTest
class ValidationControllerTest {

    private static final String UUID_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    private static Response create(final String xmlFile) {
        return given().contentType(ContentType.XML).body(new File(TestData.file(xmlFile))).when().post("/api/validation");
    }

    @ParameterizedTest
    @ValueSource(strings = { "examples/simple/input/simple.xml", "examples/simple/input/simple-sch-with-sch.xml" })
    void testCreatingARunAnswersCreatedWithALocation(final String xmlFile) {
        create(xmlFile).then().statusCode(201).contentType(ContentType.JSON)
                .header("Location", matchesRegex(".*/api/validation/result/" + UUID_PATTERN)).body("id", matchesRegex(UUID_PATTERN))
                .body("status", is("completed")).body("result", matchesRegex(".*/api/validation/result/" + UUID_PATTERN));
    }

    @Test
    void testTheResultIsTheCvrOfTheRun() {
        final String location = create("examples/simple/input/simple.xml").then().statusCode(201).extract().header("Location");

        given().when().get(location).then().statusCode(200).contentType(ContentType.XML)
                .body(startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"))
                .body(containsString("xmlns=\"http://www.xproc.org/ns/xvrl\""))
                .body(containsString("xmlns:cvr=\"urn:conformatron:cvr:draft\"")).body(containsString("cvr:status=\"COMPLETED\""))
                .body(containsString("cvr:conformant=\"true\"")).body(containsString("<creator name=\"decision-recommendation\"/>"))
                .body(containsString("cvr:decision=\"ACCEPT\""));
    }

    @Test
    void testTheResultCanBeFetchedMoreThanOnce() {
        final String location = create("examples/simple/input/simple.xml").then().extract().header("Location");

        final String first = given().when().get(location).then().statusCode(200).extract().asString();
        final String second = given().when().get(location).then().statusCode(200).extract().asString();
        org.junit.jupiter.api.Assertions.assertEquals(first, second);
    }

    @Test
    void testADocumentThatDoesNotParseStillCreatesARun() {
        // not a bad request: the pipeline cancelled at parse-document, and that is what the report says
        final String location = create("examples/simple/input/no-xml.file").then().statusCode(201).body("status", is("completed")).extract()
                .header("Location");

        given().when().get(location).then().statusCode(200).contentType(ContentType.XML).body(containsString("cvr:status=\"CANCELLED\""))
                .body(containsString("cvr:conformant=\"false\"")).body(containsString("cvr:decision=\"REJECT\""))
                .body(not(containsString("<creator name=\"detect-scenarios\"/>")));
    }

    private static final String ADHOC = "/api/validation/adhoc";

    private static File testFile(final String name) {
        return new File(TestData.file(name));
    }

    /** An ad hoc request with the document and the given resources as single files. */
    private static Response createAdHoc(final String xmlFile, final String... resources) {
        io.restassured.specification.RequestSpecification request = given().multiPart("document", testFile(xmlFile), "application/xml");
        for (final String resource : resources) {
            request = request.multiPart("resource", testFile(resource), "application/xml");
        }
        return request.when().post(ADHOC);
    }

    private static String resultOf(final Response created) {
        return created.then().statusCode(201).contentType(ContentType.JSON).body("status", is("completed")).extract().header("Location");
    }

    @Test
    void testAnAdHocRunValidatesAgainstThePostedSchematron() {
        final String location = resultOf(createAdHoc("examples/simple/input/simple.xml", "examples/simple/repository/simple.sch"));

        // the report is a CVR like for every other run; its scenario is named after the posted rule set
        given().when().get(location).then().statusCode(200).contentType(ContentType.XML).body(containsString("cvr:decision=\"ACCEPT\""))
                .body(containsString("scenario-id=\"simple.sch\""));
    }

    @Test
    void testTheSchematronPartOfTheFirstVersionIsStillAccepted() {
        final String location = resultOf(given().multiPart("document", testFile("examples/simple/input/simple.xml"), "application/xml")
                .multiPart("schematron", testFile("examples/simple/repository/simple.sch"), "application/xml").when().post(ADHOC));

        given().when().get(location).then().statusCode(200).body(containsString("scenario-id=\"simple.sch\""));
    }

    @Test
    void testAnAdHocRunAcceptsAPrecompiledSchematron() {
        final String location = resultOf(createAdHoc("examples/simple/input/simple.xml", "examples/simple/repository/simple.xsl"));

        given().when().get(location).then().statusCode(200).body(containsString("cvr:decision=\"ACCEPT\""))
                .body(containsString("scenario-id=\"simple.xsl\""));
    }

    @Test
    void testAnAdHocRunReportsTheViolations() {
        final String location = resultOf(
                createAdHoc("examples/simple/input/simple-schematron-invalid.xml", "examples/simple/repository/simple.sch"));

        given().when().get(location).then().statusCode(200).body(containsString("cvr:decision=\"REJECT\""));
    }

    @Test
    void testAnAdHocRunAppliesASetOfArtifacts() {
        // schema and rules together: the schema rejects what the rules alone would accept
        final String location = resultOf(createAdHoc("examples/simple/input/simple-schema-invalid.xml",
                "examples/simple/repository/simple.xsd", "examples/simple/repository/simple.sch"));

        given().when().get(location).then().statusCode(200).body(containsString("scenario-id=\"simple.xsd, simple.sch\""))
                .body(containsString("cvr:decision=\"REJECT\"")).body(containsString("code=\"schema-violation\""));
    }

    @Test
    void testAResourceWithoutUsableNameIsTypedByItsRootElement() {
        // no file name on the part: the kind is read from the root element, the name generated
        final String location = resultOf(given().multiPart("document", testFile("examples/simple/input/simple.xml"), "application/xml")
                .multiPart("resource", "rules", readBytes("examples/simple/repository/simple.sch"), "application/xml").when().post(ADHOC));

        given().when().get(location).then().statusCode(200).body(containsString("scenario-id=\"resource-1.sch\""))
                .body(containsString("cvr:decision=\"ACCEPT\""));
    }

    @Test
    void testARepositoryZipResolvesIncludes() throws IOException {
        // the modular Schematron of e2e/adhoc/rules: the only rule lives in an included file
        final Path rules = Paths.get("..", "e2e", "adhoc", "rules");
        final byte[] zip = zipOf(rules, "with-include.sch", "abstracts.sch");

        final String accepted = resultOf(given().multiPart("document", testFile("examples/simple/input/simple.xml"), "application/xml")
                .multiPart("repository", "rules.zip", zip, "application/zip").multiPart("artifact", "with-include.sch").when().post(ADHOC));
        given().when().get(accepted).then().statusCode(200).body(containsString("cvr:decision=\"ACCEPT\""))
                .body(containsString("scenario-id=\"with-include.sch\""));

        // the included rule fires for a document with another root
        final String rejected = resultOf(given().multiPart("document", testFile("examples/simple/input/foo.xml"), "application/xml")
                .multiPart("repository", "rules.zip", zip, "application/zip").multiPart("artifact", "with-include.sch").when().post(ADHOC));
        given().when().get(rejected).then().statusCode(200).body(containsString("cvr:decision=\"REJECT\"")).body(containsString("inc-1"));
    }

    @Test
    void testAnAdHocRunNeedsTheDocumentAndAtLeastOneArtifact() {
        given().multiPart("document", testFile("examples/simple/input/simple.xml"), "application/xml").when().post(ADHOC).then()
                .statusCode(400).body("message", containsString("at least one artifact"));
        given().multiPart("resource", testFile("examples/simple/repository/simple.sch"), "application/xml").when().post(ADHOC).then()
                .statusCode(400).body("message", containsString("document"));
    }

    @Test
    void testAnArtifactOfUnknownKindIsABadRequest() {
        // not XML at all, and an unknown entry of a repository
        createAdHoc("examples/simple/input/simple.xml", "examples/simple/repository/some.txt").then().statusCode(400).body("message",
                containsString("Unsupported artifact"));
        given().multiPart("document", testFile("examples/simple/input/simple.xml"), "application/xml")
                .multiPart("repository", "rules.zip", zipOfNothing(), "application/zip").multiPart("artifact", "missing.sch").when()
                .post(ADHOC).then().statusCode(400).body("message", containsString("no entry"));
    }

    private static byte[] readBytes(final String name) {
        try {
            return Files.readAllBytes(testFile(name).toPath());
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] zipOf(final Path directory, final String... entries) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try ( ZipOutputStream zip = new ZipOutputStream(bytes) ) {
            for (final String entry : entries) {
                zip.putNextEntry(new ZipEntry(entry));
                zip.write(Files.readAllBytes(directory.resolve(entry)));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static byte[] zipOfNothing() {
        try {
            return zipOf(Paths.get("."));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testAnUnknownRunIsNotFound() {
        given().when().get("/api/validation/result/" + UUID.randomUUID()).then().statusCode(404);
    }

    @Test
    void testAMalformedIdentifierIsNotFound() {
        given().when().get("/api/validation/result/not-a-uuid").then().statusCode(404);
    }

    @Test
    void testTheLocationEndsWithTheIdOfTheBody() {
        final Response response = create("examples/simple/input/simple.xml");
        final String id = response.then().extract().path("id");

        response.then().header("Location", endsWith("/api/validation/result/" + id));
    }
}
