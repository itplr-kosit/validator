package org.kosit.validator.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.kosit.validator.client.api.ValidationApi;
import org.kosit.validator.model.mvrl.MVRLCompactReport;
import org.kosit.validator.model.xvrl.XVRLReportSummary;

import java.io.File;

@ApplicationScoped
public class ValidationClient {

    @RestClient
    ValidationApi api;

    private final JAXBContext jaxbContext;

    public ValidationClient() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(XVRLReportSummary.class, MVRLCompactReport.class);
    }

    public File validateRaw(File input) {
        return api.validate(input);
    }

    public File validateMinimalRaw(File input) {
        return api.validateMinimal(input);
    }

    public XVRLReportSummary validate(File input) {
        return unmarshal(api.validate(input), XVRLReportSummary.class);
    }

    public MVRLCompactReport validateMinimal(File input) {
        return unmarshal(api.validateMinimal(input), MVRLCompactReport.class);
    }

    private <T> T unmarshal(File file, Class<T> type) {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return type.cast(unmarshaller.unmarshal(file));
        } catch (JAXBException e) {
            throw new ValidatorClientException("Failed to unmarshal response", e);
        }
    }

}
