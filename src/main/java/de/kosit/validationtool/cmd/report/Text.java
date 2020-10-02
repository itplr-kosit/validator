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

import java.util.Arrays;

import org.fusesource.jansi.AnsiRenderer;
import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.Getter;

/**
 * Ansi formatted text for outputting to the console.
 * 
 * @author Andreas Penski
 */
@Getter
public class Text {

    private final String value;

    private Format format;

    public Text(final Object value) {
        this.value = value != null ? value.toString() : "";
        this.format = new Format();
    }

    public Text(final Object value, final Format format) {
        this(value);
        this.format = format;
    }

    public Text(final Object value, final Code... codes) {
        this(value, new Format().addCodes(codes));
    }

    public String getVisibleText(final int startIndex, final int length) {
        if (startIndex < 0) {
            return "Wrong cell text index";
        }
        if (startIndex > this.value.length()) {
            return "";
        }
        final String substring = this.value.substring(startIndex);
        return substring.length() > length ? substring.substring(0, length) : substring;
    }

    public String render(final String text, final Format baseformat) {
        return AnsiRenderer.render(text,
                Arrays.stream(this.format.mergeCodes(baseformat.getCodes())).map(Code::name).toArray(String[]::new));
    }

    public int getLength() {
        return render(this.format).length();
    }

    public String render(final Format baseFormat) {
        return render(getValue(), baseFormat);
    }

    public int getVisibleLength() {
        return this.value.length();
    }
}
