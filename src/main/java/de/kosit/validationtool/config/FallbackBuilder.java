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

package de.kosit.validationtool.config;

import java.net.URI;
import java.nio.file.Path;

import org.apache.commons.lang3.tuple.Pair;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.CreateReportType;
import de.kosit.validationtool.model.scenarios.ScenarioType;

/**
 * Create a fallback {@link Scenario} configuration.
 * 
 * @author Andreas Penski
 */
public class FallbackBuilder implements Builder<Scenario> {

    private final ReportBuilder internal = new ReportBuilder().name("fallback");

    @Override
    public Result<Scenario, String> build(final ContentRepository repository) {
        final ScenarioType object = createObject();
        final Result<Pair<CreateReportType, Transformation>, String> build = this.internal.build(repository);
        final Result<Scenario, String> result;
        if (build.isValid()) {
            object.setCreateReport(build.getObject().getLeft());
            final Scenario s = new Scenario(object);
            s.setFallback(true);
            s.setReportTransformation(build.getObject().getRight());
            result = new Result<>(s);
        } else {
            result = new Result<>(build.getErrors());
        }
        return result;
    }

    private static ScenarioType createObject() {
        final ScenarioType t = new ScenarioType();
        t.setName("Fallback-Scenario");
        t.setMatch("count(/)<0");
        // always reject
        t.setAcceptMatch("count(/)<0");
        return t;
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public FallbackBuilder source(final String source) {
        this.internal.source(source);
        return this;
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public FallbackBuilder source(final URI source) {
        this.internal.source(source);
        return this;
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public FallbackBuilder source(final Path source) {
        this.internal.source(source);
        return this;
    }

    /**
     * Sets the name of the report source to a specific value.
     *
     * @param name the name
     * @return this
     */
    public FallbackBuilder name(final String name) {
        this.internal.name(name);
        return this;
    }

}
