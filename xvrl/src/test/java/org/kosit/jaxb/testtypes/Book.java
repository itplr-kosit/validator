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
package org.kosit.jaxb.testtypes;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Namespaced JAXB-annotated test type exercising the namespace-prefix-mapping support of
 * {@link org.kosit.jaxb.JaxbConversionService}. The root element lives in {@code urn:example:book}; the {@code author}
 * child lives in {@code urn:example:author}.
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
