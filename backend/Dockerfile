# Stage 1: Die Anwendung mit Gradle bauen
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
# Fix windows line endings and make gradlew executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
# Führe den Build aus, um die JAR-Datei zu erstellen
# --no-daemon ist wichtig, um Prozesse in Docker zu vermeiden
RUN ./gradlew :backend:bootJar --no-daemon

# Stage 2: Ein schlankes Image für die Laufzeit erstellen
FROM eclipse-temurin:21-jre
WORKDIR /app
# Kopiere die erstellte JAR-Datei aus der Build-Phase
COPY --from=build /app/backend/build/libs/*.jar app.jar
EXPOSE 9000
# Starte die Anwendung
CMD ["java", "-jar", "app.jar"]