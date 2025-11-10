FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the prebuilt jar from local target directory
COPY target/Daily-Questions-MS-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 9090

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
