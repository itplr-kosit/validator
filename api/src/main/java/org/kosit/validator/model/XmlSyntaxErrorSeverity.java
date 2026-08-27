//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 * 
 * <p>Java-Klasse für XmlSyntaxErrorSeverity.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * <pre>{@code
 * <simpleType name="XmlSyntaxErrorSeverity">
 *   <restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     <enumeration value="SEVERITY_WARNING"/>
 *     <enumeration value="SEVERITY_ERROR"/>
 *     <enumeration value="SEVERITY_FATAL_ERROR"/>
 *   </restriction>
 * </simpleType>
 * }</pre>
 * 
 */
@XmlType(name = "XmlSyntaxErrorSeverity", namespace = "http://www.xoev.de/de/validator/framework/1/model")
@XmlEnum
public enum XmlSyntaxErrorSeverity {

    SEVERITY_WARNING,
    SEVERITY_ERROR,
    SEVERITY_FATAL_ERROR;

    public String value() {
        return name();
    }

    public static XmlSyntaxErrorSeverity fromValue(String v) {
        return valueOf(v);
    }

}
