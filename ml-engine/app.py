from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
import pandas as pd
from datetime import datetime, timedelta
import joblib
import logging
from models.lstm_predictor import LSTMPredictor
from utils.data_preprocessor import DataPreprocessor

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Initialize ML components
lstm_predictor = LSTMPredictor()
data_preprocessor = DataPreprocessor()

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'ML Prediction Engine',
        'version': '1.0.0',
        'timestamp': datetime.now().isoformat()
    })

@app.route('/predict', methods=['POST'])
def predict_maintenance():
    """
    Main prediction endpoint for infrastructure maintenance
    """
    try:
        data = request.get_json()
        
        # Extract asset information
        asset_id = data.get('asset_id')
        asset_type = data.get('asset_type')
        installation_date = data.get('installation_date')
        last_maintenance = data.get('last_maintenance')
        sensor_data = data.get('sensor_data', [])
        
        logger.info(f"Prediction request for asset: {asset_id}")
        
        # Preprocess sensor data
        processed_data = data_preprocessor.preprocess_sensor_data(sensor_data)
        
        # Generate prediction using LSTM model
        prediction_result = lstm_predictor.predict(
            asset_id=asset_id,
            asset_type=asset_type,
            sensor_data=processed_data,
            installation_date=installation_date,
            last_maintenance=last_maintenance
        )
        
        return jsonify(prediction_result)
        
    except Exception as e:
        logger.error(f"Error in prediction: {str(e)}")
        return jsonify({
            'error': 'Prediction failed',
            'message': str(e)
        }), 500

@app.route('/train', methods=['POST'])
def train_model():
    """
    Endpoint to retrain the LSTM model with new data
    """
    try:
        data = request.get_json()
        training_data = data.get('training_data', [])
        
        logger.info("Starting model training...")
        
        # Train the LSTM model
        training_result = lstm_predictor.train(training_data)
        
        return jsonify({
            'status': 'success',
            'message': 'Model training completed',
            'metrics': training_result
        })
        
    except Exception as e:
        logger.error(f"Error in training: {str(e)}")
        return jsonify({
            'error': 'Training failed',
            'message': str(e)
        }), 500

@app.route('/model/info', methods=['GET'])
def model_info():
    """
    Get information about the current model
    """
    return jsonify({
        'model_type': 'LSTM Neural Network',
        'version': lstm_predictor.get_model_version(),
        'last_trained': lstm_predictor.get_last_training_date(),
        'features': lstm_predictor.get_feature_names(),
        'performance_metrics': lstm_predictor.get_performance_metrics()
    })

@app.route('/simulate', methods=['POST'])
def simulate_data():
    """
    Generate simulated sensor data for testing
    """
    try:
        data = request.get_json()
        asset_type = data.get('asset_type', 'BRIDGE')
        duration_hours = data.get('duration_hours', 24)
        
        simulated_data = data_preprocessor.generate_simulated_data(
            asset_type=asset_type,
            duration_hours=duration_hours
        )
        
        return jsonify({
            'status': 'success',
            'data': simulated_data
        })
        
    except Exception as e:
        logger.error(f"Error in simulation: {str(e)}")
        return jsonify({
            'error': 'Simulation failed',
            'message': str(e)
        }), 500

if __name__ == '__main__':
    logger.info("Starting ML Prediction Engine...")
    
    # Load pre-trained model if available
    lstm_predictor.load_model()
    
    app.run(host='0.0.0.0', port=5000, debug=False)