<?xml version="1.0" encoding="UTF-8"?>
<!-- A modular Schematron: the only rule comes from abstracts.sch next to this file.
     CLI -S resolves the include (the directory of the .sch is the repository); the server ad hoc endpoint
     receives one file and cannot (rule-prepare-error). -->
<schema xmlns="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
    <title>Ad hoc rules with an include</title>
    <include href="abstracts.sch"/>
</schema>
