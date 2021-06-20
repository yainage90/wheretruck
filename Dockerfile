FROM adoptopenjdk/openjdk11

COPY build/libs/wheretruck-0.0.1-SNAPSHOT.jar app.jar

ENV spring.config.location=/app-config/application.yml

CMD ["java", "-jar", "app.jar"]
