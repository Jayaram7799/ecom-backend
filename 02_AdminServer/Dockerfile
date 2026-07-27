FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/AdminServer.jar app.jar

EXPOSE 1111

ENTRYPOINT ["java","-jar","app.jar"]