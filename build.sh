#!/bin/bash
#
# build.sh - scheduler 프로젝트 빌드 스크립트
# Maven/Gradle 없이 javac로 직접 컴파일
#

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
WEBAPP_DIR="${WEBAPP_DIR_OVERRIDE:-$PROJECT_DIR/webapp}"
CLASSES_DIR="$WEBAPP_DIR/WEB-INF/classes"
LIB_DIR="$WEBAPP_DIR/WEB-INF/lib"
RESOURCES_DIR="$WEBAPP_DIR/WEB-INF/resources"

# Java 11 설정 (kis_client가 java.net.http API 사용)
if [ -z "$JAVA_HOME" ]; then
    if [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home -v 11 2>/dev/null || /usr/libexec/java_home 2>/dev/null)"
    fi
fi

JAVAC="$JAVA_HOME/bin/javac"

if [ ! -x "$JAVAC" ]; then
    echo "ERROR: javac를 찾을 수 없습니다. JAVA_HOME을 확인하세요: $JAVA_HOME"
    exit 1
fi

echo "=== scheduler 프로젝트 빌드 ==="
echo "JAVA_HOME: $JAVA_HOME"
echo "Java version: $($JAVAC -version 2>&1)"
echo ""

# 1. classes 디렉토리 초기화
echo "[1/3] classes 디렉토리 초기화..."
rm -rf "$CLASSES_DIR"
mkdir -p "$CLASSES_DIR"

# 2. Java 소스 컴파일
echo "[2/3] Java 소스 컴파일 중..."

# classpath 구성: WEB-INF/lib 내 모든 JAR (하위 디렉토리 포함)
# springWebFlux/ 제외 (Java 17용 JARs, Java 8과 충돌)
CLASSPATH=""
while IFS= read -r jar; do
    if [ -n "$CLASSPATH" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    else
        CLASSPATH="$jar"
    fi
done < <(find "$LIB_DIR" -name "*.jar" -type f -not -path "*/springWebFlux/*")

# Lombok annotation processor 경로
LOMBOK_JAR=$(find "$LIB_DIR" -name "lombok-*.jar" -type f | head -1)

# 모든 .java 파일 목록
JAVA_FILES=$(find "$SRC_DIR" -name "*.java" -type f)
FILE_COUNT=$(echo "$JAVA_FILES" | wc -l | tr -d ' ')

echo "  컴파일 대상: ${FILE_COUNT}개 Java 파일"

# javac로 컴파일 (source/target 1.8, Lombok annotation processor 포함)
PROCESSOR_OPTS=""
if [ -n "$LOMBOK_JAR" ]; then
    PROCESSOR_OPTS="-processorpath $LOMBOK_JAR"
    echo "  Lombok: $LOMBOK_JAR"
fi

echo "$JAVA_FILES" | $JAVAC \
    -source 1.8 \
    -target 1.8 \
    -encoding UTF-8 \
    $PROCESSOR_OPTS \
    -cp "$CLASSPATH" \
    -d "$CLASSES_DIR" \
    @/dev/stdin

echo "  컴파일 완료!"

# 3. 리소스 파일 복사
echo "[3/3] 리소스 파일 복사 중..."

# MyBatis mapper XML 파일 복사 (src 하위의 .xml 파일들)
find "$SRC_DIR" -name "*.xml" -type f | while read xml_file; do
    # src/ 이후의 상대 경로 계산
    rel_path="${xml_file#$SRC_DIR/}"
    dest="$CLASSES_DIR/$rel_path"
    mkdir -p "$(dirname "$dest")"
    cp "$xml_file" "$dest"
done
echo "  MyBatis mapper XML 복사 완료"

# Spring/MyBatis 설정 파일 복사 (webapp/WEB-INF/resources/ → classes/)
if [ -d "$RESOURCES_DIR" ]; then
    cp -R "$RESOURCES_DIR"/* "$CLASSES_DIR/"
    echo "  Spring/MyBatis 설정 파일 복사 완료"
fi

# src 하위 .properties 파일 복사 (있을 경우)
find "$SRC_DIR" -name "*.properties" -type f | while read prop_file; do
    rel_path="${prop_file#$SRC_DIR/}"
    dest="$CLASSES_DIR/$rel_path"
    mkdir -p "$(dirname "$dest")"
    cp "$prop_file" "$dest"
done

echo ""
echo "=== 빌드 완료! ==="
CLASS_COUNT=$(find "$CLASSES_DIR" -name "*.class" | wc -l | tr -d ' ')
echo "  생성된 class 파일: ${CLASS_COUNT}개"
echo "  출력 디렉토리: $CLASSES_DIR"
