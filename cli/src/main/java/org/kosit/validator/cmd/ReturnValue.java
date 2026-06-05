package org.kosit.validator.cmd;

/**
 * CLI return codes. Codes &gt; 0 indicate a processing error. Codes &lt; 0 indicates a configuration error. Code 0
 * indicates a successful processing.
 * 
 * @author Andreas Penski
 */
public class ReturnValue {

    public static final ReturnValue SUCCESS = new ReturnValue(0);

    public static final ReturnValue HELP_REQUEST = new ReturnValue(0);

    public static final ReturnValue CONFIGURATION_ERROR = new ReturnValue(-2);

    public static final ReturnValue PARSING_ERROR = new ReturnValue(-1);

    private final int code;

    public static ReturnValue createFailed(final int count) {
        return new ReturnValue(count);
    }

    public boolean isError() {
        return this.code < 0;
    }

    public ReturnValue(final int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}
