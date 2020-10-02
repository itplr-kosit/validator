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

package de.kosit.validationtool.cmd.report;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.Getter;

/**
 * Simple value holder for ansi formatting codes.
 * 
 * @author Andreas Penski
 */
@Getter
public class Format {

    private Code textColor;

    private Code background;

    @Getter
    private final Set<Code> codes = new HashSet<>();

    public Code[] mergeCodes(final Collection<Code> newCodes) {
        return mergeCodes(newCodes.toArray(new Code[newCodes.size()]));
    }

    public Code[] mergeCodes(final Code... newCodes) {
        final Code[] allCodes = ArrayUtils.addAll(ArrayUtils.addAll(this.codes.toArray(new Code[0]), newCodes), this.textColor,
                this.background);

        final Optional<Code> color = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isColor).findFirst();
        final Optional<Code> bg = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isBackground).findFirst();
        final List<Code> attributes = Arrays.stream(allCodes).filter(Objects::nonNull).filter(Code::isBackground).filter(Code::isColor)
                .collect(Collectors.toList());
        attributes.add(color.orElse(this.textColor));
        attributes.add(bg.orElse(this.background));
        return attributes.stream().filter(Objects::nonNull).toArray(Code[]::new);
    }

    /**
     * Sets explicit text color.
     *
     * @param textColor the color.
     *
     * @return this {@link Format}
     */
    public Format color(final Code textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * Sets explicit background color.
     *
     * @param color the color.
     *
     * @return this {@link Format}
     */
    public Format background(final Code color) {
        this.background = color;
        return this;
    }

    /**
     * Fügt weitere Formatierungscodes hinzu.
     *
     * @param codes die Codes
     *
     * @return this {@link Format}
     */
    public Format addCodes(final Code... codes) {
        this.codes.addAll(Arrays.asList(codes));
        return this;
    }
}
