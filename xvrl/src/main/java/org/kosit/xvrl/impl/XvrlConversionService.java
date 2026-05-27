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
package org.kosit.xvrl.impl;

import java.util.HashMap;
import java.util.Map;

import org.kosit.jaxb.JaxbConversionService;
import org.kosit.xvrl.model.ObjectFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the XVRL JAXB model package
 * ({@code org.kosit.xvrl.model}).
 */
public class XvrlConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    private static final Map<String, String> NS_PREFIX = new HashMap<>();

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), XvrlConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create XVRL JAXB context", e);
        }
        NS_PREFIX.put("http://www.xproc.org/ns/xvrl", "");
    }

    /**
     * Creates a new conversion service for the XVRL model.
     *
     * @throws IllegalStateException if the JAXB context for the XVRL model package can not be created
     */
    public XvrlConversionService() {
        super(JAXB_CTX);
        withNamespacePrefixMap(NS_PREFIX);
    }
}
