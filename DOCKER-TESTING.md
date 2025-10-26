# Docker Setup Testing Guide

## ✅ What We've Created

### 1. Complete Docker Configuration
- `docker-compose.yml` - Orchestrates all 5 microservices + PostgreSQL
- `Dockerfile` for each service (Discovery, Config, Auth, Employee, Gateway)
- Docker-specific configuration files (`application-docker.yml`)
- Database initialization script (`init-db.sql`)

### 2. Service Dependencies & Health Checks
- PostgreSQL starts first with health checks
- Services start in proper order with dependency management
- Health endpoints configured for all services
- Automatic service discovery and registration

### 3. Management Scripts
- `./docker-start.sh` - One-command startup with health verification
- `./docker-stop.sh` - Clean shutdown of all services
- `./docker-status.sh` - Real-time status monitoring

## 🚀 Testing Instructions

### Prerequisites
1. **Start Docker Desktop**
   - Make sure Docker Desktop is running
   - Verify with: `docker --version`

### Test the Complete System

1. **Start all services:**
   ```bash
   cd employee-management-system
   ./docker-start.sh
   ```

2. **Expected output:**
   ```
   🚀 Starting Employee Management System...
   🏗️  Building and starting all services...
   🔍 Checking service health...
   📋 Service Health Check:
   ✅ Discovery Service is healthy
   ✅ Config Server is healthy  
   ✅ Auth Service is healthy
   ✅ Employee Service is healthy
   ✅ API Gateway is healthy
   
   🎉 Employee Management System is ready!
   ```

3. **Verify services:**
   - Eureka Dashboard: http://localhost:8761
   - API Gateway: http://localhost:8080
   - All services should be registered in Eureka

4. **Test API endpoints via Gateway:**
   ```bash
   # Health check
   curl http://localhost:8080/actuator/health
   
   # Auth endpoints  
   curl -X POST http://localhost:8080/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"password","email":"test@example.com","role":"USER"}'
   
   # Login and get JWT token
   curl -X POST http://localhost:8080/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","password":"password"}'
   ```

5. **Monitor services:**
   ```bash
   ./docker-status.sh
   ```

6. **View logs:**
   ```bash
   docker-compose logs -f discovery-service
   docker-compose logs -f api-gateway
   ```

7. **Stop all services:**
   ```bash
   ./docker-stop.sh
   ```

## 🎯 Architecture Benefits

### Scalability
- Each service can be scaled independently
- Load balancing through Eureka service discovery
- Container orchestration with Docker Compose

### Development Experience
- One-command startup/shutdown
- Isolated environments
- Easy debugging with container logs
- Health monitoring for all services

### Production Ready
- Proper service dependencies
- Health checks and graceful degradation
- Database persistence with volumes
- Network isolation

## 🔧 Troubleshooting

### If services fail to start:
1. Check Docker Desktop is running
2. Ensure ports are not in use: `lsof -i :8080,:8761,:8082,:8083,:8888,:5432`
3. View specific service logs: `docker-compose logs [service-name]`
4. Restart with: `./docker-stop.sh && ./docker-start.sh`

### Database issues:
- Database data persists in Docker volume `postgres_data`
- To reset database: `docker-compose down -v`
- Check database: `docker-compose exec postgres psql -U admin -d employee_management`

## 📊 Service Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │────│ Discovery Svc   │────│  Config Server  │
│    Port 8080    │    │   Port 8761     │    │   Port 8888     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ├───────────────────────┼───────────────────────┘
         │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Service  │    │Employee Service │    │   PostgreSQL    │
│    Port 8082    │    │   Port 8083     │    │   Port 5432     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

All services communicate through the API Gateway and register with Eureka for service discovery.