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

package de.kosit.validationtool.api;

/**
 * Tri-state recommendation whether to accept the {@link Input} or not.
 */
public enum AcceptRecommendation {

    /**
     * The evaluation of the overall validation could not be computed.
     */
    UNDEFINED,

    /**
     * Recommendation is to accept {@link Input} based on the evaluation of the overall validation.
     */
    ACCEPTABLE,

    /**
     * Recommendation is to reject {@link Input} based on the evaluation of the overall validation.
     */
    REJECT
}
