#!/usr/bin/env bash
# 在 WSL 中运行 driver-only 的真实数据库契约测试。
# 使用方式：bash /mnt/d/Github/MySqlClient/scripts/wsl-driver-tests.sh
set -euo pipefail

cd "$(dirname "$0")/.."

export JAVA_TOOL_OPTIONS="-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dapi.version=1.44"
export DOCKER_HOST="unix:///var/run/docker.sock"
export DOCKER_API_VERSION="1.44"

bash ./gradlew -c settings-drivers.gradle.kts \
  :driver-mysql:test :driver-postgresql:test \
  --no-daemon --console=plain --stacktrace
