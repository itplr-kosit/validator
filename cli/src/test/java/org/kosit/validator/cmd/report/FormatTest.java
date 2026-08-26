package org.kosit.validator.cmd.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fusesource.jansi.AnsiRenderer.Code;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Format} and especially that {@link Format#mergeCodes(Collection)} still behaves like the array based
 * implementation that was used before the code cleansing.
 *
 * @author Philip Helger
 */
public class FormatTest {

    /**
     * The attribute selection of the array based implementation. {@link Code#isColor()} is <code>true</code> for the
     * background colors as well, so this combination only ever selected the {@code BG_*} codes and dropped all real
     * attributes.
     */
    private static final Predicate<Code> LEGACY_ATTRIBUTES = c -> c.isBackground() && c.isColor();

    /** The corrected attribute selection */
    private static final Predicate<Code> FIXED_ATTRIBUTES = Code::isAttribute;

    /**
     * A faithful copy of the array based {@code mergeCodes} implementation as it was before the refactoring, but
     * without the {@code ArrayUtils} usage, with the previously implicit {@code codes} iteration order made explicit
     * and with the attribute selection made configurable.
     *
     * @param ownCodes the codes contained in the {@link Format} itself
     * @param textColor the explicit text color of the {@link Format}. May be <code>null</code>.
     * @param background the explicit background color of the {@link Format}. May be <code>null</code>.
     * @param newCodes the codes to be merged in
     * @param attributeFilter the filter to select the attributes to be kept
     * @return the merged codes in the legacy order
     */
    private static Code[] legacyMergeCodes(final List<Code> ownCodes, final Code textColor, final Code background,
            final List<Code> newCodes, final Predicate<Code> attributeFilter) {
        final List<Code> all = new ArrayList<>(ownCodes);
        all.addAll(newCodes);
        all.add(textColor);
        all.add(background);
        final Code[] allCodes = all.toArray(new Code[0]);

        final Optional<Code> color = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isColor).findFirst();
        final Optional<Code> bg = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isBackground).findFirst();
        final List<Code> attributes = Arrays.stream(allCodes).filter(Objects::nonNull).filter(attributeFilter).collect(Collectors.toList());
        attributes.add(color.orElse(textColor));
        attributes.add(bg.orElse(background));
        return attributes.stream().filter(Objects::nonNull).toArray(Code[]::new);
    }

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
    public void legacyImplementationDroppedThePlainAttributes() {
        final List<Code> ownCodes = List.of(Code.BOLD);
        final List<Code> newCodes = List.of(Code.RED);
        // The array based version dropped BOLD, because only BG_* codes passed its attribute filter
        assertThat(legacyMergeCodes(ownCodes, null, null, newCodes, LEGACY_ATTRIBUTES)).containsExactly(Code.RED);
        assertThat(createFormat(ownCodes, null, null).mergeCodes(newCodes)).containsExactly(Code.BOLD, Code.RED);
    }

    @Test
    public void legacyImplementationEmittedBackgroundColorsTwice() {
        // BG_BLUE was contained in the attributes and as the resolved background color
        assertThat(legacyMergeCodes(List.of(), Code.RED, Code.BG_BLUE, List.of(), LEGACY_ATTRIBUTES)).containsExactly(Code.BG_BLUE,
                Code.RED, Code.BG_BLUE);
        assertThat(new Format().color(Code.RED).background(Code.BG_BLUE).mergeCodes(List.of())).containsExactly(Code.RED, Code.BG_BLUE);
    }

    @Test
    public void firstColorWinsEvenIfItIsABackgroundColor() {
        // BG_GREEN is a color as well, so it is picked as text color, and RED never makes it into the result
        assertThat(new Format().addCodes(Code.BG_GREEN).mergeCodes(List.of(Code.RED))).containsExactly(Code.BG_GREEN, Code.BG_GREEN);
        // The other way around the text color is found first
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

    @Test
    public void sameResultAsLegacyImplementation() {
        final List<Code> none = List.of();
        final List<List<Object>> scenarios = List.of(Arrays.asList("empty", none, null, null, none),
                Arrays.asList("text color only", none, Code.RED, null, none),
                Arrays.asList("background only", none, null, Code.BG_BLUE, none),
                Arrays.asList("text color and background", none, Code.RED, Code.BG_BLUE, none),
                Arrays.asList("attributes only", List.of(Code.BOLD, Code.UNDERLINE), null, null, List.of(Code.ITALIC)),
                Arrays.asList("own background, new text color", List.of(Code.BG_GREEN), null, null, List.of(Code.RED)),
                Arrays.asList("own text color, new background", List.of(Code.RED), null, null, List.of(Code.BG_GREEN)),
                Arrays.asList("null codes", none, Code.RED, null, Arrays.asList(null, Code.BOLD, null)),
                Arrays.asList("duplicates", List.of(Code.BOLD), null, null, List.of(Code.BOLD)), Arrays.asList("all mixed",
                        List.of(Code.BOLD, Code.BG_RED), Code.GREEN, Code.BG_BLUE, Arrays.asList(Code.UNDERLINE, null, Code.BG_YELLOW)));

        for (final List<Object> scenario : scenarios) {
            final String name = (String) scenario.get(0);
            @SuppressWarnings("unchecked")
            final List<Code> ownCodes = (List<Code>) scenario.get(1);
            final Code textColor = (Code) scenario.get(2);
            final Code background = (Code) scenario.get(3);
            @SuppressWarnings("unchecked")
            final List<Code> newCodes = (List<Code>) scenario.get(4);

            // Identical to the array based version, except for the corrected attribute selection
            final List<Code> actual = createFormat(ownCodes, textColor, background).mergeCodes(newCodes);
            final List<Code> expected = Arrays.asList(legacyMergeCodes(ownCodes, textColor, background, newCodes, FIXED_ATTRIBUTES));
            assertThat(actual).as(name).isEqualTo(expected);
        }
    }
}
