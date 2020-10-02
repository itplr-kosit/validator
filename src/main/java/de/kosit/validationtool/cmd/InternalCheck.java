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

package de.kosit.validationtool.cmd;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;
import de.kosit.validationtool.cmd.report.Grid;
import de.kosit.validationtool.cmd.report.Grid.ColumnDefinition;
import de.kosit.validationtool.cmd.report.Justify;
import de.kosit.validationtool.cmd.report.Line;
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
    InternalCheck(final Configuration configuration) {
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

    void printResults(final Map<String, Result> results) {
        final PrintWriter writer = new PrintWriter(System.out);// NOSONAR
        writer.write("Results:\n");
        writer.write(createResultGrid(results).render());
        writer.write(createStatusLine(results));
        writer.write(createAssertionStatus());
        writer.flush();
    }

    private String createAssertionStatus() {
        final Line line = new Line();
        if (this.failedAssertions > 0) {
            log.error("Assertion check failed.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
            line.add(MessageFormat.format("Assertions run: {0}, Assertions failed: ", this.checkAssertions));
            line.add(this.failedAssertions, Code.RED);
        } else if (this.checkAssertions > 0) {
            log.info("Assertion check successful.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
            line.add(MessageFormat.format("Assertions run: {0}, Assertions failed: {1}", this.checkAssertions, this.failedAssertions));
        }
        return line.render(true, false);
    }

    @Override
    public boolean isSuccessful(final Map<String, Result> results) {
        if (this.checkAssertions > 0) {
            return this.failedAssertions == 0;
        }
        return super.isSuccessful(results);
    }

    public int getNotAcceptableCount(final Map<String, Result> results) {
        return (int) (this.failedAssertions + results.values().stream().filter(e -> !e.isAcceptable()).count());
    }

    private static String createStatusLine(final Map<String, Result> results) {
        final long acceptable = results.entrySet().stream().filter(e -> e.getValue().isAcceptable()).count();
        final long rejected = results.entrySet().stream().filter(e -> !e.getValue().isAcceptable()).count();
        final long errors = results.entrySet().stream().filter(e -> !e.getValue().isProcessingSuccessful()).count();
        final Line line = new Line();
        line.add("Acceptable: ").add(acceptable, Code.GREEN);
        line.add(" Rejected: ").add(rejected, Code.RED);
        if (errors > 0) {
            line.add(" Processing errors: ").add(errors, Code.RED);
        }
        return line.render(true, false);
    }

    private static Grid createResultGrid(final Map<String, Result> results) {
        final Grid grid = new Grid(
        //@formatter:off
                new ColumnDefinition("filename", 60, 10, 1), 
                new ColumnDefinition("Schema", 7).justify(Justify.CENTER),
                new ColumnDefinition("Schematron", 10).justify(Justify.CENTER),
                new ColumnDefinition("Acceptance", 10, 5).justify(Justify.CENTER),
                new ColumnDefinition("Error/Description", 60,20,3) 
        );
        //@formatter:on
        results.entrySet().stream().sorted(Entry.comparingByKey()).forEach(e -> {
            final Result value = e.getValue();

            final Code textcolor = value.isAcceptable() ? Code.GREEN : Code.RED;
            grid.addCell(e.getKey(), textcolor);
            grid.addCell(value.isSchemaValid() ? "Y" : "N", textcolor);
            grid.addCell(value.isSchematronValid() ? "Y" : "N", textcolor);
            grid.addCell(value.getAcceptRecommendation(), textcolor);
            grid.addCell(joinErrors(value));
        });
        return grid;
    }

    private static String joinErrors(final Result value) {
        final StringBuilder b = new StringBuilder();
        b.append(String.join(";", value.getProcessingErrors()));
        if (value.getSchemaViolations() != null && !value.getSchemaViolations().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchemaViolations().stream().map(XmlError::getMessage).collect(Collectors.joining(";")));
        }
        if (value.getSchematronResult() != null && !value.getSchematronResult().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchematronResult().stream().flatMap(e -> e.getMessages().stream()).collect(Collectors.joining(";")));
        }
        return b.toString();
    }

}
