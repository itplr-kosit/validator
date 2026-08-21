package org.kosit.validator.impl.tasks;

import org.jspecify.annotations.NonNull;
import org.kosit.validator.impl.xml.XMLReaderWrapper;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.XVRLReportSummary;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.util.JAXBSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class XvrlSerializer {

    private final XvrlConversionService conversionService;

    private final Processor processor;

    public XvrlSerializer(final @NonNull XvrlConversionService conversionService, final @NonNull Processor processor) {
        this.conversionService = conversionService;
        this.processor = processor;
    }

    public XdmNode serialize(final XVRLReportSummary summary) throws JAXBException, SaxonApiException {
        final DocumentBuilder documentBuilder = this.processor.newDocumentBuilder();
        final Marshaller marshaller = this.conversionService.getJaxbContext().createMarshaller();
        final JAXBSource source = new JAXBSource(marshaller, summary);
        // wrap to circumvent inconsistency between sax and saxon
        source.setXMLReader(new XMLReaderWrapper(source.getXMLReader()));
        return documentBuilder.build(source);
    }
}
