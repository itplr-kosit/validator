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

package de.kosit.validationtool.daemon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.config.TestConfigurationFactory;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.Helper;

/**
 * @author Andreas Penski
 */
class ConfigHandlerTest {

    @Test
    void apiConfiguration() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final Headers headers = mock(Headers.class);
        final OutputStream stream = mock(OutputStream.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(stream);
        final Configuration config = TestConfigurationFactory.createSimpleConfiguration().build(Helper.getTestProcessor());
        final ConfigHandler handler = new ConfigHandler(Collections.singletonList(config), new ConversionService());
        handler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(HttpStatus.SC_OK, 0);
        verify(stream, atLeast(1)).write(any());
    }

    @Test
    void error() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final Headers headers = mock(Headers.class);
        final OutputStream stream = mock(OutputStream.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(stream);
        final ArgumentCaptor<Integer> valueCapture = ArgumentCaptor.forClass(Integer.class);
        doNothing().when(exchange).sendResponseHeaders(valueCapture.capture(), anyLong());
        final ConfigHandler handler = new ConfigHandler(null/* will produce npe */, new ConversionService());
        handler.handle(exchange);
        verify(headers, times(1)).add(any(), any());
        verify(stream, atLeast(1)).write(any());
        assertThat(valueCapture.getValue()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
