# Urban Infrastructure Predictive Maintenance System

A full-stack application for monitoring urban infrastructure and performing predictive maintenance, built with modern microservices architecture and machine learning technologies.

## System Architecture

### Technology Stack
- **Backend**: Spring Boot + Java
- **Frontend**: React + TypeScript
- **Database**: InfluxDB (Time-series data)
- **Message Queue**: RabbitMQ
- **Machine Learning**: Python + LSTM + scikit-learn
- **Security**: JWT Authentication
- **Containerization**: Docker + Docker Compose

### Key Features
1. **Time-series Data Storage**: Optimized high-frequency sensor data storage using InfluxDB
2. **Predictive Analytics Engine**: LSTM neural network-based maintenance predictions
3. **Modular API Gateway**: JWT authentication ensures data security
4. **Event-driven Architecture**: RabbitMQ handles distributed sensor events (10,000+ daily)

## Project Structure
```
├── backend/          # Spring Boot backend service
├── frontend/         # React frontend application
├── ml-engine/        # Python machine learning engine
├── docker/           # Docker configuration files
├── config/           # Configuration files
└── docs/             # Project documentation
```

## Quick Start
```bash
# Start all services
docker-compose up -d

# Access frontend application
http://localhost:3000

# Access backend API
http://localhost:8080/api
```