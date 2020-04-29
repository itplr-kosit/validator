//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.0 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.04.29 um 03:45:08 PM CEST 
//


package de.kosit.validationtool.model.daemon;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.xoev.de.validator.framework._1.daemon package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Health_QNAME = new QName("http://www.xoev.de/de/validator/framework/1/daemon", "health");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.xoev.de.validator.framework._1.daemon
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HealthType }
     * 
     */
    public static HealthType createHealthType() {
        return new HealthType();
    }

    /**
     * Create an instance of {@link ApplicationType }
     * 
     */
    public static ApplicationType createApplicationType() {
        return new ApplicationType();
    }

    /**
     * Create an instance of {@link MemoryType }
     * 
     */
    public static MemoryType createMemoryType() {
        return new MemoryType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HealthType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link HealthType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.xoev.de/de/validator/framework/1/daemon", name = "health")
    public static JAXBElement<HealthType> createHealth(final HealthType value) {
        return new JAXBElement<HealthType>(_Health_QNAME, HealthType.class, null, value);
    }

}
