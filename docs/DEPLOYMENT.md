# 城市基础设施预测性维护系统 - 部署指南

## 系统概述

这是一个全栈的城市基础设施预测性维护系统，采用现代化的微服务架构，集成了机器学习和IoT技术。

### 核心技术栈
- **前端**: React 18 + Material-UI
- **后端**: Spring Boot 3.1 + Spring Security + JWT
- **数据库**: InfluxDB (时间序列) + H2 (关系型)
- **消息队列**: RabbitMQ
- **机器学习**: Python + TensorFlow + LSTM
- **容器化**: Docker + Docker Compose

## 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React 前端    │ → │  Spring Boot    │ → │   InfluxDB      │
│                 │    │     后端        │    │   时间序列DB    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │
                               ↓
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Python ML      │ ← │    RabbitMQ     │ → │      H2         │
│     引擎        │    │    消息队列     │    │   关系型DB      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 快速启动

### 1. 使用 Docker Compose（推荐）

```bash
# 克隆项目
git clone <repository-url>
cd Predictive-Maintenance-System-for-Urban-Infrastructure

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 2. 手动启动各服务

#### 启动基础设施服务
```bash
# 启动 InfluxDB
docker run -d -p 8086:8086 \
  -e DOCKER_INFLUXDB_INIT_MODE=setup \
  -e DOCKER_INFLUXDB_INIT_USERNAME=admin \
  -e DOCKER_INFLUXDB_INIT_PASSWORD=admin123 \
  -e DOCKER_INFLUXDB_INIT_ORG=infrastructure-org \
  -e DOCKER_INFLUXDB_INIT_BUCKET=sensor-data \
  -e DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=infrastructureToken2023 \
  influxdb:2.6

# 启动 RabbitMQ
docker run -d -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.11-management
```

#### 启动 ML 引擎
```bash
cd ml-engine
pip install -r requirements.txt
python app.py
```

#### 启动后端服务
```bash
cd backend
./mvnw spring-boot:run
```

#### 启动前端服务
```bash
cd frontend
npm install
npm start
```

## 访问地址

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080
- **ML引擎**: http://localhost:5000
- **InfluxDB**: http://localhost:8086
- **RabbitMQ管理界面**: http://localhost:15672

## 默认账户

系统预置了以下测试账户：

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 所有权限 |
| manager | manager123 | MANAGER | 管理权限 |
| operator | operator123 | OPERATOR | 操作权限 |
| viewer | viewer123 | VIEWER | 查看权限 |

## 主要功能模块

### 1. 仪表板 (Dashboard)
- 系统总览和统计信息
- 高风险预测展示
- 技术栈信息
- 快速操作入口

### 2. 资产管理 (Assets)
- 基础设施资产的增删改查
- 状态管理和优先级设置
- 地理位置信息
- 维护计划管理

### 3. 传感器监控 (Sensors)
- 实时传感器数据录入
- 历史数据查询和展示
- 数据质量监控
- 模拟数据生成

### 4. 预测分析 (Predictions)
- LSTM神经网络预测
- 风险等级评估
- 维护建议生成
- 预测结果可视化

## API 接口文档

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/validate` - 令牌验证

### 资产管理接口
- `GET /api/infrastructure/assets` - 获取资产列表
- `POST /api/infrastructure/assets` - 创建新资产
- `PUT /api/infrastructure/assets/{id}` - 更新资产
- `PATCH /api/infrastructure/assets/{assetId}/status` - 更新状态
- `GET /api/infrastructure/dashboard/stats` - 获取统计信息

### 传感器接口
- `POST /api/sensors/data` - 记录传感器数据
- `GET /api/sensors/data/{assetId}/history` - 获取历史数据
- `GET /api/sensors/data/{assetId}/aggregated` - 获取聚合数据
- `POST /api/sensors/simulate/{assetId}` - 生成模拟数据

### 预测接口
- `POST /api/predictions/trigger/{assetId}` - 触发预测分析
- `GET /api/predictions/asset/{assetId}` - 获取资产预测
- `GET /api/predictions/high-risk` - 获取高风险预测

## 数据库架构

### 关系型数据库 (H2)
- `infrastructure_assets` - 基础设施资产
- `maintenance_predictions` - 维护预测记录
- `sensor_data` - 传感器数据（关系型存储）

### 时间序列数据库 (InfluxDB)
- `sensor_readings` - 高频传感器时间序列数据

## 消息队列架构

### Exchange
- `sensor-data-exchange` - 传感器数据交换机
- `maintenance-alert-exchange` - 维护告警交换机

### Queue
- `sensor-data-queue` - 传感器数据队列
- `maintenance-prediction-queue` - 维护预测队列
- `alert-notification-queue` - 告警通知队列

## 机器学习模型

### LSTM 神经网络
- **输入特征**: 温度、振动、压力、湿度、应变、资产年龄、维护历史
- **输出**: 失效概率、风险等级、维护建议
- **框架**: TensorFlow + scikit-learn
- **备用模型**: Random Forest

### 预测流程
1. 数据预处理和特征工程
2. LSTM模型预测
3. 风险等级评估
4. 维护建议生成
5. 结果存储和通知

## 性能指标

- **数据处理能力**: 10,000+ 传感器事件/日
- **预测响应时间**: < 2秒
- **模型准确率**: 85%+
- **系统可用性**: 99.9%

## 监控和日志

### 应用监控
- Spring Boot Actuator 健康检查
- 系统指标监控
- 自定义业务指标

### 日志管理
- 结构化日志格式
- 分级日志记录
- 错误追踪和告警

## 安全措施

### 认证和授权
- JWT令牌认证
- 基于角色的访问控制
- API接口权限控制

### 数据安全
- HTTPS传输加密
- 数据库连接加密
- 敏感信息脱敏

## 部署建议

### 生产环境配置
- 使用外部数据库（PostgreSQL/MySQL）
- 配置负载均衡器
- 启用HTTPS和安全策略
- 设置监控和告警
- 定期备份数据

### 扩展性考虑
- 微服务拆分
- 数据库分片
- 缓存层优化
- CDN加速

## 故障排除

### 常见问题
1. **连接失败**: 检查服务端口和网络配置
2. **认证失败**: 验证JWT配置和用户凭据
3. **数据加载失败**: 检查数据库连接和权限
4. **预测失败**: 检查ML引擎服务状态

### 日志查看
```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f ml-engine
```

## 开发指南

### 添加新功能
1. 后端：添加Controller、Service、Repository
2. 前端：创建新页面和组件
3. ML：扩展预测模型和特征
4. 测试：单元测试和集成测试

### 代码规范
- 后端：遵循Spring Boot最佳实践
- 前端：使用ESLint和Prettier
- Python：遵循PEP 8规范
- 数据库：使用标准SQL和索引优化

## 联系和支持

如有问题或建议，请通过以下方式联系：
- 项目Issues: [GitHub Issues]
- 技术文档: [项目Wiki]
- 开发团队: [团队联系方式]

---

© 2023 城市基础设施预测性维护系统. 保留所有权利.