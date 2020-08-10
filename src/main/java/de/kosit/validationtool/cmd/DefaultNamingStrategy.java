package de.kosit.validationtool.cmd;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * A default {@link NamingStrategy} supporting prefix and postfix configurations for generating report names
 * 
 * @author Andreas Penski
 */
@Getter
@Setter
public class DefaultNamingStrategy implements NamingStrategy {

    private String prefix;

    private String postfix = "report";

    @Override
    public String createName(final String base) {
        if (StringUtils.isEmpty(base)) {
            throw new IllegalArgumentException("Can not generate name based on null input");
        }
        final int index = base.lastIndexOf('.');
        final StringBuilder result = new StringBuilder();
        if (isNotEmpty(this.prefix)) {
            result.append(this.prefix).append("-");
        }
        result.append(base, 0, index > 0 ? index : base.length());
        if (isNotEmpty(this.postfix)) {
            result.append("-").append(this.postfix);
        }
        if (index > 0) {
            result.append(base.substring(index));
        } else {
            result.append(".xml");
        }
        return result.toString();
    }
}
