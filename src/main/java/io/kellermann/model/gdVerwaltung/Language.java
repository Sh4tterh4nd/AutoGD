package io.kellermann.model.gdVerwaltung;

public enum Language {
    GERMAN("de"),
    ENGLISH("en"),
    PORTUGUESE("pt"),
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
