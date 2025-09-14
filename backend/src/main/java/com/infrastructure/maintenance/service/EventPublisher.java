package com.infrastructure.maintenance.service;

import com.infrastructure.maintenance.config.RabbitMQConfig;
import com.infrastructure.maintenance.model.SensorData;
import com.infrastructure.maintenance.model.MaintenancePrediction;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishSensorDataReceived(SensorData sensorData) {
        SensorDataEvent event = new SensorDataEvent(
                sensorData.getSensorId(),
                sensorData.getSensorType(),
                sensorData.getValue(),
                sensorData.getTimestamp().toString(),
                sensorData.getAsset().getAssetId()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SENSOR_DATA_EXCHANGE,
                RabbitMQConfig.SENSOR_DATA_ROUTING_KEY,
                event
        );
    }

    public void publishMaintenancePrediction(MaintenancePrediction prediction) {
        MaintenancePredictionEvent event = new MaintenancePredictionEvent(
                prediction.getAsset().getAssetId(),
                prediction.getPredictedFailureDate().toString(),
                prediction.getConfidenceScore(),
                prediction.getRiskLevel().toString(),
                prediction.getRecommendedAction()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAINTENANCE_ALERT_EXCHANGE,
                RabbitMQConfig.MAINTENANCE_PREDICTION_ROUTING_KEY,
                event
        );
    }

    public void publishAlert(String assetId, String alertType, String message, String severity) {
        AlertEvent event = new AlertEvent(assetId, alertType, message, severity);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.MAINTENANCE_ALERT_EXCHANGE,
                RabbitMQConfig.ALERT_ROUTING_KEY,
                event
        );
    }

    // Event DTOs
    public static class SensorDataEvent {
        private String sensorId;
        private String sensorType;
        private Double value;
        private String timestamp;
        private String assetId;

        public SensorDataEvent() {}

        public SensorDataEvent(String sensorId, String sensorType, Double value, String timestamp, String assetId) {
            this.sensorId = sensorId;
            this.sensorType = sensorType;
            this.value = value;
            this.timestamp = timestamp;
            this.assetId = assetId;
        }

        // Getters and setters
        public String getSensorId() { return sensorId; }
        public void setSensorId(String sensorId) { this.sensorId = sensorId; }
        public String getSensorType() { return sensorType; }
        public void setSensorType(String sensorType) { this.sensorType = sensorType; }
        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getAssetId() { return assetId; }
        public void setAssetId(String assetId) { this.assetId = assetId; }
    }

    public static class MaintenancePredictionEvent {
        private String assetId;
        private String predictedFailureDate;
        private Double confidenceScore;
        private String riskLevel;
        private String recommendedAction;

        public MaintenancePredictionEvent() {}

        public MaintenancePredictionEvent(String assetId, String predictedFailureDate, 
                                        Double confidenceScore, String riskLevel, String recommendedAction) {
            this.assetId = assetId;
            this.predictedFailureDate = predictedFailureDate;
            this.confidenceScore = confidenceScore;
            this.riskLevel = riskLevel;
            this.recommendedAction = recommendedAction;
        }

        // Getters and setters
        public String getAssetId() { return assetId; }
        public void setAssetId(String assetId) { this.assetId = assetId; }
        public String getPredictedFailureDate() { return predictedFailureDate; }
        public void setPredictedFailureDate(String predictedFailureDate) { this.predictedFailureDate = predictedFailureDate; }
        public Double getConfidenceScore() { return confidenceScore; }
        public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }

    public static class AlertEvent {
        private String assetId;
        private String alertType;
        private String message;
        private String severity;

        public AlertEvent() {}

        public AlertEvent(String assetId, String alertType, String message, String severity) {
            this.assetId = assetId;
            this.alertType = alertType;
            this.message = message;
            this.severity = severity;
        }

        // Getters and setters
        public String getAssetId() { return assetId; }
        public void setAssetId(String assetId) { this.assetId = assetId; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}