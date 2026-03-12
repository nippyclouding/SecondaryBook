#!/bin/bash
set -e

echo "=== Starting Application ==="

TOMCAT_HOME="/opt/tomcat9"
DEPLOY_DIR="/opt/codedeploy-agent/deployment-root/secondarybook"
WAR_FILE="$DEPLOY_DIR/project-1.0.0-BUILD-SNAPSHOT.war"

# macOS 리소스 포크 파일 정리 (._* 파일들이 JAR로 인식되어 에러 발생 방지)
find $TOMCAT_HOME -name "._*" -delete 2>/dev/null || true
find $DEPLOY_DIR -name "._*" -delete 2>/dev/null || true
echo "Cleaned up macOS resource fork files"

# 기존 웹앱 제거
rm -rf $TOMCAT_HOME/webapps/ROOT
rm -rf $TOMCAT_HOME/webapps/ROOT.war

# WAR 파일 복사
if [ -f "$WAR_FILE" ]; then
    cp "$WAR_FILE" "$TOMCAT_HOME/webapps/ROOT.war"
    echo "WAR file deployed"
else
    echo "ERROR: WAR file not found at $WAR_FILE"
    exit 1
fi

# JVM 타임존을 한국 시간(KST)으로 설정
export CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=Asia/Seoul"



# CloudFront CDN 설정
export AWS_CLOUDFRONT_DOMAIN="d3p8m254izebr5.cloudfront.net"


# Tomcat 시작
$TOMCAT_HOME/bin/startup.sh

echo "Tomcat started"

# 시작 대기
sleep 10

echo "Application Start completed"
