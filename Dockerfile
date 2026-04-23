# Dockerfile for SanEpidCenter Application
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Install Gradle
RUN apk add --no-cache gradle

# Copy gradle files
COPY build.gradle settings.gradle ./

# Copy source code
COPY src ./src

# Build the application
RUN gradle bootWar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install PostgreSQL client for health checks
RUN apk add --no-cache postgresql-client

# Create non-root user
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -D appuser

# Copy WAR file from builder
COPY --from=builder /app/build/libs/*.war ./app.war

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.war"]
