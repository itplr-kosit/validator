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

package de.kosit.validationtool.impl.xml;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.extern.slf4j.Slf4j;

/**
 * This is a slightly more open implementation that allows resolving artifacts from local filesystems. Your are not
 * bound to a specific 'repository'. But your validation artifacts (schema, xsl, etc.) must be available locally. This
 * implementation does not allow loading from http sources
 * 
 * @author Andreas Penski
 */
@Slf4j
public class StrictLocalResolvingStrategy extends StrictRelativeResolvingStrategy {

    /**
     * Allow loading schema files from any local location.
     * 
     * @return a configured {@link SchemaFactory}
     */
    @Override
    public SchemaFactory createSchemaFactory() {
        final SchemaFactory schemaFactory = super.createSchemaFactory();
        allowExternalSchema(schemaFactory, "file");
        return schemaFactory;
    }

    /**
     * The default resolver is able to resolve locally and relative.
     * 
     * @param repository the repository is not used by this strategy
     * @return null!
     */
    @Override
    public URIResolver createResolver(final URI repository) {
        // intentionally return 'null', since all resolving is configured with the other objects
        return null;
    }

    @Override
    public Validator createValidator(final Schema schema) {
        final Validator validator = super.createValidator(schema);
        allowExternalSchema(validator, "file");
        return validator;
    }

}
