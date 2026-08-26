package org.kosit.validator.cmd.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.fusesource.jansi.AnsiRenderer.Code;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Format} and especially that {@link Format#mergeCodes(Collection)} still behaves like the array based
 * implementation that was used before the code cleansing.
 *
 * @author Philip Helger
 */
public class FormatTest {

    private static Format createFormat(final List<Code> ownCodes, final Code textColor, final Code background) {
        return new Format().addCodes(ownCodes.toArray(new Code[0])).color(textColor).background(background);
    }

    @Test
    public void emptyFormatMergesToNothing() {
        assertThat(new Format().mergeCodes(List.of())).isEmpty();
    }

    @Test
    public void explicitTextColorOnly() {
        assertThat(new Format().color(Code.RED).mergeCodes(List.of())).containsExactly(Code.RED);
    }

    @Test
    public void explicitTextColorAndBackground() {
        assertThat(new Format().color(Code.RED).background(Code.BG_BLUE).mergeCodes(List.of())).containsExactly(Code.RED, Code.BG_BLUE);
    }

    @Test
    public void plainAttributesAreKept() {
        assertThat(new Format().addCodes(Code.BOLD, Code.UNDERLINE).mergeCodes(List.of(Code.ITALIC))).containsExactly(Code.BOLD,
                Code.UNDERLINE, Code.ITALIC);
        assertThat(new Format().addCodes(Code.BOLD).color(Code.RED).background(Code.BG_BLUE).mergeCodes(List.of(Code.UNDERLINE)))
                .containsExactly(Code.BOLD, Code.UNDERLINE, Code.RED, Code.BG_BLUE);
    }

    @Test
    public void firstColorWinsEvenIfItIsABackgroundColor() {
        assertThat(new Format().addCodes(Code.BG_GREEN).mergeCodes(List.of(Code.RED))).containsExactly(Code.RED, Code.BG_GREEN);
        assertThat(new Format().addCodes(Code.RED).mergeCodes(List.of(Code.BG_GREEN))).containsExactly(Code.RED, Code.BG_GREEN);
    }

    @Test
    public void nullCodesAreIgnored() {
        assertThat(new Format().addCodes((Code[]) null).getCodes()).isEmpty();
        assertThat(new Format().addCodes(null, Code.BOLD).mergeCodes(Arrays.asList(null, Code.RED, null))).containsExactly(Code.BOLD,
                Code.RED);
    }

    @Test
    public void ownCodesAreDeduplicatedAndKeepInsertionOrder() {
        final Format format = new Format().addCodes(Code.BOLD, Code.UNDERLINE).addCodes(Code.BOLD);
        assertThat(format.getCodes()).containsExactly(Code.BOLD, Code.UNDERLINE);
        assertThat(format.mergeCodes(List.of())).containsExactly(Code.BOLD, Code.UNDERLINE);
    }

    @Test
    public void duplicatesBetweenOwnAndNewCodesArePreserved() {
        // Like the array based version, the merging itself does not deduplicate
        final List<Code> ownCodes = List.of(Code.BOLD);
        final List<Code> newCodes = List.of(Code.BOLD);
        assertThat(createFormat(ownCodes, null, null).mergeCodes(newCodes)).containsExactly(Code.BOLD, Code.BOLD);
    }

    @Test
    public void resultIsModifiable() {
        final List<Code> merged = new Format().color(Code.RED).mergeCodes(List.of());
        merged.add(Code.BOLD);
        assertThat(merged).containsExactly(Code.RED, Code.BOLD);
    }
}
