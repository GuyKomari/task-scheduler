FROM openjdk:11-jre-slim

# Copy your fat jar to the container
COPY "build/libs/task-scheduler-1.0.0-SNAPSHOT-fat.jar" '/usr/app/'

# Use default user
USER 1000

# Launch the application
CMD ["java","-jar", "/usr/app/task-scheduler-1.0.0-SNAPSHOT-fat.jar"]
