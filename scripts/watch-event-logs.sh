#!/bin/bash

set -euo pipefail

LOG_ROOT="${TOMCAT_LOG_ROOT:-/opt/homebrew/opt/tomcat@9/libexec/logs}"
TODAY="${TODAY:-$(date +%F)}"
HTTP_FILTER="${1:-}"

ACCESS_LOG="${LOG_ROOT}/localhost_access_log.${TODAY}.txt"
CATALINA_OUT="${LOG_ROOT}/catalina.out"
CATALINA_LOG="${LOG_ROOT}/catalina.${TODAY}.log"
LOCALHOST_LOG="${LOG_ROOT}/localhost.${TODAY}.log"
APP_LOG="${LOG_ROOT}/scheduler/scheduler.log"

PIDS=()

cleanup() {
    local pid
    for pid in "${PIDS[@]:-}"; do
        kill "$pid" 2>/dev/null || true
    done
}

watch_http() {
    if [ -n "$HTTP_FILTER" ]; then
        tail -n 0 -F "$ACCESS_LOG" 2>/dev/null | awk -v keyword="$HTTP_FILTER" '
            index($0, keyword) > 0 {
                print "[HTTP] " $0
                fflush()
            }
        '
    else
        tail -n 0 -F "$ACCESS_LOG" 2>/dev/null | sed -u 's/^/[HTTP] /'
    fi
}

watch_sql() {
    tail -n 0 -F "$CATALINA_OUT" 2>/dev/null | awk '
        /BaseJdbcLogger/ && /Executing Statement:/ {
            capture = 1
            print "[SQL] " $0
            fflush()
            next
        }

        capture {
            print "[SQL] " $0
            fflush()
            if ($0 ~ /^[[:space:]]*$/) {
                capture = 0
            }
            next
        }

        /BaseJdbcLogger/ && /Using Connection/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /BaseJdbcLogger/ && /==> Parameters:/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /BaseJdbcLogger/ && /<==/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /BaseJdbcLogger/ && /Updates:/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /==>[[:space:]]+Preparing:/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /==>[[:space:]]+Parameters:/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /<==[[:space:]]+Total:/ {
            print "[SQL] " $0
            fflush()
            next
        }

        /StdOutImpl/ {
            print "[SQL] " $0
            fflush()
        }
    '
}

watch_raw() {
    local label="$1"
    local file="$2"
    tail -n 0 -F "$file" 2>/dev/null | sed -u "s/^/[${label}] /"
}

trap cleanup EXIT INT TERM

echo "[INFO] watching ${ACCESS_LOG}"
echo "[INFO] watching ${CATALINA_OUT}"
echo "[INFO] watching ${CATALINA_LOG}"
echo "[INFO] watching ${LOCALHOST_LOG}"
echo "[INFO] watching ${APP_LOG}"
if [ -n "$HTTP_FILTER" ]; then
    echo "[INFO] HTTP filter: ${HTTP_FILTER}"
fi

watch_http &
PIDS+=("$!")

watch_sql &
PIDS+=("$!")

watch_raw "TOMCAT" "$CATALINA_LOG" &
PIDS+=("$!")

watch_raw "WEBAPP" "$LOCALHOST_LOG" &
PIDS+=("$!")

watch_raw "APP" "$APP_LOG" &
PIDS+=("$!")

wait
