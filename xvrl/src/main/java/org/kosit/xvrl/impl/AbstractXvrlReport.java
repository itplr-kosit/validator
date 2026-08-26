package org.kosit.xvrl.impl;

import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.api.BaseDetection;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlDigestType;

public abstract class AbstractXvrlReport {

    public abstract List<XvrlDetectionType> getDetection();

    @ReturnsImmutableObject
    public List<String> getAllErrors() {
        return getDetection().stream().filter(BaseDetection::hasErrors).flatMap(xvrlDetection -> xvrlDetection.getAllMessages().stream())
                .toList();
    }

    protected abstract XvrlDigestType getDigest();

    @Override
    public String toString() {
        return "id=" + getDigest().getId() + ", errors=" + getDigest().getErrorCount() + ", valid=" + getDigest().getValid();
    }
}
