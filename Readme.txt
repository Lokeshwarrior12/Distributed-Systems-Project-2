Project Description:

This project implements a logical timestamp-based message multicasting system, where a group of processes communicate with each other by multicasting messages. Each message is timestamped with the sender's logical time, and the system ensures that messages are delivered in a consistent order across all processes.

How to Run the Project:
1. Compile:
Compile the Java source code using a Java compiler.

2. Run:
Execute the compiled program.
Optionally, set the environment variable NUM_OF_PROCESSES to control the number of processes (default is 3).

3. Observation:
Observe the simulated communication and message delivery among processes.
Messages are timestamped, acknowledged, and delivered in a consistent order.

# Build the Docker image for the server
step1:build "docker build -t totalordermulticast ."

# runs a Docker container based on the "totalordermulticast" image, setting the environment variable "NUM_OF_PROCESSES" to 10, and removes the container after it exits.
step2:run "docker run --rm -e NUM_OF_PROCESSES=10 totalordermulticast"