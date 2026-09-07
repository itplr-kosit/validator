package org.kost.validator.api.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class CollectingSaxErrorHandlerTest {

    private static final String RESOURCE_ID = "test.xml";

    private static SAXParseException createException() {
        return new SAXParseException("Element type must be terminated", null, "system.xml", 4, 17);
    }

    @Test
    public void warningIsIgnored() {
        final List<CTDetection> errors = new ArrayList<>();
        new CollectingSaxErrorHandler(RESOURCE_ID, errors).warning(createException());
        assertThat(errors).isEmpty();
    }

    @Test
    public void errorIsCollected() {
        final List<CTDetection> errors = new ArrayList<>();
        final SAXParseException ex = createException();
        new CollectingSaxErrorHandler(RESOURCE_ID, errors).error(ex);

        assertThat(errors).hasSize(1);
        final CTDetection detection = errors.get(0);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(4);
        assertThat(detection.getLocation().getColumnNumber()).isEqualTo(17);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Element type must be terminated");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }

    @Test
    public void fatalErrorIsCollectedAndRethrown() {
        final List<CTDetection> errors = new ArrayList<>();
        final SAXParseException ex = createException();
        final CollectingSaxErrorHandler handler = new CollectingSaxErrorHandler(RESOURCE_ID, errors);

        assertThatThrownBy(() -> handler.fatalError(ex)).isSameAs(ex);
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
        assertThat(errors.get(0).getLinkedException()).isSameAs(ex);
    }

    @Test
    public void allErrorsAreCollected() {
        final List<CTDetection> errors = new ArrayList<>();
        final CollectingSaxErrorHandler handler = new CollectingSaxErrorHandler(RESOURCE_ID, errors);
        handler.error(createException());
        handler.error(createException());
        handler.warning(createException());

        assertThat(errors).hasSize(2);
        assertThat(handler.resourceId()).isEqualTo(RESOURCE_ID);
        assertThat(handler.errors()).isSameAs(errors);
    }
}
