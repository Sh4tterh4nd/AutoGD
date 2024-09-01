package io.kellermann.model.gdVerwaltung;

public enum ImageType {
    ALBUMART("albumart"),
    WIDESCREEN("images");

    private final String path;

    ImageType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
