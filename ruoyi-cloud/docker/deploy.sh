#!/bin/sh

usage() {
    echo "Usage: sh deploy.sh [port|config|base|modules|extras|all|stop|rm]"
    exit 1
}

compose() {
    if command -v docker >/dev/null 2>&1; then
        docker compose "$@"
    elif command -v docker-compose >/dev/null 2>&1; then
        docker-compose "$@"
    else
        echo "docker compose or docker-compose is required" >&2
        exit 1
    fi
}

port() {
    firewall-cmd --add-port=80/tcp --permanent
    service firewalld restart
}

config() {
    compose --env-file .env config
}

base() {
    compose --env-file .env up -d ruoyi-mysql ruoyi-redis ruoyi-nacos
}

modules() {
    compose --env-file .env up -d career-web ruoyi-gateway ruoyi-auth ruoyi-modules-system ruoyi-agent ruoyi-model ruoyi-knowledge
}

extras() {
    compose --env-file .env up -d ruoyi-modules-file ruoyi-modules-gen ruoyi-modules-job ruoyi-visual-monitor
}

all() {
    compose --env-file .env up -d
}

stop() {
    compose --env-file .env stop
}

remove() {
    compose --env-file .env rm
}

case "$1" in
"port")
    port
;;
"config")
    config
;;
"base")
    base
;;
"modules")
    modules
;;
"extras")
    extras
;;
"all")
    all
;;
"stop")
    stop
;;
"rm")
    remove
;;
*)
    usage
;;
esac
