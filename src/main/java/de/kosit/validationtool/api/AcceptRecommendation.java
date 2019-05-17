package de.kosit.validationtool.api;

/**
 * Status der Empfehlung.
 */
public enum AcceptRecommendation {
    /**
     * Nicht definiert, weil eine Evaluierung nicht durchgeführt wurde, oder nicht durchgeführt werden konnte.
     */
    UNDEFINED,

    /**
     * Das Dokument ist gemäß Konfiguration valide und kann akzeptiert werden.
     */
    ACCEPTABLE,

    /**
     * Das Dokuemnt ist gemäß Konfiguration invalide und sollte NICHT akzeptiert werden.
     */
    REJECT
}