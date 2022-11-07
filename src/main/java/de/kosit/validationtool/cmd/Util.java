/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.cmd;

import de.kosit.validationtool.impl.model.ProcessStepResult;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.CheckAction;
import de.kosit.validationtool.model.xvrl.XVRLReport;

class Util {

    public static <T, E> ProcessStepResult<T, E> createResult(final CheckAction.Process.Key<T, E> key, final T result,
            final XVRLReport report) {
        final ProcessStepResult<T, E> processStepResult = new ProcessStepResult<>(key);
        processStepResult.setResult(new Result<>(result));
        processStepResult.setReport(report);
        return processStepResult;

    }

}
