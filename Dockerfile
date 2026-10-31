# =========================================================================
# STAGE 1: Build Application with Maven & JDK 21
# =========================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /workspace

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and compile production JAR
COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================================
# STAGE 2: Lightweight, Secure, Cross-Platform JRE 21 Runtime (Windows/Linux/Mac)
# =========================================================================
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Add wget for healthcheck if needed and create non-root user
RUN apt-get update && apt-get install -y --no-install-recommends wget && rm -rf /var/lib/apt/lists/* && \
    groupadd -r appgroup && useradd -r -g appgroup -d /app appuser

# Copy JAR artifact from build stage
COPY --from=builder /workspace/target/online-examination-system-1.0.0.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/ || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
