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
