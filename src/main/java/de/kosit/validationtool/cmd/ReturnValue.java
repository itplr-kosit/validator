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
