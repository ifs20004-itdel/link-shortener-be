
FROM openjdk:17-oracle
ARG JAR_FILE=target/linkshortener-0.0.1-SNAPSHOT.jar
COPY --from=builder target/*.jar application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/application.jar"]