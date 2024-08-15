# Usage

There are various configuration options for the XSLT transformations.

## PDF Transformation

### Choice of PDF Profile

There are various profiles for PDF generation. The default uses none with enabled accessibility. 
In order to use profile PDF/UA-1 and PDF/A-1, you can use another FOP configuration during build time, e.g. 
```bash
ant -Dfop.config=conf/fop-with-ua1-a1.xconf transform-xr-to-pdf
```

Note: PDF/A-1 does not allow embedded files. Use the default profile for display of embedded documents.

### Choice of FO engine 

The FO engine used can be specified. Engine specific extensions will then be enabled. 

```xml
<xsl:param name="foengine"/>
```

Supported values are: 
* axf - Antenna House XSL Formatter
* fop - Apache FOP

### Layout options

**Configuration of the general invoice layout **

```xml
<xsl:param name="invoiceline-layout">normal</xsl:param>
```
Supported values are: 
* normal - Similar to HTML layout incl. box layout of invoice lines 
* tabular - Tabular layout of invoice lines

**Configuration of table column width**

```xml
<xsl:param name="tabular-layout-widths">2 7 2 2 2 2 1.3 2</xsl:param>
```

Change column proportions according to your tabular layout.

**Configuration of the invoice line numbering scheme**

```xml
<xsl:param name="invoiceline-numbering">normal</xsl:param>
```

Supported values are: 
* normal - use numbers as in original invoice 
* 1.1    - use multilevel arabic numbering
* 1.i    - use mixture of arabic and roman numbering
* 00001  - use aligned arabic numbering 
* *other* - any picture string supported by [xsl:number](https://developer.mozilla.org/en-US/docs/Web/XSLT/Element/number) instruction can be used

### Choice of Language for HTML and PDF

Default language is German (de), an English (en) translation is also provided.

```xml
<xsl:param name="lang" select="'de'"/>
```

Translation files are located in the [l10n subdirectory](../src/xsl/l10n/) and can be customized according to specific local needs.

Translation files are formatted according to Java Properties in XML (see https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Properties.html for details).
Additional languages can be included by adding XML Properties files to the [l10n directory](../src/xsl/l10n/). By default, files have to be named according to ISO 639-1 two letter language codes (e.g. `fr.xml` for French).