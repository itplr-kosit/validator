<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xvrl="http://www.xproc.org/ns/xvrl"
                exclude-result-prefixes="xs"
                version="2.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:param name="input-document" as="document-node(element())" required="yes" />


    <xsl:template match="xvrl:reports">
        <report xmlns="http://validator.kosit.de/test-report">
            <input>
                <xsl:copy-of select="$input-document" />
            </input>
            <result>
                <xsl:copy-of select="." />
            </result>
            <text>
                <xsl:value-of select="unparsed-text('someText.txt','UTF-8')" />
            </text>
        </report>
    </xsl:template>


</xsl:stylesheet>
