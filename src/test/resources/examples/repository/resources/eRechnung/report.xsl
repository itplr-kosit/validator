<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:rep="http://www.xoev.de/de/validator/varl/1 "
    xmlns:s="http://www.xoev.de/de/validator/framework/1/scenarios" 
    xmlns:in="http://www.xoev.de/de/validator/framework/1/createreportinput"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="input-document" as="document-node(element())" required="yes"/>

    <xsl:variable name="custom-levels" as="element(s:customLevel)*" select="//s:customLevel"/>
    

    <xsl:template match="in:createReportInput">

        <xsl:variable name="validationStepResults" as="element(rep:validationStepResult)*">
            <xsl:apply-templates select="in:validationResultsWellformedness"/>
            <xsl:apply-templates select="in:validationResultsXmlSchema"/>
            <xsl:apply-templates select="in:validationResultsSchematron"/>
        </xsl:variable>

        <xsl:variable name="report" as="document-node(element(rep:report))">
            <xsl:document>
                <rep:report valid="{if ($validationStepResults[@valid = 'false']) then false() else true()}">
                    <xsl:apply-templates select="in:engine" mode="copy-to-report-ns"/>    
                    <xsl:apply-templates select="in:timestamp" mode="copy-to-report-ns"/>
                    <xsl:apply-templates select="in:documentIdentification" mode="copy-to-report-ns"/>
                    <xsl:apply-templates select="s:scenario" mode="copy-to-report-ns"/>
                    <xsl:call-template name="document-data"/>
                    
                    <rep:validationResult>
                        <xsl:sequence select="$validationStepResults"/>
                    </rep:validationResult>
                </rep:report>
            </xsl:document>
        </xsl:variable>
        
        <xsl:variable name="report-with-ids" as="document-node(element(rep:report))">
            <xsl:document>
                <xsl:apply-templates select="$report" mode="add-ids"/>
            </xsl:document>
        </xsl:variable>
        
        <rep:report varlVersion="1.0.0">
            <xsl:copy-of select="$report-with-ids/rep:report/(node()|@*)"/>
            <xsl:apply-templates select="$report-with-ids"/>
        </rep:report>
        
    </xsl:template>
    
    <!-- Overwrite in customisation layer to generate a documentData element -->
    <xsl:template name="document-data"/>
    
    
    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Syntax checks (well-formedness and XML Schema                                      * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->

    <xsl:template match="in:validationResultsWellformedness|in:validationResultsXmlSchema">
        <xsl:variable name="messages" as="element(rep:message)*">
            <xsl:apply-templates select="in:xmlSyntaxError"/>
        </xsl:variable>
        <!-- Skip output for implicit validation steps (i. e., wellformedness implemenation) unless there is anything to tell -->
        <xsl:if test="exists($messages) or exists(s:resource)">
            <rep:validationStepResult valid="{if ($messages[@level = ('warning', 'error')]) then false() else true()}">
                <xsl:apply-templates mode="copy-to-report-ns" select="s:resource"/>
                <xsl:sequence select="$messages"/>
            </rep:validationStepResult>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="in:xmlSyntaxError">
        <rep:message level="{if (@severity = 'SEVERITY_WARNING') then 'warning' else 'error'}">
            <xsl:apply-templates select="in:location/*"/>
            <xsl:value-of select="in:message"/>
        </rep:message> 
    </xsl:template>
    
    <xsl:template match="in:xmlSyntaxError/in:rowNumber">
        <xsl:attribute name="lineNumber" select="."/>
    </xsl:template>
    
    <xsl:template match="in:xmlSyntaxError/in:columnNumber">
        <xsl:attribute name="columnNumber" select="."/>
    </xsl:template>

    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Schematron (SVRL)                                                                  * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->
    

    <xsl:template match="in:validationResultsSchematron">
        <xsl:variable name="schematron-output" as="element(svrl:schematron-output)?" select="in:results/svrl:schematron-output"/>
        <xsl:if test="empty($schematron-output)">
            <xsl:message terminate="yes">Unexpected result from schematron validation - there is no svrl:schematron-output element!</xsl:message>
        </xsl:if>
        <xsl:variable name="messages" as="element(rep:message)*">
            <xsl:apply-templates select="$schematron-output/(svrl:failed-assert|svrl:successful-report)"/>
        </xsl:variable>
        <rep:validationStepResult valid="{if ($messages[@level = ('warning', 'error')]) then false() else true()}">
            <xsl:apply-templates mode="copy-to-report-ns" select="s:resource"/>
            <xsl:sequence select="$messages"/>
        </rep:validationStepResult>
    </xsl:template>
    
    
    <xsl:template match="svrl:failed-assert|svrl:successful-report">
        <rep:message>
            <xsl:apply-templates select="in:location/*"/>
            <xsl:attribute name="level">
                <xsl:choose>
                    <xsl:when test="(@flag,@role) = ('fatal', 'error')">error</xsl:when>
                    <xsl:when test="(@flag,@role) = ('warning', 'warn')">warning</xsl:when>
                    <xsl:when test="(@flag,@role) = ('information', 'info')">information</xsl:when>
                    <xsl:when test="@role = ('information', 'info')">warning</xsl:when>
                    <xsl:otherwise>error</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="@location"/>
            <xsl:apply-templates select="@id"/>
            <xsl:value-of select="svrl:text"/>
        </rep:message> 
    </xsl:template>
    
    <xsl:template match="svrl:*/@location">
        <xsl:attribute name="xpathLocation" select="."/>
    </xsl:template>
    
    <xsl:template match="svrl:failed-assert/@id">
        <xsl:attribute name="code" select="."/>
    </xsl:template>

    <xsl:template match="svrl:successful-report/@id">
        <xsl:attribute name="code" select="."/>
    </xsl:template>
    
    <!-- ************************************************************************************** -->
    <!-- * Validation helpers                                                                 * -->
    <!-- ************************************************************************************** -->
    
    <!-- Identity template -->
    <xsl:template mode="copy-to-report-ns" match="element()|@*">
        <xsl:copy>
            <xsl:apply-templates mode="copy-to-report-ns" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="copy-to-report-ns" match="element()" priority="5">
        <xsl:element name="rep:{local-name()}">
            <xsl:apply-templates mode="copy-to-report-ns" select="node()|@*"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template mode="copy-to-report-ns" match="s:*" priority="10">
        <xsl:copy>
            <xsl:apply-templates mode="copy-to-report-ns" select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="node()|@*" mode="add-ids">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*" mode="add-ids"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="rep:validationStepResult" mode="add-ids">
        <xsl:copy>
            <xsl:attribute name="id">
                <xsl:text>step_</xsl:text>
                <xsl:number level="multiple" count="rep:validationStepResult"/>
            </xsl:attribute>
            <xsl:apply-templates select="node()|@*" mode="add-ids"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="rep:message" mode="add-ids">
        <xsl:copy>
            <xsl:attribute name="id">
                <xsl:text>message_</xsl:text>
                <xsl:number level="multiple" count="rep:message | rep:validationStepResult"/>
            </xsl:attribute>
            <xsl:apply-templates select="node()|@*" mode="add-ids"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Assessment (ohne HTML)                                                             * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->
    
    <xd:doc>
        <xd:desc>
            <xd:p>Dies ist das zentrale Template des Skripts. Angewandt auf ein
                validationReport-Dokument, und unter Nutzung des <xd:ref name="assessment"
                    type="parameter"/> Parameters wird eine Handlungsempfehlung in Form eines
                <xd:i>accept</xd:i> oder <xd:i>reject</xd:i> Elements erstellt, welches eine
                Begründung der jeweiligen Empfehlung enthalten kann.</xd:p>
            <xd:p>Das Template realisiert eine Funktion f:validationReport, assessment →
                suggestion</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="rep:report">
        <xsl:variable name="element-name" select="if (//rep:message[rep:custom-level(.) = 'error']) then 'reject' else 'accept'"/>
        <rep:assessment>
            <xsl:element name="rep:{$element-name}" exclude-result-prefixes="#all">
                <rep:explanation>
                    <html xmlns="http://www.w3.org/1999/xhtml" data-report-type="devreport-{$element-name}">
                        <xsl:call-template name="html:head"/>
                        <body>
                            <xsl:call-template name="html:document"/>
                        </body>
                    </html>
                </rep:explanation>
            </xsl:element>
        </rep:assessment>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Ermittelt für eine während der Validierung ausgegebene Fehlernachricht deren
                Fehlerlevel <xd:i>(error, warning, information)</xd:i> gemäß der
                benutzerspezifischen Qualifizierung.</xd:p>
            <xd:p>Jede Fehlernachricht hat im Rahmen der Validierung ein solches Fehlerlevel
                erhalten (siehe Attribut <xd:i>@level</xd:i>). Im Regelfall entspricht die
                benutzerspezifische Qualifizierung unverändert diesem Level. Nutzer können jedoch im
                Rahmen der Bewertung eigene Qualifizierungen vereinbaren und in dem als Parameter
                <xd:ref name="assessment" type="parameter"/> übergebenen <xd:i>assessment</xd:i>
                Element für bestimmte, anhand des Fehlercodes identifizierten Fehlermeldungen eine
                eigene Qualifizierung als <xd:i>customLevel</xd:i> festlegen.</xd:p>
            <xd:p>Dies kann z. B. genutzt werden, um einen <xd:i>error</xd:i>, der ansonsten zur
                Rückweisung der Nachricht führen würde, zumindest zeitweilig als
                <xd:i>warning</xd:i> zu qualifizieren, so dass eine entsprechende
                Dokumenteninstanz trotz einer Warnung angenommen und verarbeitet würde.</xd:p>
            <xd:p>Die Funktion prüft für eine Fehlernachricht, ob deren <xd:i>@code</xd:i> Attribut
                Bestandteil der für ein bestimmtes <xd:i>customLevel</xd:i> des <xd:ref
                    name="assessment" type="parameter"/> Parameters angegebenen Fehlercodes ist.
                Falls ja, dann gilt das jeweilige <xd:i>customLevel</xd:i>. Andernfalls wird der im
                Rahmen der Validierung ermittelte Fehlerlevel unverändert übernommen.</xd:p>
        </xd:desc>
        <xd:param name="message">Eine im Rahmen der Validierung ausgegebene
            Fehlernachricht</xd:param>
        <xd:return/>
    </xd:doc>
    <xsl:function name="rep:custom-level" >
        <xsl:param name="message" as="element(rep:message)"/>
        <xsl:variable name="cl" as="element(s:customLevel)?"
            select="$custom-levels[tokenize(., '\s+') = $message/@code]"/>
        <xsl:value-of select="if ($cl) then $cl/@level else $message/@level"/>
    </xsl:function>
    
    
    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * HTML                                                                         * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->
    
    <xsl:template name="html:document">
        <xsl:call-template name="html:report-header"/>
        <xsl:call-template name="html:prolog"/>
        <!-- TODO: documentData -->
        <xsl:call-template name="html:conformance"/>
        <xsl:if test="//rep:message">
            <xsl:call-template name="html:messagetable"/>
        </xsl:if>
        <xsl:call-template name="html:assessment"/>
        <xsl:call-template name="html:epilog"/>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert das <xd:i>head</xd:i> Element eines eingebetteten HTML Dokuments,
                welches den Prüf- und Bewertungsbericht visualisiert und die Handlungsempfehlung
                begründet</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:head" as="element(html:head)">
        <head xmlns="http://www.w3.org/1999/xhtml">
            <title>Pruefbericht der KoSIT</title>
            <style>
                body{
                    font-family: Calibri;
                    width: 230mm;
                }
                
                table{
                    border-collapse: collapse;
                    width: 100%;
                }
                
                table.tbl-errors{
                    font-size: 10pt;
                }
                
                .tbl-errors td{
                    border: 1px solid lightgray;
                    padding: 2px;
                    vertical-align: top;
                }
                
                thead{
                    font-weight: bold;
                    background-color: #f0f0f0;
                    padding-top: 6pt;
                    padding-bottom: 2pt;
                }
                
                .tbl-meta td{
                    padding-right: 1em;
                }
                
                
                
                tr{
                    vertical-align: bottom;
                    border-bottom: 1px solid #c0c0c0;
                }
                
                tr.error{
                    font-weight: bold;
                    color: red;
                }
                
                tr.warning{
                    font-weight: bold;
                }
                
                
                
                tr.pos{
                    font-weight: bold;
                }
                
                p.important{
                    font-weight: bold;
                    text-align: left;
                    background-color: #e0e0e0;
                    padding: 3pt;
                }
                
                td.right{
                    text-align: right
                }</style>
        </head>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert die Überschrift des eines eingebetteten HTML Dokuments, welches den
                Prüf- und Bewertungsbericht visualisiert und die Handlungsempfehlung
                begründet</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:report-header" as="element()+">
        <h1 xmlns="http://www.w3.org/1999/xhtml">Prüfbericht der KoSIT</h1>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert am Beginn eines eingebetteten HTML Dokuments, welches den Prüf- und
                Bewertungsbericht visualisiert und die Handlungsempfehlung begründet, eine Übersicht
                mit Metadaten des geprüften Dokuments.</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:prolog" as="element()+">
        <h2 xmlns="http://www.w3.org/1999/xhtml">
            <xsl:text>Angaben zum geprüften Dokument</xsl:text>
        </h2>
        <table class="tbl-meta" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:if test="/*/@id">
                <tr>
                    <td colspan="2">Prüfbericht Nr.</td>
                    <td colspan="3">
                        <xsl:value-of select="/*/@id"/>
                    </td>
                </tr>
            </xsl:if>
            <tr>
                <td colspan="2">Dokument:</td>
                <td colspan="3">
                    <xsl:value-of select="rep:documentIdentification/rep:documentReference"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">Szenario:</td>
                <td colspan="3">
                    <xsl:value-of select="s:scenario/@name"/>
                </td>
            </tr>
            <tr>
                <td colspan="2">Zeitpunkt:</td>
                <td colspan="3">
                    <xsl:value-of select="format-dateTime(rep:timestamp, '[D].[M].[Y] [H]:[m]:[s]')"
                    />
                </td>
            </tr>
            <tr>
                <td colspan="2">Validierungsschritte:</td>
                <td>Fehler</td>
                <td>Warnung</td>
                <td>Information</td>
            </tr>
            <xsl:for-each select="/rep:validationResults/rep:validationStepResult">
                <xsl:variable name="step-id" select="@id"/>
                <tr>
                    <td>
                        <xsl:value-of select="@id"/>
                    </td>
                    <td>
                        <xsl:value-of select="s:resource/s:resouceName"/>
                    </td>
                    <td>
                        <xsl:value-of
                            select="count(//rep:message[@step eq $step-id and @level eq 'error'])"/>
                    </td>
                    <td>
                        <xsl:value-of
                            select="count(//rep:message[@step eq $step-id and @level eq 'warning'])"/>
                    </td>
                    <td>
                        <xsl:value-of
                            select="count(//rep:message[@step eq $step-id and @level eq 'information'])"
                        />
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert am Ende eines eingebetteten HTML Dokuments, welches den Prüf- und
                Bewertungsbericht visualisiert und die Handlungsempfehlung begründet, eine Übersicht
                mit Metadaten zum Prüfmodul.</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:epilog" as="element()+">
        <p class="info" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:text>Erstellt mit: </xsl:text>
            <xsl:value-of select="rep:engine/rep:name"/>
            <xsl:text> für das InstructionSet </xsl:text>
            <em>
                <xsl:value-of select="rep:engine/rep:info[@key eq 'title']"/>
            </em>
            <xsl:text> vom </xsl:text>
            <xsl:value-of
                select="format-dateTime(xs:dateTime(rep:engine/rep:info[@key eq 'version']), '[D].[M].[Y]')"/>
            <xsl:text>.</xsl:text>
        </p>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetetteten HTML Dokument eine Tabelle mit den während der
                Validierung ausgegebenen Daten.</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:messagetable" as="element()">
        <table xmlns="http://www.w3.org/1999/xhtml" class="tbl-errors">
            <xsl:call-template name="html:messagetable.head"/>
            <tbody>
                <xsl:for-each select="//rep:message">
                    <xsl:call-template name="html:messagetable.row"/>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in der HTML-Tabelle der Validierungsnachtichten in dem eingebetteten
                HTML Dokument dn Tabellenkopf</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:messagetable.head" xmlns="http://www.w3.org/1999/xhtml">
        <thead>
            <tr>
                <th>Pos</th>
                <th>Code</th>
                <th>CustomLevel (Level)</th>
                <th>Step</th>
                <th>Text</th>
            </tr>
        </thead>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in der HTML-Tabelle der Validierungsnachtichten in dem eingebetteten
                HTML Dokument eine oder mehrere Zeilen pro Validierungsnachricht</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:messagetable.row" xmlns="http://www.w3.org/1999/xhtml">
        <tr>
            <xsl:attribute name="class">
                <xsl:value-of select="rep:custom-level(.)"/>
            </xsl:attribute>
            <td>
                <xsl:value-of select="position()"/>
            </td>
            <td>
                <xsl:value-of select="@code"/>
            </td>
            <td>
                <xsl:value-of select="rep:custom-level(.)"/>
                <xsl:if test="not(rep:custom-level(.) eq @level)">
                    <xsl:value-of select="concat(' (', @level, ')')"/>
                </xsl:if>
            </td>
            <td>
                <xsl:value-of select="@step"/>
            </td>
            <td>
                <xsl:value-of select="normalize-space(.)"/>
            </td>
        </tr>
        <tr>
            <td colspan="4"/>
            <td>
                <xsl:value-of select="@location"/>
            </td>
        </tr>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetteten HTML Dokument eine Aussage zur Konformität des
                geprüften Dokuments zu den formalen Vorgaben.</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:conformance">
        <xsl:variable name="e" as="xs:integer" select="count(//rep:message[@level eq 'error'])"/>
        <xsl:variable name="w" as="xs:integer" select="count(//rep:message[@level eq 'warning'])"/>
        <p class="important" xmlns="http://www.w3.org/1999/xhtml">
            <b>Konformitätsprüfung: </b>
            <xsl:text>Das geprüfte Dokument enthält </xsl:text>
            <xsl:choose>
                <xsl:when test="$e + $w eq 0">
                    <xsl:text>weder Fehler noch Warnungen. Es ist konform zu den formalen Vorgaben.</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($e, ' Fehler / ', $w, ' Warnungen. Es ist ')"/>
                    <b>nicht konform</b>
                    <xsl:text> zu den formalen Vorgaben.</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </p>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetteten HTML Dokument die Aussage zur
                Handlungsempfehlung.</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:assessment">
        <xsl:variable name="e1" as="xs:integer" select="count(//message[@level eq 'error'])"/>
        <xsl:variable name="e2" as="xs:integer"
            select="count(//rep:message[rep:custom-level(.) eq 'error'])"/>
        <p class="important" xmlns="http://www.w3.org/1999/xhtml">
            <b>Bewertung: </b>
            <xsl:choose>
                <xsl:when test="$e1 eq 0 and $e2 eq 0">
                    <xsl:text>Es wird empfohlen das Dokument anzunehmen un weiter zu verarbeiten.</xsl:text>
                </xsl:when>
                <xsl:when test="$e1 gt 0 and $e2 eq 0">
                    <xsl:text>Es wird empfohlen das Dokument anzunehmen und zu verarbeiten, da die vorhandenen Fehler derzeit toleriert werden.</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Es wird empfohlen das Dokument zurückzuweisen.</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </p>
    </xsl:template>

        
</xsl:stylesheet>