/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.cmd;

import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Gibt das Ergebnis-Document auf std-out aus.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class PrintReportAction implements CheckAction {

    @Override
    public void check(Bag results) {
        try {
            Transformer transformer = ObjectFactory.createTransformer(true);
            final StringWriter writer = new StringWriter();
            Result output = new StreamResult(writer);
            Source input = new DOMSource(results.getReport());
            transformer.transform(input, output);
            System.out.print(writer.toString());
        } catch (TransformerException e) {
            log.error("Error while printing result to stdout", e);
        }
    }
}
