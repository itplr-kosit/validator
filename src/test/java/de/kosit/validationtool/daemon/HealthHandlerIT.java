package de.kosit.validationtool.daemon;

import static io.restassured.RestAssured.given;

import org.junit.Test;

import io.restassured.http.ContentType;

/**
 * Checks the health endpoint.
 * 
 * @author Andreas Penski
 */
public class HealthHandlerIT extends BaseIT {

    @Test
    public void checkHealth() {
        given().when().get("/server/health").then().statusCode(200).and().contentType(ContentType.XML);
    }
}
