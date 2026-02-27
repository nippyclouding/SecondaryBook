#!/bin/bash
set -e

echo "=== Before Install ==="

# 기존 배포 파일 정리
DEPLOY_DIR="/opt/codedeploy-agent/deployment-root/secondarybook"
if [ -d "$DEPLOY_DIR" ]; then
    rm -rf "$DEPLOY_DIR"/*
fi

mkdir -p "$DEPLOY_DIR"

echo "Before Install completed"
