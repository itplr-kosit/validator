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

package de.kosit.validationtool.api;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

/**
 * Main validator interface for checking incoming files.
 *
 * @author Andreas Penski
 */
public interface Check {

    /**
     * Checks an incoming xml {@link Input Inputs}. The result-{@link Document} is readonly. To change the this document
     * you need to copy the nodes into an new {@link Document}.
     *
     * @param input the resource / xml file to validate.
     * @return a result-{@link Document} (readonly)
     */
    default Document check(final Input input) {
        final Result result = checkInput(input);
        // readonly view of the document!!!
        return result.getReportDocument();
    }

    /**
     * Checks an incoming xml file.
     *
     * @param input the resource / xml file to validate.
     * @return a {@link Result} object
     */
    Result checkInput(Input input);

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential. The result-{@link Document Documents} are
     * readonly. To change the this document you need to copy them into new {@link Document Documents}.
     * 
     * 
     * @param input list of xml {@link Input Inputs}
     * @return list of result-{@link Document Documents} (readonly)
     */
    default List<Document> check(final List<Input> input) {
        return input.stream().map(this::check).collect(Collectors.toList());
    }

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential.
     *
     * @param input list of xml {@link Input Inputs}
     * @return list of {@link Result}
     */
    default List<Result> checkInput(final List<Input> input) {
        return input.stream().map(this::checkInput).collect(Collectors.toList());
    }

}
