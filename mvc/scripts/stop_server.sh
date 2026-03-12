#!/bin/bash
set -e

echo "=== Stopping Tomcat ==="

TOMCAT_HOME="/opt/tomcat9"

# Tomcat 중지
if [ -f "$TOMCAT_HOME/bin/shutdown.sh" ]; then
    $TOMCAT_HOME/bin/shutdown.sh || true
    sleep 5
fi

# 프로세스 강제 종료 (남아있을 경우)
TOMCAT_PID=$(pgrep -f "catalina" || true)
if [ -n "$TOMCAT_PID" ]; then
    echo "Killing Tomcat process: $TOMCAT_PID"
    kill -9 $TOMCAT_PID || true
    sleep 2
fi

echo "Tomcat stopped"
