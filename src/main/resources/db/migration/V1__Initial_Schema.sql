-- Initial database schema for Weather Microservice
-- Creates tables for locations, weather records, and forecast records

-- Locations table
CREATE TABLE locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    region VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes for locations table
CREATE INDEX idx_location_name ON locations(name);
CREATE INDEX idx_location_country ON locations(country);

-- Weather records table
CREATE TABLE weather_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_id BIGINT NOT NULL,
    temperature DOUBLE NOT NULL,
    feels_like DOUBLE,
    humidity INT NOT NULL,
    wind_speed DOUBLE NOT NULL,
    wind_direction VARCHAR(10),
    condition VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    pressure_mb DOUBLE,
    precipitation_mm DOUBLE,
    cloud_coverage INT,
    uv_index DOUBLE,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_weather_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);

-- Indexes for weather_records table
CREATE INDEX idx_weather_location_id ON weather_records(location_id);
CREATE INDEX idx_weather_timestamp ON weather_records(timestamp);

-- Forecast records table
CREATE TABLE forecast_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    location_id BIGINT NOT NULL,
    forecast_date DATE NOT NULL,
    max_temperature DOUBLE NOT NULL,
    min_temperature DOUBLE NOT NULL,
    avg_temperature DOUBLE,
    max_wind_speed DOUBLE,
    avg_humidity INT,
    condition VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    precipitation_mm DOUBLE,
    precipitation_probability INT,
    uv_index DOUBLE,
    sunrise_time VARCHAR(10),
    sunset_time VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_forecast_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);

-- Indexes for forecast_records table
CREATE INDEX idx_forecast_location_id ON forecast_records(location_id);
CREATE INDEX idx_forecast_date ON forecast_records(forecast_date);
