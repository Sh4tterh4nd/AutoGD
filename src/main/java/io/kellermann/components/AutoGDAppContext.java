package io.kellermann.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.kellermann.components.deserializers.PersonDeserializer;
import io.kellermann.components.deserializers.SeriesDeserializer;
import io.kellermann.components.deserializers.WorshipDeserializer;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.PersonMetaData;
import io.kellermann.model.gdVerwaltung.SeriesMetaData;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

@Configuration
public class AutoGDAppContext {
    private VideoConfiguration videoConfiguration;

    public AutoGDAppContext(VideoConfiguration videoConfiguration) {
        this.videoConfiguration = videoConfiguration;
    }

    @Bean
    public FFmpeg generateFfmpeg() {
        try {
            return new FFmpeg(videoConfiguration.getFfmpegLocation());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Bean
    public FFprobe generateFfprobe() {
        try {
            return new FFprobe(videoConfiguration.getFfprobeLocation());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }


    @Bean
    public SimpleModule worshipDeserializer() {
        SimpleModule module = new SimpleModule("Worship-Deserializer-Module");
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return beanDesc.getBeanClass() == WorshipMetaData.class ? new WorshipDeserializer(deserializer, new SeriesDeserializer(deserializer), new PersonDeserializer(deserializer)) : deserializer;
            }
        });
        return module;
    }
    @Bean
    public SimpleModule seriesDeserializer() {
        SimpleModule module = new SimpleModule("Series-Deserializer-Module");
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return beanDesc.getBeanClass() == SeriesMetaData.class ? new SeriesDeserializer(deserializer) : deserializer;
            }
        });
        return module;
    }
    @Bean
    public SimpleModule personDeserializer() {
        SimpleModule module = new SimpleModule("Person-Deserializer-Module");
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                return beanDesc.getBeanClass() == PersonMetaData.class ? new PersonDeserializer(deserializer) : deserializer;
            }
        });
        return module;
    }

    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder(List<Module> theModules) {
        return new Jackson2ObjectMapperBuilder()
                .findModulesViaServiceLoader(true)
                .modules(theModules)
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }


    @Bean
    public WebClient webClient(ObjectMapper objectMapper) {
        WebClient client = WebClient.builder()
                .baseUrl("https://meine.church/services/")
                .codecs(configure -> configure.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper)))
                .exchangeStrategies(useMaxMemory())
                .defaultHeaders(headers -> {
                    headers.addAll(createHeaders());
                })
                .build();
        return client;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "*/*");
        return headers;
    }

    private static ExchangeStrategies useMaxMemory() {
        long totalMemory = Runtime.getRuntime().maxMemory();
        return ExchangeStrategies.builder()
                .codecs(configurer -> {
                            configurer.defaultCodecs()
                                    .maxInMemorySize((int) totalMemory);
                        }
                )
                .build();
    }
}
