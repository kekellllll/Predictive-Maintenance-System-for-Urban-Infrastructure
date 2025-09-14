package com.infrastructure.maintenance.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.infrastructure.maintenance.config.InfluxDBConfig;
import com.infrastructure.maintenance.model.SensorData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class InfluxDBService {

    @Autowired
    private InfluxDBClient influxDBClient;

    @Autowired
    private InfluxDBConfig influxDBConfig;

    public void writeSensorData(SensorData sensorData) {
        Point point = Point
                .measurement("sensor_readings")
                .addTag("sensor_id", sensorData.getSensorId())
                .addTag("sensor_type", sensorData.getSensorType())
                .addTag("asset_id", sensorData.getAsset().getAssetId())
                .addTag("unit", sensorData.getUnit())
                .addField("value", sensorData.getValue())
                .addField("quality_score", sensorData.getQualityScore())
                .time(sensorData.getTimestamp().toInstant(ZoneOffset.UTC), WritePrecision.MS);

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoint(influxDBConfig.getBucket(), influxDBConfig.getOrg(), point);
    }

    public void writeSensorDataBatch(List<SensorData> sensorDataList) {
        List<Point> points = new ArrayList<>();
        
        for (SensorData sensorData : sensorDataList) {
            Point point = Point
                    .measurement("sensor_readings")
                    .addTag("sensor_id", sensorData.getSensorId())
                    .addTag("sensor_type", sensorData.getSensorType())
                    .addTag("asset_id", sensorData.getAsset().getAssetId())
                    .addTag("unit", sensorData.getUnit())
                    .addField("value", sensorData.getValue())
                    .addField("quality_score", sensorData.getQualityScore())
                    .time(sensorData.getTimestamp().toInstant(ZoneOffset.UTC), WritePrecision.MS);
            points.add(point);
        }

        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
        writeApi.writePoints(influxDBConfig.getBucket(), influxDBConfig.getOrg(), points);
    }

    public List<FluxRecord> querySensorData(String assetId, String sensorType, String timeRange) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %s) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"sensor_readings\") " +
                        "|> filter(fn: (r) => r[\"asset_id\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"sensor_type\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\")",
                influxDBConfig.getBucket(), timeRange, assetId, sensorType);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, influxDBConfig.getOrg());
        List<FluxRecord> records = new ArrayList<>();
        
        for (FluxTable table : tables) {
            records.addAll(table.getRecords());
        }
        
        return records;
    }

    public List<FluxRecord> getAggregatedSensorData(String assetId, String aggregationWindow) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -24h) " +
                        "|> filter(fn: (r) => r[\"_measurement\"] == \"sensor_readings\") " +
                        "|> filter(fn: (r) => r[\"asset_id\"] == \"%s\") " +
                        "|> filter(fn: (r) => r[\"_field\"] == \"value\") " +
                        "|> aggregateWindow(every: %s, fn: mean, createEmpty: false) " +
                        "|> yield(name: \"mean\")",
                influxDBConfig.getBucket(), assetId, aggregationWindow);

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery, influxDBConfig.getOrg());
        List<FluxRecord> records = new ArrayList<>();
        
        for (FluxTable table : tables) {
            records.addAll(table.getRecords());
        }
        
        return records;
    }
}