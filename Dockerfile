FROM maven:3.9.6-eclipse-temurin AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package

FROM eclipse-temurin:17
WORKDIR /maze

COPY --from=build /build/target/proyecto3.jar .
ENTRYPOINT ["java", "-jar", "proyecto3.jar"]
