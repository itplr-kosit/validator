package de.kosit.validationtool.impl.tasks;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.AcceptRecommendation;

import net.sf.saxon.s9api.XPathSelector;

/**
 * Berechnet die Akzeptanz-Empfehlung gemäß konfigurierten 'acceptMatch' des aktuellen Szenarios.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
@Slf4j
public class ComputeAcceptanceAction implements CheckAction {

    @Override
    public void check(final Bag results) {
        final String acceptMatch = results.getScenarioSelectionResult().getObject().getAcceptMatch();
        if (isNotBlank(acceptMatch)) {

            try {

                final XPathSelector selector = results.getScenarioSelectionResult().getObject().getAcceptSelector();
                selector.setContextItem(results.getReport());
                results.setAcceptStatus(selector.effectiveBooleanValue() ? AcceptRecommendation.ACCEPTABLE : AcceptRecommendation.REJECT);
            } catch (final Exception e) {
                log.error("Fehler bei Evaluierung des Accept-Status: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isSkipped(final Bag results) {
        return results.getReport() == null;
    }

}
