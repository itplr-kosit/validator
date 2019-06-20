package de.kosit.validationtool.api;

/**
 * Fehlerobjekt für die Bereitstellung von Fehlern aus der internen Verarbeitung, bspw. Schema-Validation-Fehler.
 * 
 * @author Andreas Penski
 */
public interface XmlError {

    /**
     * Gibt die Fehlermeldung zurück.
     * 
     * @return die Fehlermeldung
     */
    String getMessage();

    /**
     * Zeigt den Schweregrad der Fehlermeldung an
     * 
     * @return der Schweregrad
     * @see Severity
     */
    Severity getSeverity();

    /**
     * Gibt optional eine Zeilennummer an, aus der der Fehler resultiert.
     * 
     * @return die Zeitelnnummer
     */
    Integer getRowNumber();

    /**
     * Gibt optional eine Spaltennummer an, aus der der Fehler resultiert.
     * 
     * @return die Spaltennummer
     */
    Integer getColumnNumber();

    enum Severity {
        SEVERITY_WARNING, SEVERITY_ERROR, SEVERITY_FATAL_ERROR;
    }

}
