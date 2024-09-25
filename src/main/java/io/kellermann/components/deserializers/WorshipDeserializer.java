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
import io.kellermann.model.gdVerwaltung.PersonMetaData;
import io.kellermann.model.gdVerwaltung.SeriesMetaData;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorshipDeserializer extends StdDeserializer<WorshipMetaData> implements ResolvableDeserializer {
    private final JsonDeserializer<?> defaultDeserializer;
    private Pattern languagePattern = Pattern.compile("(?<lang>[^_]+$)");
    private SeriesDeserializer seriesDeserializer;
    private PersonDeserializer personDeserializer;

    public WorshipDeserializer(JsonDeserializer<?> theDefaultDeserializer, SeriesDeserializer seriesDeserializer, PersonDeserializer personDeserializer) {
        super(WorshipMetaData.class);
        defaultDeserializer = theDefaultDeserializer;
        this.seriesDeserializer = seriesDeserializer;
        this.personDeserializer = personDeserializer;
    }


    @Override
    public WorshipMetaData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        TokenBuffer buf = new TokenBuffer(jsonParser);
        buf.copyCurrentStructure(jsonParser);
        JsonParser initialParser = buf.asParser();
        initialParser.nextToken();
        WorshipMetaData worshipMetaData = (WorshipMetaData) defaultDeserializer.deserialize(initialParser, deserializationContext);

        JsonParser seriesParser = buf.asParser();
        seriesParser.nextToken();
        SeriesMetaData seriesMetaData = seriesDeserializer.deserialize(seriesParser, deserializationContext);
        worshipMetaData.setSeries(seriesMetaData);

        JsonParser personParser = buf.asParser();
        personParser.nextToken();
        PersonMetaData personMetaData = personDeserializer.deserialize(personParser, deserializationContext);
        worshipMetaData.setPerson(personMetaData);



        JsonParser secondary = buf.asParser();

        while (!secondary.isClosed()) {
            JsonToken jsonToken = secondary.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = secondary.getCurrentName();
                secondary.nextToken();
                if (fieldName.startsWith("service_title_")) {
                    worshipMetaData.addTitleLang(getLanguage(fieldName), secondary.getValueAsString());
                }
            } else if (jsonToken == null) {
                secondary.close();
            }
        }
        return worshipMetaData;
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
