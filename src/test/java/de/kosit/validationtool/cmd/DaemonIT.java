package de.kosit.validationtool.cmd;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import io.restassured.RestAssured;

/**
 * Testet the Daemon-Mode input , Methoden , Output Content-Type and the success case
 *
 * @author Roula Antoun
 */
public class DaemonIT {

    private static final String EXAMPLE_FILE = "examples/UBLReady/UBLReady_EU_UBL-NL_20170102_FULL.xml";

    private static final String APPLICATION_XML = "application/xml";

    private static final String INVALID_XML = "examples/UBLReady/UBLReady_EU_UBL-NL_20170102_FULL-invalid.xml";

    @Before
    public void setup() {
        final String port = System.getProperty("daemon.port");
        if (port != null) {
            RestAssured.port = Integer.valueOf(port);
        }
        final String baseHost = System.getProperty("daemon.host");
        if (baseHost != null) {
            RestAssured.baseURI = baseHost;
        }

    }

    @Test
    public void makeSureThatSuccessTest() throws IOException {
        try ( InputStream io = DaemonIT.class.getClassLoader().getResourceAsStream(EXAMPLE_FILE) ) {
            given().contentType(APPLICATION_XML).body(io).when().post("/").then().statusCode(200);
        }
    }

    @Test
    public void NoInputTest() {
        given().body("").contentType(APPLICATION_XML).when().post("/").then().statusCode(400);
    }

    @Test
    public void internalServerErrorTest() throws IOException {
        try ( InputStream io = DaemonIT.class.getClassLoader().getResourceAsStream(INVALID_XML) ) {
            given().contentType(APPLICATION_XML).body(io).when().post("/").then().statusCode(200);
        }
    }

    @Test
    public void methodNotAllowedTest() {
        given().when().get("/").then().statusCode(405);
        given().when().put("/").then().statusCode(405);
        given().when().patch("/").then().statusCode(405);
        given().when().delete("/").then().statusCode(405);
        given().when().head("/").then().statusCode(405);
        given().when().options("/").then().statusCode(405);
    }

    @Test
    public void xmlResultTest() throws IOException {
        try ( InputStream io = DaemonIT.class.getClassLoader().getResourceAsStream(EXAMPLE_FILE) ) {
            given().body(io).when().post("/").then().contentType(APPLICATION_XML).and().statusCode(200);
        }
    }
}
