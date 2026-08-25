<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:rep="http://www.xoev.de/de/validator/varl/1"
    xmlns:s="http://www.xoev.de/de/validator/framework/1/scenarios" 
    xmlns:in="http://www.xoev.de/de/validator/framework/1/createreportinput"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:ubl="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
    xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" 
    xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
    xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
    xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
    xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
    exclude-result-prefixes="xs"
    version="2.0">

    <xsl:import href="./default-report.xsl"/>

    <xsl:output method="xml" indent="yes"/>

    <!-- Überschrieben (default-report.xsl) -->
    <xsl:template name="documentData">
        <rep:documentData>
            <xsl:for-each select="
                $input-document/*/cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName, 
                $input-document/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty/ram:Name">
                <seller>
                    <xsl:value-of select="."/>
                </seller>
            </xsl:for-each>
            
            <xsl:for-each select="
                $input-document/*/cbc:ID,
                $input-document/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:ID">
                <id>
                    <xsl:value-of select="."/>
                </id>
            </xsl:for-each>
            
            <xsl:for-each select="$input-document/*/cbc:IssueDate,
                $input-document/rsm:CrossIndustryInvoice/rsm:ExchangedDocument/ram:IssueDateTime/udt:DateTimeString">
                <issueDate>
                    <xsl:value-of select="."/>
                </issueDate>
            </xsl:for-each>
        </rep:documentData>
    </xsl:template>

    <!-- Overridden from default-report.xsl -->
    <xsl:template name="html:documentData" xmlns="http://www.w3.org/1999/xhtml" >
        <dl>
            <xsl:for-each select="/rep:report/rep:scenarioMatched/rep:documentData/seller">
                <dt>Erkannter Rechnungssteller:</dt>
                <dd>
                    <xsl:value-of select="."/>
                </dd>
            </xsl:for-each>
            
            <xsl:for-each select="/rep:report/rep:scenarioMatched/rep:documentData/id">
                <dt>Erkannte Rechnungsnummer:</dt>
                <dd>
                    <xsl:value-of select="."/>
                </dd>
            </xsl:for-each>
            
            <xsl:for-each select="/rep:report/rep:scenarioMatched/rep:documentData/issueDate">
                <dt>Erkanntes Rechnungsdatum:</dt>
                <dd>
                    <xsl:value-of select="."/>
                </dd>
            </xsl:for-each>
        </dl>
    </xsl:template>
    
    
    
    


        
</xsl:stylesheet>