package de.kosit.validationtool.cmd;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

/**
 * Testet the Daemon-Mode input , Methoden , Output Content-Type and the success case
 *
 * @author Roula Antoun
 */
public class DaemonIT {


    private static final String APPLICATION_XML = "application/xml";


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
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void makeSureThatSuccessTest() throws IOException {
        try ( final InputStream io = Simple.SIMPLE_VALID.toURL().openStream() ) {
            given().contentType(ContentType.XML).body(toContent(io)).when().post("/").then().statusCode(200);
        }
    }

    @Test
    public void NoInputTest() {
        given().body("").contentType(APPLICATION_XML).when().post("/").then().statusCode(400);
    }

    @Test
    @Ignore // no default error report yet
    public void internalServerErrorTest() throws IOException {
        try ( final InputStream io = Simple.INVALID.toURL().openStream() ) {
            given().contentType(APPLICATION_XML).body(toContent(io)).when().post("/").then().statusCode(200);
        }
    }

    private static byte[] toContent(final InputStream io) throws IOException {
        return IOUtils.toByteArray(io);
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

        try ( final InputStream io = Simple.SIMPLE_VALID.toURL().openStream() ) {
            given().body(toContent(io)).when().post("/").then().contentType(APPLICATION_XML).and().statusCode(200);
        }
    }
}
