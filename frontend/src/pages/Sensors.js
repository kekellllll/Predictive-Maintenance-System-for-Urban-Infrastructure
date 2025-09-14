import React, { useState, useEffect } from 'react';
import { SensorService } from '../services/ApiService';
import { InfrastructureService } from '../services/InfrastructureService';

const Sensors = ({ user }) => {
  const [assets, setAssets] = useState([]);
  const [selectedAsset, setSelectedAsset] = useState('');
  const [sensorData, setSensorData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [newSensorData, setNewSensorData] = useState({
    sensorId: '',
    sensorType: 'TEMPERATURE',
    value: '',
    unit: ''
  });

  const sensorTypes = [
    { value: 'TEMPERATURE', label: '温度', unit: '°C' },
    { value: 'VIBRATION', label: '振动', unit: 'mm/s' },
    { value: 'PRESSURE', label: '压力', unit: 'kPa' },
    { value: 'HUMIDITY', label: '湿度', unit: '%' },
    { value: 'STRAIN', label: '应变', unit: 'με' }
  ];

  useEffect(() => {
    loadAssets();
  }, []);

  useEffect(() => {
    if (selectedAsset) {
      loadSensorData();
    }
  }, [selectedAsset]);

  const loadAssets = async () => {
    try {
      const response = await InfrastructureService.getAssets(0, 100);
      setAssets(response.data.content || []);
      
      // Auto-select first asset if available
      if (response.data.content && response.data.content.length > 0) {
        setSelectedAsset(response.data.content[0].assetId);
      }
    } catch (error) {
      setError('加载资产列表失败');
    }
  };

  const loadSensorData = async () => {
    if (!selectedAsset) return;
    
    try {
      setLoading(true);
      
      // Try to get aggregated sensor data for the selected asset
      const response = await SensorService.getAggregatedSensorData(selectedAsset, '1h');
      setSensorData(response.data || []);
    } catch (error) {
      console.log('No sensor data available for asset:', selectedAsset);
      setSensorData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRecordSensorData = async (e) => {
    e.preventDefault();
    
    if (!selectedAsset) {
      setError('请先选择一个资产');
      return;
    }

    try {
      const sensorPayload = {
        assetId: selectedAsset,
        ...newSensorData,
        value: parseFloat(newSensorData.value)
      };

      await SensorService.recordSensorData(sensorPayload);
      
      // Reset form
      setNewSensorData({
        sensorId: '',
        sensorType: 'TEMPERATURE',
        value: '',
        unit: ''
      });
      
      // Reload sensor data
      loadSensorData();
      
      setError('');
      alert('传感器数据记录成功！');
    } catch (error) {
      setError('记录传感器数据失败: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleSimulateData = async () => {
    if (!selectedAsset) {
      setError('请先选择一个资产');
      return;
    }

    try {
      await SensorService.simulateSensorData(selectedAsset);
      loadSensorData();
      alert('模拟传感器数据生成成功！');
    } catch (error) {
      setError('生成模拟数据失败');
    }
  };

  const getSensorTypeLabel = (type) => {
    const sensor = sensorTypes.find(s => s.value === type);
    return sensor ? sensor.label : type;
  };

  const getUnitForSensorType = (type) => {
    const sensor = sensorTypes.find(s => s.value === type);
    return sensor ? sensor.unit : '';
  };

  // Update unit when sensor type changes
  useEffect(() => {
    const unit = getUnitForSensorType(newSensorData.sensorType);
    setNewSensorData(prev => ({ ...prev, unit }));
  }, [newSensorData.sensorType]);

  return (
    <div>
      <h1>📡 传感器数据监控</h1>

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

      {/* Asset Selection */}
      <div className="card">
        <h2>选择资产</h2>
        <div className="form-group">
          <label>基础设施资产</label>
          <select
            value={selectedAsset}
            onChange={(e) => setSelectedAsset(e.target.value)}
          >
            <option value="">请选择资产</option>
            {assets.map(asset => (
              <option key={asset.assetId} value={asset.assetId}>
                {asset.name} ({asset.assetId}) - {asset.type}
              </option>
            ))}
          </select>
        </div>
      </div>

      {selectedAsset && (
        <>
          {/* Record New Sensor Data */}
          <div className="card">
            <h2>记录传感器数据</h2>
            <form onSubmit={handleRecordSensorData}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                <div className="form-group">
                  <label>传感器ID</label>
                  <input
                    type="text"
                    value={newSensorData.sensorId}
                    onChange={(e) => setNewSensorData({...newSensorData, sensorId: e.target.value})}
                    placeholder="例如: TEMP_001"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>传感器类型</label>
                  <select
                    value={newSensorData.sensorType}
                    onChange={(e) => setNewSensorData({...newSensorData, sensorType: e.target.value})}
                  >
                    {sensorTypes.map(type => (
                      <option key={type.value} value={type.value}>
                        {type.label}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>数值</label>
                  <input
                    type="number"
                    step="0.01"
                    value={newSensorData.value}
                    onChange={(e) => setNewSensorData({...newSensorData, value: e.target.value})}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>单位</label>
                  <input
                    type="text"
                    value={newSensorData.unit}
                    onChange={(e) => setNewSensorData({...newSensorData, unit: e.target.value})}
                    readOnly
                  />
                </div>
              </div>
              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button type="submit" className="btn">
                  📝 记录数据
                </button>
                <button 
                  type="button" 
                  className="btn secondary"
                  onClick={handleSimulateData}
                >
                  🎲 生成模拟数据
                </button>
              </div>
            </form>
          </div>

          {/* Sensor Data Display */}
          <div className="card">
            <h2>传感器数据历史</h2>
            {loading ? (
              <div className="loading">正在加载传感器数据...</div>
            ) : sensorData.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
                <p>📊 暂无传感器数据</p>
                <p>请记录一些传感器数据或生成模拟数据来开始监控。</p>
              </div>
            ) : (
              <div>
                <p>显示最近的传感器数据 ({sensorData.length} 条记录)</p>
                <div style={{ overflowX: 'auto' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '1rem' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f5f5f5' }}>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>时间</th>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>传感器</th>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>类型</th>
                        <th style={{ padding: '0.5rem', textAlign: 'left', border: '1px solid #ddd' }}>数值</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sensorData.slice(0, 20).map((record, index) => (
                        <tr key={index}>
                          <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                            {record._time ? new Date(record._time).toLocaleString('zh-CN') : '未知'}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                            {record.sensor_id || '未知'}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                            {getSensorTypeLabel(record.sensor_type || 'UNKNOWN')}
                          </td>
                          <td style={{ padding: '0.5rem', border: '1px solid #ddd', fontSize: '0.9rem' }}>
                            {typeof record._value === 'number' ? record._value.toFixed(2) : record._value || 'N/A'}
                            {record.unit && ` ${record.unit}`}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </div>

          {/* Sensor Statistics */}
          <div className="card">
            <h2>📊 传感器统计</h2>
            <div className="stats-grid">
              <div className="stat-card">
                <h3>{sensorData.length}</h3>
                <p>总数据点</p>
              </div>
              <div className="stat-card">
                <h3>{new Set(sensorData.map(d => d.sensor_type)).size}</h3>
                <p>传感器类型</p>
              </div>
              <div className="stat-card">
                <h3>{new Set(sensorData.map(d => d.sensor_id)).size}</h3>
                <p>传感器设备</p>
              </div>
              <div className="stat-card">
                <h3>
                  {sensorData.length > 0 ? 
                    new Date(Math.max(...sensorData.map(d => new Date(d._time).getTime()))).toLocaleDateString('zh-CN') : 
                    'N/A'
                  }
                </h3>
                <p>最新数据时间</p>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="card">
            <h2>🚀 快速操作</h2>
            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
              <button className="btn secondary" onClick={loadSensorData}>
                🔄 刷新数据
              </button>
              <button 
                className="btn secondary"
                onClick={() => window.location.href = `/predictions?assetId=${selectedAsset}`}
              >
                🔮 查看预测
              </button>
              <button 
                className="btn secondary"
                onClick={() => {
                  if (sensorData.length > 0) {
                    const latest = sensorData[0];
                    alert(`最新传感器数据:\n时间: ${new Date(latest._time).toLocaleString('zh-CN')}\n类型: ${getSensorTypeLabel(latest.sensor_type)}\n数值: ${latest._value}`);
                  } else {
                    alert('暂无传感器数据');
                  }
                }}
              >
                📈 数据概览
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Sensors;