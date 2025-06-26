package io.kellermann.controller.api;

import io.kellermann.model.dto.WorshipMetaDataDTO;
import io.kellermann.model.gd.GdJob;
import io.kellermann.services.DtoConverter;
import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.GDCreateService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

@RequestMapping("api/service")
@RestController
public class ServiceRestController {
    private final WorshipServiceApi worshipServiceApi;
    private final UtilityComponent utilityComponent;
    private final StatusService statusService;
    private final DtoConverter converter;
    private final GDCreateService gdCreateService;

    public ServiceRestController(WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent, StatusService statusService, DtoConverter converter, GDCreateService gdCreateService) {
        this.worshipServiceApi = worshipServiceApi;
        this.utilityComponent = utilityComponent;
        this.statusService = statusService;
        this.converter = converter;
        this.gdCreateService = gdCreateService;
    }

    @GetMapping()
    public WorshipMetaDataDTO getWorshipMetaDataByDate(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer serviceId) {
        date = date == null ? LocalDate.now() : date;
        if (serviceId != null) {
            return converter.worshipMetaDataToDto(worshipServiceApi.getWorshipByServiceId(serviceId));
        }
        return converter.worshipMetaDataToDto(worshipServiceApi.getAllWorshipsFromTheMostRecentWorshipDay(date).getFirst());
    }


    @GetMapping(value = "/stream/{id}")
    public ResponseEntity<FileSystemResource> streamFile(@PathVariable Integer id) throws IOException {
        Path mainRecording = utilityComponent.getMainRecording(worshipServiceApi.getWorshipByServiceId(id));
        return ResponseEntity.ok().body(new FileSystemResource(mainRecording));
    }


    @PostMapping("/generate")
    public ResponseEntity<String> generateGD(@RequestBody GdJob job) {
        gdCreateService.startGDCreation(worshipServiceApi.getWorshipByServiceId(job.serviceId()), job);
        return ResponseEntity.ok().body("");
    }
}
