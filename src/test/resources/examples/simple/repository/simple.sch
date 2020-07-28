<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Schematron Simple</title>

    <ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>
    <ns uri="http://validator.kosit.de/test-sample" prefix="s"/>


    <pattern xmlns="http://purl.oclc.org/dsdl/schematron" name="Check structure">
        <rule context="s:simple">
            <assert id="content-1" test="count(s:inner) = 1">The element inner appears exactly once.</assert>
       </rule>
       <rule context="s:foo">
             <assert id="content-2" test="count(s:inner) = 1">The element inner appears exactly once.</assert>
       </rule>
    </pattern>

</schema>