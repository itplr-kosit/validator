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

package de.kosit.validationtool.impl.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.model.scenarios.*;

import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Eine Basis-Klasse für Szenarien. Wiederverwendbare Objekte mit Bezug zum Szenario werden hier gecached. Die Klasse
 * stellt im eigentlichen Sinne eine Erweiterung der durch JAXB generierten Strukturen dar.
 * 
 * @author Andreas Penski
 */
public abstract class BaseScenario {

    private XPathExecutable xPathExecutable;

    private Schema schema;

    private List<Transformation> schematronValidations;

    private ContentRepository repository;

    private Transformation reportTransformation;

    /**
     * Gibt eine Transformation zurück.
     * @return initialisierte Transformation
     */
    public Transformation getReportTransformation() {
        if (reportTransformation == null) {
            final ResourceType resource = getCreateReport().getResource();
            final XsltExecutable executable = repository.loadXsltScript(URI.create(resource.getLocation()));
            reportTransformation = new Transformation(executable, resource);
        }
        return reportTransformation;
    }

    /**
     * Lieferrt das Schema zu diesem Szenario.
     * @return das passende Schema
     */
    public Schema getSchema() {
        if (schema == null) {
            final List<String> schemaResources = getValidateWithXmlSchema().getResource().stream().map(ResourceType::getLocation)
                    .collect(Collectors.toList());
            schema = repository.createSchema(schemaResources);
        }
        return schema;
    }


    /**
     * Initialisiert das Szenario auf Basis eines [@link ContentRepository}
     * @param repository das Repository mit den Szenario-Artefakten
     * @param lazy optionales lazy loading der XML-Artefakte
     * @return true wenn erfolgreich
     */
    public boolean initialize(ContentRepository repository, boolean lazy) {
        this.repository = repository;
        if (!lazy) {
            getSchema();
            getSelector();
            getSchematronValidations();
            getReportTransformation();
        }
        return true;
    }

    /**
     * Liefer eine Liste mit Schematron Validierungs-Transformationen
     * @return liste mit initialisierten Transformationsinformationen
     */
    public List<Transformation> getSchematronValidations() {
        if (schematronValidations == null) {
            schematronValidations = new ArrayList<>();
            getValidateWithSchematron().forEach(v -> {
                if (v.isPsvi()) {
                    throw new NotImplementedException("This implemenation does not support PSVI usage");
                }
                final XsltExecutable xsltExecutable = repository.loadXsltScript(URI.create(v.getResource().getLocation()));
                schematronValidations.add(new Transformation(xsltExecutable, v.getResource()));
            });
        }
        return schematronValidations;
    }

    /**
     * Der XPath-Selector zur Identifikation des Scenarios.
     * 
     * @return vorbereiteten selector
     * @see {@link ScenarioRepository#selectScenario(Document)}.
     */
    public XPathSelector getSelector() {
        if (xPathExecutable == null) {
            final Map<String, String> namespaces = getNamespace().stream().collect(Collectors.toMap(NamespaceType::getPrefix, NamespaceType::getValue));
            xPathExecutable = repository.createXPath(getMatch(), namespaces);
        }
        return xPathExecutable.load();
    }

    /**
     * Getter aus dem schema.
     *
     * @return xpath match
     */
    public abstract String getMatch();

    /**
     * Getter aus dem schema.
     *
     * @return {@link List} of {@link NamespaceType}
     */
    public abstract List<NamespaceType> getNamespace();

    /**
     * Getter aus dem schema.
     *
     * @return Validierungsanweisungen
     */
    public abstract ValidateWithXmlSchema getValidateWithXmlSchema();

    /**
     * Getter aus dem schema.
     *
     * @return Validierungsanweisungne
     */
    public abstract List<ValidateWithSchematron> getValidateWithSchematron();

    /**
     * Getter aus dem schema.
     *
     * @return report informationen
     */
    public abstract CreateReportType getCreateReport();

    /**
     * Laufzeitinformationen über eine Transformation.
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public class Transformation {

        private XsltExecutable executable;

        private ResourceType resourceType;
    }

}
