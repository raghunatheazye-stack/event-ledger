FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY account-service/pom.xml account-service/pom.xml
COPY event-gateway/pom.xml event-gateway/pom.xml
COPY account-service/src account-service/src
RUN mvn -q -pl account-service -am -DskipTests package
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/account-service/target/account-service-1.0.0-exec.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
