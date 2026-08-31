package org.kosit.validator.xvrl;

import org.jspecify.annotations.Nullable;
import org.kosit.base.xml.XmlReaderWrapper;
import org.kosit.jaxb.eventhandler.LoggingEventHandler;
import org.kosit.validator.impl.saxon.ProcessorProvider;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kosit.xvrl.jaxb.XvrlJaxbCreator;
import org.kosit.xvrl.model.XvrlReports;

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

    public @Nullable XdmNode marshalToXdmNode(final XvrlReports summary) throws JAXBException, SaxonApiException {
        if (false)
            new XvrlConverter().withEventHandler(new LoggingEventHandler()).writeXml(summary);

        final Marshaller marshaller = XvrlConverter.JAXB_CTX.createMarshaller();
        final JAXBSource source = new JAXBSource(marshaller, XvrlJaxbCreator.createReports(summary));
        // wrap for security to circumvent inconsistency between sax and saxon
        source.setXMLReader(new XmlReaderWrapper(source.getXMLReader()));

        return this.processor.newDocumentBuilder().build(source);
    }
}
