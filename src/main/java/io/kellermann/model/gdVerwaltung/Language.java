package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Language {
    @JsonProperty("de")
    GERMAN("de"),
    @JsonProperty("en")
    ENGLISH("en"),
    @JsonProperty("pt")
    PORTUGUESE("pt"),
    @JsonProperty("es")
    SPANISH("es");

    Language(String langString) {
        this.langString = langString;
    }

    private final String langString;

    public String getLanguageString() {
        return langString;
    }

    public static Language fromString(String langString) {
        for (Language value : Language.values()) {
            if (value.langString.equals(langString)) {
                return value;
            }
        }
        return null;
    }

}
