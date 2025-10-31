#!/bin/bash

echo "🚀 Simple Service Startup Script"
echo "================================"

# Kill any existing processes
echo "🛑 Stopping existing services..."
pkill -f "spring-boot:run" 2>/dev/null || true
sleep 3

# Create logs directory
mkdir -p logs

# Start services in order
echo "🔧 Starting Config Server..."
cd config-server && nohup ./mvnw spring-boot:run > ../logs/config-server.log 2>&1 &
echo "   Config Server PID: $!"
cd ..
sleep 10

echo "🔧 Starting Discovery Service..."
cd discovery-service && nohup ./mvnw spring-boot:run > ../logs/discovery-service.log 2>&1 &
echo "   Discovery Service PID: $!"
cd ..
sleep 15

echo "🔧 Starting Auth Service..."
cd auth-service && nohup ./mvnw spring-boot:run > ../logs/auth-service.log 2>&1 &
echo "   Auth Service PID: $!"
cd ..
sleep 15

echo "🔧 Starting Employee Service..."
cd employee-service && nohup ./mvnw spring-boot:run > ../logs/employee-service.log 2>&1 &
echo "   Employee Service PID: $!"
cd ..
sleep 15

echo "🔧 Starting API Gateway..."
cd api-gateway && nohup ./mvnw spring-boot:run > ../logs/api-gateway.log 2>&1 &
echo "   API Gateway PID: $!"
cd ..

echo ""
echo "✅ All services started!"
echo "📋 Service URLs:"
echo "   Config Server:    http://localhost:8888"
echo "   Discovery Service: http://localhost:8761"
echo "   Auth Service:     http://localhost:8082"
echo "   Employee Service: http://localhost:8083"
echo "   API Gateway:      http://localhost:8080"
echo ""
echo "📝 Check logs with: tail -f logs/[service-name].log"
echo "🛑 Stop all with: pkill -f 'spring-boot:run'"