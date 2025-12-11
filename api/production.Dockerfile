FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:21-jre-slim AS layertools_extractor
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract --destination extracted



#FROM eclipse-temurin:21-jre-jammy
FROM eclipse-temurin:21-jre-slim
WORKDIR /app

#COPY --from=build /app/target/*.jar app.jar

#ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -XX:CompressedClassSpaceSize=32m -Dspring.profiles.active=render"

#EXPOSE 8080
#ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]


COPY --from=layertools_extractor /app/extracted/dependencies/ ./
COPY --from=layertools_extractor /app/extracted/spring-boot-loader/ ./
COPY --from=layertools_extractor /app/extracted/snapshot-dependencies/ ./
COPY --from=layertools_extractor /app/extracted/application/ ./

EXPOSE 8080

ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -XX:CompressedClassSpaceSize=32m -Dspring.profiles.active=render"

ENTRYPOINT ["java", "${JAVA_OPTS}", "org.springframework.boot.loader.JarLauncher"]