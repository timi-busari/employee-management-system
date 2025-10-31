# Employee Management System

> A comprehensive microservices-based Employee Management System built with Spring Boot 3.5.7, showcasing modern enterprise architecture patterns and best practices.

## 🔒 Security Notice

**⚠️ IMPORTANT:** This project uses environment variables for security. Default credentials are **DEVELOPMENT ONLY**. Change all credentials before production deployment.

## ⚡ Quick Start

### 📋 Prerequisites

- **Docker & Docker Compose** (recommended)
- **OR** Java 17+, Maven 3.8+, PostgreSQL 13+ (for local development)

### 🐳 Docker Deployment (Recommended)

1. **Clone and navigate to project**

   ```bash
   git clone <repository-url>
   cd employee-management-system
   ```

2. **Start all services with one command**
   ```bash
   docker-compose up -d
   ```

The system automatically:

- ✅ Starts PostgreSQL with auto-created databases
- ✅ Runs Flyway migrations for schema setup
- ✅ Starts all microservices in dependency order
- ✅ Registers services with Eureka discovery
- ✅ Configures API Gateway routing
- ✅ Sets up Kafka event streaming
- ✅ Loads sample test data

### �️ Local Development Setup

1. **Prerequisites**

   ```bash
   # Verify Java version
   java -version  # Should be 17+

   # Start PostgreSQL (Docker)
   docker run -d --name postgres-dev \
     -e POSTGRES_DB=employee_management \
     -e POSTGRES_USER=admin \
     -e POSTGRES_PASSWORD=admin123 \
     -p 5432:5432 postgres:15-alpine
   ```

2. **Start services manually**

   ```bash
   # Terminal 1: Config Server
   cd config-server && ./mvnw spring-boot:run

   # Terminal 2: Discovery Service
   cd discovery-service && ./mvnw spring-boot:run

   # Terminal 3: Auth Service
   cd auth-service && ./mvnw spring-boot:run

   # Terminal 4: Employee Service
   cd employee-service && ./mvnw spring-boot:run

   # Terminal 5: API Gateway
   cd api-gateway && ./mvnw spring-boot:run
   ```

### 🔄 Development vs Production Modes

**Local Development:**

- Services run on host machine
- Config server reads from `file://` path
- Direct service-to-service communication
- Development profiles active

**Docker Production:**

- All services containerized
- Shared config repository mounted as volume
- Service discovery via container networking
- Production profiles with optimized settings

## 📊 Service Dashboard

Once started, access these URLs:

| Service                 | URL                        | Purpose             |
| ----------------------- | -------------------------- | ------------------- |
| 🌐 **Eureka Dashboard** | http://localhost:8761      | Service registry    |
| 🚪 **API Gateway**      | http://localhost:8080      | Main entry point    |
| � **Swagger UI**        | http://localhost:8080/docs | API documentation   |
| �🔐 **Auth Service**    | http://localhost:8082      | Authentication      |
| 👥 **Employee Service** | http://localhost:8083      | Employee management |
| ⚙️ **Config Server**    | http://localhost:8888      | Configuration       |
| 🗄️ **PostgreSQL**       | localhost:5432             | Database            |
| 📬 **Kafka**            | localhost:9092             | Event streaming     |

## 🧪 Quick API Tests

### 1. Health Checks

```bash
# Auth Service Health
curl http://localhost:8082/api/auth/health

# Employee Service Health
curl http://localhost:8083/api/employees/health

# Department Service Health
curl http://localhost:8083/api/departments/health
```

### 2. Authentication (Get JWT Token)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Sample Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@company.com"
  },
  "timestamp": "2024-10-31T10:30:00"
}
```

### 3. Get All Employees (Admin Only)

```bash
# Save token from login response
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -X GET http://localhost:8080/api/employees/all \
  -H "Authorization: Bearer $TOKEN"
```

### 4. Create New Employee (Admin Only)

```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "phoneNumber": "1234567890",
    "hireDate": "2024-01-15",
    "jobTitle": "Software Engineer",
    "salary": 75000.00,
    "departmentId": 1,
    "status": "ACTIVE"
  }'
```

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   API Gateway   │    │Discovery Service│    │  Config Server  │
│   Port: 8080    │    │   Port: 8761    │    │   Port: 8888    │
│  (Entry Point)  │    │    (Eureka)     │    │ (Spring Cloud)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Auth Service   │    │Employee Service │    │   PostgreSQL    │
│   Port: 8082    │    │   Port: 8083    │    │   Port: 5432    │
│ (JWT + Security)│    │(Business Logic) │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Zookeeper     │    │     Kafka       │    │  Event Topics   │
│   Port: 2181    │    │   Port: 9092    │    │• employee-events│
│ (Coordination)  │    │ (Event Stream)  │    │• department-evt │
└─────────────────┘    └─────────────────┘    │• audit-events   │
                                               └─────────────────┘
```

## � Complete API Documentation

### 🔐 Authentication Endpoints

| Method | Endpoint             | Description          | Auth Required |
| ------ | -------------------- | -------------------- | ------------- |
| POST   | `/api/auth/login`    | User authentication  | ❌            |
| POST   | `/api/auth/register` | User registration    | ❌            |
| GET    | `/api/auth/profile`  | Get user profile     | ✅            |
| GET    | `/api/auth/health`   | Service health check | ❌            |

#### 🔑 Login Request/Response

**Request:**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "admin",
    "email": "admin@company.com"
  },
  "timestamp": "2024-10-31T10:30:00"
}
```

### 👥 Employee Endpoints

| Method | Endpoint                    | Description                   | Auth Required | Role    |
| ------ | --------------------------- | ----------------------------- | ------------- | ------- |
| GET    | `/api/employees/all`        | Get all employees (paginated) | ✅            | ADMIN   |
| GET    | `/api/employees/{id}`       | Get employee by ID            | ✅            | ADMIN   |
| GET    | `/api/employees/view`       | Get own employee details      | ✅            | USER    |
| GET    | `/api/employees/department` | Get department employees      | ✅            | MANAGER |
| POST   | `/api/employees`            | Create new employee           | ✅            | ADMIN   |
| PUT    | `/api/employees/{id}`       | Update employee               | ✅            | ADMIN   |
| DELETE | `/api/employees/{id}`       | Delete employee               | ✅            | ADMIN   |
| GET    | `/api/employees/health`     | Service health check          | ❌            | -       |

#### 🆕 Create Employee Request/Response

**Request:**

```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@company.com",
  "phoneNumber": "0987654321",
  "hireDate": "2024-02-01",
  "jobTitle": "Senior Developer",
  "salary": 85000.0,
  "departmentId": 2,
  "managerId": 1,
  "status": "ACTIVE",
  "notes": "Experienced full-stack developer"
}
```

**Response:**

```json
{
  "success": true,
  "message": "Resource created successfully",
  "data": {
    "id": 15,
    "employeeCode": "EMP015",
    "firstName": "Jane",
    "lastName": "Smith",
    "fullName": "Jane Smith",
    "email": "jane.smith@company.com",
    "phoneNumber": "0987654321",
    "hireDate": "2024-02-01",
    "jobTitle": "Senior Developer",
    "salary": 85000.0,
    "status": "ACTIVE",
    "department": {
      "id": 2,
      "name": "Engineering",
      "description": "Software Development"
    },
    "managerId": 1,
    "notes": "Experienced full-stack developer",
    "createdAt": "2024-10-31T10:30:00",
    "updatedAt": "2024-10-31T10:30:00"
  },
  "timestamp": "2024-10-31T10:30:00"
}
```

### 🏢 Department Endpoints

| Method | Endpoint                  | Description                     | Auth Required | Role  |
| ------ | ------------------------- | ------------------------------- | ------------- | ----- |
| GET    | `/api/departments/all`    | Get all departments (paginated) | ✅            | ADMIN |
| GET    | `/api/departments/{id}`   | Get department by ID            | ✅            | ADMIN |
| POST   | `/api/departments`        | Create new department           | ✅            | ADMIN |
| PUT    | `/api/departments/{id}`   | Update department               | ✅            | ADMIN |
| DELETE | `/api/departments/{id}`   | Delete department               | ✅            | ADMIN |
| GET    | `/api/departments/health` | Service health check            | ❌            | -     |

#### 🆕 Create Department Request/Response

**Request:**

```json
{
  "name": "Marketing",
  "description": "Marketing and Communications",
  "location": "Building A, Floor 3",
  "budget": 250000.0
}
```

**Response:**

```json
{
  "success": true,
  "message": "Resource created successfully",
  "data": {
    "id": 5,
    "name": "Marketing",
    "description": "Marketing and Communications",
    "location": "Building A, Floor 3",
    "budget": 250000.0,
    "employeeCount": 0,
    "createdAt": "2024-10-31T10:30:00",
    "updatedAt": "2024-10-31T10:30:00"
  },
  "timestamp": "2024-10-31T10:30:00"
}
```

### 📄 Pagination Support

All list endpoints support pagination with these query parameters:

| Parameter | Type    | Default  | Description              |
| --------- | ------- | -------- | ------------------------ |
| `page`    | Integer | 0        | Page number (0-based)    |
| `size`    | Integer | 10       | Number of items per page |
| `sort`    | String  | "id,asc" | Sort field and direction |

**Example:**

```bash
curl "http://localhost:8080/api/employees/all?page=0&size=5&sort=firstName,asc" \
  -H "Authorization: Bearer $TOKEN"
```

**Paginated Response:**

```json
{
  "success": true,
  "message": "All employees retrieved successfully",
  "data": {
    "content": [...],
    "totalElements": 25,
    "totalPages": 5,
    "size": 5,
    "number": 0,
    "first": true,
    "last": false
  },
  "timestamp": "2024-10-31T10:30:00"
}
```

## 🔧 Architecture Decisions

### 🏛️ Microservices Architecture

**Decision:** Implemented microservices pattern with service separation by business domain.

**Rationale:**

- **Scalability**: Each service can be scaled independently
- **Technology Flexibility**: Different services can use different tech stacks
- **Team Independence**: Teams can develop and deploy services independently
- **Fault Isolation**: Failure in one service doesn't bring down the entire system

**Trade-offs:**

- Increased complexity in deployment and monitoring
- Network latency between services
- Distributed transaction challenges

### 🔐 Security Implementation

**Decision:** Multi-layered security with JWT tokens and role-based access control.

**Layers:**

1. **API Gateway**: First line of defense, JWT validation
2. **Service Level**: Role-based authorization with `@RoleRequired`
3. **Method Level**: Fine-grained access control

**Rationale:**

- Stateless authentication scales better
- Centralized token validation at gateway
- Flexible role-based permissions

### 🌐 API Gateway Pattern

**Decision:** Spring Cloud Gateway for reactive routing and filtering.

**Benefits:**

- Single entry point for clients
- Centralized cross-cutting concerns (auth, logging, rate limiting)
- Load balancing and service discovery integration
- Reactive programming model for better performance

### 📡 Service Discovery

**Decision:** Netflix Eureka for service registration and discovery.

**Rationale:**

- Automatic service registration
- Health checking and failover
- Load balancing integration
- Well-established in Spring ecosystem

### ⚙️ Configuration Management

**Decision:** Spring Cloud Config Server with Git repository.

**Benefits:**

- Centralized configuration management
- Environment-specific configurations
- Configuration versioning and audit trail
- Runtime configuration refresh capability

### 📊 Event-Driven Architecture

**Decision:** Apache Kafka for asynchronous event processing.

**Use Cases:**

- Employee lifecycle events
- Audit logging
- Real-time notifications
- Data synchronization between services

### 🗄️ Database Strategy

**Decision:** Database-per-service pattern with PostgreSQL.

**Rationale:**

- Data isolation between services
- Independent schema evolution
- Technology diversity if needed
- Better fault isolation

## 🔑 Default Users

| Username     | Password   | Role    | Access Level          |
| ------------ | ---------- | ------- | --------------------- |
| `admin`      | `admin123` | ADMIN   | Full system access    |
| `hr_manager` | `hr123`    | MANAGER | Department management |
| `employee1`  | `emp123`   | USER    | Own profile only      |

## � System Assumptions

### 🔒 Security Assumptions

- JWT tokens have 24-hour expiration
- Default credentials are for development/demo only
- HTTPS should be enforced in production
- Rate limiting should be implemented for production

### 💾 Data Assumptions

- Employee codes are auto-generated if not provided
- Email addresses must be unique across the system
- Departments can exist without employees
- Managers must be employees in the same department

### 🌐 Infrastructure Assumptions

- PostgreSQL is available and properly configured
- Kafka cluster is healthy for event processing
- Services can communicate via service discovery
- Sufficient memory allocated for JVM processes

### 🔄 Business Assumptions

- Working hours are based on hire date
- Salary information is sensitive and admin-only
- Department hierarchy is single-level (no sub-departments)
- Employee status changes trigger audit events

### 🐳 Docker Production Setup

Create `docker-compose.prod.yml`:

```yaml
version: "3.8"

services:
  api-gateway:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://discovery-service:8761/eureka
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  auth-service:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/auth_service_db
      - JWT_SECRET=${JWT_SECRET}
    deploy:
      replicas: 2

  employee-service:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/employee_service_db
    deploy:
      replicas: 2
```

## 🔧 Technical Stack

- **Framework**: Spring Boot 3.5.7
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15 + Flyway migrations
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway (Reactive)
- **Configuration**: Spring Cloud Config
- **Message Broker**: Apache Kafka 2.8
- **Event Processing**: Spring Kafka
- **Build Tool**: Maven 3.8+
- **Java Version**: 17+
- **Containerization**: Docker & Docker Compose

## 📊 Database Schema

### Auth Service Database (`auth_service_db`)

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- User roles junction table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id BIGINT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);
```

### Employee Service Database (`employee_service_db`)

```sql
-- Departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    budget DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    employee_code VARCHAR(20) UNIQUE NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15),
    hire_date DATE NOT NULL,
    job_title VARCHAR(100),
    salary DECIMAL(10,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    department_id BIGINT REFERENCES departments(id),
    manager_id BIGINT REFERENCES employees(id),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 📬 Kafka Event Topics

### Employee Events (`employee-events`)

```json
{
  "eventType": "employee.created",
  "timestamp": "2024-10-31T10:30:00Z",
  "employeeId": 15,
  "employeeCode": "EMP015",
  "data": {
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@company.com",
    "departmentId": 2,
    "managerId": 1
  },
  "metadata": {
    "userId": "admin",
    "source": "employee-service",
    "version": "1.0"
  }
}
```

### Department Events (`department-events`)

```json
{
  "eventType": "department.created",
  "timestamp": "2024-10-31T10:30:00Z",
  "departmentId": 5,
  "data": {
    "name": "Marketing",
    "description": "Marketing and Communications",
    "budget": 250000.0
  },
  "metadata": {
    "userId": "admin",
    "source": "employee-service",
    "version": "1.0"
  }
}
```

### Audit Events (`audit-events`)

```json
{
  "eventType": "user.login",
  "timestamp": "2024-10-31T10:30:00Z",
  "userId": "admin",
  "data": {
    "username": "admin",
    "ipAddress": "192.168.1.100",
    "userAgent": "curl/7.68.0",
    "success": true
  },
  "metadata": {
    "source": "auth-service",
    "version": "1.0"
  }
}
```

## 🛑 Stop Services

### Docker

```bash
docker-compose down
```

### Local Development

```bash
# Kill all Spring Boot processes
pkill -f "spring-boot:run"

# Or kill specific ports
lsof -ti:8080,8082,8083,8761,8888 | xargs kill -9
```

## 🐛 Troubleshooting

### Common Issues

1. **Port already in use**

   ```bash
   # Check what's running on ports
   lsof -i :8080

   # Kill specific process
   kill -9 <PID>
   ```

2. **Database connection issues**

   ```bash
   # Check PostgreSQL status
   docker ps | grep postgres

   # Check database logs
   docker logs employee-mgt-postgres

   # Connect to database directly
   docker exec -it employee-mgt-postgres psql -U admin -d auth_service_db
   ```

3. **Services not registering with Eureka**

   - Wait 30-60 seconds for initial registration
   - Check Eureka dashboard at http://localhost:8761
   - Verify `eureka.client.service-url.defaultZone` configuration

4. **JWT authentication failures**

   ```bash
   # Verify JWT secret consistency across services
   grep -r "JWT_SECRET" shared-config-repo/

   # Check token expiration
   # Default: 24 hours (86400000 ms)
   ```

### Service Logs

**Docker logs:**

```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
docker-compose logs -f employee-service
```

**Local development logs:**
Check console output in each terminal window or IDE console.

### Health Monitoring

```bash
# Health check script
#!/bin/bash
echo "🔍 Checking service health..."

services=(
  "8761:Eureka Discovery"
  "8888:Config Server"
  "8082:Auth Service"
  "8083:Employee Service"
  "8080:API Gateway"
)

for service in "${services[@]}"; do
  port="${service%%:*}"
  name="${service##*:}"

  if curl -s -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
    echo "✅ $name (port $port) - UP"
  else
    echo "❌ $name (port $port) - DOWN"
  fi
done
```

## 💡 For Developers & Assessors

This project demonstrates:

### 🏗️ **Architecture Excellence**

- Clean microservices separation
- Proper service boundaries
- Event-driven communication
- Reactive programming patterns

### 🔐 **Security Best Practices**

- Multi-layered security approach
- JWT-based stateless authentication
- Role-based access control
- Input validation and sanitization

### 📊 **Data Management**

- Database-per-service pattern
- Proper schema design with constraints
- Automated migrations with Flyway
- Event sourcing with Kafka

### 🛠️ **Development Experience**

- One-command deployment
- Hot reloading for development
- Comprehensive API documentation
- Extensive test coverage

### 🚀 **Production Readiness**

- Health checks and monitoring
- Centralized configuration
- Scalable Docker setup
- CI/CD pipeline templates

**Time to start**: < 2 minutes  
**Commands needed**: Just `docker-compose up -d`

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For questions or support, please open an issue in the GitHub repository.
