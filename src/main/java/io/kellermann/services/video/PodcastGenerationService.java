package io.kellermann.services.video;


import io.kellermann.components.FtpConnector;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
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

    private FtpConnector ftpConnector;
    private VideoConfiguration videoConfiguration;
    private WorshipServiceApi worshipServiceApi;
    private JaffreeFFmpegService jaffreeFFmpegService;
    private UtilityComponent utility;

    public PodcastGenerationService(FtpConnector ftpConnector) {
        this.ftpConnector = ftpConnector;
    }


    /**
     * Generates the GD Podcast file
     *
     * @param worshipMetaData
     * @return
     * @throws IOException
     */
    public Path generateGDPodcast(WorshipMetaData worshipMetaData) throws IOException {
        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path albumartImage;
        if (Objects.isNull(worshipMetaData.getService_albumart()) || worshipMetaData.getService_albumart().isBlank()) {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getService_albumart());
        }

        worshipServiceApi.saveGDImageTo(ImageType.ALBUMART, worshipMetaData, albumartImage);


        Path originalCut = tempWorkspace.resolve("original_cut.mp3");
        jaffreeFFmpegService.cutAudioVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), utility.getMainRecording(worshipMetaData), originalCut, true);
        List<Path> audioSegmentsList = new ArrayList<>();

        audioSegmentsList.add(videoConfiguration.getIntroPodcastName());
        audioSegmentsList.add(originalCut);
        audioSegmentsList.add(videoConfiguration.getOutroPodcastName());

        Path tempPodcast = videoConfiguration.getTempWorkspace().resolve("temp-podcast.wav");

        jaffreeFFmpegService.concatAudio(tempPodcast, audioSegmentsList, 2.5);


        String generatePodcastName = podcastNameGenerator(worshipMetaData);
        Path podcastFilePath = videoConfiguration.getTempWorkspace().resolve(generatePodcastName);

        jaffreeFFmpegService.convertToPodcastMp3WithMetadata(tempPodcast,
                albumartImage,
                podcastFilePath,
                worshipMetaData);
        if (!ftpConnector.fileExistsOnFtp(podcastFilePath)) {
            ftpConnector.uploadFTPFileToConfiguredPath(podcastFilePath);
        }


        worshipServiceApi.registerPodcastMp3ToPodcastRegistry(worshipMetaData.getServiceID(),
                generatePodcastName,
                Files.size(podcastFilePath),
                jaffreeFFmpegService.getDurationLocalTime(podcastFilePath).toSecondOfDay());

        return podcastFilePath;
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
