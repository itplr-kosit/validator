package org.kosit.validator.client.api;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ValidationApiIT {

    @RestClient
    ValidationApi validationApi;

    @Test
    void shouldValidateXml() throws IOException {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        File result = validationApi.validate(input);

        Assertions.assertNotNull(result);
        assertTrue(result.length() > 0, "Response should not be empty");
        String content = Files.readString(result.toPath());
        assertFalse(content.isBlank(), "Response XML should not be blank");
    }

    @Test
    void shouldValidateMinimalXml() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        File result = validationApi.validateMinimal(input);

        Assertions.assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(Exception.class, () -> {
            File invalid = Path.of("src/test/resources/examples/simple/input/no-xml.file").toFile();
            validationApi.validate(invalid);
        });
    }
}