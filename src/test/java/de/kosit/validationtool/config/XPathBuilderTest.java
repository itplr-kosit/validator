/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Tests {@link XPathBuilder}.
 * 
 * @author Andreas Penski
 */
public class XPathBuilderTest {

    @Test
    public void testSimpleString() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final XPathBuilder b = new XPathBuilder(name);
        b.setXpath("//*");
        final Result<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotNull();
        assertThat(b.getNamespaces()).isEmpty();
        assertThat(b.getXPath()).isNotEmpty();
        assertThat(b.getName()).isNotEmpty();
    }

    @Test
    public void testStringWithNamespace() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final XPathBuilder b = new XPathBuilder(name);
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        b.setNamespaces(ns);
        b.setXpath("//p:*");
        final Result<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotEmpty();
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testStringWithUnknownNamespace() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final XPathBuilder b = new XPathBuilder(name);
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        b.setNamespaces(ns);
        b.setXpath("//u:*");
        final Result<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testExecutable() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final ContentRepository repository = Simple.createContentRepository();
        final XPathExecutable xpath = repository.createXPath("//*", Collections.emptyMap());
        final XPathBuilder b = new XPathBuilder(name);
        b.setExecutable(xpath);
        final Result<XPathExecutable, String> result = b.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isEmpty();
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testExecutableWithNamespace() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final ContentRepository repository = Simple.createContentRepository();
        final Map<String, String> ns = new HashMap<>();
        ns.put("p", "http://somens");
        final XPathExecutable xpath = repository.createXPath("//p:*", ns);
        final XPathBuilder b = new XPathBuilder(name);
        b.setExecutable(xpath);
        final Result<XPathExecutable, String> result = b.build(repository);
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getNamespaces()).isNotEmpty();
        assertThat(b.getNamespaces()).containsKey("p");
        assertThat(b.getXPath()).isNotEmpty();
    }

    @Test
    public void testNoName() {
        final XPathBuilder b = new XPathBuilder(null);
        b.setXpath("//*");
        final Result<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(b.getName()).isNull();
    }

    @Test
    public void testNoConfig() {
        final String name = RandomStringUtils.randomAlphanumeric(5);
        final XPathBuilder b = new XPathBuilder(name);
        final Result<XPathExecutable, String> result = b.build(Simple.createContentRepository());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
    }
}
