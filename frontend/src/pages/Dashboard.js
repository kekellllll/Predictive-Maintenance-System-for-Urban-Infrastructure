import React, { useState, useEffect } from 'react';
import { InfrastructureService } from '../services/InfrastructureService';
import { PredictionService } from '../services/ApiService';

const Dashboard = ({ user }) => {
  const [stats, setStats] = useState(null);
  const [highRiskPredictions, setHighRiskPredictions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      
      // Load dashboard stats
      const [statsResponse, predictionsResponse] = await Promise.all([
        InfrastructureService.getDashboardStats(),
        PredictionService.getHighRiskPredictions().catch(() => ({ data: [] }))
      ]);

      setStats(statsResponse.data);
      setHighRiskPredictions(predictionsResponse.data || []);
    } catch (error) {
      setError('加载仪表板数据失败');
      console.error('Dashboard data loading error:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">正在加载仪表板数据...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  return (
    <div>
      <h1>📊 系统仪表板</h1>
      
      {/* Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card operational">
          <h3>{stats?.operational || 0}</h3>
          <p>正常运行</p>
        </div>
        <div className="stat-card maintenance">
          <h3>{stats?.maintenance_required || 0}</h3>
          <p>需要维护</p>
        </div>
        <div className="stat-card maintenance">
          <h3>{stats?.under_maintenance || 0}</h3>
          <p>维护中</p>
        </div>
        <div className="stat-card critical">
          <h3>{stats?.critical || 0}</h3>
          <p>严重状态</p>
        </div>
        <div className="stat-card">
          <h3>{stats?.out_of_service || 0}</h3>
          <p>停用</p>
        </div>
      </div>

      {/* System Overview */}
      <div className="card">
        <h2>🏗️ 系统概览</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem' }}>
          <div>
            <h3>主要功能</h3>
            <ul>
              <li>🕒 实时传感器数据监控</li>
              <li>🤖 基于LSTM神经网络的预测分析</li>
              <li>📊 时间序列数据存储 (InfluxDB)</li>
              <li>🔔 事件驱动架构 (RabbitMQ)</li>
              <li>🔐 JWT身份认证</li>
              <li>🗺️ 地理位置可视化</li>
            </ul>
          </div>
          <div>
            <h3>系统架构</h3>
            <ul>
              <li>🌐 React前端 + Spring Boot后端</li>
              <li>📈 InfluxDB时间序列数据库</li>
              <li>🧠 Python机器学习引擎</li>
              <li>🐰 RabbitMQ消息队列</li>
              <li>🐳 Docker容器化部署</li>
              <li>🔧 模块化API网关</li>
            </ul>
          </div>
        </div>
      </div>

      {/* High Risk Predictions */}
      {highRiskPredictions.length > 0 && (
        <div className="card">
          <h2>⚠️ 高风险预测</h2>
          <div className="asset-grid">
            {highRiskPredictions.slice(0, 6).map((prediction, index) => (
              <div key={index} className="asset-card">
                <h4>{prediction.asset?.name || prediction.asset?.assetId}</h4>
                <div className="asset-status status-critical">
                  风险等级: {prediction.riskLevel}
                </div>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem' }}>
                  预测失效时间: {new Date(prediction.predictedFailureDate).toLocaleDateString('zh-CN')}
                </p>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem' }}>
                  置信度: {(prediction.confidenceScore * 100).toFixed(1)}%
                </p>
                <p style={{ margin: '0.5rem 0', fontSize: '0.8rem', color: '#666' }}>
                  {prediction.recommendedAction}
                </p>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      <div className="card">
        <h2>🚀 快速操作</h2>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <button 
            className="btn"
            onClick={() => window.location.href = '/assets'}
          >
            📋 查看所有资产
          </button>
          <button 
            className="btn"
            onClick={() => window.location.href = '/sensors'}
          >
            📡 传感器监控
          </button>
          <button 
            className="btn"
            onClick={() => window.location.href = '/predictions'}
          >
            🔮 预测分析
          </button>
          <button 
            className="btn secondary"
            onClick={loadDashboardData}
          >
            🔄 刷新数据
          </button>
        </div>
      </div>

      {/* Technology Stack */}
      <div className="card">
        <h2>💻 技术栈信息</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
          <div>
            <h4>🎨 前端技术</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>React 18.2</li>
              <li>React Router</li>
              <li>Axios HTTP客户端</li>
              <li>Recharts图表库</li>
              <li>Leaflet地图</li>
            </ul>
          </div>
          <div>
            <h4>⚙️ 后端技术</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>Spring Boot 3.1</li>
              <li>Spring Security + JWT</li>
              <li>Spring Cloud Gateway</li>
              <li>Spring AMQP</li>
              <li>JPA/Hibernate</li>
            </ul>
          </div>
          <div>
            <h4>🗄️ 数据存储</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>InfluxDB (时间序列)</li>
              <li>H2数据库 (开发)</li>
              <li>RabbitMQ (消息队列)</li>
            </ul>
          </div>
          <div>
            <h4>🤖 机器学习</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>Python + Flask</li>
              <li>TensorFlow + LSTM</li>
              <li>scikit-learn</li>
              <li>NumPy + Pandas</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;