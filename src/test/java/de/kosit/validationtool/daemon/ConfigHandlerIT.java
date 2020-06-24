package de.kosit.validationtool.daemon;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.restassured.http.ContentType;

/**
 * Integration test for the {@link ConfigHandler}.
 * 
 * @author Andreas Penski
 */
public class ConfigHandlerIT extends BaseIT {

    @Test
    public void checkHealth() {
        given().when().get("/server/config").then().statusCode(200).and().contentType(ContentType.XML);
    }
}
