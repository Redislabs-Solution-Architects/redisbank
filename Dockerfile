#
# Build stage
#
FROM maven AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -DskipTests -f /home/app/pom.xml clean package

#
# Package stage
#
FROM eclipse-temurin:21-jdk
COPY --from=build /home/app/target/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]


