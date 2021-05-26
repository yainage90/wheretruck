../gradlew clean build

docker build  -f ../Dockerfile -t yaincoding/wheretruck:$1 .
docker push yaincoding/wheretruck:$1
