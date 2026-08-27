package org.kosit.base.error;

import java.util.Objects;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Default immutable implementation of the {@link SimpleError} interface. Use {@link SimpleErrorBuilder} to create
 * instances in a fluent way.
 *
 * @author Philip Helger
 */
public final class DefaultSimpleError implements SimpleError {

    private final @Nullable String systemId;

    private final long lineNumber;

    private final long columnNumber;

    private final CTStandardSeverity severity;

    private final String message;

    private final @Nullable Exception linkedException;

    /**
     * Constructor.
     *
     * @param systemId the object in which the error was found. May be <code>null</code>.
     * @param lineNumber the line number. Values &le; 0 mean none available.
     * @param columnNumber the column number. Values &le; 0 mean none available.
     * @param severity the severity of the error. May not be <code>null</code>.
     * @param message the main error message. May not be <code>null</code>.
     * @param linkedException the exception that caused this error. May be <code>null</code>.
     */
    public DefaultSimpleError(final @Nullable String systemId, final long lineNumber, final long columnNumber,
            @NonNull final CTStandardSeverity severity, @NonNull final String message, final @Nullable Exception linkedException) {
        this.systemId = systemId;
        // Unify negative values for safe comparison
        this.lineNumber = lineNumber > 0 ? lineNumber : -1;
        this.columnNumber = columnNumber > 0 ? columnNumber : -1;
        this.severity = Objects.requireNonNull(severity, "Severity must not be null");
        this.message = Objects.requireNonNull(message, "Message must not be null");
        this.linkedException = linkedException;
    }

    public @Nullable String getSystemID() {
        return this.systemId;
    }

    public long getLineNumber() {
        return this.lineNumber;
    }

    public long getColumnNumber() {
        return this.columnNumber;
    }

    public CTStandardSeverity getSeverity() {
        return this.severity;
    }

    public String getMessage() {
        return this.message;
    }

    public @Nullable Exception getLinkedException() {
        return this.linkedException;
    }

    /**
     * Compares two exceptions for equality. Because {@link Exception} has no usable <code>equals</code> implementation,
     * only the class and the message are compared.
     *
     * @param left the first exception. May be <code>null</code>.
     * @param right the second exception. May be <code>null</code>.
     * @return <code>true</code> if both are <code>null</code> or considered equal
     */
    private static boolean equalsException(final @Nullable Exception left, final @Nullable Exception right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.getClass().equals(right.getClass()) && Objects.equals(left.getMessage(), right.getMessage());
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof final DefaultSimpleError rhs)) {
            return false;
        }
        return Objects.equals(this.systemId, rhs.systemId) && this.lineNumber == rhs.lineNumber && this.columnNumber == rhs.columnNumber
                && this.severity == rhs.severity && this.message.equals(rhs.message)
                && equalsException(this.linkedException, rhs.linkedException);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.systemId, Long.valueOf(this.lineNumber), Long.valueOf(this.columnNumber), this.severity, this.message,
                this.linkedException == null ? null : this.linkedException.getClass(),
                this.linkedException == null ? null : this.linkedException.getMessage());
    }

    @Override
    public String toString() {
        return "DefaultSimpleError(systemId=" + this.systemId + ", lineNumber=" + this.lineNumber + ", columnNumber=" + this.columnNumber
                + ", severity=" + this.severity + ", message=" + this.message + ", linkedException=" + this.linkedException + ")";
    }

    /**
     * Creates a new empty builder using the default severity {@link SimpleErrorBuilder#DEFAULT_SEVERITY}.
     *
     * @return a new builder. Never <code>null</code>.
     */
    @NonNull
    public static SimpleErrorBuilder builder() {
        return new SimpleErrorBuilder();
    }

    /**
     * Creates a new builder containing all the data of the provided error.
     *
     * @param error the error to copy the data from. May not be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    @NonNull
    public static SimpleErrorBuilder builder(@NonNull final SimpleError error) {
        return new SimpleErrorBuilder(error);
    }

    /**
     * Creates a new empty builder with the severity {@link CTStandardSeverity#NONE}.
     *
     * @return a new builder. Never <code>null</code>.
     */
    @NonNull
    public static SimpleErrorBuilder builderNone() {
        return builder().severity(CTStandardSeverity.NONE);
    }

    /**
     * Creates a new empty builder with the severity {@link CTStandardSeverity#WARNING}.
     *
     * @return a new builder. Never <code>null</code>.
     */
    @NonNull
    public static SimpleErrorBuilder builderWarning() {
        return builder().severity(CTStandardSeverity.WARNING);
    }

    /**
     * Creates a new empty builder with the severity {@link CTStandardSeverity#ERROR}.
     *
     * @return a new builder. Never <code>null</code>.
     */
    @NonNull
    public static SimpleErrorBuilder builderError() {
        return builder().severity(CTStandardSeverity.ERROR);
    }
}
