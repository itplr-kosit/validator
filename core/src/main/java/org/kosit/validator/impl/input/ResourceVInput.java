package org.kosit.validator.impl.input;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.stream.StreamSource;

/**
 * An {@link VInput} carries an {@link URL} which can be used for all 'locatable' inputs such as {@link File},
 * {@link java.nio.file.Path} and any other {@link URL}.
 * 
 * This stream is NOT read into memory. So this implementation has good in memory efficieny. The validation process MAY
 * read the stream more than once. Make sure, that the {@link URL} points to fast I/O devices
 * 
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public class ResourceVInput extends AbstractVInput {

    private final URL url;

    private final String name;

    private final String digestAlgorithm;

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public StreamSource getSource() throws IOException {
        InputStream stream = this.url.openStream();
        if (!isHashcodeComputed()) {
            stream = StreamHelper.wrapDigesting(this, stream, getDigestAlgorithm());
        }
        return new StreamSource(stream, this.name);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public URL getUrl() {
        return this.url;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getName() {
        return this.name;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public String getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public ResourceVInput(final URL url, final String name, final String digestAlgorithm) {
        this.url = url;
        this.name = name;
        this.digestAlgorithm = digestAlgorithm;
    }
}
