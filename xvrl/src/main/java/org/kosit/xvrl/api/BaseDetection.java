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

package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.xvrl.model.Location;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLMessage;

public interface BaseDetection {

    List<XVRLMessage> getMessages();

    List<Location> getLocations();

    XVRLDetection.Severity getSeverity();

    void setSeverity(XVRLDetection.Severity value);

    default List<String> getAllMessages() {
        return getMessages().stream().flatMap(message -> message.getMessageStrings().stream()).collect(Collectors.toList());
    }

    default String getErrorMessage() {
        if (getMessages().isEmpty()) {
            return null;
        }
        return getMessages().get(0).getContent().stream().map(Object::toString).collect(Collectors.joining());
    }

    default Location getErrorLocation() {
        if (getLocations().isEmpty()) {
            return null;
        }
        return getLocations().get(0);
    }

    default boolean hasErrors() {
        return getSeverity() == XVRLDetection.Severity.ERROR || getSeverity() == XVRLDetection.Severity.FATAL_ERROR;
    }
}
