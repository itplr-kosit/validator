<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2"
    xmlns:sqf="http://www.schematron-quickfix.com/validator/process">
    
    <sch:ns uri="http://www.xoev.de/de/validator/varl/1" prefix="rep"/>
    <sch:ns uri="http://www.xoev.de/de/validator/framework/1/scenarios" prefix="s"/>

    <sch:pattern>
        <sch:rule context="rep:report">
            <sch:assert test="@varlVersion='1.0.0'">VARL version must be 1.0.0.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:rule context="rep:report[rep:noScenarioMatched]">
            <sch:assert test="@valid='false'">If no scenario matched, then the report must be flagged invalid.</sch:assert>
        </sch:rule>
        <sch:rule context="rep:report[rep:scenarioMatched/rep:validationStepResult[@valid = 'false']]">
            <sch:assert test="@valid='false'">If any validation step has been flagged invalid, then the report must be flagged invalid.</sch:assert>
        </sch:rule>
        <sch:rule context="rep:report[rep:scenarioMatched and empty(rep:scenarioMatched/rep:validationStepResult[@valid = 'false'])]">
            <sch:assert test="@valid='true'">If a scenario matched and no validation step has been flagged invalid, then the report must be flagged valid.</sch:assert>
        </sch:rule>
    </sch:pattern>
    

    <sch:pattern>
        <sch:rule context="rep:scenarioMatched[rep:validationStepResult[@id = 'val-xsd' and @valid='true']]">
            <sch:assert test="rep:validationStepResult[@id = 'val-sch.1']">If xsd is valid then schematron checks have to be performed.</sch:assert>
        </sch:rule>
        <sch:rule context="rep:scenarioMatched[rep:validationStepResult[@id = 'val-xsd' and @valid='false']]">
            <sch:assert test="empty(rep:validationStepResult[@id = 'val-sch.1'])">If xsd is invalid then schematron checks must not be performed.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern>
        <sch:rule context="rep:validationStepResult[rep:message[@level=('error','warning')]]">
            <sch:assert test="@valid = 'false'">If a validation step has errors or warnings, it must be flagged invalid.</sch:assert>
        </sch:rule>
        <sch:rule context="rep:validationStepResult[not(rep:message[@level=('error','warning')])]">
            <sch:assert test="@valid = 'true'">If a validation step has no errors or warnings, it must be flagged valid.</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <sch:pattern>
        <sch:rule context="rep:message">
            <sch:assert test="@lineNumber or @xpathLocation">Some kind of error location must be given.</sch:assert>
        </sch:rule>
    </sch:pattern>

</sch:schema>