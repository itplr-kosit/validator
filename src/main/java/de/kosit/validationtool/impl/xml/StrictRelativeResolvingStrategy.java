/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
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

import javax.xml.XMLConstants;
import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import lombok.RequiredArgsConstructor;

import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public class StrictRelativeResolvingStrategy extends BaseResolvingStrategy {

    @Override
    public SchemaFactory createSchemaFactory() {
        forceOpenJdkXmlImplementation();
        @SuppressWarnings("java:S2755") //
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        disableExternalEntities(sf);
        allowExternalSchema(sf, "file");
        return sf;
    }

    @Override
    public URIResolver createResolver(final URI repositoryURI) {
        return new RelativeUriResolver(repositoryURI);
    }

    @Override
    public UnparsedTextURIResolver createUnparsedTextURIResolver(final URI scenarioRepository) {
        return new RelativeUriResolver(scenarioRepository);
    }

    @Override
    public Validator createValidator(final Schema schema) {
        if (schema == null) {
            throw new IllegalArgumentException("No schema supplied. Can not create validator");
        }
        forceOpenJdkXmlImplementation();
        final Validator validator = schema.newValidator();
        disableExternalEntities(validator);
        allowExternalSchema(validator, "file" /* allow nothing external */);
        return validator;

    }

}
