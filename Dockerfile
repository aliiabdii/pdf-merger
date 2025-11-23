FROM eclipse-temurin:21.0.2_13-jre

WORKDIR /app
COPY build/libs/pdf-merger-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
