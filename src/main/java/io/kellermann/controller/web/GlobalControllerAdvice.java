package io.kellermann.controller.web;

import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {
    private WorshipServiceApi worshipServiceApi;

    public GlobalControllerAdvice(WorshipServiceApi worshipServiceApi) {
        this.worshipServiceApi = worshipServiceApi;
    }

    @ModelAttribute("lastWorships")
    public List<WorshipMetaData> getTest() {
        return worshipServiceApi.getAllWorshipsPreviousToToday();
    }
}
