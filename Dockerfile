# Use a base image with Java installed
FROM openjdk:latest

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the application JAR file to the container
COPY Main.java /usr/src/app

# Compile the Java code
RUN javac Main.java

# Run the Java program
CMD ["java", "Main"]