<?xml version="1.0" encoding="UTF-8"?>
<!-- Included by with-include.sch: the rule lives in a second file to show where includes are resolved. -->
<pattern xmlns="http://purl.oclc.org/dsdl/schematron" id="included-pattern">
    <rule context="/*">
        <assert id="inc-1" test="local-name() = 'simple'" flag="fatal">The root element must be 'simple' (rule from an included file)</assert>
    </rule>
</pattern>
