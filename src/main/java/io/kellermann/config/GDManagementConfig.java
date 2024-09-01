package io.kellermann.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autogd.gdverwaltung.select")
public class GDManagementConfig {
    private String location;

    @DateTimeFormat(fallbackPatterns = "dd.MM.yyyy")
    private LocalDate date = LocalDate.now();

    private LocalTime time = LocalTime.now();


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
}
