FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-Duser.timezone=Asia/Ulaanbaatar", "-jar", "app.jar"]