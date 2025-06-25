package io.kellermann.controller.api;

import io.kellermann.model.dto.WorshipMetaDataDTO;
import io.kellermann.model.gd.GdJob;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.DtoConverter;
import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
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
import java.util.HashMap;
import java.util.Map;

@RequestMapping("api/service")
@RestController
public class ServiceRestController {
    private final WorshipServiceApi worshipServiceApi;
    private final UtilityComponent utilityComponent;
    private final StatusService statusService;
    private final DtoConverter converter;
    Map<Integer, WorshipMetaData> worshipMetaDataMap = new HashMap<Integer, WorshipMetaData>();

    public ServiceRestController(DtoConverter converter, StatusService statusService, UtilityComponent utilityComponent, WorshipServiceApi worshipServiceApi) {
        this.converter = converter;
        this.statusService = statusService;
        this.utilityComponent = utilityComponent;
        this.worshipServiceApi = worshipServiceApi;
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
        WorshipMetaData worshipMetaData = worshipMetaDataMap.get(id);
        if (worshipMetaData == null) {
            worshipMetaData = worshipServiceApi.getWorshipByServiceId(id);
            worshipMetaDataMap.put(id, worshipMetaData);
        }
        Path mainRecording = utilityComponent.getMainRecording(worshipMetaData);
        return ResponseEntity.ok().body(new FileSystemResource(mainRecording));
    }


    @PostMapping("/generate")
    public ResponseEntity<String> generateGD(@RequestBody GdJob job) {
        System.out.println(job);
        return ResponseEntity.ok().body("");
    }


}
