package org.kosit.validator.cmd;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.validation.CTValidationStandard;
import org.fusesource.jansi.AnsiRenderer.Code;
import org.kosit.validator.cmd.report.Grid;
import org.kosit.validator.cmd.report.Grid.ColumnDefinition;
import org.kosit.validator.cmd.report.Justify;
import org.kosit.validator.cmd.report.Line;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;

/**
 * The result table the validator writes to the console — one row per checked document, unchanged in shape from the 1.6
 * output: file, schema, schematron, acceptance, findings.
 * <p>
 * The values come from the canonical pipeline run. Two of the five columns are derived differently than in the
 * intermediate 2.0 state, and both changes restore what 1.6 does:
 * </p>
 * <ul>
 * <li><b>Schematron</b> counts errors, not findings. 1.6 reports {@code validationStepResult valid="true"} for a step
 * whose only message carries {@code level="information"}; the legacy result object in between counted every
 * {@code failed-assert} regardless of its flag.</li>
 * <li><b>Acceptance</b> is the recommendation of step 9, not an XPath on a rendered report. The XPath had no report
 * left to run on once the 1.x report rendering went away.</li>
 * </ul>
 *
 * @author Andreas Penski
 * @author Andreas Schmitz
 */
final class ResultTable {

    private ResultTable() {
        // static utility
    }

    /**
     * The 1.6 vocabulary of the {@code Acceptance} column. {@code EVALUATE_FURTHER} maps to {@code UNDEFINED}: neither
     * is an acceptance, and 1.6 has no third positive outcome.
     */
    private static AcceptRecommendation recommendation(final ConformanceValidationResult result) {
        return switch (result.getDecision()) {
            case ACCEPT -> AcceptRecommendation.ACCEPTABLE;
            case REJECT -> AcceptRecommendation.REJECT;
            default -> AcceptRecommendation.UNDEFINED;
        };
    }

    static boolean isAcceptable(final ConformanceValidationResult result) {
        return recommendation(result) == AcceptRecommendation.ACCEPTABLE;
    }

    /**
     * Whether rule sets of the given kind ran <b>and</b> none of them reported an error. A run that never got as far as
     * rule application shows {@code N}, as in 1.6: its report carries no {@code validationStepResult} for the step at
     * all, and "no rule set complained" is not the same statement as "no rule set ran".
     */
    private static boolean isValid(final ConformanceValidationResult result, final CTValidationStandard standard) {
        final List<CTDetectionList> ofKind = result.getFindingsByRuleSet().entrySet().stream().filter(e -> standard(e.getKey()) == standard)
                .map(Entry::getValue).toList();
        return !ofKind.isEmpty() && ofKind.stream().noneMatch(CTDetectionList::containsAtLeastOneError);
    }

    private static CTValidationStandard standard(final CTPreparedRuleSet ruleSet) {
        return ruleSet.getEngineType().getStandard();
    }

    /**
     * What the {@code Error/Description} column shows: the findings of rule application, or — for a run that never got
     * that far — what the cancelling step detected.
     */
    private static String describe(final ConformanceValidationResult result) {
        final List<CTDetection> detections = new ArrayList<>();
        if (result.isCompleted()) {
            result.getFindingsByRuleSet().values().forEach(d -> detections.addAll(d.getAll()));
        } else {
            detections.addAll(result.getRun().cancelDetections().getAll());
        }
        return detections.stream().map(d -> d.getText().getDisplayTextLocaleIndependent()).collect(Collectors.joining(";"));
    }

    private static Grid createResultGrid(final Map<String, ConformanceValidationResult> results) {
        final Grid grid = new Grid(
        //@formatter:off
        new ColumnDefinition("File", 60, 10, 1), new ColumnDefinition("Schema", 7).justify(Justify.CENTER), new ColumnDefinition("Schematron", 10).justify(Justify.CENTER), new ColumnDefinition("Acceptance", 10, 5).justify(Justify.CENTER), new ColumnDefinition("Error/Description", 60, 20, 3));
        //@formatter:on
        results.entrySet().stream().sorted(Entry.comparingByKey()).forEach(e -> {
            final ConformanceValidationResult value = e.getValue();
            final Code textcolor = isAcceptable(value) ? Code.GREEN : Code.RED;
            grid.addCell(e.getKey(), textcolor);
            grid.addCell(isValid(value, CTValidationStandard.XSD) ? "Y" : "N", textcolor);
            grid.addCell(isValid(value, CTValidationStandard.SCHEMATRON) ? "Y" : "N", textcolor);
            grid.addCell(recommendation(value), textcolor);
            grid.addCell(describe(value));
        });
        return grid;
    }

    private static String createStatusLine(final Map<String, ConformanceValidationResult> results) {
        final long acceptable = results.values().stream().filter(ResultTable::isAcceptable).count();
        final long rejected = results.size() - acceptable;
        final long errors = results.values().stream().filter(r -> !r.isCompleted()).count();
        final Line line = new Line();
        line.add("Acceptable: ").add(acceptable, Code.GREEN);
        line.add(" Rejected: ").add(rejected, Code.RED);
        if (errors > 0) {
            line.add(" Processing errors: ").add(errors, Code.RED);
        }
        return line.render(true, false);
    }

    /** Writes the table and the status line to standard output. */
    static void print(final Map<String, ConformanceValidationResult> results) {
        final PrintWriter writer = new PrintWriter(System.out); // NOSONAR
        writer.write("Results:\n");
        writer.write(createResultGrid(results).render());
        writer.write(createStatusLine(results));
        writer.flush();
    }

    /** @return the number of documents that are not acceptable — the exit code of the run. */
    static int notAcceptableCount(final Map<String, ConformanceValidationResult> results) {
        return (int) results.values().stream().filter(r -> !isAcceptable(r)).count();
    }
}
