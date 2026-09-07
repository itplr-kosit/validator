package org.kost.validator.api.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.CTDetectionList;
import org.junit.jupiter.api.Test;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;

public class DetailedValidationResultTest {

    private static DetectionList createDetections(final String code) {
        return new DetectionList(Detection.builderError().code(code).location("test.xml").text("Something went wrong").build());
    }

    @Test
    public void allDetectionListsPresent() {
        final CTDetectionList parse = createDetections("parse");
        final CTDetectionList schema = createDetections("schema");
        final CTDetectionList schematron = createDetections("schematron");
        final DetailedValidationResult result = new DetailedValidationResult(parse, schema, schematron);

        assertThat(result.parseDetections()).isSameAs(parse);
        assertThat(result.schemaDetections()).isSameAs(schema);
        assertThat(result.schematronDetections()).isSameAs(schematron);
    }

    @Test
    public void onlyParseDetectionsPresent() {
        final CTDetectionList parse = createDetections("parse");
        final DetailedValidationResult result = new DetailedValidationResult(parse, null, null);

        assertThat(result.parseDetections()).isSameAs(parse);
        assertThat(result.schemaDetections()).isNull();
        assertThat(result.schematronDetections()).isNull();
    }

    @Test
    public void equalsAndHashCode() {
        final CTDetectionList parse = DetectionList.empty();
        final DetailedValidationResult result = new DetailedValidationResult(parse, null, null);

        assertThat(result).isEqualTo(new DetailedValidationResult(parse, null, null));
        assertThat(result).hasSameHashCodeAs(new DetailedValidationResult(parse, null, null));
        assertThat(result).isNotEqualTo(new DetailedValidationResult(parse, DetectionList.empty(), null));
        assertThat(result.toString()).contains("parseDetections");
    }
}
