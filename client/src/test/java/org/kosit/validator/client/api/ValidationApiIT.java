package org.kosit.validator.client.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kosit.validator.testdata.TestData;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ValidationApiIT {

    @RestClient
    ValidationApi validationApi;

    @Test
    void shouldValidateXml() throws IOException {
        File input = new File(TestData.file("examples/simple/input/simple.xml"));

        File result = validationApi.validate(input);

        Assertions.assertNotNull(result);
        assertTrue(result.length() > 0, "Response should not be empty");
        String content = Files.readString(result.toPath());
        assertFalse(content.isBlank(), "Response XML should not be blank");
    }

    @Test
    void shouldValidateMinimalXml() {
        File input = new File(TestData.file("examples/simple/input/simple.xml"));

        File result = validationApi.validateMinimal(input);

        Assertions.assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(Exception.class, () -> {
            File invalid = new File(TestData.file("examples/simple/input/no-xml.file"));
            validationApi.validate(invalid);
        });
    }
}