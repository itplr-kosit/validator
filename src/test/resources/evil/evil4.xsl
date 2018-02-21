<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    
	<!-- Absoluter Pfad, sicherlich nicht unterhalb von -r -->
	<xsl:include href="file://c/temp/myfile.txt"/>
    
    <xsl:template match="/">
        <evil-content/>
    </xsl:template>
    
</xsl:stylesheet>