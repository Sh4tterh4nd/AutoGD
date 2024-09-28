package io.kellermann.services.gdManagement;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.Language;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class WorshipServiceApi {
    private WebClient webClient;
    private GDManagementConfig config;

    public WorshipServiceApi(WebClient theWebClient, GDManagementConfig theConfig) {
        webClient = theWebClient;
        config = theConfig;
    }

    /**
     * Returns all currently available worships
     *
     * @return
     */
    public List<WorshipMetaData> getAvailableWorships() {
        WorshipMetaData[] response = webClient
                .get()
                .uri("/interfaces/services/list")
                .retrieve()
                .bodyToMono(WorshipMetaData[].class)
                .block();
        return Arrays.asList(response);
    }

    /**
     * Get image by imageType
     * @param imageType
     * @param worshipMetaData
     * @return
     */
    public byte[] getSeriesImage(ImageType imageType, WorshipMetaData worshipMetaData) {
        Mono<byte[]> mono = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment("media", "series",imageType.getPath(),worshipMetaData.getServiceLanguage().getLanguageString() ,worshipMetaData.getSeries().getImageByType(worshipMetaData.getServiceLanguage(), imageType))
                        .build())
                .retrieve()
                .bodyToMono(byte[].class);

        return mono.block();
    }

    public void saveSeriesImageTo(ImageType imageType, WorshipMetaData worshipMetaData, Path seriesImagePath) {
        try {
            Files.write(seriesImagePath,getSeriesImage(imageType,worshipMetaData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns Worships by Date
     *
     * @param theLocalDate
     * @return
     */
    public List<WorshipMetaData> getWorshipsByDate(LocalDate theLocalDate) {
        return getAvailableWorships()
                .stream()
                .filter(this::locationFilter)
                .filter(s -> s.getStartDate().isEqual(theLocalDate))
                .toList();
    }


    /**
     * This returns the worships that are available today, if not specified otherwise in the config.
     *
     * @return a List of WorshipMetaData
     */
    public List<WorshipMetaData> getWorshipsTodayOverwritten() {
        return getWorshipsByDate(config.getDate());
    }

    /**
     * returns true or false based on
     *
     * @param theWorship
     * @return
     */
    private boolean locationFilter(WorshipMetaData theWorship) {
        return theWorship.getCampusShortname().equals(config.getLocation());
    }

    public WorshipMetaData getMostRecentWorship() {
        return getWorshipsByDate(config.getDate())
                .stream()
                .filter(this::isPassed)
                .sorted(Comparator.reverseOrder())
                .findFirst().get();

    }

    private boolean isPassed(WorshipMetaData theWorship) {
        return config.getTime().isAfter(theWorship.getStartTime());
    }
}
