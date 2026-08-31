/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.ScenarioType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

/**
 * Serializes an individual scenario back to XML so the report can embed the scenario that was
 * selected. Note the distinction: this is the <b>single scenario</b>, not the scenario
 * configuration file it came from.
 * <p>
 * Scenario configurations are UTF-8 by definition, so the embedded scenario never needs the base64
 * detour that applies to arbitrary source documents.
 * </p>
 * <p>
 * The generated scenario type carries no root element and {@link Scenario1Converter} is bound to
 * the enclosing {@code scenarios} document, so this marshals against the converter's JAXB context
 * directly with an explicit element name.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioXml
{

  /** Namespace of the scenario configuration (framework 2). */
  public static final String NS_SCENARIOS = "http://www.xoev.de/de/validator/framework/2/scenarios";

  private static final QName SCENARIO_QNAME = new QName (NS_SCENARIOS, "scenario");

  private static final JAXBContext JAXB_CONTEXT = new Scenario2Converter ().getJaxbContext ();

  private ScenarioXml ()
  {
    // static utility
  }

  /**
   * Serializes the given scenario as a standalone XML document.
   *
   * @param configuration
   *        the scenario configuration to serialize
   * @return the scenario as UTF-8 encoded XML
   */
  public static byte @NonNull [] toXmlBytes (final @NonNull ScenarioType configuration)
  {
    try
    {
      final Marshaller marshaller = JAXB_CONTEXT.createMarshaller ();
      marshaller.setProperty (Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name ());
      final ByteArrayOutputStream out = new ByteArrayOutputStream ();
      marshaller.marshal (new JAXBElement <> (SCENARIO_QNAME, ScenarioType.class, configuration), out);
      return out.toByteArray ();
    }
    catch (final JAXBException e)
    {
      throw new IllegalStateException ("Can not serialize the selected scenario", e);
    }
  }

  /**
   * Serializes the given scenario as a standalone XML document.
   *
   * @param configuration
   *        the scenario configuration to serialize
   * @return the scenario as XML text
   */
  public static @NonNull String toXml (final @NonNull ScenarioType configuration)
  {
    return new String (toXmlBytes (configuration), StandardCharsets.UTF_8);
  }
}
