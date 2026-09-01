<?xml version="1.0" encoding="UTF-8"?>
<!-- Compiles fine with SchXslt, but raises a dynamic XPath error (FORG0001) at validation
     time: casting the non-numeric content of s:inner to xs:integer fails when the rule
     fires. Used to verify that a schxslt processing error is detected and reported. -->
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematron with runtime processing error</title>

    <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>
    <ns uri="http://validator.kosit.de/test-sample" prefix="s"/>

    <pattern name="Provoke dynamic error">
        <rule context="s:simple">
            <assert id="runtime-error-1" test="xs:integer(s:inner) lt 5">
                The inner element must be a number smaller than 5.
            </assert>
        </rule>
    </pattern>
</schema>
