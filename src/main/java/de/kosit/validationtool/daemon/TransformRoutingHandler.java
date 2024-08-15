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

import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * A simple handler which routes between the {@link CheckHandler} and the {@link GuiHandler} depending on the request.
 */
@RequiredArgsConstructor
class TransformRoutingHandler extends BaseHandler {

    private final TransformHandler checkHandler;

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equals("POST")) {
            this.checkHandler.handle(exchange);
        } else {
            error(exchange, 405, String.format("Method % not supported", requestMethod));
        }
    }
}
