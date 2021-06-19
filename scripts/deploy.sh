#!/bin/bash

REPOSITORY=/home/ec2-user/app/
PROJECT_NAME=wheretruck

docker-compose down

docker-compose build

docker-compose up -d --scale wheretruck=2d