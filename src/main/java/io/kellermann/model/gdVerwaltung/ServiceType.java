package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class ServiceType {
    @JsonProperty("servicetype_id")
    public Integer id;
    @JsonProperty("servicetype_webprio")
    public Integer webprio;


    public Map<Language, String> serviceTypeName = new HashMap<Language, String>();
    public Map<Language, String> serviceTypeNamePlural = new HashMap<Language, String>();


    public void addNameLanguage(Language lang, String name) {
        serviceTypeName.put(lang, name);
    }

    public void addNamePluralLanguage(Language lang, String name) {
        serviceTypeNamePlural.put(lang, name);
    }

    public String getNameLanguage(Language language) {
        return serviceTypeName.get(language);
    }

    public String getNamePluralLanguage(Language language) {
        return serviceTypeNamePlural.get(language);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWebprio() {
        return webprio;
    }

    public void setWebprio(Integer webprio) {
        this.webprio = webprio;
    }

    @Override
    public String toString() {
        return "ServiceType{" +
                "id=" + id +
                ", webprio=" + webprio +
                ", serviceTypeName=" + serviceTypeName +
                ", serviceTypeNamePlural=" + serviceTypeNamePlural +
                '}';
    }
}
