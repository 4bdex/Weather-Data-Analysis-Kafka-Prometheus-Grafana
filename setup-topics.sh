#!/bin/bash

# Script to set up Kafka topics for Weather Data Analysis (Docker version)

BOOTSTRAP_SERVER="localhost:9092"
INPUT_TOPIC="weather-data"
OUTPUT_TOPIC="station-averages"
CONTAINER_NAME="broker"

echo "=== Setting up Kafka Topics (Docker) ==="
echo ""

# Check if Docker container is running
echo "Checking if Kafka container is running..."
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "ERROR: Kafka container '${CONTAINER_NAME}' is not running"
    echo ""
    echo "Please start Kafka container first:"
    echo "  docker ps  # to check running containers"
    exit 1
fi

echo "Kafka container is running"
echo ""

# Check if Kafka is accessible
echo "Checking Kafka connectivity..."
if ! nc -z localhost 9092 2>/dev/null; then
    echo "WARNING: Cannot connect to localhost:9092"
    echo "Make sure Kafka port is exposed in docker-compose.yml"
fi

# Create input topic
echo "Creating topic: $INPUT_TOPIC"
docker exec --workdir /opt/kafka/bin/ -it $CONTAINER_NAME sh -c \
  "./kafka-topics.sh --create --topic $INPUT_TOPIC --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 2>/dev/null || echo 'Topic may already exist'"

echo "Topic $INPUT_TOPIC created/verified"
echo ""

# Create output topic
echo "Creating topic: $OUTPUT_TOPIC"
docker exec --workdir /opt/kafka/bin/ -it $CONTAINER_NAME sh -c \
  "./kafka-topics.sh --create --topic $OUTPUT_TOPIC --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 2>/dev/null || echo 'Topic may already exist'"

echo "Topic $OUTPUT_TOPIC created/verified"
echo ""

echo "=== Topic Setup Complete ==="
echo ""
echo "Listing all topics:"
docker exec --workdir /opt/kafka/bin/ $CONTAINER_NAME sh -c \
  "./kafka-topics.sh --list --bootstrap-server localhost:9092"

echo ""
echo "You can now run the application with:"
echo "  java -jar target/meteo-data-analysis-1.0-SNAPSHOT.jar"
echo ""
echo "To monitor topics:"
echo "  ./monitor-topics.sh"
