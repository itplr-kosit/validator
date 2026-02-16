package org.kosit.validator.client;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kosit.validator.model.mvrl.AcceptanceStatusType;
import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.model.xvrl.XVRLReportSummary;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ValidationClientIT {

    @Inject
    ValidationClient validationClient;

    @Test
    void shouldValidateXml() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        XVRLReportSummary result = validationClient.validate(input);

        Assertions.assertNotNull(result);
        assertFalse(result.getReports().isEmpty(), "Response should not be empty");
    }

    @Test
    void shouldValidateMinimalXml() {
        File input = Path.of("src/test/resources/examples/simple/input/simple.xml").toFile();

        MVRLCompactReport result = validationClient.validateMinimal(input);

        Assertions.assertNotNull(result.getResult());
        assertFalse(result.getResult().isEmpty());
        assertEquals(AcceptanceStatusType.ACCEPTABLE, result.getResult().get(0).getAcceptance());
        assertTrue(result.getAcceptable() > 0);
    }
}