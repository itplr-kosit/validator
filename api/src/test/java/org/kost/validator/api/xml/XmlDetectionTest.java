package org.kost.validator.api.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HexFormat;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.source.CTValidationSource;
import org.junit.jupiter.api.Test;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.conformatron.source.ValidationSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;

public class XmlDetectionTest {

    private static final String RESOURCE_ID = "test.xml";

    private static CTValidationSource createSource() throws IOException {
        return ValidationSource.completeXml(ReadResource.inMemory(Resource.utf8(RESOURCE_ID, "<root/>")));
    }

    @Test
    public void success() throws IOException {
        final CTValidationSource source = createSource();
        final Detection detection = XmlDetection.success(source);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.NONE);
        assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_DOCUMENT_PARSED);
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getLocation().hasLineNumber()).isFalse();
        assertThat(detection.getText().getDisplayText(Locale.ROOT))
                .isEqualTo(ReadResource.HASH_ALGORITHM_NAME + "=" + HexFormat.of().formatHex(source.getReadResource().getHashBytes()));
        assertThat(detection.getLinkedException()).isNull();
    }

    @Test
    public void errorNotWellformedFromGenericException() {
        final SAXException ex = new SAXException("Something is broken");
        final Detection detection = XmlDetection.errorNotWellformed(RESOURCE_ID, ex);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getLocation().hasLineNumber()).isFalse();
        assertThat(detection.getLocation().hasColumnNumber()).isFalse();
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Something is broken");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }

    @Test
    public void errorNotWellformedFromSaxParseException() {
        final SAXParseException ex = new SAXParseException("Element type must be terminated", null, "other.xml", 7, 3);
        final Detection detection = XmlDetection.errorNotWellformed(RESOURCE_ID, ex);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_NOT_WELLFORMED);
        // the resource ID always wins over the system ID of the exception
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(7);
        assertThat(detection.getLocation().getColumnNumber()).isEqualTo(3);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Element type must be terminated");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }

    @Test
    public void ioError() {
        final IOException ex = new IOException("Stream closed");
        final Detection detection = XmlDetection.ioError(RESOURCE_ID, ex);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isEqualTo(XmlDetection.CODE_SOURCE_READ_ERROR);
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Stream closed");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }

    @Test
    public void saxonApiErrorWithoutDetails() {
        final SaxonApiException ex = new SaxonApiException("Transformation failed");
        final CTDetection detection = XmlDetection.saxonApiError(RESOURCE_ID, ex);
        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isNull();
        // no system ID in the exception - the resource ID is used as the fallback
        assertThat(detection.getLocation().getResourceId()).isEqualTo(RESOURCE_ID);
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(CTDetectionLocation.ILLEGAL_NUMBER);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Transformation failed");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }

    @Test
    public void saxonApiErrorWithErrorCodeAndLocation() {
        final XPathException cause = new XPathException("Type error", "XPTY0004", new Loc("other.xml", 12, 4));
        final SaxonApiException ex = new SaxonApiException(cause);
        final CTDetection detection = XmlDetection.saxonApiError(RESOURCE_ID, ex);
        assertThat(detection.getCode()).contains("XPTY0004");
        // the system ID of the exception wins over the resource ID
        assertThat(detection.getLocation().getResourceId()).isEqualTo("other.xml");
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(12);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).contains("Type error");
        assertThat(detection.getLinkedException()).isSameAs(ex);
    }
}
