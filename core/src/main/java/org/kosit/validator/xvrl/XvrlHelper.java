package org.kosit.validator.xvrl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlDigest;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlSeverity;
import org.kosit.xvrl.model.XvrlValidity;

public final class XvrlHelper {

    private static @NonNull XvrlValidity calcValidity(final XvrlReport.Builder builder) {
        return builder.getDetections().stream().anyMatch(XvrlDetection::hasErrors) ? XvrlValidity.FALSE : XvrlValidity.TRUE;
    }

    @Nullable
    private static Long countDetections(final XvrlReport.Builder builder, final @NonNull XvrlSeverity severity) {
        // Only values > 0 are emitted
        final long count = builder.getDetections().stream().filter(e -> e.getSeverity() == severity).count();
        return count == 0 ? null : Long.valueOf(count);
    }

    public static XvrlReport finalizeAndBuild(final XvrlReport.Builder builder) {
        builder.digest(XvrlDigest.builder().fatalErrorCount(countDetections(builder, XvrlSeverity.FATAL_ERROR))
                .errorCount(countDetections(builder, XvrlSeverity.ERROR)).warningCount(countDetections(builder, XvrlSeverity.WARNING))
                .infoCount(countDetections(builder, XvrlSeverity.INFO)).unspecifiedCount(countDetections(builder, XvrlSeverity.UNSPECIFIED))
                .valid(calcValidity(builder)));

        return builder.build();
    }
}
