package io.kellermann.controller.web;

import io.kellermann.services.StatusService;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Objects;

@Controller()
@RequestMapping("/")
public class MainWebController {
    private final WorshipServiceApi worshipServiceApi;
    private final StatusService statusService;

    public MainWebController(WorshipServiceApi worshipServiceApi, StatusService statusService) {
        this.worshipServiceApi = worshipServiceApi;
        this.statusService = statusService;
    }

    @GetMapping()
    public String index(@RequestParam(required = false) Integer id, Model model) {
        if (statusService.isFinished()) {
            if (Objects.isNull(id)) {
                return "redirect:/?id=" + worshipServiceApi.getAllWorshipsPreviousToDate(LocalDate.now()).getFirst().getServiceID();
            }
            model.addAttribute("worshipMetadata", worshipServiceApi.getWorshipByServiceId(id));
            return "index";
        } else {
            return "redirect:/status";
        }
    }

}
