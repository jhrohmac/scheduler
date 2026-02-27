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
    # Intel Mac (Homebrew)
    elif [ -d "/usr/local/opt/tomcat@9/libexec" ]; then
        export CATALINA_HOME="/usr/local/opt/tomcat@9/libexec"
    fi
fi

if [ ! -d "$CATALINA_HOME" ]; then
    echo "ERROR: Tomcat을 찾을 수 없습니다. CATALINA_HOME을 확인하세요."
    echo "  brew install tomcat@9 으로 설치할 수 있습니다."
    exit 1
fi

CONTEXT_DIR="$CATALINA_HOME/conf/Catalina/localhost"
CONTEXT_FILE="$CONTEXT_DIR/scheduler.xml"

# Tomcat Context 설정 파일 생성
setup_context() {
    mkdir -p "$CONTEXT_DIR"
    cat > "$CONTEXT_FILE" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<Context docBase="$WEBAPP_DIR" reloadable="true">
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

# 시작 (daemon)
start() {
    kill_existing
    setup_context

    echo ""
    echo "=== Tomcat 시작(daemon) ==="
    echo "JAVA_HOME: $JAVA_HOME"
    echo "CATALINA_HOME: $CATALINA_HOME"
    echo "CATALINA_PID: $CATALINA_PID_FILE"
    echo ""

    export CATALINA_PID="$CATALINA_PID_FILE"

    "$CATALINA_HOME/bin/catalina.sh" start

    # PID 파일이 갱신될 시간을 조금 준다
    sleep 2

    local pid=$(cat "$CATALINA_PID_FILE" 2>/dev/null || true)
    if [ -n "$pid" ]; then
        echo "Tomcat이 시작되었습니다 (PID: $pid)"
    else
        echo "Tomcat이 시작되었습니다 (PID 확인 실패)"
    fi
    echo "접속 URL: http://localhost:8080/scheduler/"
    echo ""
    echo "참고: Oracle DB가 없으면 DB 연결 에러가 발생할 수 있지만 Tomcat 자체는 동작합니다."
}

# 8080, 8005 포트 사용 중인 프로세스 종료
kill_existing() {
    local pids=$(lsof -ti:8080,8005 2>/dev/null)
    if [ -n "$pids" ]; then
        echo "기존 프로세스 종료 중 (포트 8080/8005)..."
        echo "$pids" | xargs kill -9 2>/dev/null || true
        sleep 1
        echo "  종료 완료"
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
