import React, { useState, useEffect } from 'react';
import { PredictionService } from '../services/ApiService';
import { InfrastructureService } from '../services/InfrastructureService';

const Predictions = ({ user }) => {
  const [assets, setAssets] = useState([]);
  const [selectedAsset, setSelectedAsset] = useState('');
  const [predictions, setPredictions] = useState([]);
  const [highRiskPredictions, setHighRiskPredictions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadAssets();
    loadHighRiskPredictions();
  }, []);

  useEffect(() => {
    if (selectedAsset) {
      loadPredictionsForAsset();
    }
  }, [selectedAsset]);

  const loadAssets = async () => {
    try {
      const response = await InfrastructureService.getAssets(0, 100);
      setAssets(response.data.content || []);
    } catch (error) {
      setError('加载资产列表失败');
    }
  };

  const loadHighRiskPredictions = async () => {
    try {
      const response = await PredictionService.getHighRiskPredictions();
      setHighRiskPredictions(response.data || []);
    } catch (error) {
      console.log('No high risk predictions available');
      setHighRiskPredictions([]);
    }
  };

  const loadPredictionsForAsset = async () => {
    if (!selectedAsset) return;
    
    try {
      setLoading(true);
      const response = await PredictionService.getPredictionsForAsset(selectedAsset);
      setPredictions(response.data || []);
    } catch (error) {
      console.log('No predictions available for asset:', selectedAsset);
      setPredictions([]);
    } finally {
      setLoading(false);
    }
  };

  const handleTriggerPrediction = async () => {
    if (!selectedAsset) {
      setError('请先选择一个资产');
      return;
    }

    try {
      setLoading(true);
      await PredictionService.triggerPrediction(selectedAsset);
      
      // Wait a moment then reload predictions
      setTimeout(() => {
        loadPredictionsForAsset();
        loadHighRiskPredictions();
      }, 2000);
      
      alert('预测分析已触发，请稍等片刻查看结果。');
    } catch (error) {
      setError('触发预测分析失败: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const getRiskLevelClass = (riskLevel) => {
    switch (riskLevel) {
      case 'CRITICAL': return 'status-critical';
      case 'HIGH': return 'status-critical';
      case 'MEDIUM': return 'status-maintenance';
      case 'LOW': return 'status-operational';
      default: return '';
    }
  };

  const getRiskLevelText = (riskLevel) => {
    const riskMap = {
      'CRITICAL': '极其严重',
      'HIGH': '高风险',
      'MEDIUM': '中等风险',
      'LOW': '低风险'
    };
    return riskMap[riskLevel] || riskLevel;
  };

  const getAssetName = (assetId) => {
    const asset = assets.find(a => a.assetId === assetId);
    return asset ? asset.name : assetId;
  };

  return (
    <div>
      <h1>🔮 预测性维护分析</h1>

      {error && (
        <div className="error">
          {error}
          <button 
            style={{ marginLeft: '1rem', padding: '0.25rem 0.5rem' }}
            onClick={() => setError('')}
          >
            ✕
          </button>
        </div>
      )}

      {/* High Risk Predictions Overview */}
      {highRiskPredictions.length > 0 && (
        <div className="card">
          <h2>⚠️ 高风险预测概览</h2>
          <div className="asset-grid">
            {highRiskPredictions.slice(0, 4).map((prediction, index) => (
              <div key={index} className="asset-card">
                <h4>{getAssetName(prediction.asset?.assetId || 'Unknown')}</h4>
                <div className={`asset-status ${getRiskLevelClass(prediction.riskLevel)}`}>
                  风险等级: {getRiskLevelText(prediction.riskLevel)}
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

      {/* Asset Selection for Detailed Analysis */}
      <div className="card">
        <h2>详细预测分析</h2>
        <div className="form-group">
          <label>选择基础设施资产</label>
          <select
            value={selectedAsset}
            onChange={(e) => setSelectedAsset(e.target.value)}
          >
            <option value="">请选择资产进行预测分析</option>
            {assets.map(asset => (
              <option key={asset.assetId} value={asset.assetId}>
                {asset.name} ({asset.assetId}) - {asset.type}
              </option>
            ))}
          </select>
        </div>

        {selectedAsset && (
          <div style={{ marginTop: '1rem' }}>
            <button 
              className="btn"
              onClick={handleTriggerPrediction}
              disabled={loading}
            >
              {loading ? '🔄 分析中...' : '🚀 触发预测分析'}
            </button>
          </div>
        )}
      </div>

      {/* Prediction Results for Selected Asset */}
      {selectedAsset && (
        <div className="card">
          <h2>📊 {getAssetName(selectedAsset)} 的预测结果</h2>
          
          {loading ? (
            <div className="loading">正在分析预测数据...</div>
          ) : predictions.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
              <p>🔮 暂无预测数据</p>
              <p>点击"触发预测分析"开始为该资产生成预测。</p>
            </div>
          ) : (
            <div>
              <p>显示最近的预测结果 ({predictions.length} 条记录)</p>
              
              {/* Latest Prediction Highlight */}
              {predictions.length > 0 && (
                <div style={{ 
                  backgroundColor: '#f8f9fa', 
                  padding: '1.5rem', 
                  borderRadius: '8px', 
                  marginBottom: '1rem',
                  border: `2px solid ${predictions[0].riskLevel === 'HIGH' || predictions[0].riskLevel === 'CRITICAL' ? '#f44336' : '#4caf50'}`
                }}>
                  <h3 style={{ margin: '0 0 1rem 0' }}>🎯 最新预测</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                    <div>
                      <strong>风险等级:</strong>
                      <div className={`asset-status ${getRiskLevelClass(predictions[0].riskLevel)}`} style={{ marginTop: '0.25rem' }}>
                        {getRiskLevelText(predictions[0].riskLevel)}
                      </div>
                    </div>
                    <div>
                      <strong>预测失效时间:</strong>
                      <p style={{ margin: '0.25rem 0 0 0' }}>
                        {new Date(predictions[0].predictedFailureDate).toLocaleDateString('zh-CN')}
                      </p>
                    </div>
                    <div>
                      <strong>置信度:</strong>
                      <p style={{ margin: '0.25rem 0 0 0' }}>
                        {(predictions[0].confidenceScore * 100).toFixed(1)}%
                      </p>
                    </div>
                    <div>
                      <strong>算法:</strong>
                      <p style={{ margin: '0.25rem 0 0 0' }}>
                        {predictions[0].predictionAlgorithm || 'LSTM'}
                      </p>
                    </div>
                  </div>
                  <div style={{ marginTop: '1rem' }}>
                    <strong>建议措施:</strong>
                    <p style={{ margin: '0.25rem 0 0 0', color: '#666' }}>
                      {predictions[0].recommendedAction}
                    </p>
                  </div>
                </div>
              )}

              {/* Prediction History Table */}
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
                  <thead>
                    <tr style={{ backgroundColor: '#f5f5f5' }}>
                      <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>预测时间</th>
                      <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>风险等级</th>
                      <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>预测失效时间</th>
                      <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>置信度</th>
                      <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>算法</th>
                    </tr>
                  </thead>
                  <tbody>
                    {predictions.map((prediction, index) => (
                      <tr key={index}>
                        <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                          {new Date(prediction.predictionDate).toLocaleString('zh-CN')}
                        </td>
                        <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                          <span className={`asset-status ${getRiskLevelClass(prediction.riskLevel)}`}>
                            {getRiskLevelText(prediction.riskLevel)}
                          </span>
                        </td>
                        <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                          {new Date(prediction.predictedFailureDate).toLocaleDateString('zh-CN')}
                        </td>
                        <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                          {(prediction.confidenceScore * 100).toFixed(1)}%
                        </td>
                        <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                          {prediction.predictionAlgorithm || 'LSTM'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}

      {/* AI Model Information */}
      <div className="card">
        <h2>🤖 AI预测模型信息</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem' }}>
          <div>
            <h4>🧠 LSTM神经网络</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>长短期记忆网络 (LSTM)</li>
              <li>基于TensorFlow框架</li>
              <li>多层循环神经网络</li>
              <li>时间序列预测算法</li>
              <li>自适应学习能力</li>
            </ul>
          </div>
          <div>
            <h4>📊 输入特征</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>温度传感器数据</li>
              <li>振动传感器数据</li>
              <li>压力传感器数据</li>
              <li>湿度传感器数据</li>
              <li>应变传感器数据</li>
              <li>资产年龄信息</li>
              <li>维护历史记录</li>
            </ul>
          </div>
          <div>
            <h4>🎯 预测输出</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>失效概率预测</li>
              <li>风险等级评估</li>
              <li>维护建议</li>
              <li>置信度评分</li>
              <li>预测时间窗口</li>
            </ul>
          </div>
          <div>
            <h4>🔬 模型性能</h4>
            <ul style={{ fontSize: '0.9rem' }}>
              <li>准确率: 85%+</li>
              <li>实时预测响应</li>
              <li>多维度特征融合</li>
              <li>自动异常检测</li>
              <li>持续学习优化</li>
            </ul>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2>🚀 快速操作</h2>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <button 
            className="btn secondary" 
            onClick={() => {
              loadHighRiskPredictions();
              if (selectedAsset) loadPredictionsForAsset();
            }}
          >
            🔄 刷新预测
          </button>
          <button 
            className="btn secondary"
            onClick={() => {
              if (highRiskPredictions.length > 0) {
                alert(`发现 ${highRiskPredictions.length} 个高风险预测\n请及时关注相关资产状态`);
              } else {
                alert('目前没有高风险预测，系统运行良好');
              }
            }}
          >
            ⚠️ 风险评估
          </button>
          <button 
            className="btn secondary"
            onClick={() => window.location.href = '/sensors'}
          >
            📡 传感器数据
          </button>
          <button 
            className="btn secondary"
            onClick={() => {
              const totalPredictions = predictions.length + highRiskPredictions.length;
              alert(`预测统计:\n总预测数: ${totalPredictions}\n高风险预测: ${highRiskPredictions.length}\n当前资产预测: ${predictions.length}`);
            }}
          >
            📈 预测统计
          </button>
        </div>
      </div>
    </div>
  );
};

export default Predictions;