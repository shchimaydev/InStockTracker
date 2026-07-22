# Expects the fat jar to already be built (`./gradlew :server:buildFatJar`),
# which requires the full monorepo (root settings.gradle.kts / version catalog,
# the `shared` module) — this image only ever copies the finished artifact, so
# Cloud Build never needs that context.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY build/libs/server-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
