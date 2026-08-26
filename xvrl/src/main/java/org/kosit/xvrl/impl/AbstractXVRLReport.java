package org.kosit.xvrl.impl;

import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.api.BaseDetection;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLDigestType;

public abstract class AbstractXVRLReport {

    public abstract List<XVRLDetectionType> getDetection();

    @ReturnsImmutableObject
    public List<String> getAllErrors() {
        return getDetection().stream().filter(BaseDetection::hasErrors).flatMap(xvrlDetection -> xvrlDetection.getAllMessages().stream())
                .toList();
    }

    protected abstract XVRLDigestType getDigest();

    @Override
    public String toString() {
        return "id=" + getDigest().getId() + ", errors=" + getDigest().getErrorCount() + ", valid=" + getDigest().getValid();
    }
}
