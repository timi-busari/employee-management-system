# Security Configuration Guide

## 🔒 Environment Variables Setup

This project uses environment variables to secure sensitive credentials. **Never commit real credentials to version control.**

### 1. Create Environment File

Copy the example environment file:
```bash
cp .env.example .env
```

### 2. Update Environment Variables

Edit `.env` with your actual credentials:

```bash
# Database Configuration
POSTGRES_DB=your_actual_database_name
POSTGRES_USER=your_secure_username
POSTGRES_PASSWORD=your_very_secure_password

# JWT Configuration  
JWT_SECRET=your_256_bit_or_longer_jwt_secret_key_here
JWT_EXPIRATION=86400000

# Database URLs
AUTH_DB_URL=jdbc:postgresql://postgres:5432/auth_service_db
AUTH_DB_USERNAME=your_secure_username
AUTH_DB_PASSWORD=your_very_secure_password

EMPLOYEE_DB_URL=jdbc:postgresql://postgres:5432/employee_service_db
EMPLOYEE_DB_USERNAME=your_secure_username
EMPLOYEE_DB_PASSWORD=your_very_secure_password
```

### 3. Generate Secure JWT Secret

Use a strong JWT secret (256+ bits):
```bash
# Generate random secret
openssl rand -base64 64
```

### 4. Production Deployment

For production environments:

1. **Use external secret management** (AWS Secrets Manager, Azure Key Vault, etc.)
2. **Set environment variables** in your deployment platform
3. **Never use default passwords** 
4. **Use strong, unique passwords** for each service
5. **Rotate secrets regularly**

### 5. Local Development

For local development, the system will fall back to default values if environment variables are not set, but **change these defaults for any non-development environment**.

## 🚨 Security Checklist

- [ ] `.env` file is in `.gitignore`
- [ ] No hardcoded credentials in source code
- [ ] JWT secret is 256+ bits and randomly generated
- [ ] Database passwords are strong and unique
- [ ] Production uses external secret management
- [ ] Environment-specific configurations are isolated

## ⚠️ Never Commit

- Real database passwords
- Production JWT secrets
- API keys or tokens
- `.env` files with real credentials
- Any sensitive configuration data