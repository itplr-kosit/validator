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
package de.kosit.validationtool.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.CreateReportType;
import de.kosit.validationtool.model.scenarios.ResourceType;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder style configuration for the report transformation.
 * 
 * @author Andreas Penski
 */
public class ReportBuilder implements Builder<Pair<CreateReportType, Transformation>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilder.class);

    private static final String DEFAULT_NAME = "manually created report";

    private XsltExecutable executable;

    private URI source;

    private String name;

    @Override
    public Result<Pair<CreateReportType, Transformation>, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError("Must supply source location and/or executable for report \'" + this.name + "\'");
        }
        final CreateReportType object = createObject();
        Result<Pair<CreateReportType, Transformation>, String> result;
        try {
            if (this.executable == null) {
                this.executable = repository.createTransformation(object.getResource()).getExecutable();
            }
            result = new Result<>(new ImmutablePair<>(object, new Transformation(this.executable, object.getResource())));
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
            result = createError("Can not create report configuration based on " + this.source + ". Exception is " + e.getMessage());
        }
        return result;
    }

    private CreateReportType createObject() {
        final CreateReportType o = new CreateReportType();
        final ResourceType r = new ResourceType();
        r.setLocation(this.source.toASCIIString());
        r.setName(isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        o.setResource(r);
        return o;
    }

    private static Result<Pair<CreateReportType, Transformation>, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final String source) {
        return source(URI.create(source));
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final URI source) {
        this.source = source;
        return this;
    }

    /**
     * Specifices a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled tranformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final Path source) {
        return source(source.toUri());
    }

    /**
     * Sets the name of the report source to a specific value.
     *
     * @param name the name
     * @return this
     */
    public ReportBuilder name(final String name) {
        this.name = name;
        return this;
    }
}
