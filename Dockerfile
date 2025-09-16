# ---- build ----
FROM maven:3.9.11-eclipse-temurin-24 AS build
WORKDIR /home/app

# 1) Copier le POM d'abord pour maximiser le cache des deps
COPY pom.xml .

# 2) Pré-télécharger les dépendances (meilleure réutilisation des couches)
RUN mvn -B -DskipTests dependency:go-offline

# 3) Puis copier les sources et builder
COPY src ./src
RUN mvn -B -DskipTests clean package

# ---- runtime ----
FROM eclipse-temurin:24-jre
WORKDIR /app

# Si ton jar repackage Spring Boot est unique dans target/, ça marche :
ARG JAR_FILE=/home/app/target/*.jar
COPY --from=build ${JAR_FILE} app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]