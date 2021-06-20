#!/bin/bash

CURRENT_CONTAINER=$(docker-compose ps -q)

if ! [ -z "$CURRENT_CONTAINER" ]; then
	echo "실행중인 컨테이너 종료 및 삭제..."
	docker-compose down
fi

echo "빌드/실행..."

docker-compose up -d
