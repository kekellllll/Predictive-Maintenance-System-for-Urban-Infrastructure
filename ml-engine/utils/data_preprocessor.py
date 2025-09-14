import numpy as np
import pandas as pd
from datetime import datetime, timedelta
import logging

logger = logging.getLogger(__name__)

class DataPreprocessor:
    """
    Data preprocessing utilities for sensor data and infrastructure assets
    """
    
    def __init__(self):
        self.sensor_types = ['temperature', 'vibration', 'pressure', 'humidity', 'strain']
        self.asset_types = ['BRIDGE', 'ROAD', 'BUILDING', 'TUNNEL']
    
    def preprocess_sensor_data(self, sensor_data):
        """
        Preprocess sensor data for ML model input
        """
        if not sensor_data:
            return []
        
        processed_data = []
        
        for record in sensor_data:
            processed_record = {
                'timestamp': record.get('timestamp', datetime.now().isoformat()),
                'temperature': self._validate_sensor_value(record.get('temperature'), 'temperature'),
                'vibration': self._validate_sensor_value(record.get('vibration'), 'vibration'),
                'pressure': self._validate_sensor_value(record.get('pressure'), 'pressure'),
                'humidity': self._validate_sensor_value(record.get('humidity'), 'humidity'),
                'strain': self._validate_sensor_value(record.get('strain'), 'strain')
            }
            processed_data.append(processed_record)
        
        # Sort by timestamp
        processed_data.sort(key=lambda x: x['timestamp'])
        
        return processed_data
    
    def _validate_sensor_value(self, value, sensor_type):
        """
        Validate and clean sensor values
        """
        if value is None:
            return self._get_default_value(sensor_type)
        
        try:
            float_value = float(value)
            
            # Apply sensor-specific validation
            if sensor_type == 'temperature':
                return max(-50, min(100, float_value))
            elif sensor_type == 'vibration':
                return max(0, min(1000, float_value))
            elif sensor_type == 'pressure':
                return max(0, min(200, float_value))
            elif sensor_type == 'humidity':
                return max(0, min(100, float_value))
            elif sensor_type == 'strain':
                return max(0, min(500, float_value))
            else:
                return float_value
                
        except (ValueError, TypeError):
            return self._get_default_value(sensor_type)
    
    def _get_default_value(self, sensor_type):
        """
        Get default values for missing sensor data
        """
        defaults = {
            'temperature': 20.0,
            'vibration': 10.0,
            'pressure': 101.3,
            'humidity': 60.0,
            'strain': 50.0
        }
        return defaults.get(sensor_type, 0.0)
    
    def generate_simulated_data(self, asset_type='BRIDGE', duration_hours=24):
        """
        Generate realistic simulated sensor data for testing
        """
        data = []
        
        # Base values for different asset types
        base_values = {
            'BRIDGE': {
                'temperature': {'mean': 20, 'std': 5, 'trend': 0.1},
                'vibration': {'mean': 15, 'std': 3, 'trend': 0.05},
                'pressure': {'mean': 101.3, 'std': 0.5, 'trend': 0},
                'humidity': {'mean': 65, 'std': 10, 'trend': 0},
                'strain': {'mean': 60, 'std': 8, 'trend': 0.02}
            },
            'ROAD': {
                'temperature': {'mean': 25, 'std': 8, 'trend': 0.2},
                'vibration': {'mean': 25, 'std': 5, 'trend': 0.1},
                'pressure': {'mean': 101.3, 'std': 0.5, 'trend': 0},
                'humidity': {'mean': 55, 'std': 12, 'trend': 0},
                'strain': {'mean': 40, 'std': 6, 'trend': 0.03}
            },
            'BUILDING': {
                'temperature': {'mean': 22, 'std': 3, 'trend': 0.05},
                'vibration': {'mean': 8, 'std': 2, 'trend': 0.02},
                'pressure': {'mean': 101.3, 'std': 0.3, 'trend': 0},
                'humidity': {'mean': 50, 'std': 8, 'trend': 0},
                'strain': {'mean': 30, 'std': 4, 'trend': 0.01}
            },
            'TUNNEL': {
                'temperature': {'mean': 18, 'std': 2, 'trend': 0.02},
                'vibration': {'mean': 12, 'std': 3, 'trend': 0.03},
                'pressure': {'mean': 101.3, 'std': 0.4, 'trend': 0},
                'humidity': {'mean': 80, 'std': 5, 'trend': 0},
                'strain': {'mean': 70, 'std': 10, 'trend': 0.04}
            }
        }
        
        asset_config = base_values.get(asset_type, base_values['BRIDGE'])
        
        # Generate hourly data
        for hour in range(duration_hours):
            timestamp = datetime.now() - timedelta(hours=duration_hours - hour - 1)
            
            record = {
                'timestamp': timestamp.isoformat(),
                'asset_type': asset_type
            }
            
            # Add sensor readings with realistic variations
            for sensor_type in self.sensor_types:
                config = asset_config[sensor_type]
                
                # Base value with normal distribution
                base_value = np.random.normal(config['mean'], config['std'])
                
                # Add time-based trend (degradation over time)
                trend_value = hour * config['trend']
                
                # Add hourly variation (day/night cycle for temperature)
                if sensor_type == 'temperature':
                    hourly_variation = 3 * np.sin(2 * np.pi * (timestamp.hour - 6) / 24)
                    base_value += hourly_variation
                
                # Add occasional anomalies (5% chance)
                if np.random.random() < 0.05:
                    anomaly_factor = np.random.choice([-1, 1]) * np.random.uniform(2, 4)
                    base_value += anomaly_factor * config['std']
                
                final_value = base_value + trend_value
                record[sensor_type] = self._validate_sensor_value(final_value, sensor_type)
            
            data.append(record)
        
        return data
    
    def create_feature_matrix(self, sensor_data, asset_info):
        """
        Create feature matrix for ML model training
        """
        features = []
        
        for record in sensor_data:
            feature_row = [
                record.get('temperature', 20.0),
                record.get('vibration', 10.0),
                record.get('pressure', 101.3),
                record.get('humidity', 60.0),
                record.get('strain', 50.0)
            ]
            
            # Add asset-specific features
            if asset_info:
                feature_row.extend([
                    asset_info.get('age_months', 12),
                    asset_info.get('months_since_maintenance', 6),
                    asset_info.get('asset_type_encoded', 1)
                ])
            
            features.append(feature_row)
        
        return np.array(features)
    
    def detect_anomalies(self, sensor_data, threshold_factor=2.5):
        """
        Simple anomaly detection using statistical methods
        """
        anomalies = []
        
        if len(sensor_data) < 10:
            return anomalies
        
        df = pd.DataFrame(sensor_data)
        
        for sensor_type in self.sensor_types:
            if sensor_type in df.columns:
                values = df[sensor_type].values
                mean_val = np.mean(values)
                std_val = np.std(values)
                
                # Find outliers using z-score
                z_scores = np.abs((values - mean_val) / std_val)
                outlier_indices = np.where(z_scores > threshold_factor)[0]
                
                for idx in outlier_indices:
                    anomalies.append({
                        'timestamp': df.iloc[idx]['timestamp'],
                        'sensor_type': sensor_type,
                        'value': values[idx],
                        'z_score': z_scores[idx],
                        'severity': 'HIGH' if z_scores[idx] > 3 else 'MEDIUM'
                    })
        
        return anomalies
    
    def calculate_health_score(self, sensor_data, asset_type='BRIDGE'):
        """
        Calculate overall health score for an asset based on sensor data
        """
        if not sensor_data:
            return 0.5  # Neutral score
        
        scores = []
        
        # Get recent data (last 24 hours)
        recent_data = sensor_data[-24:] if len(sensor_data) >= 24 else sensor_data
        
        for sensor_type in self.sensor_types:
            values = [record.get(sensor_type, 0) for record in recent_data]
            if values:
                score = self._calculate_sensor_health_score(values, sensor_type, asset_type)
                scores.append(score)
        
        if scores:
            overall_score = np.mean(scores)
        else:
            overall_score = 0.5
        
        return max(0.0, min(1.0, overall_score))
    
    def _calculate_sensor_health_score(self, values, sensor_type, asset_type):
        """
        Calculate health score for a specific sensor type
        """
        if not values:
            return 0.5
        
        mean_val = np.mean(values)
        std_val = np.std(values)
        
        # Define healthy ranges for different sensor types and assets
        healthy_ranges = {
            'BRIDGE': {
                'temperature': (15, 25),
                'vibration': (5, 20),
                'pressure': (100, 102),
                'humidity': (40, 80),
                'strain': (30, 80)
            },
            'ROAD': {
                'temperature': (20, 30),
                'vibration': (10, 30),
                'pressure': (100, 102),
                'humidity': (30, 70),
                'strain': (20, 60)
            },
            'BUILDING': {
                'temperature': (18, 26),
                'vibration': (2, 15),
                'pressure': (100, 102),
                'humidity': (30, 70),
                'strain': (10, 50)
            },
            'TUNNEL': {
                'temperature': (15, 22),
                'vibration': (5, 20),
                'pressure': (100, 102),
                'humidity': (60, 95),
                'strain': (40, 100)
            }
        }
        
        range_config = healthy_ranges.get(asset_type, healthy_ranges['BRIDGE'])
        healthy_min, healthy_max = range_config.get(sensor_type, (0, 100))
        
        # Calculate score based on how many values are within healthy range
        in_range_count = sum(1 for val in values if healthy_min <= val <= healthy_max)
        in_range_ratio = in_range_count / len(values)
        
        # Penalize high variability
        variability_penalty = min(std_val / mean_val, 0.5) if mean_val > 0 else 0
        
        # Calculate final score
        health_score = in_range_ratio - variability_penalty
        
        return max(0.0, min(1.0, health_score))
    
    def get_feature_names(self):
        """
        Get list of feature names for the model
        """
        return [
            'temperature', 'vibration', 'pressure', 'humidity', 'strain',
            'asset_age_months', 'months_since_maintenance', 'asset_type_encoded'
        ]