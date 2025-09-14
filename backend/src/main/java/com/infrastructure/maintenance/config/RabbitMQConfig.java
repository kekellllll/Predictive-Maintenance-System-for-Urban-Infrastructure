package com.infrastructure.maintenance.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String SENSOR_DATA_EXCHANGE = "sensor-data-exchange";
    public static final String MAINTENANCE_ALERT_EXCHANGE = "maintenance-alert-exchange";

    // Queue names
    public static final String SENSOR_DATA_QUEUE = "sensor-data-queue";
    public static final String MAINTENANCE_PREDICTION_QUEUE = "maintenance-prediction-queue";
    public static final String ALERT_NOTIFICATION_QUEUE = "alert-notification-queue";

    // Routing keys
    public static final String SENSOR_DATA_ROUTING_KEY = "sensor.data.received";
    public static final String MAINTENANCE_PREDICTION_ROUTING_KEY = "maintenance.prediction.generated";
    public static final String ALERT_ROUTING_KEY = "alert.notification";

    // Exchanges
    @Bean
    public TopicExchange sensorDataExchange() {
        return new TopicExchange(SENSOR_DATA_EXCHANGE);
    }

    @Bean
    public TopicExchange maintenanceAlertExchange() {
        return new TopicExchange(MAINTENANCE_ALERT_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue sensorDataQueue() {
        return QueueBuilder.durable(SENSOR_DATA_QUEUE).build();
    }

    @Bean
    public Queue maintenancePredictionQueue() {
        return QueueBuilder.durable(MAINTENANCE_PREDICTION_QUEUE).build();
    }

    @Bean
    public Queue alertNotificationQueue() {
        return QueueBuilder.durable(ALERT_NOTIFICATION_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding sensorDataBinding() {
        return BindingBuilder
                .bind(sensorDataQueue())
                .to(sensorDataExchange())
                .with(SENSOR_DATA_ROUTING_KEY);
    }

    @Bean
    public Binding maintenancePredictionBinding() {
        return BindingBuilder
                .bind(maintenancePredictionQueue())
                .to(maintenanceAlertExchange())
                .with(MAINTENANCE_PREDICTION_ROUTING_KEY);
    }

    @Bean
    public Binding alertNotificationBinding() {
        return BindingBuilder
                .bind(alertNotificationQueue())
                .to(maintenanceAlertExchange())
                .with(ALERT_ROUTING_KEY);
    }

    // Message converter
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}