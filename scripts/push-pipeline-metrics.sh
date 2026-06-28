#!/usr/bin/env bash
set -euo pipefail

PUSHGATEWAY_URL="${PUSHGATEWAY_URL:-http://localhost:9091}"
JACOCO_XML="target/site/jacoco/jacoco.xml"
START_TIME="${1:-$(date +%s)}"
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

if [ ! -f "$JACOCO_XML" ]; then
  echo "No se encontro $JACOCO_XML. Ejecuta 'mvn verify' antes de correr este script."
  exit 1
fi

LINE_COUNTER=$(grep -oE '<counter type="LINE" missed="[0-9]+" covered="[0-9]+"/>' "$JACOCO_XML" | tail -1)
MISSED=$(echo "$LINE_COUNTER" | sed -E 's/.*missed="([0-9]+)".*/\1/')
COVERED=$(echo "$LINE_COUNTER" | sed -E 's/.*covered="([0-9]+)".*/\1/')
TOTAL=$((MISSED + COVERED))
PERCENT=$(awk "BEGIN { printf \"%.2f\", ($COVERED/$TOTAL)*100 }")

cat <<EOM | curl --silent --data-binary @- "$PUSHGATEWAY_URL/metrics/job/ci_pipeline/instance/hamburgueseria-pedidos"
# TYPE pipeline_test_coverage_percent gauge
pipeline_test_coverage_percent $PERCENT
# TYPE pipeline_build_duration_seconds gauge
pipeline_build_duration_seconds $DURATION
# TYPE pipeline_last_run_timestamp gauge
pipeline_last_run_timestamp $END_TIME
EOM

echo "Metricas enviadas a Pushgateway: cobertura=${PERCENT}% duracion=${DURATION}s"
