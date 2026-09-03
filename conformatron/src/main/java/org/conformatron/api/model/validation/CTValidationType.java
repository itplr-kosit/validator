package org.conformatron.api.model.validation;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Interface for a validation type. That basically is a combination of syntax, rule language and result layout.
 *
 * @author Philip Helger
 * @see CTStandardValidationType
 */
public interface CTValidationType {

    @NonNull
    @Nonempty
    String getID();

    /**
     * @return The validation standard used. Never <code>null</code>.
     */
    @NonNull
    CTValidationStandard getStandard();

    @NonNull
    @Nonempty
    String getName();

    /**
     * @return <code>true</code> to stop validation if an error occurs when using this validation type. This is helpful
     *         to avoid running Schematron validations when the XML/XSD validations already failed.
     */
    boolean isStopValidationOnError();

    /**
     * @return <code>true</code> if the application of this validation type requires additional context parameters.
     */
    boolean isContextRequired();
}
