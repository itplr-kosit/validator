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

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario.Transformation;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.scenarios.ResourceType;
import de.kosit.validationtool.model.scenarios.ValidateWithSchematron;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder for schematron validation configuration.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class SchematronBuilder implements Builder<Pair<ValidateWithSchematron, Transformation>> {

    private static final String DEFAULT_NAME = "manually configured";

    private XsltExecutable executable;

    private URI source;

    private String name;

    @Override
    public Result<Pair<ValidateWithSchematron, Transformation>, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError(String.format("Must supply source location and/or executable for schematron '%s'", this.name));
        }
        final ValidateWithSchematron object = createObject();
        Result<Pair<ValidateWithSchematron, Transformation>, String> result;

        try {
            if (this.executable == null) {
                this.executable = repository.createSchematronTransformation(object).getExecutable();
            }
            result = new Result<>(new ImmutablePair<>(object, new Transformation(this.executable, object.getResource())));
        } catch (final IllegalStateException e) {
            log.error(e.getMessage(), e);
            result = createError(
                    String.format("Can not create schematron configuration based  on %s. Exception is %s", this.source, e.getMessage()));
        }
        return result;
    }

    private ValidateWithSchematron createObject() {
        final ValidateWithSchematron o = new ValidateWithSchematron();
        final ResourceType r = new ResourceType();
        r.setLocation(this.source.toASCIIString());
        r.setName(isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        o.setResource(r);
        return o;
    }

    private static Result<Pair<ValidateWithSchematron, Transformation>, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
    }

    /**
     * Specifices a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled tranformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final String source) {
        return source(URI.create(source));
    }

    /**
     * Specifices a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled tranformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final URI source) {
        this.source = source;
        return this;
    }

    /**
     * Specifices a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled tranformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final Path source) {
        return source(source.toUri());
    }

    /**
     * Sets the name of the schematron source to a specific value.
     * 
     * @param name the name
     * @return this
     */
    public SchematronBuilder name(final String name) {
        this.name = name;
        return this;
    }
}
