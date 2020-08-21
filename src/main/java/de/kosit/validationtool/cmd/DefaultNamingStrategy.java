package de.kosit.validationtool.cmd;

import static org.apache.commons.io.FilenameUtils.isExtension;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Setter;

/**
 * A default {@link NamingStrategy} supporting prefix and postfix configurations for generating report names
 * 
 * @author Andreas Penski
 */
@Setter
public class DefaultNamingStrategy implements NamingStrategy {

    private String prefix;

    private String postfix;

    @Override
    public String createName(final String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Can not generate name based on null input");
        }
        final String base = isExtension(name.toLowerCase(), "xml") ? FilenameUtils.getBaseName(name) : name;
        final StringBuilder result = new StringBuilder();
        if (isNotEmpty(this.prefix)) {
            result.append(this.prefix).append("-");
        }
        result.append(base);
        if (isNotEmpty(this.postfix)) {
            result.append("-").append(this.postfix);
        } else if (isEmpty(this.prefix)) {
            result.append("-").append("report");
        }
        result.append(".xml");
        return result.toString();
    }
}
