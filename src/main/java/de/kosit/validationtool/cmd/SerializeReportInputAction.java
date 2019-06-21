package de.kosit.validationtool.cmd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.tasks.CheckAction;

/**
 * Serializes the {@link de.kosit.validationtool.model.reportInput.CreateReportInput report input} document.
 *
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class SerializeReportInputAction implements CheckAction {

    private final Path outputDirectory;

    private final ConversionService conversionService;

    @Override
    public void check(final Bag results) {
        final Path file = this.outputDirectory.resolve(results.getName() + "-reportInput.xml");
        try {
            log.info("Serializing result to {}", file.toAbsolutePath());
            final String xml = this.conversionService.writeXml(results.getReportInput());
            Files.write(file, xml.getBytes());
        } catch (final IOException e) {
            log.error("Can not serialize result report to {}", file.toAbsolutePath(), e);
        }
    }

    @Override
    public boolean isSkipped(final Bag results) {
        if (results.getReportInput() == null) {
            log.warn("Can not serialize  report input. No object found");
            return true;
        }
        return false;
    }
}
