package de.kosit.validationtool.cmd.report;

import org.apache.commons.lang3.StringUtils;

/**
 * Justification modes for the text in grid columns.
 * 
 * @author Andreas Penski
 */
public enum Justify {

    LEFT {

        @Override
        public String apply(final String string, final int length) {
            return StringUtils.rightPad(string, length);
        }
    },
    CENTER {

        @Override
        public String apply(final String string, final int length) {
            return StringUtils.center(string, length);
        }
    },
    RIGHT {

        @Override
        public String apply(final String string, final int length) {
            return StringUtils.leftPad(string, length);
        }
    };

    public abstract String apply(String string, int length);
}
