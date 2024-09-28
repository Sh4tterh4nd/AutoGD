package io.kellermann.components.deserializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import io.kellermann.model.gdVerwaltung.Language;
import io.kellermann.model.gdVerwaltung.SeriesMetaData;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeriesDeserializer extends StdDeserializer<SeriesMetaData> implements ResolvableDeserializer {
    private final JsonDeserializer<?> defaultDeserializer;
    private Pattern languagePattern = Pattern.compile("(?<lang>[^_]+$)");

    public SeriesDeserializer(JsonDeserializer<?> theDefaultDeserializer) {
        super(WorshipMetaData.class);
        defaultDeserializer = theDefaultDeserializer;
    }


    @Override
    public SeriesMetaData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        TokenBuffer buf = new TokenBuffer(jsonParser);
        buf.copyCurrentStructure(jsonParser);
        JsonParser secondary = buf.asParser();
        secondary.nextToken();
        SeriesMetaData seriesMetaData = new SeriesMetaData();

        while (!secondary.isClosed()) {
            JsonToken jsonToken = secondary.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = secondary.getCurrentName();
                secondary.nextToken();

                if (fieldName.startsWith("series_id ")) {
                    seriesMetaData.setId(secondary.getValueAsInt(0));
                } else if (fieldName.startsWith("series_title_")) {
                    seriesMetaData.addTitleLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("series_description_")) {
                    seriesMetaData.addDescriptionLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("series_albumart_")) {
                    seriesMetaData.addAlbumartLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("series_image_")) {
                    seriesMetaData.addImageLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("series_signage_")) {
                    seriesMetaData.addSignageLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("series_url_")) {
                    seriesMetaData.addUrlLanguage(getLanguage(fieldName), secondary.getValueAsString());
                }
            } else if (jsonToken == null) {
                secondary.close();
            }
        }
        return seriesMetaData;
    }

    @Override
    public void resolve(DeserializationContext deserializationContext) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(deserializationContext);
    }


    public Language getLanguage(String theName) {
        Matcher matcher = languagePattern.matcher(theName);
        while (matcher.find()) {
            return Language.fromString(matcher.group("lang"));

        }
        return null;
    }
}
