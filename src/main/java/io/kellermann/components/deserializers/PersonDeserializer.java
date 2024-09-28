package io.kellermann.components.deserializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.kellermann.model.gdVerwaltung.PersonMetaData;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;

import java.io.IOException;

public class PersonDeserializer extends StdDeserializer<PersonMetaData> implements ResolvableDeserializer {
    private final JsonDeserializer<?> defaultDeserializer;

    public PersonDeserializer(JsonDeserializer<?> theDefaultDeserializer) {
        super(WorshipMetaData.class);
        defaultDeserializer = theDefaultDeserializer;
    }

    @Override
    public PersonMetaData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        PersonMetaData personMetaData = new PersonMetaData();
        while (!jsonParser.isClosed()) {
            JsonToken jsonToken = jsonParser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                if (fieldName.startsWith("person_id")) {
                    personMetaData.setId(jsonParser.getValueAsInt(0));
                } else if (fieldName.startsWith("person_lastname")) {
                    personMetaData.setLastName(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_firstname")) {
                    personMetaData.setFirstName(jsonParser.getValueAsString());
                } else if (fieldName.startsWith("person_picture")) {
                    personMetaData.setPicture(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_email")) {
                    personMetaData.setEmail(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_phone")) {
                    personMetaData.setPhone(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_instagram")) {
                    personMetaData.setInstagram(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_facebook")) {
                    personMetaData.setFacebook(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_twitter")) {
                    personMetaData.setTwitter(jsonParser.getValueAsString());
                } else if (fieldName.startsWith("person_youtube")) {
                    personMetaData.setYoutube(jsonParser.getValueAsString());
                }else if (fieldName.startsWith("person_active")) {
                    personMetaData.setActive(jsonParser.getValueAsInt(0)==1);
                }
            }

            if (jsonToken == null) {
                jsonParser.close();
            }
        }

        return personMetaData;
    }

    @Override
    public void resolve(DeserializationContext deserializationContext) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(deserializationContext);
    }

}
