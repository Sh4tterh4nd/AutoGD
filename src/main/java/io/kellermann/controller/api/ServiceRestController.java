package io.kellermann.controller.api;

import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/service")
@RestController
public class ServiceRestController {
    private WorshipServiceApi worshipServiceApi;

    public ServiceRestController(WorshipServiceApi worshipServiceApi) {
        this.worshipServiceApi = worshipServiceApi;
    }

    @GetMapping()
    public WorshipMetaData getWorshipMetaDataByDate() {
        return worshipServiceApi.getMostRecentWorship();
    }

}
