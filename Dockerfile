FROM openjdk:8-jre-alpine

COPY target/docker-compose-rule-spark-demo*.jar /app.jar

EXPOSE 4567

CMD ["java", "-jar", "/app.jar"]
