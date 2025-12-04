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

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.validator.model.xvrl.XVRLDetection;
import org.kosit.validator.model.xvrl.XVRLDigest;

public abstract class BaseReport {

    public abstract List<XVRLDetection> getDetection();

    public List<String> getAllErrors() {
        return getDetection().stream().filter(BaseDetection::hasErrors).flatMap(xvrlDetection -> xvrlDetection.getAllMessages().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return String.format("id=%s, errors=%s, valid=%s", getDigest().getId(), getDigest().getErrorCount(), getDigest().getValid());
    }

    protected abstract XVRLDigest getDigest();
}
