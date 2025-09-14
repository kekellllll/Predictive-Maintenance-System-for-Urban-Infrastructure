package com.infrastructure.maintenance.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

    @Value("${influxdb.url:http://localhost:8086}")
    private String influxDbUrl;

    @Value("${influxdb.token:infrastructureToken2023}")
    private String token;

    @Value("${influxdb.org:infrastructure-org}")
    private String org;

    @Value("${influxdb.bucket:sensor-data}")
    private String bucket;

    @Bean
    public InfluxDBClient influxDBClient() {
        return InfluxDBClientFactory.create(influxDbUrl, token.toCharArray(), org, bucket);
    }

    public String getBucket() {
        return bucket;
    }

    public String getOrg() {
        return org;
    }
}