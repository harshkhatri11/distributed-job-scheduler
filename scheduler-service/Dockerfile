# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom first — Docker caches this layer.
# Dependencies are only re-downloaded when pom.xml changes, not on every code change.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Now copy source and build. This layer only invalidates on source changes.
COPY src/ src/

RUN ./mvnw package -DskipTests -B

# Extract Spring Boot layered JAR — separates deps, spring-boot-loader, snapshot-deps,
# and application classes into distinct filesystem layers for optimal Docker caching.
RUN java -Djarmode=layertools \
    -jar target/scheduler-service-0.0.1-SNAPSHOT.jar extract \
    --destination target/extracted

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user — never run production services as root inside a container.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Copy layered JAR contents in order of least-to-most frequently changed.
# Docker caches each COPY as a separate layer, so dep layers survive code-only rebuilds.
COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

EXPOSE 8080

# JVM flags:
#   -XX:+UseContainerSupport        — respect Docker CPU/memory limits (default in JDK 21)
#   -XX:MaxRAMPercentage=75.0       — use 75% of container memory for heap, leave room for metaspace + threads
#   -Djava.security.egd=...urandom  — faster startup; /dev/random can block waiting for entropy
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "org.springframework.boot.loader.launch.JarLauncher"]