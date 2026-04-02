#!/bin/bash
set -e

echo "=== Validating Service ==="

# Health check
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health || echo "000")

    if [ "$HTTP_STATUS" = "200" ]; then
        echo "Health check passed!"
        exit 0
    fi

    echo "Waiting for application to start... (attempt $((RETRY_COUNT + 1))/$MAX_RETRIES)"
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep 5
done

echo "ERROR: Health check failed after $MAX_RETRIES attempts"
exit 1
