cd ..

./gradlew clean build -x test

tag="$(date +%Y%m%d%H%M)"

docker build -t yaincoding/wheretruck:$tag .
docker push yaincoding/wheretruck:$tag
