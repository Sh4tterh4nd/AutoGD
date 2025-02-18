package io.kellermann.services.youtube;

import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;

@Component
public class TemplatingEngine {
    private final Pattern WORD_JOIN_LOCATION_PATTERN = Pattern.compile("(?<=[a-z])(?<underscore>)(?=[A-Z])");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private Map<Pattern, String> patternKeyValuesStore = new HashMap<>();
    private Object cachedObject = new WorshipMetaData();

    public String processTemplateWithKeyModel(String templateString, Object model) {
        Map<Pattern, String> patternTemplateMap = getPatternKeyValuesStore(model);
        int count = 0;
        while (templateString.contains("[%")) {
            for (Map.Entry<Pattern, String> patternTemplateEntry : patternTemplateMap.entrySet()) {
                templateString = patternTemplateEntry.getKey().matcher(templateString).replaceAll(patternTemplateEntry.getValue());
            }
            if (count++ > 100) break;
        }

        return templateString;
    }


    public Map<Pattern, String> getPatternKeyValuesStore(Object model) {
        if (cachedObject == model) {
            return patternKeyValuesStore;
        } else {
            cachedObject = model;
            patternKeyValuesStore = generateTemplatePatternMapFromModel(model);
        }

        return patternKeyValuesStore;
    }


    public Map<Pattern, String> generateTemplatePatternMapFromModel(Object model) {
        try {
            Map<String, String> stringMap = generateTemplateMapFromModel(model);

            Map<Pattern, String> patternMap = new HashMap<>();

            stringMap.forEach((key, value) -> patternMap.put(Pattern.compile("\\[%" + key + "%\\]"), Objects.isNull(value) ? "" : value));
            return patternMap;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> generateTemplateMapFromModel(Object worshipMetaData) throws IllegalAccessException {
        Class<? extends Object> aClass = worshipMetaData.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        Map<String, String> allValues = new TreeMap<>();

        for (Field declaredField : declaredFields) {
            if (declaredField.getName().contains("ANNOTATION")) continue;
            declaredField.setAccessible(true);
            Object fieldObject = declaredField.get(worshipMetaData);
            if (fieldObject != null && fieldObject.getClass().getPackageName().startsWith("io.kellermann") && !Enum.class.isAssignableFrom(fieldObject.getClass())) {

                Map<String, String> stringStringMap = generateTemplateMapFromModel(fieldObject);
                stringStringMap
                        .forEach((key, value) ->
                                allValues.put(generateUnderscoreWordSpacing((declaredField.getName() + "_" + key)), value));

            } else if (fieldObject != null && Map.class.isAssignableFrom(fieldObject.getClass())) {
                Map<Object, Object> map = (Map<Object, Object>) fieldObject;

                map
                        .forEach((key, val) ->
                                allValues.put(generateUnderscoreWordSpacing(declaredField.getName() + "_" + key),
                                        String.valueOf(val)));

            } else if (fieldObject != null && LocalDate.class.isAssignableFrom(fieldObject.getClass())) {
                LocalDate localDate = (LocalDate) fieldObject;
                allValues.put(generateUnderscoreWordSpacing(declaredField.getName()), String.valueOf(dateTimeFormatter.format(localDate)));
            } else {
                allValues.put(generateUnderscoreWordSpacing(declaredField.getName()), String.valueOf(fieldObject));
            }
        }


        return allValues;
    }

    private String generateUnderscoreWordSpacing(String key) {
        String normalizedKey = WORD_JOIN_LOCATION_PATTERN.matcher(key).replaceAll("_");
        return normalizedKey.toUpperCase();
    }


}
