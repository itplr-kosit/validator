package de.kosit.validationtool.daemon;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import de.kosit.validationtool.impl.Helper.Simple;

import io.restassured.http.ContentType;

/**
 * Testet the Daemon-Mode input , Methoden , Output Content-Type and the success case
 *
 * @author Roula Antoun
 * @author Andreas Penski
 */
public class CheckHandlerIT extends BaseIT {

    private static final String APPLICATION_XML = "application/xml";

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
        try ( final InputStream io = Simple.SCHEMA_INVALID.toURL().openStream() ) {
            given().contentType(APPLICATION_XML).body(toContent(io)).when().post("/").then().statusCode(200);
        }
    }

    private static byte[] toContent(final InputStream io) throws IOException {
        return IOUtils.toByteArray(io);
    }



    @Test
    public void xmlResultTest() throws IOException {

        try ( final InputStream io = Simple.SIMPLE_VALID.toURL().openStream() ) {
            given().body(toContent(io)).when().post("/").then().contentType(APPLICATION_XML).and().statusCode(200);
        }
    }
}
