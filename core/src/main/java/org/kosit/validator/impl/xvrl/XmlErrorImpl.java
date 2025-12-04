/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

package org.kosit.validator.impl.xvrl;

import org.kosit.validator.api.XmlError;
import org.kosit.validator.model.xvrl.Location;
import org.kosit.validator.model.xvrl.XVRLDetection;

public class XmlErrorImpl implements XmlError {

    private final String message;

    private final Severity severity;

    private Long rowNumber;

    private Long columnNumber;

    public XmlErrorImpl(final XVRLDetection xvrlDetection) {
        this.message = xvrlDetection.getErrorMessage();
        this.severity = getSeverityFromDetection(xvrlDetection);
        final Location location = xvrlDetection.getErrorLocation();
        if (location != null) {
            this.rowNumber = location.getLine();
            this.columnNumber = location.getColumn();
        }
    }

    private static Severity getSeverityFromDetection(final XVRLDetection xvrlDetection) {
        switch (xvrlDetection.getSeverity()) {
            case ERROR: {
                return Severity.SEVERITY_ERROR;
            }
            case FATAL_ERROR: {
                return Severity.SEVERITY_FATAL_ERROR;
            }
            default: {
                return Severity.SEVERITY_WARNING;
            }
        }
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    @Override
    public Integer getRowNumber() {
        return Math.toIntExact(this.rowNumber);
    }

    @Override
    public Integer getColumnNumber() {
        return Math.toIntExact(this.columnNumber);
    }
}
