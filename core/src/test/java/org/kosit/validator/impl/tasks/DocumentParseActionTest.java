package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.conformatron.api.model.validation.ECTValidationBaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.conformatron.SourceDigest;
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

    @Test
    public void testCheckCarriesConformatronParsedSource() {
        final CheckAction.Process process = new CheckAction.Process(read(Simple.SIMPLE_VALID));
        this.action.check(process);

        final ICTParsedValidationSource parsedSource = process.getParsedSource();
        assertThat(parsedSource).isNotNull();
        assertThat(parsedSource.isParsed()).isTrue();
        assertThat(parsedSource.getParsedContent()).isInstanceOf(XdmNode.class);
        assertThat(parsedSource.getAsDom()).isNotNull();
        assertThat(parsedSource.getAsDom().getDocumentElement()).isNotNull();
        assertThat(parsedSource.getSourceBytes()).isNotEmpty();
        assertThat(parsedSource.getHashAlgorithmName()).isEqualTo(SourceDigest.getAlgorithmName());
        assertThat(parsedSource.getHashBytes()).isEqualTo(SourceDigest.hashBytes(parsedSource.getSourceBytes()));
        assertThat(parsedSource.getSource().getDetectedSyntax()).isEqualTo(ECTValidationBaseType.XML);
    }

    @Test
    public void testCheckLeavesNoParsedSourceOnFailure() {
        final CheckAction.Process process = new CheckAction.Process(read(Simple.NOT_WELLFORMED));
        this.action.check(process);

        assertThat(process.getParsedSource()).isNull();
        assertThat(process.isStopped()).isTrue();
    }

}
