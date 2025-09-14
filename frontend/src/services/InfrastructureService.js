import axios from 'axios';

export const InfrastructureService = {
  // Get all assets
  getAssets: (page = 0, size = 20) => {
    return axios.get(`/api/infrastructure/assets?page=${page}&size=${size}`);
  },

  // Get asset by ID
  getAssetById: (id) => {
    return axios.get(`/api/infrastructure/assets/${id}`);
  },

  // Get asset by asset ID
  getAssetByAssetId: (assetId) => {
    return axios.get(`/api/infrastructure/assets/asset-id/${assetId}`);
  },

  // Create new asset
  createAsset: (assetData) => {
    return axios.post('/api/infrastructure/assets', assetData);
  },

  // Update asset
  updateAsset: (id, assetData) => {
    return axios.put(`/api/infrastructure/assets/${id}`, assetData);
  },

  // Update asset status
  updateAssetStatus: (assetId, status) => {
    return axios.patch(`/api/infrastructure/assets/${assetId}/status`, { status });
  },

  // Schedule maintenance
  scheduleMaintenance: (assetId, scheduledDate) => {
    return axios.post(`/api/infrastructure/assets/${assetId}/schedule-maintenance`, {
      scheduledDate: scheduledDate.toISOString()
    });
  },

  // Delete asset
  deleteAsset: (id) => {
    return axios.delete(`/api/infrastructure/assets/${id}`);
  },

  // Get assets by type
  getAssetsByType: (type) => {
    return axios.get(`/api/infrastructure/assets/type/${type}`);
  },

  // Get assets by status
  getAssetsByStatus: (status) => {
    return axios.get(`/api/infrastructure/assets/status/${status}`);
  },

  // Get high priority assets
  getHighPriorityAssets: (priority = 5) => {
    return axios.get(`/api/infrastructure/assets/high-priority?priority=${priority}`);
  },

  // Get assets near location
  getAssetsNearLocation: (latitude, longitude, radiusKm = 10) => {
    return axios.get('/api/infrastructure/assets/nearby', {
      params: { latitude, longitude, radiusKm }
    });
  },

  // Get dashboard stats
  getDashboardStats: () => {
    return axios.get('/api/infrastructure/dashboard/stats');
  }
};