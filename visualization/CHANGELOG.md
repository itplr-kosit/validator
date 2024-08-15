# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).


## v2024-06-20

This release is compatible with XRechnung 3.0.x

### Fixed

* Incorrect German translation for BT-10 (thanks to GitHub user @futurescenario9)
* BT-31: scheme identifier 'VAT' removed from templates
* condition removed from BT-29 in cii2xr conversion, which prevented display of multiple BT-29 from different bindings
* Display of specification identifier in PDF

## v2023-11-15

This release is compatible with XRechnung 3.0.x

### Changed

* Display of BT-158 in HTML
* BG-3 "PRECEDING INVOICE REFERENCE" was added to test files `maxRechnung_ubl.xml` and `maxRechnung_creditnote.xml`
* BT-11 "Project reference" with Document Type Code `50` was added to test file `maxRechnung_creditnote.xml`

## v2023-09-22

This release is compatible with XRechnung 3.0.x

### Fixed

* Cardinalities of BT-23 "Business process type", BT-34 "Seller electronic address", and BT-49 "Buyer electronic address" in `src/xsd/xrechnung-semantic-model.xsd`

## v2023-07-31

This release is compatible with XRechnung 3.0.x

### Changed

* Test files in accordance with new Schematron rules (see [XRechnung Schematron 2.0.0](https://github.com/itplr-kosit/xrechnung-schematron/releases/tag/release-2.0.0))
* Removed references to "Verzugszinsen" from `xrechnung-semantic-model.xsd`

### Fixed

* Bug on selection of BT-61 path in `cii-xr.xsl`

## v2023-05-12

This release is compatible with XRechnung 2.3.x

### Added

* Notification if JavaScript is disabled

### Changed

* BT-160 marked as value, not label
* Removed id for Third Party Payment Total from translation files and html

## Fixed

* Display of elements with unrestricted number of fraction digits (BT-146, BT-147, BT-148) in PDF (thanks to GitHub user @JannickWeisshaupt).
* Display of BT-29 and BT-60 in UBL to prevent display of BT-90 as BT-29 or BT-60.
* Superfluous display of BT-120 and BT-121 labels for VAT category codes that prohibit BT-120 and BT-121.
* Missing output of BT-49 (Buyer electronic address) in HTML.
* Misplaced output of BT-30 (Seller legal registration identifier) and BT-31 (Seller VAT identifier) values in Buyer section in HTML.
* Bug on dates with years less than 1000 (thanks to GitHub user @JannickWeisshaupt).
* Display of multiple BT-29 and BT-158.

## v2023-01-31

This release is compatible with XRechnung 2.3.x

### Added

* Visualization of third party payment

### Changed

* Normalization of newlines in embedded documents
* Percentage sign format in PDF and HTML now identical

### Fixed

* Missing condition to BT-110 and BT-111 in ubl-creditnote-xr.xsl
* Selection of identifier for pdf attachments
* Missing percentage sign `%` output in PDF and PDF Tabular

## v2022-11-15

This release is compatible with XRechnung 2.2.0

### Added

* BG-26 INVOICE LINE PERIOD elements to `maxRechnung_ubl.xml` and `maxRechnung_creditNote.xml` test instances

### Changed

* Unified country code labels
* Unified translation of BG-1 INVOICE NOTE in PDF and HTML

### Fixed

* Missing display of BG-14 INVOICING PERIOD
* Missing display of BT-54 "Buyer country subdivision" in PDF
* Removed unnecessary xslt messages
* Display of BT-128 scheme identifier label in PDF normal
* Incorrect elements in UBL test files removed
* bug that prevented output of BT-82 in UBL


### Changed

* Percentage sign format in PDF and HTML now identical

### Fixed

* Missing percentage sign `%` output in PDF and PDF Tabular

## v2022-07-31

This release is compatible with XRechnung 2.2.0

### Added

* Added multiple BG-27 and BG-28 to `maxRechnung_ubl.xml` and `maxRechnung_creditNote.xml` test instances

### Fixed

* Bug that prevented display of multiple embedded documents in PDF


## v2022-07-15

This release is compatible with XRechnung 2.2.0

### Added

* BT-128 "Invoice line object identifier" and "Invoice line object identifier/Scheme identifier" to `maxRechnung_ubl.xml` and `maxRechnung_creditNote.xml` test instances
* Tests for BT-90 Scheme ID (see [Guide for visual testing Direct Debit](./doc/guide-for-visual-testing.md))

### Fixed

* Missing display of BT-107 "Sum of allowances on document level"
* Superfluous display of BT-32 scheme identifier

## v2022-05-31

### Added

* Several more test documents
* Guide for visual testing

### Changed

* Added FileSaver.js for better cross-browser attachment download functionality
* Created ubl-common-xr.xsl for common named templates as a single point of change
* Handling of calendar date display is now more robust

### Fixed

* Missing display of BG-32 (thanks to GitHub user @JannickWeisshaupt)
* BT-47 bug in UBL CreditNote
* Logic and display of BT-110 and BT-111 in CII
* Display of percentage sign for VAT percentage rate
* Missing tooltips in HTML

## v2022-01-31

### Changed

* Tests for all Testsuite instances (except for DiGA example codes) are included 

### Fixed

* HTML errors except "stray start tag script" as VNU The Nu Html Checker (v.Nu) reports
* Translation key for BT-126 (Invoice Line Identifier)
* Address Labels in HTML for:
  * Buyer Address (BT-50, BT-51, BT-163), 
  * Seller Address (BT-35, BT-36, BT-162), 
  * Tax representative Address (BT-64, BT-65, BT-164), and 
  * Deliver To Address (BT-75, BT-76, BT-165)
* Display of BT-72 (Actual Delivery Date)


## v2021-11-15

### Added

* Added documentation about [architecture](doc/architecture.md) and [usage](doc/usage.md)
* Added support for localization -- English and German output is supported. This was done for HTML and PDF output.
* Added BT-26 to maxRechnung.xml

### Changed

* Enhanced accessibility of HTML output 
* PDF output is now accessible (PDF/UA level), fonts are embedded into PDF
* Saxon version is configurable with properties (thanks to GitHub user @knoxyz)
* Default saxon version is set to HE-10.6
* Rewrote README.md for more details and added links to documentation

### Fixed

* Fixed format-date of BT-26 in xrechnung-html.xsl (thanks to GitHub user @knoxyz)
* BT-30-Scheme-ID visualized

## v2021-07-31

### Added

* Configuration option for customizable line numbering of invoice lines
* Configuration option for tabular display of line items for PDF generation

### Fixed

* BT-23 get displayed
* BT-7 and BT-8 is now displayed in invoice data section
* Correct translation of BT-86 in cii

## v2020-12-31

### Changed

* cii-xr.xsl tolerates dates with hyphens

### Fixed

* Fixed german date format of bt-9 in pdf visualization
* Fixed visualization of BG-20, BG-21, BG-27, BG-28
* Fixed visualization of BT-11 in UBL-CreditNote

## v2020-07-30

### Added

* Sub Invoice Line with recursion in UBL-Invoice
* PDF visualization
* Ids to html divs

### Changed

* Compatible with XRechnung 2.0.0
* Xsl scripts are not generated automatically from xrechnung-model anymore
* Add scheme-ids and scheme-version-ids to div ids
* Show multiple payment terms and payment due days from CII

### Fixed

* Issue double generation of BT-47, BT-86
* Multiple line allowances and line charges (BG-27, BG-28)
* Id of BG-27 fixed in xr-mapping.xsl and xrechnung-html.xsl
* Fixed german decimal seperator and missing zero in decimal smaller than 1
* Fixed visualization of BT-74 and BT-74
* Fixed BT-39 in HTML

## v2019-06-24

### Added

* License

### Changed

* compatible with XRechnung 1.2.1
* Add CEN license statement

### Fixed

* BUG in the creation of `<xsl:template name="identifier-with-scheme-and-version">`
