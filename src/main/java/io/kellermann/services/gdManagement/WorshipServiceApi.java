package io.kellermann.services.gdManagement;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class WorshipServiceApi {
    private WebClient webClient;
    private GDManagementConfig config;
    List<WorshipMetaData> cachedMetaData = new ArrayList<>();
    private LocalTime lastUpdate = LocalTime.now();

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
        if (cachedMetaData.isEmpty() || lastUpdate.until(LocalTime.now(), ChronoUnit.MINUTES) > 30) {
            cachedMetaData = Arrays.asList(webClient
                    .get()
                    .uri("/interfaces/services/list")
                    .retrieve()
                    .bodyToMono(WorshipMetaData[].class)
                    .block());
        }

        return cachedMetaData;
    }


    /**
     * Get GDimage by imageType
     *
     * @param imageType
     * @param worshipMetaData
     * @return
     */
    public byte[] getGDImage(ImageType imageType, WorshipMetaData worshipMetaData) {
        Mono<byte[]> mono;
        if (Objects.isNull(worshipMetaData.getServiceImage()) || worshipMetaData.getServiceImage().isBlank()) {
            mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("media", "series", imageType.getPath(), worshipMetaData.getServiceLanguage().getLanguageString(), worshipMetaData.getSeries().getImageByType(worshipMetaData.getServiceLanguage(), imageType))
                            .build())
                    .retrieve()
                    .bodyToMono(byte[].class);
        } else {
            mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("media", "services", imageType.equals(ImageType.ALBUMART) ? "albumart" : "images", worshipMetaData.getServiceImage())
                            .build())
                    .retrieve()
                    .bodyToMono(byte[].class);
        }
        return mono.block();
    }

    /**
     * This will download and save the image for the GD to a file specified. In most cases this will be the series image except a dedicated GD image is defined.
     *
     * @param imageType
     * @param worshipMetaData
     * @param seriesImagePath
     */
    public void saveGDImageTo(ImageType imageType, WorshipMetaData worshipMetaData, Path seriesImagePath) {
        try {
            Files.write(seriesImagePath, getGDImage(imageType, worshipMetaData));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns all Worships from specific date
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

    public List<WorshipMetaData> getAllWorshipsFromTheMostRecentWorshipDay(LocalDate theLocalDate) {
        List<WorshipMetaData> allWorshipsPreviousToDate = getAllWorshipsPreviousToDate(theLocalDate);
        if (allWorshipsPreviousToDate.isEmpty()) {
            return allWorshipsPreviousToDate;
        }
        LocalDate startDate = allWorshipsPreviousToDate.getFirst().getStartDate();

        return allWorshipsPreviousToDate
                .stream()
                .filter(s -> s.getStartDate().isEqual(startDate))
                .toList();
    }

    /**
     * Returns Worships by Date
     *
     * @param theLocalDate
     * @return
     */
    public List<WorshipMetaData> getAllWorshipsPreviousToDate(LocalDate theLocalDate) {
        return getAvailableWorships()
                .stream()
                .filter(this::locationFilter)
                .filter(s -> s.getStartDate().isBefore(theLocalDate) || s.getStartDate().isEqual(theLocalDate))
                .sorted(Comparator.comparing(WorshipMetaData::getStartDate).reversed())
                .toList();
    }

    public List<WorshipMetaData> getAllWorshipsPreviousToToday() {
        return getAllWorshipsPreviousToDate(LocalDate.now());
    }



    public WorshipMetaData getWorshipByServiceId(Integer serviceId) {
        WorshipMetaData worshipMetaData = getAvailableWorships()
                .stream()
                .filter(s -> s.getServiceID().equals(serviceId))
                .findFirst()
                .orElse(new WorshipMetaData());
        System.out.println(worshipMetaData);
        return worshipMetaData;
    }


    /**
     * This returns the worships that are available today, if not specified otherwise in the config.
     *
     * @return a List of WorshipMetaData
     */
    public List<WorshipMetaData> getWorshipsToday() {
        return getWorshipsByDate(LocalDate.now());
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
        return getWorshipsByDate(LocalDate.now())
                .stream()
                .filter(this::isPassed)
                .sorted(Comparator.reverseOrder())
                .findFirst().orElse(null);

    }

    private boolean isPassed(WorshipMetaData theWorship) {
        return LocalTime.now().isAfter(theWorship.getStartTime());
    }


    public void submitYoutubeUrlToGDManagement(String url, WorshipMetaData worshipMetaData) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", config.getToken());
        formData.add("link", url);
        formData.add("id", String.valueOf(worshipMetaData.getServiceID()));
        webClient.post()
                .uri("/interfaces/services/set-youtube-link")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public void registerPodcastMp3ToPodcastRegistry(Integer serviceID, String podcastFileName, Long fileSize, Integer duration) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/actions/media/checkin")
                        .queryParam("format", "mp3")
                        .queryParam("service_id", serviceID)
                        .queryParam("filename", podcastFileName)
                        .queryParam("filesize", fileSize)
                        .queryParam("duration", duration)
                        .queryParam("user", "streaming_czs")
                        .build()
                )
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
