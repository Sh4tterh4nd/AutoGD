package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesMetaData {

    @JsonProperty("series_id")
    public Integer id;

    public Map<Language, String> titleLanguage = new HashMap<Language, String>();
    public Map<Language, String> descriptionLanguage = new HashMap<Language, String>();
    public Map<Language, String> albumartLanguage = new HashMap<Language, String>();
    public Map<Language, String> imageLanguage = new HashMap<Language, String>();
    public Map<Language, String> signageLanguage = new HashMap<Language, String>();
    public Map<Language, String> urlLanguage = new HashMap<Language, String>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void addTitleLanguage(Language language, String title) {
        titleLanguage.put(language, title);
    }

    public void addDescriptionLanguage(Language language, String description) {
        descriptionLanguage.put(language, description);
    }

    public void addAlbumartLanguage(Language language, String albumart) {
        albumartLanguage.put(language, albumart);
    }

    public void addImageLanguage(Language language, String image) {
        imageLanguage.put(language, image);
    }

    public void addSignageLanguage(Language language, String signage) {
        signageLanguage.put(language, signage);
    }

    public void addUrlLanguage(Language language, String url) {
        urlLanguage.put(language, url);
    }

    public String getTitleLanguage(Language language) {
        return titleLanguage.get(language);
    }

    public String getDescriptionLanguage(Language language) {
        return descriptionLanguage.get(language);
    }

    public String getAlbumartLanguage(Language language) {
        return albumartLanguage.get(language);
    }

    public String getImageLanguage(Language language) {
        return imageLanguage.get(language);
    }

    public String getSignageLanguage(Language language) {
        return signageLanguage.get(language);
    }

    public String getUrlLanguage(Language language) {
        return urlLanguage.get(language);
    }


    public String getImageByType(Language language, ImageType imageType) {
        if (ImageType.ALBUMART.equals(imageType)) {
            return albumartLanguage.get(language);
        } else {
            return imageLanguage.get(language);
        }
    }

    @Override
    public String toString() {
        return "SeriesMetaData{" +
                "id=" + id +
                ", titleLanguage=" + titleLanguage +
                ", descriptionLanguage=" + descriptionLanguage +
                ", albumartLanguage=" + albumartLanguage +
                ", imageLanguage=" + imageLanguage +
                ", signageLanguage=" + signageLanguage +
                ", urlLanguage=" + urlLanguage +
                '}';
    }
}
