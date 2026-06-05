package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.XMLSyntaxError;

import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the document parsing functionality.
 *
 * @author Andreas Penski
 */
public class DocumentParseActionTest {

    private DocumentParseAction action;

    @BeforeEach
    public void setup() {
        this.action = new DocumentParseAction(Helper.createProcessor());
    }

    @Test
    public void testSimple() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testIllformed() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.NOT_WELLFORMED));
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getObject()).isNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> this.action.parseDocument(null));

    }

}
