package io.kellermann.controller.api;

import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private WorshipServiceApi worshipServiceApi;
    private UtilityComponent utilityComponent;
    Map<Integer, WorshipMetaData> worshipMetaDataMap = new HashMap<Integer, WorshipMetaData>();

    public ServiceRestController(WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent) {
        this.worshipServiceApi = worshipServiceApi;
        this.utilityComponent = utilityComponent;
    }

    @GetMapping()
    public WorshipMetaData getWorshipMetaDataByDate(@RequestParam(required = false, defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate time) {
        System.out.println(time);
        return worshipServiceApi.getWorshipsByDate(LocalDate.now()).getFirst();
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

}
