FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml ./
COPY Services/ Services/

RUN mvn -q -pl Services/PostService -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /build/Services/PostService/target/PostService-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
