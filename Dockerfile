
FROM openjdk:17-oracle
ARG JAR_FILE=target/linkshortener-0.0.1-SNAPSHOT.jar
COPY target/linkshortener-0.0.1-SNAPSHOT.jar /application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/application.jar"]