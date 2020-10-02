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

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.Scenario;

/**
 * Default implementation class for {@link Configuration}. This class contains all information to run a
 * {@link de.kosit.validationtool.impl.DefaultCheck}.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Setter
public class DefaultConfiguration implements Configuration {

    private final List<Scenario> scenarios;

    private final Scenario fallbackScenario;

    private ContentRepository contentRepository;

    private String name;

    private String author;

    private String date;

    private Map<String, Object> additionalParameters;
}
