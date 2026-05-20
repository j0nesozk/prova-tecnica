# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -e -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /data/uploads \
    && chown -R app:app /data
USER app

COPY --from=build /workspace/target/*.jar app.jar

VOLUME ["/data/uploads"]

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
