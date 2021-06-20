#!/bin/bash

CURRENT_CONTAINER=$(docker-compose ps -q)

if[ -z "$CURRENT_CONTAINER" ]; then
	pass
else
	echo "실행중인 컨테이너 종료 및 삭제..."
	docker-compose down
fi

echo "컨테이너 생성"

docker load -i wheretruck_app.tar
docker load -i wheretruck_nginx.tar

echo "컨테이너 실행"

docker-compose up -d
