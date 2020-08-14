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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.config.TestScenarioFactory;
import de.kosit.validationtool.impl.ConversionService;

/**
 * @author Andreas Penski
 */
public class ConfigHandlerTest {

    @Test
    public void testApiConfiguration() throws IOException {
        final HttpExchange exchange = mock(HttpExchange.class);
        final Headers headers = mock(Headers.class);
        final OutputStream stream = mock(OutputStream.class);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(stream);
        final Configuration config = TestScenarioFactory.createSimpleConfiguration().build();
        final ConfigHandler handler = new ConfigHandler(config, new ConversionService());
        handler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(ConfigHandler.OK, 0);
        verify(stream, atLeast(1)).write(any());
    }

    @Test
    public void testError() throws IOException {
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
        assertThat(valueCapture.getValue()).isEqualTo(500);
    }
}
