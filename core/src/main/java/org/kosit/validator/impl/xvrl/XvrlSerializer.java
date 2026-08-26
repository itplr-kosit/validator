package org.kosit.validator.impl.xvrl;

import org.jspecify.annotations.Nullable;
import org.kosit.base.xml.XMLReaderWrapper;
import org.kosit.jaxb.LoggingEventHandler;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.XVRLReportsType;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.util.JAXBSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class XvrlSerializer {

    private final Processor processor;

    public XvrlSerializer(final @Nullable Processor processor) {
        this.processor = processor != null ? processor : ProcessorProvider.getProcessor();
    }

    public @Nullable XdmNode marshalToXdmNode(final XVRLReportsType summary) throws JAXBException, SaxonApiException {
        if (false)
            new XvrlConversionService().withEventHandler(new LoggingEventHandler()).writeXml(summary);

        final Marshaller marshaller = XvrlConversionService.JAXB_CTX.createMarshaller();
        final JAXBSource source = new JAXBSource(marshaller, new ObjectFactory().createReports(summary));
        // wrap for security to circumvent inconsistency between sax and saxon
        source.setXMLReader(new XMLReaderWrapper(source.getXMLReader()));

        return this.processor.newDocumentBuilder().build(source);
    }
}
