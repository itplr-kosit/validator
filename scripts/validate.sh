#!/usr/bin/env bash
# Validate XML documents with a released validator jar. The shared engine
# behind the GitHub Action (action.yml) and the GitLab CI template
# (gitlab/validator.gitlab-ci.yml); configured through environment
# variables so both platforms drive the identical logic.
#
#   KOSIT_VERSION        validator release, e.g. "1.6.2"
#   KOSIT_CHECKSUM       optional SHA-256 of the validator jar
#   KOSIT_CONFIGURATION  URL of a configuration zip, or path to an
#                        unpacked configuration containing scenarios.xml
#   KOSIT_CONFIGURATION_CHECKSUM  optional SHA-256 of the configuration zip
#   KOSIT_FILES          file, directory (recursive *.xml) or glob
#   KOSIT_REPORT_DIR     report output directory (default validator-reports)
#   KOSIT_SEALED         auto (default) | always | never — run the
#                        validator in a Docker container without network,
#                        a read-only root, no capabilities and no
#                        privilege escalation
#   KOSIT_IMAGE          container image for sealed runs (default
#                        eclipse-temurin:21-jre; floats the Java 21 JRE
#                        contract — pass a digest-pinned reference for a
#                        stricter policy)
#   KOSIT_CACHE          download cache directory
#
# Exit 0 iff every document is accepted. When GITHUB_OUTPUT is set, the
# accepted/rejected counts and the report directory are written to it.
set -euo pipefail

VERSION="${KOSIT_VERSION:-1.6.2}"
CHECKSUM="${KOSIT_CHECKSUM:-}"
CONFIGURATION="${KOSIT_CONFIGURATION:-}"
CONFIGURATION_CHECKSUM="${KOSIT_CONFIGURATION_CHECKSUM:-}"
FILES="${KOSIT_FILES:-}"
REPORT_DIR="${KOSIT_REPORT_DIR:-validator-reports}"
SEALED="${KOSIT_SEALED:-auto}"
IMAGE="${KOSIT_IMAGE:-eclipse-temurin:21-jre}"
CACHE="${KOSIT_CACHE:-${RUNNER_TEMP:-/tmp}/kosit-validator}"

verify() { # <checksum> <file>
  if command -v sha256sum >/dev/null 2>&1; then
    echo "$1  $2" | sha256sum -c - >/dev/null
  else
    echo "$1  $2" | shasum -a 256 -c - >/dev/null
  fi
}

mkdir -p "$CACHE"

jar="$CACHE/validator-$VERSION-standalone.jar"
if [ ! -f "$jar" ]; then
  echo "Downloading validator $VERSION..."
  curl -fsSL \
    "https://github.com/itplr-kosit/validator/releases/download/v$VERSION/validator-$VERSION-standalone.jar" \
    -o "$jar"
fi
if [ -n "$CHECKSUM" ]; then
  verify "$CHECKSUM" "$jar"
fi

case "$CONFIGURATION" in
  http://*|https://*)
    config_dir="$CACHE/configuration"
    if [ ! -f "$config_dir/scenarios.xml" ]; then
      echo "Downloading configuration..."
      curl -fsSL "$CONFIGURATION" -o "$CACHE/configuration.zip"
      if [ -n "$CONFIGURATION_CHECKSUM" ]; then
        verify "$CONFIGURATION_CHECKSUM" "$CACHE/configuration.zip"
      fi
      unzip -o -q "$CACHE/configuration.zip" -d "$config_dir"
    fi
    ;;
  *)
    config_dir="$CONFIGURATION"
    ;;
esac
if [ ! -f "$config_dir/scenarios.xml" ]; then
  echo "No scenarios.xml in configuration: $CONFIGURATION" >&2
  exit 2
fi

shopt -s nullglob
files=()
if [ -d "$FILES" ]; then
  while IFS= read -r f; do files+=("$f"); done \
    < <(find "$FILES" -name '*.xml' -type f | sort)
else
  for f in $FILES; do files+=("$f"); done
fi
if [ "${#files[@]}" -eq 0 ]; then
  echo "No XML documents matched: $FILES" >&2
  exit 2
fi

# Sealed execution: the validator runs in a JRE container with no network,
# a read-only root, no capabilities, no privilege escalation, an init
# process and a pids limit, as the caller's uid. The document, the jar and
# the configuration are mounted read-only; only the report directory and
# /tmp (a bounded tmpfs) are writable. Downloads happened above, on the host.
use_docker=0
case "$SEALED" in
  never) ;;
  always)
    if ! docker info >/dev/null 2>&1; then
      echo "KOSIT_SEALED=always, but Docker is unavailable" >&2
      exit 2
    fi
    use_docker=1
    ;;
  auto|"")
    if docker info >/dev/null 2>&1; then use_docker=1; fi
    ;;
  *)
    echo "Unknown value for KOSIT_SEALED: $SEALED" >&2
    exit 2
    ;;
esac

run_validator() { # <document> <report-dir>
  if [ "$use_docker" = "1" ]; then
    workdir="$(mktemp -d "${RUNNER_TEMP:-/tmp}/kosit-run.XXXXXX")"
    cp "$1" "$workdir/$(basename "$1")"
    mkdir -p "$workdir/reports"
    docker run --rm --init --network none --read-only \
      --tmpfs /tmp:rw,nosuid,nodev,noexec,size=64m \
      --cap-drop ALL --security-opt no-new-privileges --pids-limit 256 \
      --user "$(id -u):$(id -g)" -e HOME=/tmp \
      -v "$jar:/opt/validator.jar:ro" \
      -v "$config_dir:/opt/configuration:ro" \
      -v "$workdir:/work:ro" \
      -v "$workdir/reports:/work/reports" -w /work/reports \
      "$IMAGE" \
      java -jar /opt/validator.jar -s /opt/configuration/scenarios.xml \
      -r /opt/configuration -o /work/reports "/work/$(basename "$1")"
    status=$?
    cp "$workdir/reports/"* "$2"/ 2>/dev/null || true
    rm -rf "$workdir"
    return "$status"
  fi
  java -jar "$jar" -s "$config_dir/scenarios.xml" \
    -r "$config_dir" -o "$2" "$1"
}

mkdir -p "$REPORT_DIR"
rule_ids='\[[A-Z][A-Z0-9]*(-[A-Z0-9]+)+\]'
accepted=0
rejected=0
for f in "${files[@]}"; do
  if output="$(run_validator "$f" "$REPORT_DIR" 2>&1)"; then
    accepted=$((accepted + 1))
    echo "PASS  $f"
  else
    rejected=$((rejected + 1))
    report="$REPORT_DIR/$(basename "${f%.xml}")-report.xml"
    ids="$( { printf '%s' "$output"; [ -f "$report" ] && cat "$report"; } \
            | grep -oE "$rule_ids" | sort -u | paste -sd' ' - || true)"
    echo "FAIL  $f  ${ids:-<no rule id parsed>}"
    printf '%s\n' "$output" | grep -iE 'rejected|error' || true
  fi
done

if [ -n "${GITHUB_OUTPUT:-}" ]; then
  {
    echo "report-dir=$REPORT_DIR"
    echo "accepted=$accepted"
    echo "rejected=$rejected"
  } >> "$GITHUB_OUTPUT"
fi
echo "Accepted: $accepted  Rejected: $rejected"
[ "$rejected" -eq 0 ]
