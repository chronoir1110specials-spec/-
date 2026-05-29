#!/bin/sh

set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DOCKER_DIR="$ROOT_DIR/docker"
WEB_DIR="$(cd "$ROOT_DIR/../career-web" && pwd)"

copy_file() {
    src="$1"
    dest_dir="$2"
    if [ ! -f "$src" ]; then
        echo "missing file: $src" >&2
        exit 1
    fi
    mkdir -p "$dest_dir"
    cp "$src" "$dest_dir/"
}

echo "begin copy sql"
mkdir -p "$DOCKER_DIR/mysql/db"
rm -f "$DOCKER_DIR/mysql/db"/*.sql
cp "$ROOT_DIR/sql/ry_20260417.sql" "$DOCKER_DIR/mysql/db/01_ry_20260417.sql"
cp "$ROOT_DIR/sql/ry_config_20260311.sql" "$DOCKER_DIR/mysql/db/02_ry_config_20260311.sql"
cp "$ROOT_DIR/sql/quartz.sql" "$DOCKER_DIR/mysql/db/03_quartz.sql"
grep -v '^SOURCE ' "$ROOT_DIR/sql/init_career.sql" > "$DOCKER_DIR/mysql/db/04_init_career.sql"

echo "begin copy web dist"
if [ ! -d "$WEB_DIR/dist" ]; then
    echo "missing frontend dist: $WEB_DIR/dist" >&2
    exit 1
fi
mkdir -p "$DOCKER_DIR/nginx/html/dist"
rm -rf "$DOCKER_DIR/nginx/html/dist"/*
cp -r "$WEB_DIR/dist"/* "$DOCKER_DIR/nginx/html/dist/"

echo "begin copy ruoyi-gateway"
copy_file "$ROOT_DIR/ruoyi-gateway/target/ruoyi-gateway.jar" "$DOCKER_DIR/ruoyi/gateway/jar"

echo "begin copy ruoyi-auth"
copy_file "$ROOT_DIR/ruoyi-auth/target/ruoyi-auth.jar" "$DOCKER_DIR/ruoyi/auth/jar"

echo "begin copy ruoyi-visual-monitor"
copy_file "$ROOT_DIR/ruoyi-visual/ruoyi-monitor/target/ruoyi-visual-monitor.jar" "$DOCKER_DIR/ruoyi/visual/monitor/jar"

echo "begin copy ruoyi-modules-system"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-system/target/ruoyi-modules-system.jar" "$DOCKER_DIR/ruoyi/modules/system/jar"

echo "begin copy ruoyi-modules-file"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-file/target/ruoyi-modules-file.jar" "$DOCKER_DIR/ruoyi/modules/file/jar"

echo "begin copy ruoyi-modules-job"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-job/target/ruoyi-modules-job.jar" "$DOCKER_DIR/ruoyi/modules/job/jar"

echo "begin copy ruoyi-modules-gen"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-gen/target/ruoyi-modules-gen.jar" "$DOCKER_DIR/ruoyi/modules/gen/jar"

echo "begin copy ruoyi-agent"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-agent/target/ruoyi-agent.jar" "$DOCKER_DIR/ruoyi/modules/agent/jar"

echo "begin copy ruoyi-model"
copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-model/target/ruoyi-model.jar" "$DOCKER_DIR/ruoyi/modules/model/jar"

if [ -f "$ROOT_DIR/ruoyi-modules/ruoyi-knowledge/target/ruoyi-knowledge.jar" ]; then
    echo "begin copy ruoyi-knowledge"
    copy_file "$ROOT_DIR/ruoyi-modules/ruoyi-knowledge/target/ruoyi-knowledge.jar" "$DOCKER_DIR/ruoyi/modules/knowledge/jar"
else
    echo "skip ruoyi-knowledge: target jar not found"
fi

echo "copy finished"
