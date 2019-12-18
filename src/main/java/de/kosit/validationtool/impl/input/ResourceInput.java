package de.kosit.validationtool.impl.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.api.Input;

/**
 * An {@link Input} carries an {@link URL} which can be used for all 'locatable' inputs such as {@link File},
 * {@link java.nio.file.Path} and any other {@link URL}.
 * 
 * This stream is NOT read into memory. So this implementation has good in memory efficieny. The validation process MAY
 * read the stream more than once. Make sure, that the {@link URL} points to fast I/O devices
 * 
 * @author Andreas Penski
 */
@Getter
@RequiredArgsConstructor
public class ResourceInput extends AbstractInput {

    private final URL url;

    private final String name;

    private final String digestAlgorithm;

    @Override
    public Source getSource() throws IOException {
        InputStream stream = this.url.openStream();
        if (!isHashcodeComputed()) {
            stream = StreamHelper.wrapDigesting(this, stream, getDigestAlgorithm());
        }
        return new StreamSource(stream, this.name);
    }
}
