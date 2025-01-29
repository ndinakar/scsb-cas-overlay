# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the WAR file from your local machine to the container
COPY build/libs/cas.war /app/scsb-cas-overlay.war

# Expose the port on which your application will run
EXPOSE 8080

# Run the WAR file with the 'java -jar' command
CMD ["java", "-jar", "scsb-cas-overlay.war"]

