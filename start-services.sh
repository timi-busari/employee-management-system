#!/bin/bash

# Employee Management System - Service Startup Script
# This script starts all microservices in the correct order

echo "🚀 Employee Management System - Service Startup"
echo "================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if port is in use
check_port() {
    local port=$1
    local service=$2
    if lsof -i :$port >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Port $port is already in use by $service${NC}"
        return 0
    else
        return 1
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=2
    local attempt=1
    
    echo -e "${BLUE}⏳ Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" >/dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        
        if [ $((attempt % 5)) -eq 0 ]; then
            echo -e "${YELLOW}   Still waiting for $service_name (attempt $attempt/$max_attempts)...${NC}"
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start within expected time${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_dir=$1
    local service_name=$2
    local port=$3
    local health_endpoint=$4
    
    echo ""
    echo -e "${BLUE}🔧 Starting $service_name...${NC}"
    
    # Check if port is already in use
    if check_port $port "$service_name"; then
        echo -e "${YELLOW}   Skipping $service_name - already running${NC}"
        return 0
    fi
    
    # Start the service in background
    cd "$service_dir"
    nohup ./mvnw spring-boot:run > "../logs/${service_name}.log" 2>&1 &
    local pid=$!
    echo "$pid" > "../logs/${service_name}.pid"
    
    echo -e "${GREEN}   Started $service_name with PID: $pid${NC}"
    
    # Wait for service to be ready
    if [ -n "$health_endpoint" ]; then
        wait_for_service "$health_endpoint" "$service_name"
    else
        echo -e "${YELLOW}   No health check endpoint provided for $service_name${NC}"
        sleep 10
    fi
    
    cd ..
}

# Function to stop all services
stop_all_services() {
    echo ""
    echo -e "${RED}🛑 Stopping all services...${NC}"
    
    # Kill any existing Spring Boot processes
    pkill -f "spring-boot:run" 2>/dev/null || true
    
    # Kill services by PID if pid files exist
    for service in config-server discovery-service auth-service employee-service api-gateway; do
        if [ -f "logs/${service}.pid" ]; then
            local pid=$(cat "logs/${service}.pid")
            if ps -p $pid > /dev/null 2>&1; then
                kill $pid 2>/dev/null || true
                echo -e "${GREEN}   Stopped $service (PID: $pid)${NC}"
            fi
            rm -f "logs/${service}.pid"
        fi
    done
    
    sleep 3
}

# Create logs directory
mkdir -p logs

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_ROOT"

# Parse command line arguments
case "${1:-start}" in
    "stop")
        stop_all_services
        exit 0
        ;;
    "restart")
        stop_all_services
        echo ""
        echo -e "${BLUE}🔄 Restarting all services...${NC}"
        sleep 2
        ;;
    "start")
        echo -e "${BLUE}📋 Starting services in dependency order...${NC}"
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
        echo "  start   - Start all services (default)"
        echo "  stop    - Stop all services"
        echo "  restart - Stop and start all services"
        exit 1
        ;;
esac

# Service startup sequence
echo ""
echo -e "${BLUE}📝 Service Startup Plan:${NC}"
echo "   1. Config Server (8888) - Configuration management"
echo "   2. Discovery Service (8761) - Service discovery (Eureka)"
echo "   3. Auth Service (8082) - Authentication & authorization"
echo "   4. Employee Service (8083) - Employee management"
echo "   5. API Gateway (8080) - Request routing & filtering"

# 1. Start Config Server
start_service "config-server" "Config Server" 8888 "http://localhost:8888/actuator/health"

# 2. Start Discovery Service (Eureka)
start_service "discovery-service" "Discovery Service" 8761 "http://localhost:8761/actuator/health"

# 3. Start Auth Service
start_service "auth-service" "Auth Service" 8082 "http://localhost:8082/actuator/health"

# 4. Start Employee Service
start_service "employee-service" "Employee Service" 8083 "http://localhost:8083/actuator/health"

# 5. Start API Gateway
start_service "api-gateway" "API Gateway" 8080 "http://localhost:8080/actuator/health"

echo ""
echo -e "${GREEN}🎉 All services startup completed!${NC}"
echo ""
echo -e "${BLUE}📊 Service Status Summary:${NC}"
echo "   Config Server:    http://localhost:8888"
echo "   Discovery Service: http://localhost:8761"
echo "   Auth Service:     http://localhost:8082"
echo "   Employee Service: http://localhost:8083"
echo "   API Gateway:      http://localhost:8080"
echo ""
echo -e "${BLUE}📋 Useful Commands:${NC}"
echo "   Check logs: tail -f logs/[service-name].log"
echo "   Stop all:   $0 stop"
echo "   Restart:    $0 restart"
echo "   Eureka UI:  http://localhost:8761"
echo ""
echo -e "${YELLOW}⚠️  Note: Services may take a few more seconds to fully initialize${NC}"
echo -e "${GREEN}✨ Ready to test employee code functionality!${NC}"