/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTSeverity;
import org.conformatron.api.model.detection.CTStandardSeverity;

/**
 * Immutable implementation of {@link CTDetectionList}.
 *
 * @author Andreas Schmitz
 */
public final class DetectionList implements CTDetectionList {

    private static final DetectionList EMPTY = new DetectionList(Collections.emptyList());

    private final List<CTDetection> detections;

    public static DetectionList empty() {
        return EMPTY;
    }

    public static DetectionList of(final CTDetection... detections) {
        return new DetectionList(List.of(detections));
    }

    public DetectionList(final List<CTDetection> detections) {
        Objects.requireNonNull(detections);
        this.detections = List.copyOf(detections);
    }

    @Override
    public List<CTDetection> getAll() {
        return this.detections;
    }

    @Override
    public int getCount() {
        return this.detections.size();
    }

    @Override
    public List<CTDetection> getAll(final Predicate<? super CTDetection> filter) {
        return this.detections.stream().filter(filter).toList();
    }

    @Override
    public int getCount(final Predicate<? super CTDetection> filter) {
        return (int) this.detections.stream().filter(filter).count();
    }

    @Override
    public CTSeverity getWorstSeverity() {
        CTSeverity worst = CTStandardSeverity.NONE;
        for (final CTDetection detection : this.detections) {
            worst = CTSeverity.getWorst(worst, detection.getSeverity());
        }
        return worst;
    }
}
