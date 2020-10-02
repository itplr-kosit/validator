/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.cmd.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.NoArgsConstructor;

/**
 * Helper for printing a colored lines (with newline at the end) to the console.
 */
@NoArgsConstructor
public class Line {

    private final List<Text> texts = new ArrayList<>();

    private Format baseFormat = new Format();

    /**
     * Constructor.
     *
     * @param format the configured base format
     */
    public Line(final Format format) {
        this.baseFormat = format;
    }

    /**
     * Constructor.
     *
     * @param codes Ansi escape codes for formatting
     */
    public Line(final Code... codes) {
        this(new Format().addCodes(codes));
    }

    /**
     * Add some text to the line.
     *
     * @param text the text
     * @return this line
     */
    public Line add(final Text text) {
        this.texts.add(text);
        return this;
    }

    public Line add(final Object t) {
        return add(new Text(t));
    }

    public Line add(final Object text, final Code... codes) {
        return add(new Text(text, codes));
    }

    public Line add(final Object text, final Format format) {
        return add(new Text(text, format));
    }

    public String render() {
        return render(true, false);
    }

    public String render(final boolean newLine, final boolean dotted) {
        final List<String> joins = new ArrayList<>();
        final List<Text> reversed = new ArrayList<>(this.texts);
        int replace = 0;
        Collections.reverse(reversed);
        if (dotted && getVisibleLength() > replace) {
            replace = 3;
        }
        for (final Text t : reversed) {
            if (replace > 0) {
                final String render = t.render(t.getVisibleText(0, t.getVisibleLength() - replace), this.baseFormat);
                if (StringUtils.isNotEmpty(render)) {
                    joins.add(render);
                }
                replace = replace - t.getVisibleLength();
            } else {
                joins.add(t.render(this.baseFormat));
            }

        }
        Collections.reverse(joins);
        return String.join(" ", joins) + (dotted ? "..." : "") + (newLine ? "\n" : "");
    }

    public int getLength() {
        return this.texts.stream().mapToInt(Text::getLength).sum();
    }

    public static String render(final String text, final Code... codes) {
        return new Line().add(text, codes).render();
    }

    public boolean isNotEmpty() {
        return !this.texts.isEmpty();
    }

    public int getVisibleLength() {
        return this.texts.stream().mapToInt(Text::getVisibleLength).sum();
    }
}
