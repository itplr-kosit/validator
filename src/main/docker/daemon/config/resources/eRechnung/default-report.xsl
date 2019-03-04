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
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:rep="http://www.xoev.de/de/validator/varl/1 "
                xmlns:s="http://www.xoev.de/de/validator/framework/1/scenarios"
                xmlns:in="http://www.xoev.de/de/validator/framework/1/createreportinput"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
                xmlns:html="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="xs"
                version="2.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:param name="input-document" required="yes" />

    <xsl:variable name="custom-levels" as="element(s:customLevel)*" select="//s:customLevel" />


    <xsl:template match="in:createReportInput">

        <xsl:variable name="validationStepResults" as="element(rep:validationStepResult)*">
            <xsl:apply-templates select="in:validationResultsWellformedness" />
            <xsl:apply-templates select="in:validationResultsXmlSchema" />
            <xsl:apply-templates select="in:validationResultsSchematron" />
        </xsl:variable>

        <xsl:variable name="report" as="document-node(element(rep:report))">
            <xsl:document>
                <rep:report>
                    <xsl:attribute name="valid">
                        <xsl:choose>
                            <xsl:when test="s:noScenarioMatched">false</xsl:when>
                            <xsl:when test="$validationStepResults[@valid = 'false']">false</xsl:when>
                            <xsl:otherwise>true</xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:apply-templates select="in:engine" mode="copy-to-report-ns" />
                    <xsl:apply-templates select="in:timestamp" mode="copy-to-report-ns" />
                    <xsl:apply-templates select="in:documentIdentification" mode="copy-to-report-ns" />

                    <xsl:choose>
                        <xsl:when test="s:scenario">
                            <rep:scenarioMatched>
                                <xsl:sequence select="s:scenario" />
                                <xsl:call-template name="document-data" />
                                <rep:validationResult>
                                    <xsl:sequence select="$validationStepResults" />
                                </rep:validationResult>
                            </rep:scenarioMatched>
                        </xsl:when>
                        <xsl:otherwise>
                            <rep:noScenarioMatched>

                            </rep:noScenarioMatched>
                        </xsl:otherwise>
                    </xsl:choose>
                </rep:report>
            </xsl:document>
        </xsl:variable>

        <rep:report varlVersion="1.0.0">
            <xsl:copy-of select="$report/rep:report/(node()|@*)" />
            <xsl:apply-templates select="$report" mode="assessment" />
        </rep:report>

    </xsl:template>

    <!-- Overwrite this template in customisation layer to generate a documentData element -->
    <xsl:template name="document-data" />


    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Syntax checks (well-formedness and XML Schema                                      * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->

    <xsl:template match="in:validationResultsWellformedness|in:validationResultsXmlSchema">
        <xsl:variable name="id" as="xs:string"
                      select="if (self::inValidationResultsWellformedness) then 'val-xml' else 'val-xsd'" />
        <xsl:variable name="messages" as="element(rep:message)*">
            <xsl:apply-templates select="in:xmlSyntaxError">
                <xsl:with-param name="parent-id" select="$id" />
            </xsl:apply-templates>
        </xsl:variable>
        <!-- Skip output for implicit validation steps (i. e., wellformedness implemenation) unless there is anything to tell -->
        <xsl:if test="exists($messages) or exists(s:resource)">
            <rep:validationStepResult id="{$id}"
                                      valid="{if ($messages[@level = ('warning', 'error')]) then false() else true()}">
                <xsl:sequence select="s:resource" />
                <xsl:sequence select="$messages" />
            </rep:validationStepResult>
        </xsl:if>
    </xsl:template>

    <xsl:template match="in:xmlSyntaxError">
        <xsl:param name="parent-id" as="xs:string" required="yes" />
        <xsl:variable name="id" select="concat($parent-id, '.', count(preceding-sibling::xmlSyntaxError) + 1)" />
        <xsl:variable name="level" select="if (@severity = 'SEVERITY_WARNING') then 'warning' else 'error'" />
        <rep:message id="{$id}" level="{$level}">
            <xsl:apply-templates select="*" />
            <xsl:value-of select="in:message" />
        </rep:message>
    </xsl:template>

    <xsl:template match="in:xmlSyntaxError/in:*" priority="-1" />


    <xsl:template match="in:xmlSyntaxError/in:rowNumber[. != '-1']">
        <xsl:attribute name="lineNumber" select="." />
    </xsl:template>

    <xsl:template match="in:xmlSyntaxError/in:columnNumber[. != '-1']">
        <xsl:attribute name="columnNumber" select="." />
    </xsl:template>

    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Schematron (SVRL)                                                                  * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->


    <xsl:template match="in:validationResultsSchematron">
        <xsl:variable name="schematron-output" as="element(svrl:schematron-output)?"
                      select="in:results/svrl:schematron-output" />
        <xsl:if test="empty($schematron-output)">
            <xsl:message terminate="yes">Unexpected result from schematron validation - there is no
                svrl:schematron-output element!
            </xsl:message>
        </xsl:if>
        <xsl:variable name="id" as="xs:string"
                      select="concat('val-sch.',1 + count(preceding-sibling::in:validationResultsSchematron))" />
        <xsl:variable name="messages" as="element(rep:message)*">
            <xsl:apply-templates select="$schematron-output/(svrl:failed-assert|svrl:successful-report)">
                <xsl:with-param name="parent-id" select="$id" />
            </xsl:apply-templates>
        </xsl:variable>
        <rep:validationStepResult id="{$id}"
                                  valid="{if ($messages[@level = ('warning', 'error')]) then false() else true()}">
            <xsl:sequence select="s:resource" />
            <xsl:sequence select="$messages" />
        </rep:validationStepResult>
    </xsl:template>


    <xsl:template match="svrl:failed-assert|svrl:successful-report">
        <xsl:param name="parent-id" as="xs:string" required="yes" />
        <xsl:variable name="id"
                      select="concat($parent-id, '.', count(preceding-sibling::failed-assert | preceding-sibling::successful-report) + 1)" />
        <rep:message id="{$id}">
            <xsl:apply-templates select="in:location/*" />
            <xsl:attribute name="level">
                <xsl:choose>
                    <xsl:when test="(@flag,@role) = ('fatal', 'error')">error</xsl:when>
                    <xsl:when test="(@flag,@role) = ('warning', 'warn')">warning</xsl:when>
                    <xsl:when test="(@flag,@role) = ('information', 'info')">information</xsl:when>
                    <xsl:when test="@role = ('information', 'info')">warning</xsl:when>
                    <xsl:otherwise>error</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates select="@location" />
            <xsl:apply-templates select="@id" />
            <xsl:value-of select="svrl:text" />
        </rep:message>
    </xsl:template>

    <xsl:template match="svrl:*/@location">
        <xsl:attribute name="xpathLocation" select="." />
    </xsl:template>

    <xsl:template match="svrl:failed-assert/@id">
        <xsl:attribute name="code" select="." />
    </xsl:template>

    <xsl:template match="svrl:successful-report/@id">
        <xsl:attribute name="code" select="." />
    </xsl:template>

    <!-- ************************************************************************************** -->
    <!-- * Validation helpers                                                                 * -->
    <!-- ************************************************************************************** -->

    <!-- Identity template -->
    <xsl:template mode="copy-to-report-ns" match="@*">
        <xsl:copy />
    </xsl:template>

    <xsl:template mode="copy-to-report-ns" match="element()" priority="5">
        <xsl:element name="rep:{local-name()}">
            <xsl:apply-templates mode="copy-to-report-ns" select="node()|@*" />
        </xsl:element>
    </xsl:template>

    <!-- ************************************************************************************** -->
    <!-- *                                                                                    * -->
    <!-- * Assessment                                                            * -->
    <!-- *                                                                                    * -->
    <!-- ************************************************************************************** -->

    <xd:doc>
        <xd:desc>
            <xd:p>Dies ist das zentrale Template des Skripts. Angewandt auf ein
                report-Dokument ergänzt es dieses um eine Handlungsempfehlung in Form eines
                <xd:i>accept</xd:i>
                oder <xd:i>reject</xd:i> Elements.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="rep:report" mode="assessment">
        <rep:assessment>
            <xsl:variable name="element-name" as="xs:string">
                <xsl:choose>
                    <xsl:when test="empty(s:scenario)">reject</xsl:when>
                    <xsl:when test="//rep:message[rep:custom-level(.) = 'error']">reject</xsl:when>
                    <xsl:otherwise>accept</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:element name="rep:{$element-name}" exclude-result-prefixes="#all">
                <xsl:call-template name="explanations" />
            </xsl:element>
        </rep:assessment>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Ermittelt für eine während der Validierung ausgegebene Fehlernachricht deren
                Fehlerlevel <xd:i>(error, warning, information)</xd:i> gemäß der
                benutzerspezifischen Qualifizierung.
            </xd:p>
            <xd:p>Jede Fehlernachricht hat im Rahmen der Validierung ein solches Fehlerlevel
                erhalten (siehe Attribut <xd:i>@level</xd:i>). Im Regelfall entspricht die
                benutzerspezifische Qualifizierung unverändert diesem Level. Nutzer können jedoch im
                Rahmen der Bewertung eigene Qualifizierungen vereinbaren und in dem als Parameter
                <xd:ref name="assessment" type="parameter" />
                übergebenen
                <xd:i>assessment</xd:i>
                Element für bestimmte, anhand des Fehlercodes identifizierten Fehlermeldungen eine
                eigene Qualifizierung als <xd:i>customLevel</xd:i> festlegen.
            </xd:p>
            <xd:p>Dies kann z. B. genutzt werden, um einen <xd:i>error</xd:i>, der ansonsten zur
                Rückweisung der Nachricht führen würde, zumindest zeitweilig als
                <xd:i>warning</xd:i>
                zu qualifizieren, so dass eine entsprechende
                Dokumenteninstanz trotz einer Warnung angenommen und verarbeitet würde.
            </xd:p>
            <xd:p>Die Funktion prüft für eine Fehlernachricht, ob deren <xd:i>@code</xd:i> Attribut
                Bestandteil der für ein bestimmtes <xd:i>customLevel</xd:i> des
                <xd:ref
                        name="assessment" type="parameter" />
                Parameters angegebenen Fehlercodes ist.
                Falls ja, dann gilt das jeweilige <xd:i>customLevel</xd:i>. Andernfalls wird der im
                Rahmen der Validierung ermittelte Fehlerlevel unverändert übernommen.
            </xd:p>
        </xd:desc>
        <xd:param name="message">Eine im Rahmen der Validierung ausgegebene
            Fehlernachricht
        </xd:param>
        <xd:return />
    </xd:doc>
    <xsl:function name="rep:custom-level" as="xs:string">
        <xsl:param name="message" as="element(rep:message)" />
        <xsl:variable name="cl" as="element(s:customLevel)?"
                      select="$custom-levels[tokenize(., '\s+') = $message/@code]" />
        <xsl:value-of select="if ($cl) then $cl/@level else $message/@level" />
    </xsl:function>

    <xsl:template name="explanations">
        <rep:explanation>
            <xsl:call-template name="html:html" />
        </rep:explanation>
    </xsl:template>

    <xsl:template name="html:html">
        <html xmlns="http://www.w3.org/1999/xhtml" data-report-type="report">
            <xsl:call-template name="html:head" />
            <body>
                <xsl:call-template name="html:document" />
            </body>
        </html>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert das <xd:i>head</xd:i> Element eines eingebetteten HTML Dokuments,
                welches den Prüf- und Bewertungsbericht visualisiert und die Handlungsempfehlung
                begründet
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:head" as="element(html:head)">
        <head xmlns="http://www.w3.org/1999/xhtml">
            <title>Prüfbericht</title>
            <style>
                body{
                font-family: Calibri;
                width: 230mm;
                }

                .metadata dt {
                float: left;
                width: 230px;
                clear: left;
                }

                .metadata dd {
                margin-left: 250px;
                }

                table{
                border-collapse: collapse;
                width: 100%;
                }

                table.tbl-errors{
                font-size: smaller;
                }

                table.document{
                font-size: smaller;
                }

                table.document td {vertical-align:top;}

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

                td.pos{
                padding-left: 3pt;
                width: 5%;
                color: gray
                }

                td.element{
                width: 95%;
                word-wrap: break-word;
                }


                td.element:before{
                content: attr(title);
                color: gray;
                }


                div.attribute{
                display: inline;
                font-style: italic;
                color: gray;
                }
                div.attribute:before{
                content: attr(title) '=';
                }
                div.val{
                display: inline;
                font-weight: bold;
                }

                td.level1{
                padding-left: 2mm;
                }

                td.level2{
                padding-left: 5mm;
                }

                td.level3{
                padding-left: 10mm;
                }

                td.level4{
                padding-left: 15mm;
                }

                td.level5{
                padding-left: 20mm;
                }
                td.level6{
                padding-left: 25mm;
                }

                tr{
                vertical-align: bottom;
                border-bottom: 1px solid #c0c0c0;
                }

                .error{
                color: red;
                }

                .warning{
                }

                p.important{
                font-weight: bold;
                text-align: left;
                background-color: #e0e0e0;
                padding: 3pt;
                }

                td.right{
                text-align: right
                }
            </style>
        </head>
    </xsl:template>

    <xsl:template name="html:document" xmlns="http://www.w3.org/1999/xhtml">
        <h1>Prüfbericht</h1>

        <xsl:call-template name="html:document-metadata" />

        <xsl:call-template name="html:conformance" />

        <xsl:if test="//rep:message">
            <xsl:call-template name="html:validationresults" />
        </xsl:if>
        <xsl:call-template name="html:assessment" />

        <!-- this is the extension -->
        <xsl:if test="$input-document instance of document-node(element())">
            <xsl:call-template name="html:contentdoc">
                <xsl:with-param name="invoice" select="$input-document" />
            </xsl:call-template>
        </xsl:if>

        <xsl:call-template name="html:epilog" />
    </xsl:template>


    <xd:doc>
        <xd:desc>
            <xd:p>Generiert am Beginn eines eingebetteten HTML Dokuments, welches den Prüf- und
                Bewertungsbericht visualisiert und die Handlungsempfehlung begründet, eine Übersicht
                mit Metadaten des geprüften Dokuments.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:document-metadata" as="element()+">
        <div class="metadata">
            <p class="important">
                <xsl:text>Angaben zum geprüften Dokument</xsl:text>
            </p>
            <dl>
                <dt>Referenz:</dt>
                <dd>
                    <xsl:value-of select="rep:documentIdentification/rep:documentReference" />
                </dd>

                <dt>Zeitpunkt der Prüfung:</dt>
                <dd>
                    <xsl:value-of select="format-dateTime(rep:timestamp, '[D].[M].[Y] [H]:[m]:[s]')" />
                </dd>

                <dt>Erkannter Dokumenttyp:</dt>
                <dd>
                    <xsl:choose>
                        <xsl:when test="rep:scenarioMatched">
                            <xsl:value-of select="rep:scenarioMatched/s:scenario/s:name" />
                        </xsl:when>
                        <xsl:otherwise>
                            <b class="error">unbekannt</b>
                        </xsl:otherwise>
                    </xsl:choose>
                </dd>
            </dl>
            <xsl:call-template name="html:documentdata" />
        </div>
    </xsl:template>

    <xsl:template name="html:documentdata" />

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert am Ende eines eingebetteten HTML Dokuments, welches den Prüf- und
                Bewertungsbericht visualisiert und die Handlungsempfehlung begründet, eine Übersicht
                mit Metadaten zum Prüfmodul.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:epilog" as="element()+">
        <p class="info" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:text>Dieser Prüfbericht wurde erstellt mit </xsl:text>
            <xsl:value-of select="rep:engine/rep:name" />
            <xsl:text>.</xsl:text>
        </p>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetetteten HTML Dokument eine Tabelle mit den während der
                Validierung ausgegebenen Daten.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:validationresults" as="element()*">
        <p>Übersicht der Validierungsergebnisse:</p>
        <table class="tbl-errors">
            <thead>
                <tr>
                    <th>Prüfschritt</th>
                    <th>Fehler</th>
                    <th>Warnungen</th>
                    <th>Informationen</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="//rep:validationResult/rep:validationStepResult">
                    <xsl:variable name="step-id" select="@id" />
                    <tr>
                        <td>
                            <xsl:value-of select="s:resource/s:name" />
                            <xsl:text> (</xsl:text>
                            <xsl:value-of select="@id" />
                            <xsl:text>)</xsl:text>
                        </td>
                        <td style="width: 30mm;">
                            <xsl:value-of
                                    select="count(rep:message[@level eq 'error'])" />
                        </td>
                        <td style="width: 30mm;">
                            <xsl:value-of
                                    select="count(rep:message[@level eq 'warning'])" />
                        </td>
                        <td style="width: 30mm;">
                            <xsl:value-of
                                    select="count(rep:message[@level eq 'information'])"
                            />
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>

        <p>Validierungsergebnisse im Detail:</p>
        <table xmlns="http://www.w3.org/1999/xhtml" class="tbl-errors">
            <thead>
                <tr>
                    <th style="width: 30mm;">Pos</th>
                    <th style="width: 25mm;">Code</th>
                    <th style="width: 25mm;">Adj. Grad (Grad)</th>
                    <th>Text</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="//rep:message">
                    <tr>
                        <xsl:attribute name="class">
                            <xsl:value-of select="rep:custom-level(.)" />
                        </xsl:attribute>
                        <td rowspan="2">
                            <xsl:value-of select="@id" />
                        </td>
                        <td rowspan="2">
                            <xsl:value-of select="@code" />
                        </td>
                        <td rowspan="2">
                            <xsl:value-of select="rep:custom-level(.)" />
                            <xsl:if test="not(rep:custom-level(.) eq @level)">
                                <xsl:value-of select="concat(' (', @level, ')')" />
                            </xsl:if>
                        </td>
                        <td>
                            <xsl:value-of select="normalize-space(.)" />
                        </td>
                    </tr>
                    <tr>
                        <xsl:attribute name="class">
                            <xsl:value-of select="rep:custom-level(.)" />
                        </xsl:attribute>
                        <td>
                            <xsl:if test="@xpathLocation">
                                <xsl:text>Pfad: </xsl:text><xsl:value-of select="@xpathLocation" />
                            </xsl:if>
                            <xsl:if test="@lineNumber">
                                <xsl:text> Zeile: </xsl:text><xsl:value-of select="@lineNumber" />
                            </xsl:if>
                            <xsl:if test="@columnNumber">
                                <xsl:text> Spalte: </xsl:text><xsl:value-of select="@columnNumber" />
                            </xsl:if>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetteten HTML Dokument eine Aussage zur Konformität des
                geprüften Dokuments zu den formalen Vorgaben.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:conformance">
        <xsl:variable name="e" as="xs:integer" select="count(//rep:message[@level eq 'error'])" />
        <xsl:variable name="w" as="xs:integer" select="count(//rep:message[@level eq 'warning'])" />
        <xsl:choose>
            <xsl:when test="rep:scenarioMatched">
                <p class="important" xmlns="http://www.w3.org/1999/xhtml">
                    <b>Konformitätsprüfung:</b>
                    <xsl:text>Das geprüfte Dokument enthält </xsl:text>
                    <xsl:choose>
                        <xsl:when test="$e + $w eq 0">
                            <xsl:text>weder Fehler noch Warnungen. Es ist konform zu den formalen Vorgaben.</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($e, ' Fehler / ', $w, ' Warnungen. Es ist ')" />
                            <b>nicht konform</b>
                            <xsl:text> zu den formalen Vorgaben.</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <p class="important" xmlns="http://www.w3.org/1999/xhtml">
                    <b>Konformitätsprüfung:</b>
                    <xsl:text>Das geprüfte Dokument entspricht keinen zulässigen Dokumenttyp und ist damit </xsl:text>
                    <b>nicht konform</b>
                    <xsl:text> zu den formalen Vorgaben.</xsl:text>
                </p>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Generiert in dem eingebetteten HTML Dokument die Aussage zur
                Handlungsempfehlung.
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="html:assessment" xmlns="http://www.w3.org/1999/xhtml">
        <xsl:variable name="e1" as="xs:integer" select="count(//message[@level eq 'error'])" />
        <xsl:variable name="e2" as="xs:integer"
                      select="count(//rep:message[rep:custom-level(.) eq 'error'])" />
        <xsl:choose>
            <xsl:when test="empty(rep:scenarioMatched)">
                <p class="important error">Bewertung: Es wird empfohlen das Dokument zurückzuweisen.</p>
            </xsl:when>
            <xsl:when test="$e1 eq 0 and $e2 eq 0">
                <p class="important">Bewertung: Es wird empfohlen das Dokument anzunehmen und weiter zu verarbeiten.</p>
            </xsl:when>
            <xsl:when test="$e1 gt 0 and $e2 eq 0">
                <p class="important">Bewertung: Es wird empfohlen das Dokument anzunehmen und zu verarbeiten, da die
                    vorhandenen Fehler derzeit toleriert werden.
                </p>
            </xsl:when>
            <xsl:otherwise>
                <p class="important error">Bewertung: Es wird empfohlen das Dokument zurückzuweisen.</p>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="html:contentdoc">
        <xsl:param name="invoice" as="document-node(element())" />
        <p class="important">
            <xsl:text>Inhalt des Rechnungsdokuments:</xsl:text>
        </p>
        <table class="document" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:apply-templates select="$invoice/*" mode="html:contentdoc" />
        </table>
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Eine Element wird als eine Zeile in einer Tabelle visualisiert. Die erste Spalte
                enthält die Zeilennummer, die zweite Attribute und Text des Elements
            </xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="*" mode="html:contentdoc">
        <xsl:variable name="line-number" as="xs:string">
            <xsl:number select="." count="*" level="any" format="0001" />
        </xsl:variable>
        <tr class="row" xmlns="http://www.w3.org/1999/xhtml" id="{$line-number}">
            <td class="pos">
                <xsl:value-of select="$line-number" />
            </td>
            <td class="element {concat('level',count(ancestor-or-self::*))}" title="{local-name()}">
                <xsl:apply-templates select="text()" mode="html:contentdoc" />
                <xsl:apply-templates select="@*" mode="html:contentdoc" />
            </td>
        </tr>
        <xsl:apply-templates select="*" mode="html:contentdoc" />
    </xsl:template>

    <xd:doc>
        <xd:desc>
            <xd:p>Ein Textbereich (in der Zeile des Elements)</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="text()" mode="html:contentdoc">
        <div class="val" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:value-of select="." />
        </div>
    </xsl:template>

    <xsl:template match="element(*, xs:base64Binary)/text()" priority="10" mode="html:contentdoc">
        <div class="val" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:text>[ … ]</xsl:text>
        </div>
    </xsl:template>

    <xsl:template match="@xsi:schemaLocation" mode="html:contentdoc" />

    <xd:doc>
        <xd:desc>
            <xd:p>Ein Attributbereich (in der Zeile des Elements)</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="@*" mode="html:contentdoc">
        <div class="attribute" title="{local-name(.)}" xmlns="http://www.w3.org/1999/xhtml">
            <xsl:value-of select="." />
        </div>
    </xsl:template>


</xsl:stylesheet>