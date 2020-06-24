package de.kosit.validationtool.daemon;

import org.junit.Before;

import io.restassured.RestAssured;

/**
 * Base for integration tests.
 * 
 * @author Andreas Penski
 */
public abstract class BaseIT {

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
}
