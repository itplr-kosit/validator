package org.kosit.validator.server.impl;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
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

    @Test
    void testValidateMinimalJson() {
        File xmlFile = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        given().contentType(ContentType.XML).body(xmlFile).accept(MediaType.APPLICATION_JSON).when().post("/api/validate/minimal").then()
                .statusCode(200).contentType(ContentType.JSON).body("acceptable", is(1)).body("rejected", is(0))
                .body("processingErrors", is(0)).body("results", hasSize(1)).body("results[0].schema", is(true))
                .body("results[0].schematron", is(true)).body("results[0].acceptance", is("ACCEPTABLE"))
                .header("Content-Disposition", allOf(containsString("attachment"), containsString("compact-validation-result.json")))
                .header("X-VALIDATOR-Acceptance", "ACCEPTABLE").header("X-VALIDATOR-Schema-Valid", "true")
                .header("X-VALIDATOR-Schematron-Valid", "true");
    }

    @Test
    void testValidateMinimalXml() {
        File xmlFile = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        given().contentType(ContentType.XML).body(xmlFile).accept(MediaType.APPLICATION_XML).when().post("/api/validate/minimal").then()
                .statusCode(200).contentType(ContentType.XML).body(containsString("http://www.xoev.de/de/validator/framework/2/mvrl"))
                .body(containsString("acceptable=\"1\"")).body(containsString("rejected=\"0\""))
                .body(containsString("processingerrors=\"0\""))
                .header("Content-Disposition", allOf(containsString("attachment"), containsString("validation-result.xml")))
                .header("X-VALIDATOR-Acceptance", "ACCEPTABLE").header("X-VALIDATOR-Schema-Valid", "true")
                .header("X-VALIDATOR-Schematron-Valid", "true").body(startsWith("<compactreport")).body(containsString("<result"));
    }
}