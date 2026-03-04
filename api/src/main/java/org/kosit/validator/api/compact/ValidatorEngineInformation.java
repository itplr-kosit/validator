package org.kosit.validator.api.compact;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Enthält Informationen über den verwendeten Validator (Name und Version).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidatorEngineInformation {

    private String name;

    private String version;
}
