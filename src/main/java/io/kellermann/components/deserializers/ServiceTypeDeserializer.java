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
import io.kellermann.model.gdVerwaltung.ServiceType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceTypeDeserializer extends StdDeserializer<ServiceType> implements ResolvableDeserializer {
    private final JsonDeserializer<?> defaultDeserializer;
    private Pattern languagePattern = Pattern.compile("(?<lang>[^_]+$)");

    public ServiceTypeDeserializer(JsonDeserializer<?> theDefaultDeserializer) {
        super(WorshipMetaData.class);
        defaultDeserializer = theDefaultDeserializer;
    }


    @Override
    public ServiceType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        TokenBuffer buf = new TokenBuffer(jsonParser);
        buf.copyCurrentStructure(jsonParser);
        JsonParser secondary = buf.asParser();
        secondary.nextToken();
        ServiceType serviceType = new ServiceType();

        while (!secondary.isClosed()) {
            JsonToken jsonToken = secondary.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = secondary.getCurrentName();
                secondary.nextToken();
//Todo if null use actual null object
                if (fieldName.startsWith("servicetype_id")) {
                    serviceType.setId(secondary.getValueAsInt(0));
                } else if (fieldName.startsWith("servicetype_webprio")) {
                    serviceType.setWebprio(secondary.getValueAsInt(0));
                } else if (fieldName.startsWith("servicetype_name_plural_")) {
                    serviceType.addNamePluralLanguage(getLanguage(fieldName), secondary.getValueAsString());
                } else if (fieldName.startsWith("servicetype_name_")) {
                    serviceType.addNameLanguage(getLanguage(fieldName), secondary.getValueAsString());
                }
            } else if (jsonToken == null) {
                secondary.close();
            }
        }
        return serviceType;
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
