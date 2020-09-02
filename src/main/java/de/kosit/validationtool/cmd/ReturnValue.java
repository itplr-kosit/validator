package de.kosit.validationtool.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * CLI return codes.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Getter
public class ReturnValue {

    public static final ReturnValue SUCCESS = new ReturnValue(0);

    public static final ReturnValue CONFIGURATION_ERROR = new ReturnValue(-2);

    public static final ReturnValue DAEMON_MODE = new ReturnValue(-100);

    public static final ReturnValue PARSING_ERROR = new ReturnValue(-1);;

    private final int code;

    public static ReturnValue createFailed(final int count) {
        return new ReturnValue(count);
    }
}
