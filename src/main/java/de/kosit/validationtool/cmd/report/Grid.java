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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An text based grid for cli based programs.
 * 
 * @author Andreas Penski
 */
public class Grid {

    /**
     * A definition / configuration for a column with a result table.
     */
    @Getter
    public static class ColumnDefinition {

        private static final int MAX_LENGTH = 80;

        private final String name;

        private int length = 0;

        private final int maxLength;

        private final int minLength;

        private final int maxLines;

        private Justify justify = Justify.LEFT;

        /**
         * Constructor.
         *
         * @param name the name of the column
         */
        public ColumnDefinition(final String name) {
            this(name, -1, -1, 1);
        }

        /**
         * Constructor.
         *
         * @param name the name of the column
         * @param maxLength the max length of the column
         */
        public ColumnDefinition(final String name, final int maxLength) {
            this(name, maxLength, -1, 1);
        }

        /**
         * Constructor.
         *
         * @param name the name of the column
         * @param maxLength the max length of the column
         */
        public ColumnDefinition(final String name, final int maxLength, final int minLength) {
            this(name, maxLength, minLength, 1);
        }

        /**
         * Constructor.
         *
         * @param name the name of the column
         * @param minLength the max length of the column
         * @param maxLines the max lines per cell
         */
        public ColumnDefinition(final String name, final int maxLength, final int minLength, final int maxLines) {
            this.name = name;
            this.maxLength = maxLength;
            this.minLength = minLength;
            this.maxLines = maxLines;
        }

        /**
         * Returns the actual max length of the column
         *
         * @return max length
         */
        public int getLength() {
            if (this.minLength > 0 && this.minLength > this.length) {
                return this.minLength;
            }
            if (this.maxLength > 0 && this.length > this.maxLength) {
                return this.maxLength;
            }
            return this.length;
        }

        /**
         * Sets a calculated length for the column.
         *
         * @param length the length
         */
        public void setLength(final int length) {
            if (length > this.length) {
                this.length = length;
            }
            if (length > MAX_LENGTH) {
                this.length = MAX_LENGTH;
            }
        }

        public ColumnDefinition justify(final Justify justify) {
            this.justify = justify;
            return this;
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static class Cell {

        private final Format format = DEFAULT_FORMAT;

        private final List<Text> text;

        public Cell(final Text txt) {
            this.text = new ArrayList<>();
            this.text.add(txt);
        }

        public Cell(final Object object, final Code... codes) {
            this(new Text(object, codes));
        }

        protected Line getFormattedLine(final int lineNumber, final ColumnDefinition def) {
            final Line line = new Line();
            int startSubstring = lineNumber * def.getLength();
            int currentVisibleLength = 0;
            for (final Text t : this.text) {
                final String part = t.getVisibleText(startSubstring, def.getLength());
                currentVisibleLength += part.length();
                if (StringUtils.isNotBlank(part)) {
                    line.add(part, t.getFormat());
                    if (currentVisibleLength >= def.getLength()) {
                        break;
                    }
                    startSubstring = 0;
                } else {
                    startSubstring = startSubstring - t.getLength();
                }
            }
            return line;
        }

        protected List<Line> getFormattedLines(final ColumnDefinition def) {
            int count = 0;
            Line line;
            final List<Line> lines = new ArrayList<>();
            while ((line = getFormattedLine(count++, def)).isNotEmpty()) {
                lines.add(line);
            }
            return lines;
        }

        public String render(final int row, final ColumnDefinition def) {
            final List<Line> test = getFormattedLines(def);
            final Line line = test.size() > row ? test.get(row) : null;
            if (line != null) {
                return def.getJustify().apply(line.render(false, row == def.getMaxLines() - 1 && test.size() > def.getMaxLines()),
                        def.getLength() + (line.getLength() - line.getVisibleLength()));
            }
            return def.getJustify().apply("", def.getLength());

        }

        public Cell add(final Object object, final Code... codes) {
            this.text.add(new Text(object, codes));
            return this;
        }

    }

    private static final Format DEFAULT_FORMAT = new Format();

    /**
     * A grid / table for printing results.
     */

    private final List<ColumnDefinition> definitions = new ArrayList<>();

    private final List<Cell> values = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param def {@link ColumnDefinition}s
     */
    public Grid(final ColumnDefinition... def) {
        Stream.of(def).forEach(this::addColumn);
    }

    private String generateGridStart() {
        return IntStream.range(0, getLineLength() + this.definitions.size()).mapToObj(i -> "-").collect(Collectors.joining("")) + "\n";
    }

    private String generateGridEnd() {
        return IntStream.range(0, getLineLength() + this.definitions.size()).mapToObj(i -> "-").collect(Collectors.joining("")) + "\n";
    }

    private String generateHeader() {
        return "|" + this.definitions.stream().map(d -> StringUtils.rightPad(d.getName(), d.getLength())).collect(Collectors.joining("|"))
                + "|\n";
    }

    /**
     * Adds new a column definition.
     *
     * @param def definitions
     * @return this grid
     */
    public Grid addColumn(final ColumnDefinition def) {
        this.definitions.add(def);
        return this;
    }

    private void calculateLength() {
        IntStream.range(0, this.definitions.size()).forEach(i -> {
            final ColumnDefinition def = this.definitions.get(i);
            final List<Cell> column = getColumn(i);
            final int maxLength = column.stream().mapToInt(cell -> cell.getText().stream().mapToInt(Text::getLength).sum()).max().orElse(0);
            def.setLength(Math.max(maxLength, def.getName().length()));

        });
    }

    public List<Cell> getColumn(final int index) {

        return IntStream.range(0, this.values.size()).filter(n -> n % this.definitions.size() == index).mapToObj(this.values::get)
                .collect(Collectors.toList());
    }

    public Grid addCell(final Cell cell) {
        this.values.add(cell);
        return this;
    }

    public Grid addCell(final Text... text) {
        return addCell(new Cell(Arrays.asList(text)));
    }

    public Grid addCell(final Object cell, final Code... codes) {
        final Format f = new Format();
        f.addCodes(codes);
        final Text t = new Text(cell, f);
        return addCell(new Cell(t));
    }

    public Grid addCell(final Object cell) {
        return addCell(cell, DEFAULT_FORMAT.getTextColor());
    }

    private Collection<List<Cell>> prepareLines() {
        final AtomicInteger counter = new AtomicInteger();
        final int chunkSize = this.definitions.size();
        return this.values.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / chunkSize)).values();
    }

    public String render() {
        final StringBuilder b = new StringBuilder();
        calculateLength();
        b.append(generateGridStart());
        b.append(generateHeader());
        prepareLines().forEach(line -> b.append(printLine(line)));

        b.append(generateGridEnd());
        return b.toString();
    }

    private String printLine(final List<Cell> line) {
        final StringBuilder b = new StringBuilder();
        int virtualLine = 0;
        while (true) {
            final StringBuilder current = new StringBuilder();
            final int bound = this.definitions.size();
            for (int i = 0; i < bound; i++) {
                final ColumnDefinition def = this.definitions.get(i);
                current.append("|");
                current.append(line.get(i).render(virtualLine, def));
            }
            current.append("|");
            if (isEmpty(current) || virtualLine >= getMaxVirtualLine()) {
                break;
            }
            b.append(current.toString());
            virtualLine++;
            b.append("\n");
        }
        return b.toString();

    }

    private static boolean isEmpty(final StringBuilder current) {
        return current.toString().replaceAll("\\|", "").trim().length() == 0;
    }

    private int getMaxVirtualLine() {
        return this.definitions.stream().mapToInt(ColumnDefinition::getMaxLines).max().orElseThrow(IllegalAccessError::new);
    }

    private int getLineLength() {
        return this.definitions.stream().map(ColumnDefinition::getLength).reduce(0, Integer::sum);
    }
}
