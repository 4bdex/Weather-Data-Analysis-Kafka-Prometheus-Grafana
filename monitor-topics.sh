#!/bin/bash

# Script to monitor Kafka topics for Weather Data Analysis (Docker version)

BOOTSTRAP_SERVER="localhost:9092"
CONTAINER_NAME="broker"

echo "=== Kafka Topic Monitor (Docker) ==="
echo ""
echo "Select which topic to monitor:"
echo "  1) weather-data (input - raw data)"
echo "  2) station-averages (output - aggregated results)"
echo "  3) List all topics"
echo "  4) Delete all topics (cleanup)"
echo ""
read -p "Enter choice [1-4]: " choice

case $choice in
    1)
        echo ""
        echo "Monitoring weather-data topic (Press Ctrl+C to stop)..."
        docker exec -it $CONTAINER_NAME sh -c \
          "/opt/kafka/bin/kafka-console-consumer.sh \
          --bootstrap-server $BOOTSTRAP_SERVER \
          --topic weather-data \
          --from-beginning \
          --property print.key=true \
          --property key.separator=' => '"
        ;;
    2)
        echo ""
        echo "Monitoring station-averages topic (Press Ctrl+C to stop)..."
        docker exec -it $CONTAINER_NAME sh -c \
          "/opt/kafka/bin/kafka-console-consumer.sh \
          --bootstrap-server $BOOTSTRAP_SERVER \
          --topic station-averages \
          --from-beginning \
          --property print.key=true \
          --property key.separator=' => '"
        ;;
    3)
        echo ""
        echo "All Kafka topics:"
        docker exec $CONTAINER_NAME sh -c \
          "/opt/kafka/bin/kafka-topics.sh --list --bootstrap-server $BOOTSTRAP_SERVER"
        echo ""
        echo "Topic details:"
        for topic in weather-data station-averages; do
            echo ""
            echo "--- $topic ---"
            docker exec $CONTAINER_NAME sh -c \
              "/opt/kafka/bin/kafka-topics.sh --describe --topic $topic --bootstrap-server $BOOTSTRAP_SERVER 2>/dev/null" || echo "Topic not found"
        done
        ;;
    4)
        echo ""
        echo "WARNING: This will delete weather-data and station-averages topics"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo "Deleting topics..."
            docker exec $CONTAINER_NAME sh -c \
              "/opt/kafka/bin/kafka-topics.sh --delete --topic weather-data --bootstrap-server $BOOTSTRAP_SERVER 2>/dev/null"
            docker exec $CONTAINER_NAME sh -c \
              "/opt/kafka/bin/kafka-topics.sh --delete --topic station-averages --bootstrap-server $BOOTSTRAP_SERVER 2>/dev/null"
            echo "Topics deleted"
        else
            echo "Cancelled"
        fi
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac
