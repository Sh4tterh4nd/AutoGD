package io.kellermann.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autogd.gdverwaltung.select")
public class GDManagementConfig {
    private String location;

//    @DateTimeFormat(fallbackPatterns = "dd.MM.yyyy")
//    private LocalDate date = LocalDate.now();
//
//    private LocalTime time = LocalTime.now();

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


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
