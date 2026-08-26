package org.kosit.validator.cmd.report;

import org.kosit.base.string.StringHelper;

/**
 * Justification modes for the text in grid columns.
 * 
 * @author Andreas Penski
 */
public enum Justify {

    LEFT {

        @Override
        public String apply(final String string, final int length) {
            return StringHelper.rightPad(string, length);
        }
    },
    CENTER {

        @Override
        public String apply(final String string, final int length) {
            return StringHelper.center(string, length);
        }
    },
    RIGHT {

        @Override
        public String apply(final String string, final int length) {
            return StringHelper.leftPad(string, length);
        }
    };

    public abstract String apply(String string, int length);
}
