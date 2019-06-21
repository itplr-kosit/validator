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
                xmlns:in="http://www.xoev.de/de/validator/framework/1/createreportinput"
                exclude-result-prefixes="xs"
                version="2.0">

    <xsl:output method="xml" indent="yes" />

    <xsl:param name="input-document" as="document-node(element())" required="yes" />


    <xsl:template match="in:createReportInput">
        <report xmlns="http://validator.kosit.de/test-report">
            <input>
                <xsl:copy-of select="$input-document" />
            </input>
            <result>
                <xsl:copy-of select="." />
            </result>
        </report>
    </xsl:template>


</xsl:stylesheet>