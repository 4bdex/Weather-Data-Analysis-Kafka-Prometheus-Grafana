# Weather Data Analysis - Makefile

# Variables
MVN = mvn
JAVA = java
PROJECT_DIR = meteo-data-analysis
JAR = $(PROJECT_DIR)/target/meteo-data-analysis-1.0-SNAPSHOT.jar
DOCKER_COMPOSE = docker compose

.PHONY: help build test clean run docker-up docker-down setup-topics monitor logs

help: ## Show this help message
	@echo "Weather Data Analysis - Available Commands:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the project
	@echo "Building project..."
	cd $(PROJECT_DIR) && $(MVN) clean package -DskipTests

test: ## Run unit tests
	@echo "Running tests..."
	cd $(PROJECT_DIR) && $(MVN) test

test-coverage: ## Run tests with coverage
	@echo "Running tests with coverage..."
	cd $(PROJECT_DIR) && $(MVN) verify

clean: ## Clean build artifacts
	@echo "Cleaning project..."
	cd $(PROJECT_DIR) && $(MVN) clean
	rm -rf logs/

compile: ## Compile without packaging
	@echo "Compiling..."
	cd $(PROJECT_DIR) && $(MVN) compile

docker-up: ## Start Docker containers (Kafka, Prometheus, Grafana)
	@echo "Starting Docker containers..."
	$(DOCKER_COMPOSE) up -d
	@echo "Waiting for services to be ready..."
	@sleep 5
	@echo "Kafka: localhost:9092"
	@echo "Prometheus: http://localhost:9090"
	@echo "Grafana: http://localhost:3000 (admin/admin)"

docker-down: ## Stop Docker containers
	@echo "Stopping Docker containers..."
	$(DOCKER_COMPOSE) down

docker-logs: ## View Docker logs
	$(DOCKER_COMPOSE) logs -f

docker-ps: ## Show running containers
	docker ps

setup-topics: ## Create Kafka topics
	@echo "Creating Kafka topics..."
	@bash setup-topics.sh

monitor: ## Monitor Kafka topics
	@echo "Monitoring Kafka topics (Ctrl+C to stop)..."
	@bash monitor-topics.sh

run: ## Run the application
	@echo "Starting Weather Data Analysis..."
	@echo "Press Ctrl+C to stop"
	$(JAVA) -jar $(JAR)

run-dev: build run ## Build and run

quick-start: docker-up setup-topics build run ## Complete setup and run

metrics: ## Check Prometheus metrics
	@echo "Fetching metrics from http://localhost:8080/metrics"
	@curl -s http://localhost:8080/metrics | head -n 50

health: ## Check application health
	@echo "Application Health Check:"
	@curl -s http://localhost:8080/metrics | grep -E "app_health_status|weather_data_received_total" || echo "Application not running"

prometheus: ## Open Prometheus UI
	@echo "Opening Prometheus UI..."
	@xdg-open http://localhost:9090 2>/dev/null || open http://localhost:9090 2>/dev/null || echo "Please open http://localhost:9090"

grafana: ## Open Grafana UI
	@echo "Opening Grafana UI (admin/admin)..."
	@xdg-open http://localhost:3000 2>/dev/null || open http://localhost:3000 2>/dev/null || echo "Please open http://localhost:3000"

logs: ## View application logs (if logging to file)
	@tail -f logs/application.log 2>/dev/null || echo "No log file found"

lint: ## Check code style
	cd $(PROJECT_DIR) && $(MVN) checkstyle:check

format: ## Format code
	cd $(PROJECT_DIR) && $(MVN) formatter:format

deps: ## Display dependency tree
	cd $(PROJECT_DIR) && $(MVN) dependency:tree

update-deps: ## Check for dependency updates
	cd $(PROJECT_DIR) && $(MVN) versions:display-dependency-updates

verify: test ## Run verification (tests + checks)
	@echo "✓ All checks passed"

install: build ## Install to local Maven repository
	cd $(PROJECT_DIR) && $(MVN) install

full-rebuild: clean build test ## Clean, build, and test

kafka-console-consumer: ## Start Kafka console consumer for weather-data topic
	docker exec -it broker kafka-console-consumer.sh \
		--bootstrap-server localhost:9092 \
		--topic weather-data \
		--from-beginning \
		--property print.key=true

kafka-console-producer: ## Start Kafka console producer for testing
	docker exec -it broker kafka-console-producer.sh \
		--bootstrap-server localhost:9092 \
		--topic weather-data \
		--property parse.key=true \
		--property key.separator=:

kafka-topics-list: ## List all Kafka topics
	docker exec -it broker kafka-topics.sh \
		--bootstrap-server localhost:9092 \
		--list

kafka-reset: ## Delete and recreate Kafka topics
	@echo "Resetting Kafka topics..."
	docker exec broker kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic weather-data || true
	docker exec broker kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic station-averages || true
	@sleep 2
	@make setup-topics

status: ## Show system status
	@echo "=== System Status ==="
	@echo ""
	@echo "Docker Containers:"
	@docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "broker|prometheus|grafana|NAMES"
	@echo ""
	@echo "Application:"
	@curl -s http://localhost:8080/metrics > /dev/null 2>&1 && echo "✓ Running (port 8080)" || echo "✗ Not running"
	@echo ""
	@echo "Services:"
	@curl -s http://localhost:9090/-/healthy > /dev/null 2>&1 && echo "✓ Prometheus (9090)" || echo "✗ Prometheus not available"
	@curl -s http://localhost:3000/api/health > /dev/null 2>&1 && echo "✓ Grafana (3000)" || echo "✗ Grafana not available"

.DEFAULT_GOAL := help
