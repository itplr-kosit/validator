# Guide for Visual Testing

## Embedded Documents
For testing embedded documents' features, use
* embedded-documents-bt-125_ubl.xml
* embedded-documents-with-newline-and-whitespace_ubl.xml
* 01.15

Expectation:
* HTML: should work across all browsers
* PDF: default configuration should work:
  * with Adobe Professional 2017
  * with PDF-XChange Viewer 2.5
  * but not in browsers at all 

## Scheme Identifier for various BTs
* maxRechnung_ubl.xml
* maxRechnung_creditnote.xml

## "Project reference" (BT-11) in UBL Creditnote
* maxRechnung_creditnote.xml

## Direct Debit (BG-19)

* direct-debit-bt-90-seller_ubl.xml (BT-29 and BT-60 with @schemeID)
* direct-debit-bt-90-payee_ubl.xml (BT-29 and BT-60 without @schemeID)
* direct-debit-bt-90-seller_creditnote.xml (BT-29 and BT-60 with @schemeID)
* direct-debit-bt-90-payee_creditnote.xml (BT-29 and BT-60 without @schemeID)

## Item Attributes (BG-32)
* itemAttributes_ubl.xml

## Dates
* wrong-date-with-text-uncefact.xml
* wrong-date-with-zeros-uncefact.xml

expectation: 
* fields should contain "no date defined" or similar
* all others should show YYYY-MM-DD without timezone
