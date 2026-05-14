#!/bin/bash
#
# run.sh - scheduler 프로젝트 Tomcat 실행 스크립트
# 사용법: ./run.sh [start|stop|restart|status]
#

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
WEBAPP_DIR="$PROJECT_DIR/webapp"

# Java 11 설정
if [ -z "$JAVA_HOME" ]; then
    if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
    fi
fi

# Tomcat 9 경로 설정
if [ -z "$CATALINA_HOME" ]; then
    # Apple Silicon Mac (Homebrew)
    if [ -d "/opt/homebrew/opt/tomcat@9/libexec" ]; then
        export CATALINA_HOME="/opt/homebrew/opt/tomcat@9/libexec"
        CATALINA_CMD="/opt/homebrew/opt/tomcat@9/bin/catalina"
    # Intel Mac (Homebrew)
    elif [ -d "/usr/local/opt/tomcat@9/libexec" ]; then
        export CATALINA_HOME="/usr/local/opt/tomcat@9/libexec"
        CATALINA_CMD="/usr/local/opt/tomcat@9/bin/catalina"
    fi
fi

if [ -z "$CATALINA_CMD" ]; then
    if [ -x "/opt/homebrew/opt/tomcat@9/bin/catalina" ]; then
        CATALINA_CMD="/opt/homebrew/opt/tomcat@9/bin/catalina"
    elif [ -x "/usr/local/opt/tomcat@9/bin/catalina" ]; then
        CATALINA_CMD="/usr/local/opt/tomcat@9/bin/catalina"
    fi
fi

if [ ! -d "$CATALINA_HOME" ]; then
    echo "ERROR: Tomcat을 찾을 수 없습니다. CATALINA_HOME을 확인하세요."
    echo "  brew install tomcat@9 으로 설치할 수 있습니다."
    exit 1
fi

if [ ! -x "$CATALINA_CMD" ]; then
    echo "ERROR: Tomcat 실행 파일을 찾을 수 없습니다."
    echo "  확인 경로: /opt/homebrew/opt/tomcat@9/bin/catalina"
    exit 1
fi

CONTEXT_DIR="$CATALINA_HOME/conf/Catalina/localhost"
CONTEXT_FILE="$CONTEXT_DIR/scheduler.xml"

# Tomcat Context 설정 파일 생성
setup_context() {
    mkdir -p "$CONTEXT_DIR"
    cat > "$CONTEXT_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<Context docBase="$WEBAPP_DIR" reloadable="false">
    <!-- Tyrus/KIS websocket client는 hot reload 중 classloader leak를 만들 수 있어 수동 재시작 기준으로 운영 -->
    <!-- Oracle DB가 없는 경우 에러가 발생할 수 있지만 Tomcat은 기동됩니다 -->
</Context>
EOF
    echo "Tomcat Context 설정 완료: $CONTEXT_FILE"
    echo "  docBase: $WEBAPP_DIR"
}

CATALINA_PID_FILE="$PROJECT_DIR/.tomcat.pid"

# 상태 확인
status() {
    if [ -f "$CATALINA_PID_FILE" ]; then
        local pid=$(cat "$CATALINA_PID_FILE" 2>/dev/null || true)
        if [ -n "$pid" ] && ps -p "$pid" >/dev/null 2>&1; then
            echo "Tomcat이 실행 중입니다 (PID: $pid)"
            echo "  접속 URL: http://localhost:8080/scheduler/"
            return
        fi
    fi

    # fallback: 포트 기반 확인
    local pid2=$(lsof -ti:8080 2>/dev/null | head -n 1)
    if [ -n "$pid2" ]; then
        echo "Tomcat이 실행 중입니다 (PID: $pid2)"
        echo "  접속 URL: http://localhost:8080/scheduler/"
    else
        echo "Tomcat이 실행되고 있지 않습니다."
    fi
}

# 시작 (foreground)
start() {
    kill_existing
    setup_context

    # Oracle Autonomous DB (OCI) TLS 인증서 신뢰 설정
    # - ewallet.pem에서 추출한 Oracle ADB CA를 Java SSL truststore에 등록
    setup_oracle_trust_store

    echo ""
    echo "=== Tomcat 시작(foreground) ==="
    echo "JAVA_HOME: $JAVA_HOME"
    echo "CATALINA_HOME: $CATALINA_HOME"
    echo "CATALINA_CMD: $CATALINA_CMD"
    echo ""

    echo "접속 URL: http://localhost:8080/scheduler/"
    echo "종료하려면 현재 터미널에서 Ctrl+C 를 누르거나 ./run.sh stop 을 실행하세요."
    echo ""
    echo "참고: Oracle DB가 없으면 DB 연결 에러가 발생할 수 있지만 Tomcat 자체는 동작합니다."

    exec "$CATALINA_CMD" run
}

# 8080, 8005 포트 사용 중인 프로세스 종료
kill_existing() {
    stop_brew_service

    local pids=$(lsof -ti:8080,8005 2>/dev/null)
    if [ -n "$pids" ]; then
        echo "기존 프로세스 종료 중 (포트 8080/8005)..."
        echo "$pids" | xargs kill -9 2>/dev/null || true
        sleep 1
        echo "  종료 완료"
    fi
}

setup_oracle_trust_store() {
    ORACLE_TRUST_STORE="$PROJECT_DIR/oracle-trust.jks"
    if [ ! -f "$ORACLE_TRUST_STORE" ]; then
        return
    fi
    if [[ "${CATALINA_OPTS:-}" == *"-Djavax.net.ssl.trustStore="* ]]; then
        return
    fi
    export CATALINA_OPTS="${CATALINA_OPTS:-} -Djavax.net.ssl.trustStore=$ORACLE_TRUST_STORE -Djavax.net.ssl.trustStorePassword=changeit"
}

stop_brew_service() {
    if ! command -v brew >/dev/null 2>&1; then
        return
    fi
    if brew services list 2>/dev/null | grep -E '^tomcat@9[[:space:]]+started' >/dev/null 2>&1; then
        echo "Homebrew tomcat@9 서비스 중지 중..."
        brew services stop tomcat@9 >/dev/null 2>&1 || true
        sleep 1
        echo "  Homebrew tomcat@9 서비스 중지 완료"
    fi
}

# 중지
stop() {
    echo "=== Tomcat 중지 ==="

    export CATALINA_PID="$CATALINA_PID_FILE"

    # 정상 종료 시도
    "$CATALINA_HOME/bin/catalina.sh" stop 2>/dev/null || true

    # 종료 대기
    local pid=""
    if [ -f "$CATALINA_PID_FILE" ]; then
        pid=$(cat "$CATALINA_PID_FILE" 2>/dev/null || true)
    fi

    if [ -n "$pid" ]; then
        for i in $(seq 1 10); do
            if ! ps -p "$pid" >/dev/null 2>&1; then
                break
            fi
            sleep 1
        done
    fi

    # 남아있으면 포트 기반 정리(최후)
    kill_existing

    rm -f "$CATALINA_PID_FILE" 2>/dev/null || true
    echo "Tomcat이 중지되었습니다."
}

# 재시작
restart() {
    stop
    sleep 2
    start
}

# 명령어 처리
case "${1:-start}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    setup)
        setup_context
        ;;
    pid)
        if [ -f "$CATALINA_PID_FILE" ]; then cat "$CATALINA_PID_FILE"; else echo ""; fi
        ;;
    *)
        echo "사용법: $0 {start|stop|restart|status|setup|pid}"
        exit 1
        ;;
esac
