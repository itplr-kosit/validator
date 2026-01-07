package org.kosit.validator.server.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ValidationControllerTest {

    @ParameterizedTest
    @ValueSource(strings = { "src/test/resources/examples/simple/input/simple.xml",
            "src/test/resources/examples/simple/input/simple-sch-with-sch.xml" })
    void testValidateReturnsXmlReportAndHeaders(final String xmlFileStrg) {
        File xmlFile = Path.of(xmlFileStrg).toFile();

        given().contentType(ContentType.XML).body(xmlFile).when().post("/api/validate").then().statusCode(200).contentType(ContentType.XML)
                .header("Content-Disposition", allOf(containsString("attachment"), containsString("validation-result.xml")))
                .header("X-VALIDATOR-Acceptance", "ACCEPTABLE").header("X-VALIDATOR-Schema-Valid", "true")
                .header("X-VALIDATOR-Schematron-Valid", "true").body(startsWith("<ns5:reports"))
                .body(containsString("<ns5:validator name=\"Schematron Validator\""));
    }
}