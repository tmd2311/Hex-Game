FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy file jar vào container
COPY target/hex-game-0.0.1-SNAPSHOT.jar app.jar

# Expose port Spring Boot dùng
EXPOSE 8080

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
