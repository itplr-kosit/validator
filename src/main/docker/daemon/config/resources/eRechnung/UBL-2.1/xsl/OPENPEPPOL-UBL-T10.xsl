<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  ~ Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
  ~ one or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  KoSIT licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                xmlns:ubl="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                version="2.0"><!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
    <xsl:param name="archiveDirParameter" />
    <xsl:param name="archiveNameParameter" />
    <xsl:param name="fileNameParameter" />
    <xsl:param name="fileDirParameter" />
    <xsl:variable name="document-uri">
        <xsl:value-of select="document-uri(/)" />
    </xsl:variable>

    <!--PHASES-->


    <!--PROLOG-->
    <xsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                method="xml"
                omit-xml-declaration="no"
                standalone="yes"
                indent="yes" />

    <!--XSD TYPES FOR XSLT2-->


    <!--KEYS AND FUNCTIONS-->


    <!--DEFAULT RULES-->


    <!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
    <!--This mode can be used to generate an ugly though full XPath for locators-->
    <xsl:template match="*" mode="schematron-select-full-path">
        <xsl:apply-templates select="." mode="schematron-get-full-path" />
    </xsl:template>

    <!--MODE: SCHEMATRON-FULL-PATH-->
    <!--This mode can be used to generate an ugly though full XPath for locators-->
    <xsl:template match="*" mode="schematron-get-full-path">
        <xsl:apply-templates select="parent::*" mode="schematron-get-full-path" />
        <xsl:text>/</xsl:text>
        <xsl:choose>
            <xsl:when test="namespace-uri()=''">
                <xsl:value-of select="name()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>*:</xsl:text>
                <xsl:value-of select="local-name()" />
                <xsl:text>[namespace-uri()='</xsl:text>
                <xsl:value-of select="namespace-uri()" />
                <xsl:text>']</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:variable name="preceding"
                      select="count(preceding-sibling::*[local-name()=local-name(current())                                   and namespace-uri() = namespace-uri(current())])" />
        <xsl:text>[</xsl:text>
        <xsl:value-of select="1+ $preceding" />
        <xsl:text>]</xsl:text>
    </xsl:template>
    <xsl:template match="@*" mode="schematron-get-full-path">
        <xsl:apply-templates select="parent::*" mode="schematron-get-full-path" />
        <xsl:text>/</xsl:text>
        <xsl:choose>
            <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>@*[local-name()='</xsl:text>
                <xsl:value-of select="local-name()" />
                <xsl:text>' and namespace-uri()='</xsl:text>
                <xsl:value-of select="namespace-uri()" />
                <xsl:text>']</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--MODE: SCHEMATRON-FULL-PATH-2-->
    <!--This mode can be used to generate prefixed XPath for humans-->
    <xsl:template match="node() | @*" mode="schematron-get-full-path-2">
        <xsl:for-each select="ancestor-or-self::*">
            <xsl:text>/</xsl:text>
            <xsl:value-of select="name(.)" />
            <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
                <xsl:text>]</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test="not(self::*)">
            <xsl:text />/@<xsl:value-of select="name(.)" />
        </xsl:if>
    </xsl:template>
    <!--MODE: SCHEMATRON-FULL-PATH-3-->
    <!--This mode can be used to generate prefixed XPath for humans
     (Top-level element has index)-->
    <xsl:template match="node() | @*" mode="schematron-get-full-path-3">
        <xsl:for-each select="ancestor-or-self::*">
            <xsl:text>/</xsl:text>
            <xsl:value-of select="name(.)" />
            <xsl:if test="parent::*">
                <xsl:text>[</xsl:text>
                <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1" />
                <xsl:text>]</xsl:text>
            </xsl:if>
        </xsl:for-each>
        <xsl:if test="not(self::*)">
            <xsl:text />/@<xsl:value-of select="name(.)" />
        </xsl:if>
    </xsl:template>

    <!--MODE: GENERATE-ID-FROM-PATH -->
    <xsl:template match="/" mode="generate-id-from-path" />
    <xsl:template match="text()" mode="generate-id-from-path">
        <xsl:apply-templates select="parent::*" mode="generate-id-from-path" />
        <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')" />
    </xsl:template>
    <xsl:template match="comment()" mode="generate-id-from-path">
        <xsl:apply-templates select="parent::*" mode="generate-id-from-path" />
        <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')" />
    </xsl:template>
    <xsl:template match="processing-instruction()" mode="generate-id-from-path">
        <xsl:apply-templates select="parent::*" mode="generate-id-from-path" />
        <xsl:value-of
                select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')" />
    </xsl:template>
    <xsl:template match="@*" mode="generate-id-from-path">
        <xsl:apply-templates select="parent::*" mode="generate-id-from-path" />
        <xsl:value-of select="concat('.@', name())" />
    </xsl:template>
    <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
        <xsl:apply-templates select="parent::*" mode="generate-id-from-path" />
        <xsl:text>.</xsl:text>
        <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')" />
    </xsl:template>

    <!--MODE: GENERATE-ID-2 -->
    <xsl:template match="/" mode="generate-id-2">U</xsl:template>
    <xsl:template match="*" mode="generate-id-2" priority="2">
        <xsl:text>U</xsl:text>
        <xsl:number level="multiple" count="*" />
    </xsl:template>
    <xsl:template match="node()" mode="generate-id-2">
        <xsl:text>U.</xsl:text>
        <xsl:number level="multiple" count="*" />
        <xsl:text>n</xsl:text>
        <xsl:number count="node()" />
    </xsl:template>
    <xsl:template match="@*" mode="generate-id-2">
        <xsl:text>U.</xsl:text>
        <xsl:number level="multiple" count="*" />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="string-length(local-name(.))" />
        <xsl:text>_</xsl:text>
        <xsl:value-of select="translate(name(),':','.')" />
    </xsl:template>
    <!--Strip characters-->
    <xsl:template match="text()" priority="-1" />

    <!--SCHEMA SETUP-->
    <xsl:template match="/">
        <svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                title="OPENPEPPOL  T10 bound to UBL"
                                schemaVersion="">
            <xsl:comment>
                <xsl:value-of select="$archiveDirParameter" />
                <xsl:value-of select="$archiveNameParameter" />
                <xsl:value-of select="$fileNameParameter" />
                <xsl:value-of select="$fileDirParameter" />
            </xsl:comment>
            <svrl:ns-prefix-in-attribute-values
                    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                    prefix="cbc" />
            <svrl:ns-prefix-in-attribute-values
                    uri="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                    prefix="cac" />
            <svrl:ns-prefix-in-attribute-values uri="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                                                prefix="ubl" />
            <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2001/XMLSchema" prefix="xs" />
            <svrl:active-pattern>
                <xsl:attribute name="document">
                    <xsl:value-of select="document-uri(/)" />
                </xsl:attribute>
                <xsl:attribute name="id">UBL-T10</xsl:attribute>
                <xsl:attribute name="name">UBL-T10</xsl:attribute>
                <xsl:apply-templates />
            </svrl:active-pattern>
            <xsl:apply-templates select="/" mode="M7" />
            <svrl:active-pattern>
                <xsl:attribute name="document">
                    <xsl:value-of select="document-uri(/)" />
                </xsl:attribute>
                <xsl:attribute name="id">CodesT10</xsl:attribute>
                <xsl:attribute name="name">CodesT10</xsl:attribute>
                <xsl:apply-templates />
            </svrl:active-pattern>
            <xsl:apply-templates select="/" mode="M8" />
        </svrl:schematron-output>
    </xsl:template>

    <!--SCHEMATRON PATTERNS-->
    <svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">OPENPEPPOL T10 bound to UBL</svrl:text>

    <!--PATTERN UBL-T10-->


    <!--RULE -->
    <xsl:template match="/ubl:Invoice/cac:AllowanceCharge" priority="1022" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="/ubl:Invoice/cac:AllowanceCharge" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="not(cbc:MultiplierFactorNumeric) or number(cbc:MultiplierFactorNumeric) &gt;=0" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(cbc:MultiplierFactorNumeric) or number(cbc:MultiplierFactorNumeric) &gt;=0">
                    <xsl:attribute name="id">EUGEN-T10-R012</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R012]-An allowance percentage MUST NOT be negative.</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="number(cbc:Amount)&gt;=0" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="number(cbc:Amount)&gt;=0">
                    <xsl:attribute name="id">EUGEN-T10-R022</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R022]-An allowance or charge amount MUST NOT be negative.</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="string-length(substring-after(cbc:Amount, '.')) &lt;= 2" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="string-length(substring-after(cbc:Amount, '.')) &lt;= 2">
                    <xsl:attribute name="id">EUGEN-T10-R052</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R052]-Document level amounts cannot have more than 2 decimals</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cbc:AllowanceChargeReasonCode" priority="1021" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cbc:AllowanceChargeReasonCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID = 'UNCL4465'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@listID = 'UNCL4465'">
                    <xsl:attribute name="id">EUGEN-T10-R029</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R029]-An allowance charge reason code MUST have a list identifier attribute
                        'UNCL4465'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:Country/cbc:IdentificationCode"
                  priority="1020"
                  mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:Country/cbc:IdentificationCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID = 'ISO3166-1:Alpha2'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="@listID = 'ISO3166-1:Alpha2'">
                    <xsl:attribute name="id">EUGEN-T10-R027</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R027]-A country identification code MUST have a list identifier attribute
                        'ISO3166-1:Alpha2'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//*[contains(name(),'CurrencyCode')]"
                  priority="1019"
                  mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//*[contains(name(),'CurrencyCode')]" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID =  'ISO4217'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@listID = 'ISO4217'">
                    <xsl:attribute name="id">EUGEN-T10-R026</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R026]-A currency code element MUST have a list identifier attribute
                        'ISO4217'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:AccountingCustomerParty" priority="1018" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:AccountingCustomerParty" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PartyName/cbc:Name)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PartyName/cbc:Name)">
                    <xsl:attribute name="id">EUGEN-T10-R036</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R036]-An invoice MUST have a buyer name</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PostalAddress)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PostalAddress)">
                    <xsl:attribute name="id">EUGEN-T10-R038</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R038]-An invoice MUST have a buyer postal address</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PartyLegalEntity)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PartyLegalEntity)">
                    <xsl:attribute name="id">EUGEN-T10-R040</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R040]-A customer SHOULD provide information about its legal entity
                        information
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:DeliveryLocation/cbc:ID" priority="1017" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:DeliveryLocation/cbc:ID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID">
                    <xsl:attribute name="id">EUGEN-T10-R034</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R034]-An delivery location identifier MUST have a scheme identifier
                        attribute.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cbc:DocumentTypeCode" priority="1016" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cbc:DocumentTypeCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID = 'UNCL1001'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@listID = 'UNCL1001'">
                    <xsl:attribute name="id">EUGEN-T10-R033</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R033]-A document type code MUST have a list identifier attribute 'UNCL1001'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cbc:EndpointID" priority="1015" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//cbc:EndpointID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID">
                    <xsl:attribute name="id">EUGEN-T10-R023</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R023]-An endpoint identifier MUST have a scheme identifier attribute.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:PayeeFinancialAccount/cbc:ID"
                  priority="1014"
                  mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:PayeeFinancialAccount/cbc:ID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID">
                    <xsl:attribute name="id">EUGEN-T10-R031</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R031]-A financial account identifier MUST have a scheme identifier
                        attribute.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="/ubl:Invoice" priority="1013" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/ubl:Invoice" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="not(//cbc:TaxCurrencyCode) or (//cac:TaxExchangeRate)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(//cbc:TaxCurrencyCode) or (//cac:TaxExchangeRate)">
                    <xsl:attribute name="id">EUGEN-T10-R044</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R044]-If the tax currency code is different from the document currency code,
                        the tax exchange rate MUST be provided
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="not(count(//*[not(node()[not(self::comment())])]) &gt; 0)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(count(//*[not(node()[not(self::comment())])]) &gt; 0)">
                    <xsl:attribute name="id">EUGEN-T10-R047</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R047]- An invoice should not contain empty elements</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cbc:InvoiceTypeCode)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="(cbc:InvoiceTypeCode)">
                    <xsl:attribute name="id">EUGEN-T10-R053</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R053]- An invoice must have an Invoice type code</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cbc:InvoiceTypeCode" priority="1012" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//cbc:InvoiceTypeCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID = 'UNCL1001'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@listID = 'UNCL1001'">
                    <xsl:attribute name="id">EUGEN-T10-R025</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R025]-An invoice type code MUST have a list identifier attribute 'UNCL1001'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:PartyLegalEntity/cbc:CompanyID"
                  priority="1011"
                  mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:PartyLegalEntity/cbc:CompanyID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID">
                    <xsl:attribute name="id">EUGEN-T10-R054</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R054]-A party legal entity company identifier SHOULD have a scheme identifier
                        attribute.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:PartyIdentification/cbc:ID" priority="1010" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:PartyIdentification/cbc:ID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID">
                    <xsl:attribute name="id">EUGEN-T10-R024</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R024]-A party identifier MUST have a scheme identifier attribute.</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:PaymentMeans" priority="1009" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//cac:PaymentMeans" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="((cbc:PaymentMeansCode = '31') and (cac:PayeeFinancialAccount/cbc:ID/@schemeID and cac:PayeeFinancialAccount/cbc:ID/@schemeID = 'IBAN') and (cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cac:FinancialInstitution/cbc:ID/@schemeID and cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cac:FinancialInstitution/cbc:ID/@schemeID = 'BIC')) or (cbc:PaymentMeansCode != '31') or ((cbc:PaymentMeansCode = '31') and  (not(cac:PayeeFinancialAccount/cbc:ID/@schemeID) or (cac:PayeeFinancialAccount/cbc:ID/@schemeID != 'IBAN')))" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="((cbc:PaymentMeansCode = '31') and (cac:PayeeFinancialAccount/cbc:ID/@schemeID and cac:PayeeFinancialAccount/cbc:ID/@schemeID = 'IBAN') and (cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cac:FinancialInstitution/cbc:ID/@schemeID and cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cac:FinancialInstitution/cbc:ID/@schemeID = 'BIC')) or (cbc:PaymentMeansCode != '31') or ((cbc:PaymentMeansCode = '31') and (not(cac:PayeeFinancialAccount/cbc:ID/@schemeID) or (cac:PayeeFinancialAccount/cbc:ID/@schemeID != 'IBAN')))">
                    <xsl:attribute name="id">EUGEN-T10-R004</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R004]-If the payment means are international account transfer and the account
                        id is IBAN then the financial institution should be identified by using the BIC id.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cbc:PaymentMeansCode" priority="1008" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cbc:PaymentMeansCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@listID = 'UNCL4461'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@listID = 'UNCL4461'">
                    <xsl:attribute name="id">EUGEN-T10-R028</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R028]-A payment means code MUST have a list identifier attribute 'UNCL4461'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:AccountingSupplierParty" priority="1007" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:AccountingSupplierParty" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PartyName/cbc:Name)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PartyName/cbc:Name)">
                    <xsl:attribute name="id">EUGEN-T10-R035</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R035]-An invoice MUST have a seller name</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PostalAddress)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PostalAddress)">
                    <xsl:attribute name="id">EUGEN-T10-R037</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R037]-An invoice MUST have a seller postal address</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cac:Party/cac:PartyLegalEntity)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cac:Party/cac:PartyLegalEntity)">
                    <xsl:attribute name="id">EUGEN-T10-R039</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R039]-A supplier SHOULD provide information about its legal entity
                        information
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="not(/ubl:Invoice/cac:TaxTotal/*/*/*/cbc:ID = 'VAT') or (some $companyID in cac:Party/cac:PartyTaxScheme/cbc:CompanyID satisfies (starts-with($companyID,cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode)))" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(/ubl:Invoice/cac:TaxTotal/*/*/*/cbc:ID = 'VAT') or (some $companyID in cac:Party/cac:PartyTaxScheme/cbc:CompanyID satisfies (starts-with($companyID,cac:Party/cac:PostalAddress/cac:Country/cbc:IdentificationCode)))">
                    <xsl:attribute name="id">EUGEN-T10-R041</xsl:attribute>
                    <xsl:attribute name="flag">warning</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R041]-The VAT identifier for the supplier SHOULD be prefixed with country code
                        for companies with VAT registration in EU countries
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:TaxCategory" priority="1006" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//cac:TaxCategory" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(parent::cac:AllowanceCharge) or (cbc:ID and cbc:Percent) or (cbc:ID = 'AE')" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(parent::cac:AllowanceCharge) or (cbc:ID and cbc:Percent) or (cbc:ID = 'AE')">
                    <xsl:attribute name="id">EUGEN-T10-R008</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R008]-For each tax subcategory the category ID and the applicable tax
                        percentage MUST be provided.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:TaxCategory/cbc:ID" priority="1005" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:TaxCategory/cbc:ID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="@schemeID = 'UNCL5305'" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@schemeID = 'UNCL5305'">
                    <xsl:attribute name="id">EUGEN-T10-R032</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R032]-A tax category identifier MUST have a scheme identifier attribute
                        'UNCL5305'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:TaxExchangeRate" priority="1004" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="//cac:TaxExchangeRate" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="(cbc:CalculationRate) and (cbc:MathematicOperatorCode)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(cbc:CalculationRate) and (cbc:MathematicOperatorCode)">
                    <xsl:attribute name="id">EUGEN-T10-R045</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R045]-Tax exchange rate MUST specify the calculation rate and the operator
                        code.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="/ubl:Invoice/cac:TaxTotal" priority="1003" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="/ubl:Invoice/cac:TaxTotal" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="(xs:decimal(child::cbc:TaxAmount)= round(number(xs:decimal(sum(cac:TaxSubtotal/cbc:TaxAmount)) * 10 * 10)) div 100) " />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="(xs:decimal(child::cbc:TaxAmount)= round(number(xs:decimal(sum(cac:TaxSubtotal/cbc:TaxAmount)) * 10 * 10)) div 100)">
                    <xsl:attribute name="id">EUGEN-T10-R043</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R043]-The total tax amount MUST equal the sum of tax amounts per category.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="string-length(substring-after(cbc:TaxAmount, '.')) &lt;= 2" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="string-length(substring-after(cbc:TaxAmount, '.')) &lt;= 2">
                    <xsl:attribute name="id">EUGEN-T10-R049</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R049]- Total tax amount cannot have more than 2 decimals.</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="/ubl:Invoice/cac:TaxTotal/cac:TaxSubtotal"
                  priority="1002"
                  mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="/ubl:Invoice/cac:TaxTotal/cac:TaxSubtotal" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="string-length(substring-after(cbc:TaxableAmount, '.')) &lt;= 2" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="string-length(substring-after(cbc:TaxableAmount, '.')) &lt;= 2">
                    <xsl:attribute name="id">EUGEN-T10-R050</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R050]- Tax subtotal amounts cannot have more than 2 decimals.</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="string-length(substring-after(cbc:TaxAmount, '.')) &lt;= 2" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="string-length(substring-after(cbc:TaxAmount, '.')) &lt;= 2">
                    <xsl:attribute name="id">EUGEN-T10-R051</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R051]-Document level amounts cannot have more than 2 decimals</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="((cbc:TaxableAmount) and (cac:TaxCategory/cbc:Percent) and (xs:decimal(cbc:TaxAmount - 1) &lt; xs:decimal(cbc:TaxableAmount * (xs:decimal(cac:TaxCategory/cbc:Percent) div 100))) and (xs:decimal(cbc:TaxAmount + 1) &gt; xs:decimal(cbc:TaxableAmount * (xs:decimal(cac:TaxCategory/cbc:Percent) div 100)))) or not(cac:TaxCategory/cbc:Percent) or not(cbc:TaxableAmount)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="((cbc:TaxableAmount) and (cac:TaxCategory/cbc:Percent) and (xs:decimal(cbc:TaxAmount - 1) &lt; xs:decimal(cbc:TaxableAmount * (xs:decimal(cac:TaxCategory/cbc:Percent) div 100))) and (xs:decimal(cbc:TaxAmount + 1) &gt; xs:decimal(cbc:TaxableAmount * (xs:decimal(cac:TaxCategory/cbc:Percent) div 100)))) or not(cac:TaxCategory/cbc:Percent) or not(cbc:TaxableAmount)">
                    <xsl:attribute name="id">EUGEN-T10-R042</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R042]-The tax amount per category MUST be the taxable amount multiplied by the
                        category percentage.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="not(/ubl:Invoice/cbc:TaxCurrencyCode) or (cbc:TaxAmount and cbc:TransactionCurrencyTaxAmount)" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(/ubl:Invoice/cbc:TaxCurrencyCode) or (cbc:TaxAmount and cbc:TransactionCurrencyTaxAmount)">
                    <xsl:attribute name="id">EUGEN-T10-R046</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R046]-If the tax currency code is different from the document currency code,
                        each tax subtotal has to include the tax amount in both currencies
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//*[contains(name(),'Quantity')]" priority="1001" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//*[contains(name(),'Quantity')]" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="not(attribute::unitCode) or (attribute::unitCode and attribute::unitCodeListID = 'UNECERec20')" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="not(attribute::unitCode) or (attribute::unitCode and attribute::unitCodeListID = 'UNECERec20')">
                    <xsl:attribute name="id">EUGEN-T10-R030</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R030]-A unit code attribute MUST have a unit code list identifier attribute
                        'UNECERec20'.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="//cac:LegalMonetaryTotal/child::*" priority="1000" mode="M7">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="//cac:LegalMonetaryTotal/child::*" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when test="string-length(substring-after(., '.')) &lt;= 2" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="string-length(substring-after(., '.')) &lt;= 2">
                    <xsl:attribute name="id">EUGEN-T10-R048</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[EUGEN-T10-R048]-Document level amounts cannot have more than 2 decimals</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>
    <xsl:template match="text()" priority="-1" mode="M7" />
    <xsl:template match="@*|node()" priority="-2" mode="M7">
        <xsl:apply-templates select="@*|*" mode="M7" />
    </xsl:template>

    <!--PATTERN CodesT10-->


    <!--RULE -->
    <xsl:template match="cac:ContractDocumentReference//cbc:DocumentTypeCode"
                  priority="1008"
                  mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cac:ContractDocumentReference//cbc:DocumentTypeCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128 129 130 131 132 133 134 135 136 137 138 139 140 141 142 143 144 145 146 147 148 149 150 151 152 153 154 155 156 157 158 159 160 161 162 163 164 165 166 167 168 169 170 171 172 173 174 175 176 177 178 179 180 181 182 183 184 185 186 187 188 189 190 191 192 193 194 195 196 197 198 199 200 201 202 203 204 205 206 207 208 209 210 211 212 213 214 215 216 217 218 219 220 221 222 223 224 225 226 227 228 229 230 231 232 233 234 235 236 237 238 239 240 241 242 243 244 245 246 247 248 249 250 251 252 253 254 255 256 257 258 259 260 261 262 263 264 265 266 267 268 269 270 271 272 273 274 275 276 277 278 279 280 281 282 283 284 285 286 287 288 289 290 291 292 293 294 295 296 297 298 299 300 301 302 303 304 305 306 307 308 309 310 311 312 313 314 315 316 317 318 319 320 321 322 323 324 325 326 327 328 329 330 331 332 333 334 335 336 337 338 339 340 341 342 343 344 345 346 347 348 349 350 351 352 353 354 355 356 357 358 359 360 361 362 363 364 365 366 367 368 369 370 371 372 373 374 375 376 377 378 379 380 381 382 383 384 385 386 387 388 389 390 391 392 393 394 395 396 397 398 399 400 401 402 403 404 405 406 407 408 409 410 411 412 413 414 415 416 417 418 419 420 421 422 423 424 425 426 427 428 429 430 431 432 433 434 435 436 437 438 439 440 441 442 443 444 445 446 447 448 449 450 451 452 453 454 455 456 457 458 459 460 461 462 463 464 465 466 467 468 469 470 481 482 483 484 485 486 487 488 489 490 491 493 494 495 496 497 498 499 520 521 522 523 524 525 526 527 528 529 530 531 532 533 534 535 536 537 538 550 575 580 610 621 622 623 624 630 631 632 633 635 640 650 655 700 701 702 703 704 705 706 707 708 709 710 711 712 713 714 715 716 720 722 723 724 730 740 741 743 744 745 746 750 760 761 763 764 765 766 770 775 780 781 782 783 784 785 786 787 788 789 790 791 792 793 794 795 796 797 798 799 810 811 812 820 821 822 823 824 825 830 833 840 841 850 851 852 853 855 856 860 861 862 863 864 865 870 890 895 896 901 910 911 913 914 915 916 917 925 926 927 929 930 931 932 933 934 935 936 937 938 940 941 950 951 952 953 954 955 960 961 962 963 964 965 966 970 971 972 974 975 976 977 978 979 990 991 995 996 998 ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 63 64 65 66 67 68 69 70 71 72 73 74 75 76 77 78 79 80 81 82 83 84 85 86 87 88 89 90 91 92 93 94 95 96 97 98 99 100 101 102 103 104 105 106 107 108 109 110 111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128 129 130 131 132 133 134 135 136 137 138 139 140 141 142 143 144 145 146 147 148 149 150 151 152 153 154 155 156 157 158 159 160 161 162 163 164 165 166 167 168 169 170 171 172 173 174 175 176 177 178 179 180 181 182 183 184 185 186 187 188 189 190 191 192 193 194 195 196 197 198 199 200 201 202 203 204 205 206 207 208 209 210 211 212 213 214 215 216 217 218 219 220 221 222 223 224 225 226 227 228 229 230 231 232 233 234 235 236 237 238 239 240 241 242 243 244 245 246 247 248 249 250 251 252 253 254 255 256 257 258 259 260 261 262 263 264 265 266 267 268 269 270 271 272 273 274 275 276 277 278 279 280 281 282 283 284 285 286 287 288 289 290 291 292 293 294 295 296 297 298 299 300 301 302 303 304 305 306 307 308 309 310 311 312 313 314 315 316 317 318 319 320 321 322 323 324 325 326 327 328 329 330 331 332 333 334 335 336 337 338 339 340 341 342 343 344 345 346 347 348 349 350 351 352 353 354 355 356 357 358 359 360 361 362 363 364 365 366 367 368 369 370 371 372 373 374 375 376 377 378 379 380 381 382 383 384 385 386 387 388 389 390 391 392 393 394 395 396 397 398 399 400 401 402 403 404 405 406 407 408 409 410 411 412 413 414 415 416 417 418 419 420 421 422 423 424 425 426 427 428 429 430 431 432 433 434 435 436 437 438 439 440 441 442 443 444 445 446 447 448 449 450 451 452 453 454 455 456 457 458 459 460 461 462 463 464 465 466 467 468 469 470 481 482 483 484 485 486 487 488 489 490 491 493 494 495 496 497 498 499 520 521 522 523 524 525 526 527 528 529 530 531 532 533 534 535 536 537 538 550 575 580 610 621 622 623 624 630 631 632 633 635 640 650 655 700 701 702 703 704 705 706 707 708 709 710 711 712 713 714 715 716 720 722 723 724 730 740 741 743 744 745 746 750 760 761 763 764 765 766 770 775 780 781 782 783 784 785 786 787 788 789 790 791 792 793 794 795 796 797 798 799 810 811 812 820 821 822 823 824 825 830 833 840 841 850 851 852 853 855 856 860 861 862 863 864 865 870 890 895 896 901 910 911 913 914 915 916 917 925 926 927 929 930 931 932 933 934 935 936 937 938 940 941 950 951 952 953 954 955 960 961 962 963 964 965 966 970 971 972 974 975 976 977 978 979 990 991 995 996 998 ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R001</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R001]-Contract document type code MUST be coded using UNCL 1001 list BII2
                        subset.
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:EndpointID//@schemeID" priority="1007" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cbc:EndpointID//@schemeID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R002</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R002]-An Endpoint Identifier Scheme MUST be from the list of PEPPOL Party
                        Identifiers described in the "PEPPOL Policy for using Identifiers".
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cac:PartyIdentification/cbc:ID//@schemeID"
                  priority="1006"
                  mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cac:PartyIdentification/cbc:ID//@schemeID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R003</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R003]-An Party Identifier Scheme MUST be from the list of PEPPOL Party
                        Identifiers described in the "PEPPOL Policy for using Identifiers".
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cac:PayeeFinancialAccount/cbc:ID//@schemeID"
                  priority="1005"
                  mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cac:PayeeFinancialAccount/cbc:ID//@schemeID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' IBAN LOCAL ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' IBAN LOCAL ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R004</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R004]-A payee account identifier scheme MUST be from the Account ID PEPPOL code
                        list
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:*/@unitCode" priority="1004" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cbc:*/@unitCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' 5 6 8 10 11 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 40 41 43 44 45 46 47 48 53 54 56 57 58 59 60 61 62 63 64 66 69 71 72 73 74 76 77 78 80 81 84 85 87 89 90 91 92 93 94 95 96 97 98 1A 1B 1C 1D 1E 1F 1G 1H 1I 1J 1K 1L 1M 1X 2A 2B 2C 2G 2H 2I 2J 2K 2L 2M 2N 2P 2Q 2R 2U 2V 2W 2X 2Y 2Z 3B 3C 3E 3G 3H 3I 4A 4B 4C 4E 4G 4H 4K 4L 4M 4N 4O 4P 4Q 4R 4T 4U 4W 4X 5A 5B 5C 5E 5F 5G 5H 5I 5J 5K 5P 5Q A1 A10 A11 A12 A13 A14 A15 A16 A17 A18 A19 A2 A20 A21 A22 A23 A24 A25 A26 A27 A28 A29 A3 A30 A31 A32 A33 A34 A35 A36 A37 A38 A39 A4 A40 A41 A42 A43 A44 A45 A47 A48 A49 A5 A50 A51 A52 A53 A54 A55 A56 A57 A58 A59 A6 A60 A61 A62 A63 A64 A65 A66 A67 A68 A69 A7 A70 A71 A73 A74 A75 A76 A77 A78 A79 A8 A80 A81 A82 A83 A84 A85 A86 A87 A88 A89 A9 A90 A91 A93 A94 A95 A96 A97 A98 A99 AA AB ACR ACT AD AE AH AI AJ AK AL AM AMH AMP ANN AP APZ AQ AR ARE AS ASM ASU ATM ATT AV AW AY AZ B0 B1 B10 B11 B12 B13 B14 B15 B16 B17 B18 B19 B2 B20 B21 B22 B23 B24 B25 B26 B27 B28 B29 B3 B30 B31 B32 B33 B34 B35 B36 B37 B38 B39 B4 B40 B41 B42 B43 B44 B45 B46 B47 B48 B49 B5 B50 B51 B52 B53 B54 B55 B56 B57 B58 B59 B6 B60 B61 B62 B63 B64 B65 B66 B67 B68 B69 B7 B70 B71 B72 B73 B74 B75 B76 B77 B78 B79 B8 B80 B81 B82 B83 B84 B85 B86 B87 B88 B89 B9 B90 B91 B92 B93 B94 B95 B96 B97 B98 B99 BAR BB BD BE BFT BG BH BHP BIL BJ BK BL BLD BLL BO BP BQL BR BT BTU BUA BUI BW BX BZ C0 C1 C10 C11 C12 C13 C14 C15 C16 C17 C18 C19 C2 C20 C21 C22 C23 C24 C25 C26 C27 C28 C29 C3 C30 C31 C32 C33 C34 C35 C36 C37 C38 C39 C4 C40 C41 C42 C43 C44 C45 C46 C47 C48 C49 C5 C50 C51 C52 C53 C54 C55 C56 C57 C58 C59 C6 C60 C61 C62 C63 C64 C65 C66 C67 C68 C69 C7 C70 C71 C72 C73 C74 C75 C76 C77 C78 C79 C8 C80 C81 C82 C83 C84 C85 C86 C87 C88 C89 C9 C90 C91 C92 C93 C94 C95 C96 C97 C98 C99 CA CCT CDL CEL CEN CG CGM CH CJ CK CKG CL CLF CLT CMK CMQ CMT CNP CNT CO COU CQ CR CS CT CTG CTM CTN CU CUR CV CWA CWI CY CZ D03 D04 D1 D10 D11 D12 D13 D14 D15 D16 D17 D18 D19 D2 D20 D21 D22 D23 D24 D25 D26 D27 D28 D29 D30 D31 D32 D33 D34 D35 D36 D37 D38 D39 D40 D41 D42 D43 D44 D45 D46 D47 D48 D49 D5 D50 D51 D52 D53 D54 D55 D56 D57 D58 D59 D6 D60 D61 D62 D63 D64 D65 D66 D67 D68 D69 D7 D70 D71 D72 D73 D74 D75 D76 D77 D78 D79 D8 D80 D81 D82 D83 D85 D86 D87 D88 D89 D9 D90 D91 D92 D93 D94 D95 D96 D97 D98 D99 DAA DAD DAY DB DC DD DE DEC DG DI DJ DLT DMA DMK DMO DMQ DMT DN DPC DPR DPT DQ DR DRA DRI DRL DRM DS DT DTN DU DWT DX DY DZN DZP E01 E07 E08 E09 E10 E11 E12 E14 E15 E16 E17 E18 E19 E2 E20 E21 E22 E23 E25 E27 E28 E3 E30 E31 E32 E33 E34 E35 E36 E37 E38 E39 E4 E40 E41 E42 E43 E44 E45 E46 E47 E48 E49 E5 E50 E51 E52 E53 E54 E55 E56 E57 E58 E59 E60 E61 E62 E63 E64 E65 E66 E67 E68 E69 E70 E71 E72 E73 E74 E75 E76 E77 E78 E79 E80 E81 E82 E83 E84 E85 E86 E87 E88 E89 E90 E91 E92 E93 E94 E95 E96 E97 E98 E99 EA EB EC EP EQ EV F01 F02 F03 F04 F05 F06 F07 F08 F1 F10 F11 F12 F13 F14 F15 F16 F17 F18 F19 F20 F21 F22 F23 F24 F25 F26 F27 F28 F29 F30 F31 F32 F33 F34 F35 F36 F37 F38 F39 F40 F41 F42 F43 F44 F45 F46 F47 F48 F49 F50 F51 F52 F53 F54 F55 F56 F57 F58 F59 F60 F61 F62 F63 F64 F65 F66 F67 F68 F69 F70 F71 F72 F73 F74 F75 F76 F77 F78 F79 F80 F81 F82 F83 F84 F85 F86 F87 F88 F89 F9 F90 F91 F92 F93 F94 F95 F96 F97 F98 F99 FAH FAR FB FBM FC FD FE FF FG FH FIT FL FM FOT FP FR FS FTK FTQ G01 G04 G05 G06 G08 G09 G10 G11 G12 G13 G14 G15 G16 G17 G18 G19 G2 G20 G21 G23 G24 G25 G26 G27 G28 G29 G3 G30 G31 G32 G33 G34 G35 G36 G37 G38 G39 G40 G41 G42 G43 G44 G45 G46 G47 G48 G49 G50 G51 G52 G53 G54 G55 G56 G57 G58 G59 G60 G61 G62 G63 G64 G65 G66 G67 G68 G69 G7 G70 G71 G72 G73 G74 G75 G76 G77 G78 G79 G80 G81 G82 G83 G84 G85 G86 G87 G88 G89 G90 G91 G92 G93 G94 G95 G96 G97 G98 G99 GB GBQ GC GD GDW GE GF GFI GGR GH GIA GIC GII GIP GJ GK GL GLD GLI GLL GM GN GO GP GQ GRM GRN GRO GRT GT GV GW GWH GY GZ H03 H04 H05 H06 H07 H08 H09 H1 H10 H11 H12 H13 H14 H15 H16 H18 H19 H2 H20 H21 H22 H23 H24 H25 H26 H27 H28 H29 H30 H31 H32 H33 H34 H35 H36 H37 H38 H39 H40 H41 H42 H43 H44 H45 H46 H47 H48 H49 H50 H51 H52 H53 H54 H55 H56 H57 H58 H59 H60 H61 H62 H63 H64 H65 H66 H67 H68 H69 H70 H71 H72 H73 H74 H75 H76 H77 H78 H79 H80 H81 H82 H83 H84 H85 H87 H88 H89 H90 H91 H92 H93 H94 H95 H96 H98 H99 HA HAR HBA HBX HC HD HDW HE HF HGM HH HI HIU HJ HK HKM HL HLT HM HMQ HMT HN HO HP HPA HS HT HTZ HUR HY IA IC IE IF II IL IM INH INK INQ IP ISD IT IU IV J10 J12 J13 J14 J15 J16 J17 J18 J19 J2 J20 J21 J22 J23 J24 J25 J26 J27 J28 J29 J30 J31 J32 J33 J34 J35 J36 J38 J39 J40 J41 J42 J43 J44 J45 J46 J47 J48 J49 J50 J51 J52 J53 J54 J55 J56 J57 J58 J59 J60 J61 J62 J63 J64 J65 J66 J67 J68 J69 J70 J71 J72 J73 J74 J75 J76 J78 J79 J81 J82 J83 J84 J85 J87 J89 J90 J91 J92 J93 J94 J95 J96 J97 J98 J99 JB JE JG JK JM JNT JO JOU JPS JR JWL K1 K10 K11 K12 K13 K14 K15 K16 K17 K18 K19 K2 K20 K21 K22 K23 K24 K25 K26 K27 K28 K3 K30 K31 K32 K33 K34 K35 K36 K37 K38 K39 K40 K41 K42 K43 K45 K46 K47 K48 K49 K5 K50 K51 K52 K53 K54 K55 K58 K59 K6 K60 K61 K62 K63 K64 K65 K66 K67 K68 K69 K70 K71 K73 K74 K75 K76 K77 K78 K79 K80 K81 K82 K83 K84 K85 K86 K87 K88 K89 K90 K91 K92 K93 K94 K95 K96 K97 K98 K99 KA KAT KB KBA KCC KD KDW KEL KF KG KGM KGS KHY KHZ KI KIC KIP KJ KJO KL KLK KMA KMH KMK KMQ KMT KNI KNS KNT KO KPA KPH KPO KPP KR KS KSD KSH KT KTM KTN KUR KVA KVR KVT KW KWH KWO KWT KX L10 L11 L12 L13 L14 L15 L16 L17 L18 L19 L2 L20 L21 L23 L24 L25 L26 L27 L28 L29 L30 L31 L32 L33 L34 L35 L36 L37 L38 L39 L40 L41 L42 L43 L44 L45 L46 L47 L48 L49 L50 L51 L52 L53 L54 L55 L56 L57 L58 L59 L60 L61 L62 L63 L64 L65 L66 L67 L68 L69 L70 L71 L72 L73 L74 L75 L76 L77 L78 L79 L80 L81 L82 L83 L84 L85 L86 L87 L88 L89 L90 L91 L92 L93 L94 L95 L96 L98 L99 LA LAC LBR LBT LC LD LE LEF LF LH LI LJ LK LM LN LO LP LPA LR LS LTN LTR LUB LUM LUX LX LY M0 M1 M10 M11 M12 M13 M14 M15 M16 M17 M18 M19 M20 M21 M22 M23 M24 M25 M26 M27 M29 M30 M31 M32 M33 M34 M35 M36 M37 M4 M5 M7 M9 MA MAH MAL MAM MAR MAW MBE MBF MBR MC MCU MD MF MGM MHZ MIK MIL MIN MIO MIU MK MLD MLT MMK MMQ MMT MND MON MPA MQ MQH MQS MSK MT MTK MTQ MTR MTS MV MVA MWH N1 N2 N3 NA NAR NB NBB NC NCL ND NE NEW NF NG NH NI NIL NIU NJ NL NMI NMP NN NPL NPR NPT NQ NR NRL NT NTT NU NV NX NY OA ODE OHM ON ONZ OP OT OZ OZA OZI P0 P1 P2 P3 P4 P5 P6 P7 P8 P9 PA PAL PB PD PE PF PFL PG PGL PI PK PL PLA PM PN PO PQ PR PS PT PTD PTI PTL PU PV PW PY PZ Q3 QA QAN QB QD QH QK QR QT QTD QTI QTL QTR R1 R4 R9 RA RD RG RH RK RL RM RN RO RP RPM RPS RS RT RU S3 S4 S5 S6 S7 S8 SA SAN SCO SCR SD SE SEC SET SG SHT SIE SK SL SMI SN SO SP SQ SQR SR SS SST ST STI STK STL STN SV SW SX T0 T1 T3 T4 T5 T6 T7 T8 TA TAH TC TD TE TF TI TIC TIP TJ TK TL TMS TN TNE TP TPR TQ TQD TR TRL TS TSD TSH TT TU TV TW TY U1 U2 UA UB UC UD UE UF UH UM VA VI VLT VP VQ VS W2 W4 WA WB WCD WE WEB WEE WG WH WHR WI WM WR WSD WTT WW X1 YDK YDQ YL YRD YT Z1 Z2 Z3 Z4 Z5 Z6 Z8 ZP ZZ ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' 5 6 8 10 11 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 40 41 43 44 45 46 47 48 53 54 56 57 58 59 60 61 62 63 64 66 69 71 72 73 74 76 77 78 80 81 84 85 87 89 90 91 92 93 94 95 96 97 98 1A 1B 1C 1D 1E 1F 1G 1H 1I 1J 1K 1L 1M 1X 2A 2B 2C 2G 2H 2I 2J 2K 2L 2M 2N 2P 2Q 2R 2U 2V 2W 2X 2Y 2Z 3B 3C 3E 3G 3H 3I 4A 4B 4C 4E 4G 4H 4K 4L 4M 4N 4O 4P 4Q 4R 4T 4U 4W 4X 5A 5B 5C 5E 5F 5G 5H 5I 5J 5K 5P 5Q A1 A10 A11 A12 A13 A14 A15 A16 A17 A18 A19 A2 A20 A21 A22 A23 A24 A25 A26 A27 A28 A29 A3 A30 A31 A32 A33 A34 A35 A36 A37 A38 A39 A4 A40 A41 A42 A43 A44 A45 A47 A48 A49 A5 A50 A51 A52 A53 A54 A55 A56 A57 A58 A59 A6 A60 A61 A62 A63 A64 A65 A66 A67 A68 A69 A7 A70 A71 A73 A74 A75 A76 A77 A78 A79 A8 A80 A81 A82 A83 A84 A85 A86 A87 A88 A89 A9 A90 A91 A93 A94 A95 A96 A97 A98 A99 AA AB ACR ACT AD AE AH AI AJ AK AL AM AMH AMP ANN AP APZ AQ AR ARE AS ASM ASU ATM ATT AV AW AY AZ B0 B1 B10 B11 B12 B13 B14 B15 B16 B17 B18 B19 B2 B20 B21 B22 B23 B24 B25 B26 B27 B28 B29 B3 B30 B31 B32 B33 B34 B35 B36 B37 B38 B39 B4 B40 B41 B42 B43 B44 B45 B46 B47 B48 B49 B5 B50 B51 B52 B53 B54 B55 B56 B57 B58 B59 B6 B60 B61 B62 B63 B64 B65 B66 B67 B68 B69 B7 B70 B71 B72 B73 B74 B75 B76 B77 B78 B79 B8 B80 B81 B82 B83 B84 B85 B86 B87 B88 B89 B9 B90 B91 B92 B93 B94 B95 B96 B97 B98 B99 BAR BB BD BE BFT BG BH BHP BIL BJ BK BL BLD BLL BO BP BQL BR BT BTU BUA BUI BW BX BZ C0 C1 C10 C11 C12 C13 C14 C15 C16 C17 C18 C19 C2 C20 C21 C22 C23 C24 C25 C26 C27 C28 C29 C3 C30 C31 C32 C33 C34 C35 C36 C37 C38 C39 C4 C40 C41 C42 C43 C44 C45 C46 C47 C48 C49 C5 C50 C51 C52 C53 C54 C55 C56 C57 C58 C59 C6 C60 C61 C62 C63 C64 C65 C66 C67 C68 C69 C7 C70 C71 C72 C73 C74 C75 C76 C77 C78 C79 C8 C80 C81 C82 C83 C84 C85 C86 C87 C88 C89 C9 C90 C91 C92 C93 C94 C95 C96 C97 C98 C99 CA CCT CDL CEL CEN CG CGM CH CJ CK CKG CL CLF CLT CMK CMQ CMT CNP CNT CO COU CQ CR CS CT CTG CTM CTN CU CUR CV CWA CWI CY CZ D03 D04 D1 D10 D11 D12 D13 D14 D15 D16 D17 D18 D19 D2 D20 D21 D22 D23 D24 D25 D26 D27 D28 D29 D30 D31 D32 D33 D34 D35 D36 D37 D38 D39 D40 D41 D42 D43 D44 D45 D46 D47 D48 D49 D5 D50 D51 D52 D53 D54 D55 D56 D57 D58 D59 D6 D60 D61 D62 D63 D64 D65 D66 D67 D68 D69 D7 D70 D71 D72 D73 D74 D75 D76 D77 D78 D79 D8 D80 D81 D82 D83 D85 D86 D87 D88 D89 D9 D90 D91 D92 D93 D94 D95 D96 D97 D98 D99 DAA DAD DAY DB DC DD DE DEC DG DI DJ DLT DMA DMK DMO DMQ DMT DN DPC DPR DPT DQ DR DRA DRI DRL DRM DS DT DTN DU DWT DX DY DZN DZP E01 E07 E08 E09 E10 E11 E12 E14 E15 E16 E17 E18 E19 E2 E20 E21 E22 E23 E25 E27 E28 E3 E30 E31 E32 E33 E34 E35 E36 E37 E38 E39 E4 E40 E41 E42 E43 E44 E45 E46 E47 E48 E49 E5 E50 E51 E52 E53 E54 E55 E56 E57 E58 E59 E60 E61 E62 E63 E64 E65 E66 E67 E68 E69 E70 E71 E72 E73 E74 E75 E76 E77 E78 E79 E80 E81 E82 E83 E84 E85 E86 E87 E88 E89 E90 E91 E92 E93 E94 E95 E96 E97 E98 E99 EA EB EC EP EQ EV F01 F02 F03 F04 F05 F06 F07 F08 F1 F10 F11 F12 F13 F14 F15 F16 F17 F18 F19 F20 F21 F22 F23 F24 F25 F26 F27 F28 F29 F30 F31 F32 F33 F34 F35 F36 F37 F38 F39 F40 F41 F42 F43 F44 F45 F46 F47 F48 F49 F50 F51 F52 F53 F54 F55 F56 F57 F58 F59 F60 F61 F62 F63 F64 F65 F66 F67 F68 F69 F70 F71 F72 F73 F74 F75 F76 F77 F78 F79 F80 F81 F82 F83 F84 F85 F86 F87 F88 F89 F9 F90 F91 F92 F93 F94 F95 F96 F97 F98 F99 FAH FAR FB FBM FC FD FE FF FG FH FIT FL FM FOT FP FR FS FTK FTQ G01 G04 G05 G06 G08 G09 G10 G11 G12 G13 G14 G15 G16 G17 G18 G19 G2 G20 G21 G23 G24 G25 G26 G27 G28 G29 G3 G30 G31 G32 G33 G34 G35 G36 G37 G38 G39 G40 G41 G42 G43 G44 G45 G46 G47 G48 G49 G50 G51 G52 G53 G54 G55 G56 G57 G58 G59 G60 G61 G62 G63 G64 G65 G66 G67 G68 G69 G7 G70 G71 G72 G73 G74 G75 G76 G77 G78 G79 G80 G81 G82 G83 G84 G85 G86 G87 G88 G89 G90 G91 G92 G93 G94 G95 G96 G97 G98 G99 GB GBQ GC GD GDW GE GF GFI GGR GH GIA GIC GII GIP GJ GK GL GLD GLI GLL GM GN GO GP GQ GRM GRN GRO GRT GT GV GW GWH GY GZ H03 H04 H05 H06 H07 H08 H09 H1 H10 H11 H12 H13 H14 H15 H16 H18 H19 H2 H20 H21 H22 H23 H24 H25 H26 H27 H28 H29 H30 H31 H32 H33 H34 H35 H36 H37 H38 H39 H40 H41 H42 H43 H44 H45 H46 H47 H48 H49 H50 H51 H52 H53 H54 H55 H56 H57 H58 H59 H60 H61 H62 H63 H64 H65 H66 H67 H68 H69 H70 H71 H72 H73 H74 H75 H76 H77 H78 H79 H80 H81 H82 H83 H84 H85 H87 H88 H89 H90 H91 H92 H93 H94 H95 H96 H98 H99 HA HAR HBA HBX HC HD HDW HE HF HGM HH HI HIU HJ HK HKM HL HLT HM HMQ HMT HN HO HP HPA HS HT HTZ HUR HY IA IC IE IF II IL IM INH INK INQ IP ISD IT IU IV J10 J12 J13 J14 J15 J16 J17 J18 J19 J2 J20 J21 J22 J23 J24 J25 J26 J27 J28 J29 J30 J31 J32 J33 J34 J35 J36 J38 J39 J40 J41 J42 J43 J44 J45 J46 J47 J48 J49 J50 J51 J52 J53 J54 J55 J56 J57 J58 J59 J60 J61 J62 J63 J64 J65 J66 J67 J68 J69 J70 J71 J72 J73 J74 J75 J76 J78 J79 J81 J82 J83 J84 J85 J87 J89 J90 J91 J92 J93 J94 J95 J96 J97 J98 J99 JB JE JG JK JM JNT JO JOU JPS JR JWL K1 K10 K11 K12 K13 K14 K15 K16 K17 K18 K19 K2 K20 K21 K22 K23 K24 K25 K26 K27 K28 K3 K30 K31 K32 K33 K34 K35 K36 K37 K38 K39 K40 K41 K42 K43 K45 K46 K47 K48 K49 K5 K50 K51 K52 K53 K54 K55 K58 K59 K6 K60 K61 K62 K63 K64 K65 K66 K67 K68 K69 K70 K71 K73 K74 K75 K76 K77 K78 K79 K80 K81 K82 K83 K84 K85 K86 K87 K88 K89 K90 K91 K92 K93 K94 K95 K96 K97 K98 K99 KA KAT KB KBA KCC KD KDW KEL KF KG KGM KGS KHY KHZ KI KIC KIP KJ KJO KL KLK KMA KMH KMK KMQ KMT KNI KNS KNT KO KPA KPH KPO KPP KR KS KSD KSH KT KTM KTN KUR KVA KVR KVT KW KWH KWO KWT KX L10 L11 L12 L13 L14 L15 L16 L17 L18 L19 L2 L20 L21 L23 L24 L25 L26 L27 L28 L29 L30 L31 L32 L33 L34 L35 L36 L37 L38 L39 L40 L41 L42 L43 L44 L45 L46 L47 L48 L49 L50 L51 L52 L53 L54 L55 L56 L57 L58 L59 L60 L61 L62 L63 L64 L65 L66 L67 L68 L69 L70 L71 L72 L73 L74 L75 L76 L77 L78 L79 L80 L81 L82 L83 L84 L85 L86 L87 L88 L89 L90 L91 L92 L93 L94 L95 L96 L98 L99 LA LAC LBR LBT LC LD LE LEF LF LH LI LJ LK LM LN LO LP LPA LR LS LTN LTR LUB LUM LUX LX LY M0 M1 M10 M11 M12 M13 M14 M15 M16 M17 M18 M19 M20 M21 M22 M23 M24 M25 M26 M27 M29 M30 M31 M32 M33 M34 M35 M36 M37 M4 M5 M7 M9 MA MAH MAL MAM MAR MAW MBE MBF MBR MC MCU MD MF MGM MHZ MIK MIL MIN MIO MIU MK MLD MLT MMK MMQ MMT MND MON MPA MQ MQH MQS MSK MT MTK MTQ MTR MTS MV MVA MWH N1 N2 N3 NA NAR NB NBB NC NCL ND NE NEW NF NG NH NI NIL NIU NJ NL NMI NMP NN NPL NPR NPT NQ NR NRL NT NTT NU NV NX NY OA ODE OHM ON ONZ OP OT OZ OZA OZI P0 P1 P2 P3 P4 P5 P6 P7 P8 P9 PA PAL PB PD PE PF PFL PG PGL PI PK PL PLA PM PN PO PQ PR PS PT PTD PTI PTL PU PV PW PY PZ Q3 QA QAN QB QD QH QK QR QT QTD QTI QTL QTR R1 R4 R9 RA RD RG RH RK RL RM RN RO RP RPM RPS RS RT RU S3 S4 S5 S6 S7 S8 SA SAN SCO SCR SD SE SEC SET SG SHT SIE SK SL SMI SN SO SP SQ SQR SR SS SST ST STI STK STL STN SV SW SX T0 T1 T3 T4 T5 T6 T7 T8 TA TAH TC TD TE TF TI TIC TIP TJ TK TL TMS TN TNE TP TPR TQ TQD TR TRL TS TSD TSH TT TU TV TW TY U1 U2 UA UB UC UD UE UF UH UM VA VI VLT VP VQ VS W2 W4 WA WB WCD WE WEB WEE WG WH WHR WI WM WR WSD WTT WW X1 YDK YDQ YL YRD YT Z1 Z2 Z3 Z4 Z5 Z6 Z8 ZP ZZ ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R006</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R006]-Unit code MUST be coded according to the UN/ECE Recommendation 20
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:CompanyID//@schemeID" priority="1003" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cbc:CompanyID//@schemeID" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' FR:SIRENE SE:ORGNR FR:SIRET FI:OVT DUNS GLN DK:P IT:FTI NL:KVK IT:SIA IT:SECETI DIGST DK:CPR DK:CVR DK:SE DK:VANS IT:VAT IT:CF NO:ORGNR NO:VAT HU:VAT EU:REID AT:VAT AT:GOV IS:KT IBAN AT:KUR ES:VAT IT:IPA AD:VAT AL:VAT BA:VAT BE:VAT BG:VAT CH:VAT CY:VAT CZ:VAT DE:VAT EE:VAT GB:VAT GR:VAT HR:VAT IE:VAT LI:VAT LT:VAT LU:VAT LV:VAT MC:VAT ME:VAT MK:VAT MT:VAT NL:VAT PL:VAT PT:VAT RO:VAT RS:VAT SI:VAT SK:VAT SM:VAT TR:VAT VA:VAT NL:ION NL:OIN SE:VAT BE:CBE FR:VAT ZZZ ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R008</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R008]-A Party Company Identifier Scheme MUST be from the list of PEPPOL Party
                        Identifiers described in the "PEPPOL Policy for using Identifiers".
                    </svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:TaxCurrencyCode" priority="1002" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="cbc:TaxCurrencyCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R009</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R009]-TaxCurrencyCode MUST be coded using ISO code list 4217</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:SourceCurrencyCode" priority="1001" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cbc:SourceCurrencyCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R010</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R010]-SourceCurrencyCode MUST be coded using ISO code list 4217</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>

    <!--RULE -->
    <xsl:template match="cbc:TargetCurrencyCode" priority="1000" mode="M8">
        <svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                         context="cbc:TargetCurrencyCode" />

        <!--ASSERT -->
        <xsl:choose>
            <xsl:when
                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )" />
            <xsl:otherwise>
                <svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                    test="( ( not(contains(normalize-space(.),' ')) and contains( ' AED AFN ALL AMD ANG AOA ARS AUD AWG AZN BAM BBD BDT BGN BHD BIF BMD BND BOB BOV BRL BSD BTN BWP BYR BZD CAD CDF CHE CHF CHW CLF CLP CNY COP COU CRC CUP CVE CZK DJF DKK DOP DZD EEK EGP ERN ETB EUR FJD FKP GBP GEL GHS GIP GMD GNF GTQ GWP GYD HKD HNL HRK HTG HUF IDR ILS INR IQD IRR ISK JMD JOD JPY KES KGS KHR KMF KPW KRW KWD KYD KZT LAK LBP LKR LRD LSL LTL LVL LYD MAD MDL MGA MKD MMK MNT MOP MRO MUR MVR MWK MXN MXV MYR MZN NAD NGN NIO NOK NPR NZD OMR PAB PEN PGK PHP PKR PLN PYG QAR RON RSD RUB RWF SAR SBD SCR SDG SEK SGD SHP SKK SLL SOS SRD STD SVC SYP SZL THB TJS TMM TND TOP TRY TTD TWD TZS UAH UGX USD USN USS UYI UYU UZS VEF VND VUV WST XAF XAG XAU XBA XBB XBC XBD XCD XDR XFU XOF XPD XPF XTS XXX YER ZAR ZMK ZWR ZWD ',concat(' ',normalize-space(.),' ') ) ) )">
                    <xsl:attribute name="id">OP-T10-R011</xsl:attribute>
                    <xsl:attribute name="flag">fatal</xsl:attribute>
                    <xsl:attribute name="location">
                        <xsl:apply-templates select="." mode="schematron-select-full-path" />
                    </xsl:attribute>
                    <svrl:text>[OP-T10-R011]-TargetCurrencyCode MUST be coded using ISO code list 4217</svrl:text>
                </svrl:failed-assert>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>
    <xsl:template match="text()" priority="-1" mode="M8" />
    <xsl:template match="@*|node()" priority="-2" mode="M8">
        <xsl:apply-templates select="@*|*" mode="M8" />
    </xsl:template>
</xsl:stylesheet>
