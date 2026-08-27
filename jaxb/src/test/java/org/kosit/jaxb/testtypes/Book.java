package org.kosit.jaxb.testtypes;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Namespaced JAXB-annotated test type exercising the namespace-prefix-mapping support of
 * {@link org.kosit.jaxb.AbstractJaxbConversionService}. The root element lives in {@code urn:example:book}; the
 * {@code author} child lives in {@code urn:example:author}.
 */
@XmlRootElement(name = "book", namespace = Book.NS_BOOK)
@XmlAccessorType(XmlAccessType.FIELD)
public class Book {

    public static final String NS_BOOK = "urn:example:book";

    public static final String NS_AUTHOR = "urn:example:author";

    @XmlElement(namespace = NS_BOOK)
    private String title;

    @XmlElement(namespace = NS_AUTHOR)
    private String author;

    public Book() {
    }

    public Book(final String title, final String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }
}
