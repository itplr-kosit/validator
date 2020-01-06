package de.kosit.validationtool.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Hält statische Informatione über diesen Validator.
 * 
 * @author Andreas Penski
 */
public class EngineInformation {

    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try ( final InputStream input = EngineInformation.class.getClassLoader().getResourceAsStream("app-info.properties") ) {
            if (input != null) {
                PROPERTIES.load(input);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Can not engine information", e);
        }
    }

    private EngineInformation() {
        // hide
    }

    /**
     * Gibt die Versions-Nummer des Validators zurück.
     * 
     * @return die Version
     */
    public static String getVersion() {
        return PROPERTIES.getProperty("project_version");
    }

    /**
     * Gibt den Namen der Engine zurück.
     * 
     * @return der Name
     */
    public static String getName() {
        return PROPERTIES.getProperty("engine_name");
    }

    /**
     * Gibt die Versions-Nummer des verwendeten Frameworks zurück. Diese ist relevant um Scenario-Konfiguration und
     * Validator-Versionen aufeinander abzustimmen.
     *
     * @return die Framework-Version
     */
    public static String getFrameworkVersion() {
        return PROPERTIES.getProperty("framework_version");
    }

    /**
     * Gibt die Major-Versions-Nummer des eingesetzten Frameworks zurück.
     * 
     * @return die Major-Versions-Nummer
     */
    public static String getFrameworkMajorVersion() {
        return getFrameworkVersion().substring(0, 1);
    }

    /**
     * Gibt den Namespace des eingesetzten Frameworks zurück.
     * 
     * @return die Major-Versions-Nummer
     */
    public static String getFrameworkNamespace() {
        return "http://www.xoev.de/de/validator/framework/" + getFrameworkMajorVersion() + "/createreportinput";
    }
}
