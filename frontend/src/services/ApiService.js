import axios from 'axios';

export const SensorService = {
  // Record sensor data
  recordSensorData: (sensorData) => {
    return axios.post('/api/sensors/data', sensorData);
  },

  // Record batch sensor data
  recordSensorDataBatch: (sensorDataList) => {
    return axios.post('/api/sensors/data/batch', sensorDataList);
  },

  // Get sensor data history
  getSensorDataHistory: (assetId, sensorType, timeRange = '-24h') => {
    return axios.get(`/api/sensors/data/${assetId}/history`, {
      params: { sensorType, timeRange }
    });
  },

  // Get aggregated sensor data
  getAggregatedSensorData: (assetId, aggregationWindow = '1h') => {
    return axios.get(`/api/sensors/data/${assetId}/aggregated`, {
      params: { aggregationWindow }
    });
  },

  // Simulate sensor data
  simulateSensorData: (assetId) => {
    return axios.post(`/api/sensors/simulate/${assetId}`);
  }
};

export const PredictionService = {
  // Trigger prediction for asset
  triggerPrediction: (assetId) => {
    return axios.post(`/api/predictions/trigger/${assetId}`);
  },

  // Get predictions for asset
  getPredictionsForAsset: (assetId) => {
    return axios.get(`/api/predictions/asset/${assetId}`);
  },

  // Get high risk predictions
  getHighRiskPredictions: () => {
    return axios.get('/api/predictions/high-risk');
  },

  // Get predictions in date range
  getPredictionsInDateRange: (startDate, endDate) => {
    return axios.get('/api/predictions/date-range', {
      params: {
        startDate: startDate.toISOString(),
        endDate: endDate.toISOString()
      }
    });
  }
};