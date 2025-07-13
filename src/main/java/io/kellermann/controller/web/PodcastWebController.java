package io.kellermann.controller.web;

import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller()
@RequestMapping("/podcast")
public class PodcastWebController {
    private final WorshipServiceApi worshipServiceApi;
    private final StatusService statusService;
    private final UtilityComponent utilityComponent;

    public PodcastWebController(WorshipServiceApi worshipServiceApi, StatusService statusService, UtilityComponent utilityComponent) {
        this.worshipServiceApi = worshipServiceApi;
        this.statusService = statusService;
        this.utilityComponent = utilityComponent;
    }

    @GetMapping()
    public String podcast(@RequestParam(required = false) Integer id, Model model) {

        model.addAttribute("worshipMetadata", worshipServiceApi.getWorshipByServiceId(id));
        model.addAttribute("utilityComponent", utilityComponent);
        return "podcast";
    }

}
