package org.kosit.validator.scenario.generic;

/**
 * The kind of a single {@link ScenarioDescriptionBlock}.
 *
 * @author Philip Helger
 */
public enum EScenarioDescriptionBlockKind {

    /**
     * Free text without any markup. Only scenario configuration version 3 can express this - version 2 writes it as a
     * {@link #PARAGRAPH} instead.
     */
    TEXT,

    /** A single paragraph - the XML element "p" */
    PARAGRAPH,

    /** An ordered list - the XML element "ol" */
    ORDERED_LIST,

    /** An unordered list - the XML element "ul" */
    UNORDERED_LIST;

    /**
     * @return <code>true</code> if this kind carries a list of items instead of a single text.
     */
    public boolean isList() {
        return this == ORDERED_LIST || this == UNORDERED_LIST;
    }
}
