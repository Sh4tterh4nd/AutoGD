package io.kellermann.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "autogd.youtube")
public class YoutubeConfiguration {
    private String title;
    private String description;
    private String clientId;
    private String clientSecret;
    private List<String> tags = new ArrayList<>();
    private Boolean forKids = false;
    private String privacyStatus;



    public YoutubeConfiguration() {
    }

    public String getPrivacyStatus() {
        return privacyStatus;
    }

    public void setPrivacyStatus(String privacyStatus) {
        this.privacyStatus = privacyStatus;
    }

    public Boolean getForKids() {
        return forKids;
    }

    public void setForKids(Boolean forKids) {
        this.forKids = forKids;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
