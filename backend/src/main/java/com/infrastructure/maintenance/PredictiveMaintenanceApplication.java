package com.infrastructure.maintenance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PredictiveMaintenanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PredictiveMaintenanceApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("sensor-data", r -> r.path("/api/sensors/**")
                        .uri("http://localhost:8081"))
                .route("ml-predictions", r -> r.path("/api/predictions/**")
                        .uri("http://localhost:8082"))
                .route("infrastructure", r -> r.path("/api/infrastructure/**")
                        .uri("http://localhost:8080"))
                .build();
    }
}