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
package org.kosit.validator.impl;

import org.kosit.jaxb.JaxbConversionService;
import org.oclc.purl.dsdl.svrl.ObjectFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the SVRL JAXB model package
 * ({@code org.kosit.validator.model.scenarios}).
 */
public class SvrlConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    SvrlConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create SVRL JAXB context", e);
        }
    }

    /**
     * Creates a new conversion service for the SVRL model.
     *
     * @throws IllegalStateException if the JAXB context for the SVRL model package can not be created
     */
    public SvrlConversionService() {
        super(JAXB_CTX);
    }
}
