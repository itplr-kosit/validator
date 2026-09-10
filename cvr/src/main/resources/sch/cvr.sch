<?xml version="1.0" encoding="UTF-8"?>
<!--
    CVR - Conformance Validation Report - the XVRL profile of the KoSIT validator.

    The structure of a report is XVRL's business (xvrl-1.0.xsd). These rules add what makes an XVRL report a CVR
    report: the canonical pipeline steps it is built from, the extension vocabulary it may use, and the internal
    consistency the producer guarantees. Every rule states which decision of the draft format it enforces.
-->
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt3">

    <sch:title>CVR - Conformance Validation Report (draft profile of XVRL)</sch:title>

    <sch:ns prefix="xvrl" uri="http://www.xproc.org/ns/xvrl"/>
    <sch:ns prefix="cvr" uri="urn:conformatron:cvr:draft"/>
    <sch:ns prefix="xs" uri="http://www.w3.org/2001/XMLSchema"/>

    <!-- D2: the canonical action names are normative for creator/@name, in pipeline order -->
    <sch:let name="steps"
             value="('parse-document', 'detect-scenarios', 'select-scenario', 'retrieve-artifacts', 'prepare-rules', 'apply-rules', 'compute-conformance', 'decision-recommendation')"/>
    <sch:let name="severities" value="('info', 'warning', 'error', 'unspecified')"/>
    <sch:let name="cvr-attributes"
             value="('conformant', 'status', 'phase', 'original-severity', 'scenario-id', 'artifact-id', 'target-id', 'conformance', 'decision', 'artifact-type', 'mime-type', 'encoding', 'source-encoding', 'algorithm')"/>

    <sch:pattern id="cvr-root">
        <sch:rule context="/*">
            <sch:assert id="root-is-xvrl-reports" test="self::xvrl:reports">The document element of a CVR report is
                xvrl:reports, found '<sch:value-of select="name()"/>'.</sch:assert>
            <sch:assert id="root-conformant" test="@cvr:conformant">D5: the root states the overall verdict in
                cvr:conformant.</sch:assert>
            <sch:assert id="root-status" test="@cvr:status">D5: the root states the run status in
                cvr:status.</sch:assert>
            <sch:assert id="root-status-value" test="not(@cvr:status) or @cvr:status = ('COMPLETED', 'CANCELLED')">D5:
                cvr:status is COMPLETED or CANCELLED, found '<sch:value-of select="@cvr:status"/>'.</sch:assert>
            <sch:assert id="root-conformant-value" test="not(@cvr:conformant) or @cvr:conformant = ('true', 'false')">
                cvr:conformant is a boolean, found '<sch:value-of select="@cvr:conformant"/>'.</sch:assert>
            <sch:assert id="cancelled-is-not-conformant"
                        test="not(@cvr:conformant = 'true') or @cvr:status = 'COMPLETED'">D5: a cancelled run is never
                conformant - the pipeline never reached the conformance statement.</sch:assert>
            <sch:assert id="root-has-metadata" test="xvrl:metadata">The root carries the metadata of the
                run.</sch:assert>
            <sch:assert id="root-has-report" test="xvrl:report">A CVR report contains at least the parse-document step
                report.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-root-metadata">
        <sch:rule context="/xvrl:reports/xvrl:metadata">
            <sch:assert id="run-timestamp" test="xvrl:timestamp">The run metadata carries the timestamp of the
                run.</sch:assert>
            <sch:assert id="run-validator" test="xvrl:validator/@name and xvrl:validator/@version">The run metadata
                names the validator and its version.</sch:assert>
            <sch:assert id="run-document" test="xvrl:document/@href">D6: the document under test is referenced by href -
                its bytes travel as output of the parse step, not here.</sch:assert>
            <sch:assert id="run-has-no-creator" test="not(xvrl:creator)">A creator identifies a step, so it belongs to a
                step report and not to the run metadata.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-run-status">
        <sch:rule context="/xvrl:reports[@cvr:status = 'COMPLETED']">
            <sch:assert id="completed-has-conformance"
                        test="xvrl:report/xvrl:metadata/xvrl:creator/@name = 'compute-conformance'">D5: a completed run
                reached the conformance statement, so it carries a compute-conformance report.</sch:assert>
        </sch:rule>
        <sch:rule context="/xvrl:reports[@cvr:status = 'CANCELLED']">
            <sch:assert id="cancelled-has-no-conformance"
                        test="not(xvrl:report/xvrl:metadata/xvrl:creator/@name = 'compute-conformance')">D5: a cancelled
                run stopped before the conformance statement, so it carries no compute-conformance
                report.</sch:assert>
            <sch:assert id="cancelled-is-rejected"
                        test="xvrl:report[last()]/xvrl:detection/@cvr:decision = 'REJECT'">A cancelled run proves
                nothing, so its decision is REJECT.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-step-report">
        <sch:rule context="/xvrl:reports/xvrl:report">
            <sch:assert id="step-single-creator" test="count(xvrl:metadata/xvrl:creator) = 1">Every step report names
                exactly one creator.</sch:assert>
            <sch:assert id="step-canonical-creator" test="xvrl:metadata/xvrl:creator/@name = $steps">D2: creator/@name
                is a canonical action name, found '<sch:value-of
                        select="xvrl:metadata/xvrl:creator/@name"/>'.</sch:assert>
            <sch:assert id="step-timestamp" test="xvrl:metadata/xvrl:timestamp">D10: every step report is
                timestamped.</sch:assert>
            <sch:assert id="step-single-digest" test="count(xvrl:digest) = 1">D4: every step report carries a digest - a
                step without one would be indistinguishable from a step that did not run.</sch:assert>
            <sch:assert id="schema-only-on-apply-rules"
                        test="not(xvrl:metadata/xvrl:schema) or xvrl:metadata/xvrl:creator/@name = 'apply-rules'">D8:
                the rule set identity belongs to the apply-rules report.</sch:assert>
            <sch:assert id="apply-rules-has-schema"
                        test="not(xvrl:metadata/xvrl:creator/@name = 'apply-rules') or count(xvrl:metadata/xvrl:schema) = 1">
                D3/D8: an apply-rules report covers exactly one rule set and names it.</sch:assert>
            <sch:assert id="validator-only-on-prepare-rules"
                        test="not(xvrl:metadata/xvrl:validator) or xvrl:metadata/xvrl:creator/@name = 'prepare-rules'">
                D15: the engine that transpiled and compiled the rules is a property of the prepare-rules
                step.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-step-order">
        <sch:rule context="/xvrl:reports/xvrl:report[not(preceding-sibling::xvrl:report)]">
            <sch:assert id="first-step-is-parse-document"
                        test="xvrl:metadata/xvrl:creator/@name = 'parse-document'">The first step report is
                parse-document - nothing can be reported about a document that was not read.</sch:assert>
        </sch:rule>
        <sch:rule context="/xvrl:reports/xvrl:report">
            <sch:let name="mine" value="index-of($steps, xvrl:metadata/xvrl:creator/@name)"/>
            <sch:let name="previous"
                     value="index-of($steps, preceding-sibling::xvrl:report[1]/xvrl:metadata/xvrl:creator/@name)"/>
            <sch:assert id="steps-in-pipeline-order"
                        test="empty($mine) or empty($previous) or $mine &gt;= $previous">D3: the step reports appear in
                pipeline order - '<sch:value-of select="xvrl:metadata/xvrl:creator/@name"/>' follows '<sch:value-of
                        select="preceding-sibling::xvrl:report[1]/xvrl:metadata/xvrl:creator/@name"/>'.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-rule-set">
        <sch:rule context="/xvrl:reports/xvrl:report/xvrl:metadata/xvrl:schema">
            <sch:assert id="schema-href" test="@href">D8: the rule set is identified by its href.</sch:assert>
            <sch:assert id="schema-typens"
                        test="@schematypens = ('http://www.w3.org/2001/XMLSchema', 'http://purl.oclc.org/dsdl/schematron')">
                D8: schematypens names the rule language by its namespace - XML Schema or Schematron, found
                '<sch:value-of select="@schematypens"/>'.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-digest">
        <sch:rule context="/xvrl:reports/xvrl:report/xvrl:digest">
            <sch:let name="detections" value="../xvrl:detection"/>
            <sch:let name="errors" value="count($detections[@severity = 'error'])"/>
            <sch:let name="warnings" value="count($detections[@severity = 'warning'])"/>
            <sch:assert id="digest-valid" test="@valid = ('true', 'false')">D4: the digest states validity as true or
                false, found '<sch:value-of select="@valid"/>'.</sch:assert>
            <sch:assert id="digest-valid-matches-detections" test="(@valid = 'false') = ($errors &gt; 0)">D4: valid is
                false exactly when the step reported a detection in the error band (<sch:value-of select="$errors"/>
                found).</sch:assert>
            <sch:assert id="digest-error-count-matches"
                        test="not(@error-count) or xs:integer(@error-count) = $errors">D4: error-count counts the error
                detections of this step (<sch:value-of select="$errors"/> found).</sch:assert>
            <sch:assert id="digest-warning-count-matches"
                        test="not(@warning-count) or xs:integer(@warning-count) = $warnings">D4: warning-count counts
                the warning detections of this step (<sch:value-of select="$warnings"/> found).</sch:assert>
            <sch:assert id="digest-omits-zero-error-count" test="($errors &gt; 0) = exists(@error-count)">D18: a count
                of zero says nothing and is omitted.</sch:assert>
            <sch:assert id="digest-omits-zero-warning-count" test="($warnings &gt; 0) = exists(@warning-count)">D18: a
                count of zero says nothing and is omitted.</sch:assert>
            <sch:assert id="digest-worst-when-it-informs"
                        test="exists(@worst) = (@valid = 'false' and count($detections) &gt; 1)">D18: worst (the XVRL
                attribute) is written exactly when it informs - the step is invalid and has more than one detection, so
                no single detection already states it.</sch:assert>
            <sch:assert id="digest-has-no-worst-severity" test="not(@worst-severity)">worst-severity is not an XVRL
                attribute; the digest's worst severity is @worst.</sch:assert>
            <sch:assert id="digest-error-codes-presence" test="($errors &gt; 0) = exists(@error-codes)">D4: error-codes
                lists the codes of the error detections, and is omitted when there are none.</sch:assert>
            <sch:assert id="digest-error-codes-are-detection-codes"
                        test="not(@error-codes) or (every $code in tokenize(normalize-space(@error-codes), ' ') satisfies $code = $detections[@severity = 'error']/@code)">
                D4: every token of error-codes is the code of an error detection of this step.</sch:assert>
            <sch:assert id="digest-has-no-fatal-error-count" test="not(@fatal-error-count)">D4: the severity model has
                no separate fatal band, so fatal-error-count would only duplicate error-count.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-detection">
        <sch:rule context="xvrl:detection">
            <sch:assert id="detection-severity-value" test="not(@severity) or @severity = $severities">The severity is
                an XVRL severity token, found '<sch:value-of select="@severity"/>'.</sch:assert>
            <sch:assert id="detection-original-severity-value"
                        test="not(@cvr:original-severity) or @cvr:original-severity = $severities">
                cvr:original-severity is an XVRL severity token, found '<sch:value-of
                        select="@cvr:original-severity"/>'.</sch:assert>
            <sch:assert id="detection-code-with-severity"
                        test="not(@severity = ('error', 'warning')) or @code">D11: a detection that reports a finding
                names it - the code says what failed, and the digest lists it for triage.</sch:assert>
            <sch:assert id="detection-one-subject"
                        test="count(@cvr:scenario-id | @cvr:artifact-id | @cvr:target-id) &lt;= 1">D13: a detection
                is about one identified subject at most.</sch:assert>
            <sch:assert id="detection-conformance-value"
                        test="not(@cvr:conformance) or @cvr:conformance = ('CONFORMANT', 'NON_CONFORMANT', 'INCONCLUSIVE')">
                D13: cvr:conformance carries a conformance verdict, found '<sch:value-of
                        select="@cvr:conformance"/>'.</sch:assert>
            <sch:assert id="detection-single-context" test="count(xvrl:context) &lt;= 1">A detection is about one
                context.</sch:assert>
            <sch:assert id="detection-says-something" test="xvrl:message or xvrl:context">A detection carries a message,
                a context, or both - an empty one reports nothing.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-context">
        <sch:rule context="cvr:hash">
            <sch:assert id="hash-in-context" test="parent::xvrl:context">D16: the fingerprint of what a detection is
                about lives in its context.</sch:assert>
            <sch:assert id="hash-algorithm" test="@cvr:algorithm">A hash without its algorithm cannot be
                reproduced.</sch:assert>
            <sch:assert id="hash-is-hex" test="matches(normalize-space(.), '^[0-9a-f]+$')">A hash is written as lower
                case hex.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-location">
        <sch:rule context="xvrl:location">
            <sch:assert id="location-locates" test="@xpath or @line or @column or @href">D9: everything positional lives
                in the location element, so a location that states nothing is an empty promise.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <!-- own pattern: within a pattern only the first matching rule fires, and the run-status rules above are also
         anchored on /xvrl:reports -->
    <sch:pattern id="cvr-terminal-step">
        <sch:rule context="/xvrl:reports">
            <sch:assert id="run-ends-with-decision"
                        test="xvrl:report[last()]/xvrl:metadata/xvrl:creator/@name = 'decision-recommendation'">Step 9
                always runs, so every run - completed or cancelled - ends with the decision-recommendation step, found
                '<sch:value-of select="xvrl:report[last()]/xvrl:metadata/xvrl:creator/@name"/>'.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-decision">
        <sch:rule context="xvrl:detection[@cvr:decision]">
            <sch:assert id="decision-value" test="@cvr:decision = ('ACCEPT', 'REJECT', 'EVALUATE_FURTHER')">Step 9: the
                decision is ACCEPT, REJECT or EVALUATE_FURTHER, found '<sch:value-of select="@cvr:decision"/>'.</sch:assert>
            <sch:assert id="decision-only-on-decision-step"
                        test="../xvrl:metadata/xvrl:creator/@name = 'decision-recommendation'">Step 9: the decision is
                stated by the decision-recommendation step, not by '<sch:value-of
                        select="../xvrl:metadata/xvrl:creator/@name"/>'.</sch:assert>
            <sch:assert id="decision-has-rationale" test="normalize-space(xvrl:message) != ''">Step 9: the decision
                carries its rationale as message.</sch:assert>
            <sch:assert id="reject-is-an-error"
                        test="(@cvr:decision = 'REJECT') = (@severity = 'error' and @code = 'decision-reject')">Step 9: a
                rejection is an error detection with code decision-reject; an acceptance carries neither severity nor
                code (D11).</sch:assert>
        </sch:rule>
        <sch:rule context="xvrl:report[xvrl:metadata/xvrl:creator/@name = 'decision-recommendation']">
            <sch:assert id="decision-step-decides" test="count(xvrl:detection[@cvr:decision]) = 1">Step 9: the
                decision-recommendation step states exactly one decision.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-payload">
        <sch:rule context="xvrl:message[@cvr:encoding]">
            <sch:assert id="payload-encoding-value" test="@cvr:encoding = ('dom', 'base64')">D6: an embedded payload is
                a dom fragment or base64, found '<sch:value-of select="@cvr:encoding"/>'.</sch:assert>
            <sch:assert id="payload-mime-type" test="@cvr:mime-type">D6: an embedded payload states its media
                type.</sch:assert>
            <sch:assert id="payload-source-encoding"
                        test="(@cvr:encoding = 'base64') = exists(@cvr:source-encoding)">D6: only a base64 payload
                needs the source encoding to be written back out - a dom fragment is by construction in the report's own
                encoding.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-payload-identity">
        <sch:rule context="xvrl:message[@xml:id]">
            <sch:assert id="payload-known-id"
                        test="@xml:id = ('parse-document-content', 'select-scenario-content')">D14: a message is
                identified by xml:id so consumers never depend on its position; '<sch:value-of select="@xml:id"/>' is
                not one of the identities the profile defines.</sch:assert>
            <sch:assert id="payload-id-implies-payload" test="@cvr:encoding">D14: the identified messages are the ones
                carrying a payload, so they state how it is encoded.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-supplemental">
        <sch:rule context="xvrl:supplemental">
            <sch:assert id="supplemental-role" test="@role">D17: a supplemental states what it carries in the XVRL role
                attribute, so a consumer can ignore it.</sch:assert>
            <sch:assert id="supplemental-not-empty" test="normalize-space(.) != '' or *">An empty supplemental
                supplements nothing.</sch:assert>
        </sch:rule>
    </sch:pattern>

    <sch:pattern id="cvr-vocabulary">
        <sch:rule context="*[namespace-uri() = 'urn:conformatron:cvr:draft']">
            <sch:assert id="known-cvr-element" test="local-name() = 'hash'">hash is the only element the CVR
                extension namespace defines, found '<sch:value-of select="local-name()"/>'.</sch:assert>
        </sch:rule>
        <sch:rule context="@*[namespace-uri() = 'urn:conformatron:cvr:draft']">
            <sch:assert id="known-cvr-attribute" test="local-name() = $cvr-attributes">'<sch:value-of
                    select="local-name()"/>' is not a CVR extension attribute.</sch:assert>
        </sch:rule>
    </sch:pattern>

</sch:schema>
