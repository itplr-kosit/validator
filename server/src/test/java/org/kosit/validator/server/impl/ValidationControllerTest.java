package org.kosit.validator.server.impl;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.File;
import java.util.UUID;

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
