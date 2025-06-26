package io.kellermann.services.video;


import io.kellermann.components.FtpConnector;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gd.GdJob;
import io.kellermann.model.gd.StatusKeys;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class PodcastGenerationService {

    private final FtpConnector ftpConnector;
    private final VideoConfiguration videoConfiguration;
    private final WorshipServiceApi worshipServiceApi;
    private final JaffreeFFmpegService jaffreeFFmpegService;
    private final UtilityComponent utility;
    private final StatusService statusService;

    public PodcastGenerationService(FtpConnector ftpConnector, VideoConfiguration videoConfiguration, WorshipServiceApi worshipServiceApi, JaffreeFFmpegService jaffreeFFmpegService, UtilityComponent utility, StatusService statusService) {
        this.ftpConnector = ftpConnector;
        this.videoConfiguration = videoConfiguration;
        this.worshipServiceApi = worshipServiceApi;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.utility = utility;
        this.statusService = statusService;
    }

    /**
     * Generates the GD Podcast file
     *
     * @param worshipMetaData
     * @return
     * @throws IOException
     */
    public Path generateGDPodcast(WorshipMetaData worshipMetaData, GdJob gdJob) throws IOException {
        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path albumartImage;
        if (Objects.isNull(worshipMetaData.getService_albumart()) || worshipMetaData.getService_albumart().isBlank()) {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getService_albumart());
        }
        statusService.sendFullDetail(StatusKeys.PODCAST_ALBUMART, 0., "");
        worshipServiceApi.saveGDImageTo(ImageType.ALBUMART, worshipMetaData, albumartImage);
        statusService.sendLogUpdate("Downloaded albumart");
        statusService.sendFullDetail(StatusKeys.PODCAST_ALBUMART, 1., "");


        Path originalCut = tempWorkspace.resolve("original_cut.wav");
        jaffreeFFmpegService.cutAudioVideo(gdJob.startTime(), gdJob.endTime(), utility.getMainRecording(worshipMetaData), originalCut, true);
        System.err.println("Podcast was Cut");
        List<Path> audioSegmentsList = new ArrayList<>();

        audioSegmentsList.add(videoConfiguration.getIntroPodcastName());
        audioSegmentsList.add(originalCut);
        audioSegmentsList.add(videoConfiguration.getOutroPodcastName());

        Path tempPodcast = videoConfiguration.getTempWorkspace().resolve("temp-podcast.wav");

        jaffreeFFmpegService.concatAudio(tempPodcast, audioSegmentsList, 2.5);
        System.err.println("Podcast was Merged with intro / Outro");

        String generatePodcastName = podcastNameGenerator(worshipMetaData);
        Path podcastFilePath = videoConfiguration.getTempWorkspace().resolve(generatePodcastName);

        jaffreeFFmpegService.convertToPodcastMp3WithMetadata(
                tempPodcast,
                albumartImage,
                podcastFilePath,
                worshipMetaData);
        return podcastFilePath;
    }


    /**
     * Upload Podcast mp3 to ftp and register podcast with meine.church
     *
     * @param podcastFilePath
     * @param serviceID
     * @throws IOException
     */
    public void uploadPodcastAndRegister(Path podcastFilePath, Integer serviceID) throws IOException {
        if (!ftpConnector.fileExistsOnFtp(podcastFilePath)) {
            ftpConnector.uploadFTPFileToConfiguredPath(podcastFilePath);
        }
        statusService.sendFullDetail(StatusKeys.PODCAST_REGISTER, 0.0, "");
        worshipServiceApi.registerPodcastMp3ToPodcastRegistry(serviceID,
                podcastFilePath.getFileName().toString(),
                Files.size(podcastFilePath),
                jaffreeFFmpegService.getDurationLocalTime(podcastFilePath).toSecondOfDay());
        statusService.sendFullDetail(StatusKeys.PODCAST_REGISTER, 1.0, "");
    }


    /**
     * Generate the podcast filename
     * -> Pattern yyyy-MM-dd-HHmm-Pastpo
     *
     * @param worshipMetaData
     * @return
     */
    public String podcastNameGenerator(WorshipMetaData worshipMetaData) {
        LocalDateTime localDateTime = worshipMetaData.getStartDate().atTime(worshipMetaData.getStartTime());

        StringBuilder sb = new StringBuilder();
        sb.append(worshipMetaData.getServiceID());
        sb.append("-");
        sb.append(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")));
        sb.append("-");
        sb.append(worshipMetaData.getPerson().getFirstName());
        sb.append("_");
        sb.append(worshipMetaData.getPerson().getLastName());
        sb.append("-");

        //Todo switch to null once series deserializer is updated to correctly represent open-topic services
        if (worshipMetaData.getSeries().getId() != 0) {
            sb.append(worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()));
            sb.append(" ");
        }
        sb.append(worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));

        return podcastNameNormalizer(sb.toString()).concat(".mp3");
    }

    /**
     * Replace umlauts with substitutions, remove all non base alphabet characters except "-_"
     *
     * @param name
     * @return
     */

    public String podcastNameNormalizer(String name) {
        name = name
                .replace("ü", "ue")
                .replace("Ü", "Ue")
                .replace("ö", "oe")
                .replace("Ö", "Oe")
                .replace("ä", "ae")
                .replace("Ä", "Ae")
                .replace("ß", "ss")
                .replace(".", " ")
                .replaceAll("\\s+", "_");
        name = Normalizer.normalize(name, Normalizer.Form.NFKD).replaceAll("\\p{M}", "");

        return name.replaceAll("[^0-9A-z-_]", "");
    }

}
