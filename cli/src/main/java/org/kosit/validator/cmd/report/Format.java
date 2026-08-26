package org.kosit.validator.cmd.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.fusesource.jansi.AnsiRenderer.Code;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Simple value holder for ansi formatting codes.
 * 
 * @author Andreas Penski
 */
public class Format {

    private Code textColor;

    private Code background;

    private final SequencedSet<Code> codes = new LinkedHashSet<>();

    public @NonNull List<@NonNull Code> mergeCodes(final @NonNull Collection<@Nullable Code> newCodes) {
        final List<Code> allCodes = new ArrayList<>(this.codes);
        for (final Code c : newCodes)
            if (c != null)
                allCodes.add(c);
        if (this.textColor != null)
            allCodes.add(this.textColor);
        if (this.background != null)
            allCodes.add(this.background);

        final Code color = allCodes.stream().filter(c -> c.isColor() && !c.isBackground()).findFirst().orElse(this.textColor);
        final Code bg = allCodes.stream().filter(Code::isBackground).findFirst().orElse(this.background);

        // Everything that is neither a text color nor a background color. Note: Code.isColor() is true for the
        // background colors as well, so they must not be selected here.
        final List<Code> attributes = allCodes.stream().filter(Code::isAttribute).collect(Collectors.toList());
        if (color != null)
            attributes.add(color);
        if (bg != null)
            attributes.add(bg);
        return attributes;
    }

    /**
     * Sets explicit text color.
     *
     * @param textColor the color.
     *
     * @return this {@link Format}
     */
    public @NonNull Format color(final @Nullable Code textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * Sets explicit background color.
     *
     * @param color the color.
     *
     * @return this {@link Format}
     */
    public @NonNull Format background(final @Nullable Code color) {
        this.background = color;
        return this;
    }

    /**
     * Adds additional formatting codes.
     *
     * @param codes the codes
     *
     * @return this {@link Format}
     */
    public @NonNull Format addCodes(final @Nullable Code @Nullable... codes) {
        if (codes != null)
            for (final Code c : codes)
                if (c != null)
                    this.codes.add(c);
        return this;
    }

    public @Nullable Code getTextColor() {
        return this.textColor;
    }

    public @Nullable Code getBackground() {
        return this.background;
    }

    public @NonNull Set<@NonNull Code> getCodes() {
        return this.codes;
    }
}
