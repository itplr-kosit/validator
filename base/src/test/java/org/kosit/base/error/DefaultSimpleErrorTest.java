package org.kosit.base.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

public class DefaultSimpleErrorTest {

    @Test
    public void buildMinimum() {
        final DefaultSimpleError error = DefaultSimpleError.builder().message("Something went wrong").build();
        assertThat(error.getSystemID()).isNull();
        assertThat(error.getLineNumber()).isEqualTo(-1);
        assertThat(error.hasLineNumber()).isFalse();
        assertThat(error.getLineNumberObj()).isNull();
        assertThat(error.getColumnNumber()).isEqualTo(-1);
        assertThat(error.hasColumnNumber()).isFalse();
        assertThat(error.getColumnNumberObj()).isNull();
        assertThat(error.hasLineOrColumnNumber()).isFalse();
        assertThat(error.getSeverity()).isEqualTo(SimpleErrorBuilder.DEFAULT_SEVERITY);
        assertThat(error.getMessage()).isEqualTo("Something went wrong");
        assertThat(error.getLinkedException()).isNull();
        assertThat(error.hasLinkedException()).isFalse();
    }

    @Test
    public void buildAllFields() {
        final IOException ex = new IOException("boom");
        final DefaultSimpleError error = DefaultSimpleError.builderWarning().location("file.xml", 12, 34).message("Attribute is missing")
                .linkedException(ex).build();
        assertThat(error.getSystemID()).isEqualTo("file.xml");
        assertThat(error.getLineNumber()).isEqualTo(12);
        assertThat(error.hasLineNumber()).isTrue();
        assertThat(error.getLineNumberObj()).isEqualTo(Long.valueOf(12));
        assertThat(error.getColumnNumber()).isEqualTo(34);
        assertThat(error.hasColumnNumber()).isTrue();
        assertThat(error.getColumnNumberObj()).isEqualTo(Long.valueOf(34));
        assertThat(error.hasLineOrColumnNumber()).isTrue();
        assertThat(error.getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(error.getMessage()).isEqualTo("Attribute is missing");
        assertThat(error.getLinkedException()).isSameAs(ex);
        assertThat(error.hasLinkedException()).isTrue();
    }

    @Test
    public void locationFromSAXParseException() {
        final SAXParseException ex = new SAXParseException("Invalid content", null, "file.xml", 7, 3);
        final DefaultSimpleError error = DefaultSimpleError.builderError().location(ex).message(ex.getMessage()).build();
        assertThat(error.getSystemID()).isEqualTo("file.xml");
        assertThat(error.getLineNumber()).isEqualTo(7);
        assertThat(error.getColumnNumber()).isEqualTo(3);
        assertThat(error.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
    }

    @Test
    public void getAsString() {
        assertThat(DefaultSimpleError.builder().message("Something went wrong").build().getAsString()).isEqualTo("Something went wrong");
        assertThat(DefaultSimpleError.builderError().location("file.xml", 7, 3).message("Invalid content").build().getAsString())
                .isEqualTo("Invalid content at line 7 at pos 3");
    }

    @Test
    public void copyBuilderCreatesEqualObject() {
        final DefaultSimpleError error = DefaultSimpleError.builderNone().location("file.xml", 1, 2).message("Just a note")
                .linkedException(new IOException("boom")).build();
        final DefaultSimpleError copy = DefaultSimpleError.builder(error).build();
        assertThat(copy).isNotSameAs(error).isEqualTo(error).hasSameHashCodeAs(error);
        assertThat(copy.toString()).isEqualTo(error.toString());
    }

    @Test
    public void equalsAndHashCode() {
        final DefaultSimpleError error = DefaultSimpleError.builder().message("msg").build();
        assertThat(error).isEqualTo(error).isNotEqualTo(null).isNotEqualTo("msg");
        assertThat(DefaultSimpleError.builder().message("msg").build()).isEqualTo(error);
        assertThat(DefaultSimpleError.builder().message("other").build()).isNotEqualTo(error);
        assertThat(DefaultSimpleError.builderWarning().message("msg").build()).isNotEqualTo(error);
        assertThat(DefaultSimpleError.builder().message("msg").lineNumber(1).build()).isNotEqualTo(error);
    }

    @Test
    public void messageIsMandatory() {
        assertThatThrownBy(() -> DefaultSimpleError.builder().build()).isInstanceOf(IllegalStateException.class)
                .hasMessage("The message must be provided");
    }

    @Test
    public void severityMustNotBeNull() {
        assertThatThrownBy(() -> DefaultSimpleError.builder().severity(null)).isInstanceOf(NullPointerException.class);
    }
}
