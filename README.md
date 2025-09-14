# 城市基础设施预测性维护系统 (Urban Infrastructure Predictive Maintenance System)

这是一个全栈应用，用于监控城市基础设施并进行预测性维护，采用现代化的微服务架构和机器学习技术。

## 系统架构 (System Architecture)

### 技术栈 (Technology Stack)
- **后端 (Backend)**: Spring Boot + Java
- **前端 (Frontend)**: React + TypeScript
- **数据库 (Database)**: InfluxDB (时间序列数据)
- **消息队列 (Message Queue)**: RabbitMQ
- **机器学习 (Machine Learning)**: Python + LSTM + scikit-learn
- **安全认证 (Security)**: JWT Authentication
- **容器化 (Containerization)**: Docker + Docker Compose

### 主要功能 (Key Features)
1. **时间序列数据存储**: 使用 InfluxDB 优化高频传感器数据存储
2. **预测分析引擎**: 基于 LSTM 神经网络的维护预测
3. **模块化 API 网关**: JWT 认证保障数据安全
4. **事件驱动架构**: RabbitMQ 处理分布式传感器事件 (10,000+ 每日)

## 项目结构 (Project Structure)
```
├── backend/          # Spring Boot 后端服务
├── frontend/         # React 前端应用
├── ml-engine/        # Python 机器学习引擎
├── docker/           # Docker 配置文件
├── config/           # 配置文件
└── docs/             # 项目文档
```

## 快速开始 (Quick Start)
```bash
# 启动所有服务
docker-compose up -d

# 访问前端应用
http://localhost:3000

# 访问后端 API
http://localhost:8080/api
```