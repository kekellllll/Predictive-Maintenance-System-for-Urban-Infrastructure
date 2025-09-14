import React, { useState, useEffect } from 'react';
import { InfrastructureService } from '../services/InfrastructureService';

const Assets = ({ user }) => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newAsset, setNewAsset] = useState({
    assetId: '',
    name: '',
    type: 'BRIDGE',
    description: '',
    latitude: '',
    longitude: ''
  });

  const assetTypes = ['BRIDGE', 'ROAD', 'BUILDING', 'TUNNEL'];
  const statusTypes = ['OPERATIONAL', 'MAINTENANCE_REQUIRED', 'UNDER_MAINTENANCE', 'CRITICAL', 'OUT_OF_SERVICE'];

  useEffect(() => {
    loadAssets();
  }, []);

  const loadAssets = async () => {
    try {
      setLoading(true);
      const response = await InfrastructureService.getAssets(0, 50);
      setAssets(response.data.content || []);
    } catch (error) {
      setError('加载资产数据失败');
      console.error('Assets loading error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAsset = async (e) => {
    e.preventDefault();
    try {
      const assetData = {
        ...newAsset,
        latitude: parseFloat(newAsset.latitude),
        longitude: parseFloat(newAsset.longitude),
        installationDate: new Date().toISOString()
      };
      
      await InfrastructureService.createAsset(assetData);
      setShowCreateForm(false);
      setNewAsset({
        assetId: '',
        name: '',
        type: 'BRIDGE',
        description: '',
        latitude: '',
        longitude: ''
      });
      loadAssets();
    } catch (error) {
      setError('创建资产失败: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleStatusChange = async (assetId, newStatus) => {
    try {
      await InfrastructureService.updateAssetStatus(assetId, newStatus);
      loadAssets();
    } catch (error) {
      setError('更新状态失败');
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'OPERATIONAL': return 'status-operational';
      case 'MAINTENANCE_REQUIRED': 
      case 'UNDER_MAINTENANCE': return 'status-maintenance';
      case 'CRITICAL': 
      case 'OUT_OF_SERVICE': return 'status-critical';
      default: return '';
    }
  };

  const getStatusText = (status) => {
    const statusMap = {
      'OPERATIONAL': '正常运行',
      'MAINTENANCE_REQUIRED': '需要维护',
      'UNDER_MAINTENANCE': '维护中',
      'CRITICAL': '严重状态',
      'OUT_OF_SERVICE': '停用'
    };
    return statusMap[status] || status;
  };

  const getTypeText = (type) => {
    const typeMap = {
      'BRIDGE': '桥梁',
      'ROAD': '道路',
      'BUILDING': '建筑',
      'TUNNEL': '隧道'
    };
    return typeMap[type] || type;
  };

  if (loading) {
    return <div className="loading">正在加载资产数据...</div>;
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>🏗️ 基础设施资产管理</h1>
        {(user.role === 'ADMIN' || user.role === 'MANAGER') && (
          <button 
            className="btn"
            onClick={() => setShowCreateForm(!showCreateForm)}
          >
            {showCreateForm ? '取消' : '+ 添加资产'}
          </button>
        )}
      </div>

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

      {/* Create Asset Form */}
      {showCreateForm && (
        <div className="card">
          <h2>添加新资产</h2>
          <form onSubmit={handleCreateAsset}>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
              <div className="form-group">
                <label>资产ID</label>
                <input
                  type="text"
                  value={newAsset.assetId}
                  onChange={(e) => setNewAsset({...newAsset, assetId: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>名称</label>
                <input
                  type="text"
                  value={newAsset.name}
                  onChange={(e) => setNewAsset({...newAsset, name: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>类型</label>
                <select
                  value={newAsset.type}
                  onChange={(e) => setNewAsset({...newAsset, type: e.target.value})}
                >
                  {assetTypes.map(type => (
                    <option key={type} value={type}>{getTypeText(type)}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>纬度</label>
                <input
                  type="number"
                  step="0.000001"
                  value={newAsset.latitude}
                  onChange={(e) => setNewAsset({...newAsset, latitude: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>经度</label>
                <input
                  type="number"
                  step="0.000001"
                  value={newAsset.longitude}
                  onChange={(e) => setNewAsset({...newAsset, longitude: e.target.value})}
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label>描述</label>
              <textarea
                value={newAsset.description}
                onChange={(e) => setNewAsset({...newAsset, description: e.target.value})}
                rows={3}
              />
            </div>
            <button type="submit" className="btn">创建资产</button>
          </form>
        </div>
      )}

      {/* Assets List */}
      <div className="card">
        <h2>资产列表 ({assets.length})</h2>
        
        {assets.length === 0 ? (
          <p>暂无资产数据。请添加一些基础设施资产开始监控。</p>
        ) : (
          <div className="asset-grid">
            {assets.map(asset => (
              <div key={asset.id} className="asset-card">
                <h4>{asset.name}</h4>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem', color: '#666' }}>
                  ID: {asset.assetId}
                </p>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem' }}>
                  类型: {getTypeText(asset.type)}
                </p>
                <div style={{ margin: '0.5rem 0' }}>
                  <span className={`asset-status ${getStatusClass(asset.status)}`}>
                    {getStatusText(asset.status)}
                  </span>
                </div>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem' }}>
                  📍 {asset.latitude?.toFixed(4)}, {asset.longitude?.toFixed(4)}
                </p>
                <p style={{ margin: '0.5rem 0', fontSize: '0.9rem' }}>
                  优先级: {asset.maintenancePriority || 0}/10
                </p>
                {asset.description && (
                  <p style={{ margin: '0.5rem 0', fontSize: '0.8rem', color: '#666' }}>
                    {asset.description}
                  </p>
                )}
                
                {/* Status Update */}
                {(user.role === 'ADMIN' || user.role === 'MANAGER') && (
                  <div style={{ marginTop: '1rem' }}>
                    <select
                      value={asset.status}
                      onChange={(e) => handleStatusChange(asset.assetId, e.target.value)}
                      style={{ fontSize: '0.8rem', padding: '0.25rem' }}
                    >
                      {statusTypes.map(status => (
                        <option key={status} value={status}>
                          {getStatusText(status)}
                        </option>
                      ))}
                    </select>
                  </div>
                )}
                
                <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
                  <button 
                    className="btn" 
                    style={{ fontSize: '0.8rem', padding: '0.25rem 0.5rem' }}
                    onClick={() => window.location.href = `/sensors?assetId=${asset.assetId}`}
                  >
                    📡 传感器
                  </button>
                  <button 
                    className="btn secondary" 
                    style={{ fontSize: '0.8rem', padding: '0.25rem 0.5rem' }}
                    onClick={() => window.location.href = `/predictions?assetId=${asset.assetId}`}
                  >
                    🔮 预测
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="card">
        <h2>🚀 快速操作</h2>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <button className="btn secondary" onClick={loadAssets}>
            🔄 刷新列表
          </button>
          <button 
            className="btn secondary"
            onClick={() => {
              const criticalAssets = assets.filter(a => a.status === 'CRITICAL');
              if (criticalAssets.length > 0) {
                alert(`发现 ${criticalAssets.length} 个严重状态资产`);
              } else {
                alert('所有资产状态正常');
              }
            }}
          >
            ⚠️ 检查严重状态
          </button>
          <button 
            className="btn secondary"
            onClick={() => {
              const maintenanceAssets = assets.filter(a => 
                a.status === 'MAINTENANCE_REQUIRED' || a.status === 'UNDER_MAINTENANCE'
              );
              alert(`${maintenanceAssets.length} 个资产需要或正在维护`);
            }}
          >
            🔧 维护统计
          </button>
        </div>
      </div>
    </div>
  );
};

export default Assets;