<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:a="http://www.xoev.de/de/validator/framework/1/assertions" exclude-result-prefixes="xs" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:template match="/" name="generate-assertions">
        <xsl:variable name="csv" select="unparsed-text('assertions.csv', 'iso-8859-1')"/>
        <xsl:variable name="rows" as="element(row)+">
            <xsl:analyze-string select="$csv" regex="\r\n?|\n">
                <xsl:non-matching-substring>
                    <row>
                        <xsl:for-each select="tokenize(., ';')">
                            <cell>
                                <xsl:value-of select="."/>
                            </cell>
                        </xsl:for-each>
                    </row>
                </xsl:non-matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <a:assertions>
            <a:namespace prefix="rep">http://www.xoev.de/de/validator/varl/1</a:namespace>
            <a:namespace prefix="s">http://www.xoev.de/de/validator/framework/1/scenarios</a:namespace>
            <a:namespace prefix="html">http://www.w3.org/1999/xhtml</a:namespace>
            <xsl:for-each select="$rows[1]/cell/position()[. ge 3]">
                <xsl:variable name="column-index" as="xs:integer" select="current()"/>
                <xsl:if test="string-length($rows[1]/cell[$column-index]) gt 0">
                    <xsl:for-each select="2 to count($rows)">
                        <xsl:variable name="row" as="element(row)" select="$rows[current()]"/>
                        <a:assertion>
                            <xsl:attribute name="report-doc">
                                <xsl:value-of select="$rows[1]/cell[$column-index]"/>
                                <xsl:text>.xml</xsl:text>
                            </xsl:attribute>
                            <xsl:variable name="test" as="xs:string" select="$row/cell[2]"/>
                            <xsl:attribute name="test">
                                <xsl:choose>
                                    <xsl:when test="$row/cell[$column-index] = '+'">
                                        <xsl:value-of select="$test"/>
                                    </xsl:when>
                                    <xsl:when test="$row/cell[$column-index] = '-'">
                                        <xsl:text>not(</xsl:text>
                                        <xsl:value-of select="$test"/>
                                        <xsl:text>)</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:message>Incomplete csv file?</xsl:message>
                                        <xsl:message select="$row"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:attribute>
                            <xsl:value-of select="$row/cell[1]"/>
                        </a:assertion>
                    </xsl:for-each>
                </xsl:if>
            </xsl:for-each>
        </a:assertions>
    </xsl:template>

</xsl:stylesheet>
