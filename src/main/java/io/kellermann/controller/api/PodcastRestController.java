package io.kellermann.controller.api;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.services.UtilityComponent;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@RequestMapping("api/podcast")
@RestController
public class PodcastRestController {

    private final VideoConfiguration videoConfiguration;
    private final UtilityComponent utilityComponent;

    public PodcastRestController(VideoConfiguration videoConfiguration, UtilityComponent utilityComponent) {
        this.videoConfiguration = videoConfiguration;
        this.utilityComponent = utilityComponent;
    }

    @PostMapping
    public String addAudiobook(@RequestParam("data") MultipartFile multipartFile,
                               @RequestParam(name = "id", defaultValue = "0", required = false) Integer id) {

        String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());

        Path output = videoConfiguration.getPodcastData().resolve(id.toString().concat(".").concat(extension));
        try (OutputStream outputStream = new FileOutputStream(output.toFile())) {
            multipartFile.getInputStream().transferTo(outputStream);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Success";
    }


    @GetMapping(value = "/stream/{id}")
    public ResponseEntity<FileSystemResource> streamFile(@PathVariable Integer id) {
        if (utilityComponent.doesPodcastRecordingExist(id)) {
            return ResponseEntity.ok().body(new FileSystemResource(utilityComponent.findPodcastRecording(id)));
        }
        return ResponseEntity.notFound().build();
    }

}
