package io.kellermann.controller.api;

import io.kellermann.model.dto.WorshipMetaDataDTO;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.DtoConverter;
import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("api/services")
@RestController
public class ServicesRestController {
    private final WorshipServiceApi worshipServiceApi;
    private final UtilityComponent utilityComponent;
    private final StatusService statusService;
    private final DtoConverter converter;
    Map<Integer, WorshipMetaData> worshipMetaDataMap = new HashMap<Integer, WorshipMetaData>();

    public ServicesRestController(DtoConverter converter, StatusService statusService, UtilityComponent utilityComponent, WorshipServiceApi worshipServiceApi) {
        this.converter = converter;
        this.statusService = statusService;
        this.utilityComponent = utilityComponent;
        this.worshipServiceApi = worshipServiceApi;
    }

    @GetMapping()
    public List<WorshipMetaDataDTO> getWorshipMetaDataByDate() {
        return converter.worshipMetaDataListToDto(worshipServiceApi.getAllWorshipsPreviousToDate(LocalDate.now()));
    }

}
