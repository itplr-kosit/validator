package org.kosit.xvrl.impl;

import java.util.List;

import org.kosit.xvrl.api.BaseDetection;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLDigest;

public abstract class AbstractXVRLReport {

    public abstract List<XVRLDetection> getDetection();

    public List<String> getAllErrors() {
        return getDetection().stream().filter(BaseDetection::hasErrors).flatMap(xvrlDetection -> xvrlDetection.getAllMessages().stream())
                .toList();
    }

    @Override
    public String toString() {
        return "id=" + getDigest().getId() + ", errors=" + getDigest().getErrorCount() + ", valid=" + getDigest().getValid();
    }

    protected abstract XVRLDigest getDigest();
}
