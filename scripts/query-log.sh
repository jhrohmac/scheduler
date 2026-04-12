#!/bin/bash

set -euo pipefail

CATALINA_OUT="${1:-/opt/homebrew/opt/tomcat@9/libexec/logs/catalina.out}"

if [ ! -f "$CATALINA_OUT" ]; then
    echo "ERROR: catalina.out 파일을 찾을 수 없습니다: $CATALINA_OUT" >&2
    exit 1
fi

tail -n 0 -F "$CATALINA_OUT" | awk '
    /BaseJdbcLogger/ && /Executing Statement:/ {
        capture = 1
        print
        fflush()
        next
    }

    capture {
        print
        fflush()
        if ($0 ~ /^[[:space:]]*$/) {
            capture = 0
        }
        next
    }

    /BaseJdbcLogger/ && /==> Parameters:/ {
        print
        fflush()
        next
    }

    /BaseJdbcLogger/ && /<==/ {
        print
        fflush()
        next
    }

    /BaseJdbcLogger/ && /Updates:/ {
        print
        fflush()
        next
    }

    /==>[[:space:]]+Preparing:/ {
        print
        fflush()
        next
    }

    /==>[[:space:]]+Parameters:/ {
        print
        fflush()
        next
    }

    /<==[[:space:]]+Total:/ {
        print
        fflush()
        next
    }
'
