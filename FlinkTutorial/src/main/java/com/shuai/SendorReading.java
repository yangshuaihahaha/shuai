package com.shuai;

public class SendorReading {
    private String name;
    private long timestamp;
    private double temperature;

    public SendorReading(String name, long timestamp, double temperature) {
        this.name = name;
        this.timestamp = timestamp;
        this.temperature = temperature;
    }

    public SendorReading() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "SendorReading{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                ", temperature=" + temperature +
                '}';
    }
}
