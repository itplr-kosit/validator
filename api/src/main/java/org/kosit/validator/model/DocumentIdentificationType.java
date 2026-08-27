//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.model;

/**
 * Dient der eindeutigen Identifikation des geprüften Dokuments anhand seines Hashwertes, der durch eine
 * Dokumentenreferenz ergänzt werden kann.
 * 
 */
public final record DocumentIdentificationType(DocumentHash documentHash, String documentReference) {
}
