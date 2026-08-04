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
package org.kosit.validator.impl.conformatron;

import java.util.List;
import java.util.function.Predicate;

import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.detection.ICTSeverity;

/**
 * Immutable implementation of {@link ICTDetectionList}.
 *
 * @author Andreas Schmitz
 */
public final class DetectionList implements ICTDetectionList {

    private static final DetectionList EMPTY = new DetectionList(List.of());

    private final List<ICTDetection> detections;

    public DetectionList(final List<ICTDetection> detections) {
        if (detections == null) {
            throw new IllegalArgumentException("detections may not be null");
        }
        this.detections = List.copyOf(detections);
    }

    public static DetectionList empty() {
        return EMPTY;
    }

    public static DetectionList of(final ICTDetection... detections) {
        return new DetectionList(List.of(detections));
    }

    @Override
    public List<ICTDetection> getAll() {
        return this.detections;
    }

    @Override
    public int getCount() {
        return this.detections.size();
    }

    @Override
    public List<ICTDetection> getAll(final Predicate<? super ICTDetection> filter) {
        return this.detections.stream().filter(filter).toList();
    }

    @Override
    public int getCount(final Predicate<? super ICTDetection> filter) {
        return (int) this.detections.stream().filter(filter).count();
    }

    @Override
    public ICTSeverity getWorstSeverity() {
        ICTSeverity worst = ECTSeverity.NONE;
        for (final ICTDetection detection : this.detections) {
            worst = ICTSeverity.getWorst(worst, detection.getSeverity());
        }
        return worst;
    }
}
