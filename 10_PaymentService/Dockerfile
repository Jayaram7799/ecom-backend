FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/PaymentService.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java","-jar","app.jar"]