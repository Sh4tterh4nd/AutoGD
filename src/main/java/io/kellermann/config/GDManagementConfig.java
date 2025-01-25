package io.kellermann.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "autogd.gdverwaltung.select")
public class GDManagementConfig {
    private String location;

    @DateTimeFormat(fallbackPatterns = "dd.MM.yyyy")
    private LocalDate date = LocalDate.now();

    private LocalTime time = LocalTime.now();

    @Value("${autogd.gdverwaltung.token}")
    private String token;


    public GDManagementConfig() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
