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
package de.kosit.validationtool.config;

import java.util.List;
import java.util.Map;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * @author Andreas Penski
 */
public class TestConfiguration implements Configuration {

    private List<Scenario> scenarios;

    private Scenario fallbackScenario;

    private String author;

    private String name;

    private String date;

    private ContentRepository contentRepository;

    private Map<String, Object> additionalParameters;

    public TestConfiguration() {
    }

    public List<Scenario> getScenarios() {
        return this.scenarios;
    }

    public Scenario getFallbackScenario() {
        return this.fallbackScenario;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getName() {
        return this.name;
    }

    public String getDate() {
        return this.date;
    }

    public ContentRepository getContentRepository() {
        return this.contentRepository;
    }

    public Map<String, Object> getAdditionalParameters() {
        return this.additionalParameters;
    }

    public void setScenarios(final List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public void setFallbackScenario(final Scenario fallbackScenario) {
        this.fallbackScenario = fallbackScenario;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDate(final String date) {
        this.date = date;
    }

    public void setContentRepository(final ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setAdditionalParameters(final Map<String, Object> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    @java.lang.Override

    public boolean equals(final java.lang.Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TestConfiguration))
            return false;
        final TestConfiguration other = (TestConfiguration) o;
        if (!other.canEqual((java.lang.Object) this))
            return false;
        final java.lang.Object this$scenarios = this.getScenarios();
        final java.lang.Object other$scenarios = other.getScenarios();
        if (this$scenarios == null ? other$scenarios != null : !this$scenarios.equals(other$scenarios))
            return false;
        final java.lang.Object this$fallbackScenario = this.getFallbackScenario();
        final java.lang.Object other$fallbackScenario = other.getFallbackScenario();
        if (this$fallbackScenario == null ? other$fallbackScenario != null : !this$fallbackScenario.equals(other$fallbackScenario))
            return false;
        final java.lang.Object this$author = this.getAuthor();
        final java.lang.Object other$author = other.getAuthor();
        if (this$author == null ? other$author != null : !this$author.equals(other$author))
            return false;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name))
            return false;
        final java.lang.Object this$date = this.getDate();
        final java.lang.Object other$date = other.getDate();
        if (this$date == null ? other$date != null : !this$date.equals(other$date))
            return false;
        final java.lang.Object this$contentRepository = this.getContentRepository();
        final java.lang.Object other$contentRepository = other.getContentRepository();
        if (this$contentRepository == null ? other$contentRepository != null : !this$contentRepository.equals(other$contentRepository))
            return false;
        final java.lang.Object this$additionalParameters = this.getAdditionalParameters();
        final java.lang.Object other$additionalParameters = other.getAdditionalParameters();
        if (this$additionalParameters == null ? other$additionalParameters != null
                : !this$additionalParameters.equals(other$additionalParameters))
            return false;
        return true;
    }

    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof TestConfiguration;
    }

    @java.lang.Override

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $scenarios = this.getScenarios();
        result = result * PRIME + ($scenarios == null ? 43 : $scenarios.hashCode());
        final java.lang.Object $fallbackScenario = this.getFallbackScenario();
        result = result * PRIME + ($fallbackScenario == null ? 43 : $fallbackScenario.hashCode());
        final java.lang.Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $date = this.getDate();
        result = result * PRIME + ($date == null ? 43 : $date.hashCode());
        final java.lang.Object $contentRepository = this.getContentRepository();
        result = result * PRIME + ($contentRepository == null ? 43 : $contentRepository.hashCode());
        final java.lang.Object $additionalParameters = this.getAdditionalParameters();
        result = result * PRIME + ($additionalParameters == null ? 43 : $additionalParameters.hashCode());
        return result;
    }

    @java.lang.Override

    public java.lang.String toString() {
        return "TestConfiguration(scenarios=" + this.getScenarios() + ", fallbackScenario=" + this.getFallbackScenario() + ", author="
                + this.getAuthor() + ", name=" + this.getName() + ", date=" + this.getDate() + ", contentRepository="
                + this.getContentRepository() + ", additionalParameters=" + this.getAdditionalParameters() + ")";
    }
}
