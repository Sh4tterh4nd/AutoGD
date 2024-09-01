package io.kellermann.model.gdVerwaltung;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesMetaData {

    @JsonProperty("series_id")
    public Integer seriesID;

    public Map<Language, String> seriesTitleLanguage = new HashMap<Language, String>();
    public Map<Language, String> seriesDescriptionLanguage = new HashMap<Language, String>();
    public Map<Language, String> seriesAlbumartLanguage = new HashMap<Language, String>();
    public Map<Language, String> seriesImageLanguage = new HashMap<Language, String>();
    public Map<Language, String> seriesSignageLanguage = new HashMap<Language, String>();
    public Map<Language, String> seriesUrlLanguage = new HashMap<Language, String>();

    public Integer getSeriesID() {
        return seriesID;
    }

    public void setSeriesID(Integer seriesID) {
        this.seriesID = seriesID;
    }

    public void addSeriesTitleLanguage(Language language, String title) {
        seriesTitleLanguage.put(language, title);
    }

    public void addSeriesDescriptionLanguage(Language language, String description) {
        seriesDescriptionLanguage.put(language, description);
    }

    public void addSeriesAlbumartLanguage(Language language, String albumart) {
        seriesAlbumartLanguage.put(language, albumart);
    }

    public void addSeriesImageLanguage(Language language, String image) {
        seriesImageLanguage.put(language, image);
    }

    public void addSeriesSignageLanguage(Language language, String signage) {
        seriesSignageLanguage.put(language, signage);
    }

    public void addSeriesUrlLanguage(Language language, String url) {
        seriesUrlLanguage.put(language, url);
    }

    public String getSeriesTitleLanguage(Language language) {
        return seriesTitleLanguage.get(language);
    }

    public String getSeriesDescriptionLanguage(Language language) {
        return seriesDescriptionLanguage.get(language);
    }

    public String getSeriesAlbumartLanguage(Language language) {
        return seriesAlbumartLanguage.get(language);
    }

    public String getSeriesImageLanguage(Language language) {
        return seriesImageLanguage.get(language);
    }

    public String getSeriesSignageLanguage(Language language) {
        return seriesSignageLanguage.get(language);
    }

    public String getSeriesUrlLanguage(Language language) {
        return seriesUrlLanguage.get(language);
    }


    public String getImageByType(Language language, ImageType imageType) {
        if (ImageType.ALBUMART.equals(imageType)) {
            return seriesAlbumartLanguage.get(language);
        } else {
            return seriesImageLanguage.get(language);
        }
    }

    @Override
    public String toString() {
        return "SeriesMetaData{" +
                "seriesID=" + seriesID +
                ", seriesTitleLanguage=" + seriesTitleLanguage +
                ", seriesDescriptionLanguage=" + seriesDescriptionLanguage +
                ", seriesAlbumartLanguage=" + seriesAlbumartLanguage +
                ", seriesImageLanguage=" + seriesImageLanguage +
                ", seriesSignageLanguage=" + seriesSignageLanguage +
                ", seriesUrlLanguage=" + seriesUrlLanguage +
                '}';
    }
}
