/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.cmd;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Simple Erweiterung der Klasse {@link DefaultCheck} um das Ergebnis der Assertion-Prüfung auszwerten und auszugeben.
 * Diese Klasse stellt keine fachlicher Erweiterung des eigentlichen Prüfvorganges dar!
 * 
 * @author Andreas Penski
 */
@Slf4j
class InternalCheck extends DefaultCheck {

    private int checkAssertions = 0;

    private int failedAssertions = 0;

    /**
     * Erzeugt eine neue Instanz mit der angegebenen Konfiguration.
     *
     * @param configuration die Konfiguration
     */
    InternalCheck(final CheckConfiguration configuration) {
        super(configuration);
    }

    /**
     * Prüft die Prüflinge und gibt Informationen über etwaige Assertions aus.
     * 
     * @param input die Prüflinge
     * @return false wenn es Assertion-Fehler gibt, sonst true
     */
    @Override
    public Result checkInput(final Input input) {
        final CheckAction.Bag bag = new CheckAction.Bag(input, createReport());
        final Result result = runCheckInternal(bag);
        if (bag.getAssertionResult() != null) {
            this.checkAssertions += bag.getAssertionResult().getObject();
            this.failedAssertions += bag.getAssertionResult().getErrors().size();
        }
        return result;
    }

    boolean printAndEvaluate() {
        if (this.failedAssertions > 0) {
            log.error("Assertion check failed.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
        } else if (this.checkAssertions > 0) {
            log.info("Assertion check successful.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
        }
        return this.failedAssertions == 0;
    }

}
