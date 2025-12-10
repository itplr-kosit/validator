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

package org.kosit.validator.config;

import static org.kosit.validator.config.TestConfigurationFactory.createSimpleConfiguration;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import org.kosit.validator.api.Configuration;
import org.kosit.validator.api.InputFactory;
import org.kosit.validator.api.Result;
import org.kosit.validator.impl.DefaultCheck;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.TestEngineInformation;

/**
 * @author Andreas Penski
 */
public class SimpleConfigTest {

    @Test
    public void testSimpleWithApi() {
        //@formatter:off
        final Configuration config = createSimpleConfiguration().build(Helper.getTestProcessor());
        //@formatter:on
        final DefaultCheck check = new DefaultCheck(new TestEngineInformation(), config);
        final Result result = check.checkInput(InputFactory.read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
    }

}
