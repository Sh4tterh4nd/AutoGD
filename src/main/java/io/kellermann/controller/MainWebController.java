package io.kellermann.controller;

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

    public MainWebController(WorshipServiceApi worshipServiceApi) {
        this.worshipServiceApi = worshipServiceApi;
    }

    @GetMapping()
    public String index(@RequestParam(required = false) Integer id, Model model) {
        if (Objects.isNull(id)) {
            return "redirect:/?id=" + worshipServiceApi.getMostRecentWorship().getServiceID();
        }

        model.addAttribute("worshipMetadata", worshipServiceApi.getWorshipByServiceId(id));
        model.addAttribute("lastWorships", worshipServiceApi.getAllWorshipsPreviousToDate(LocalDate.now()));
        return "index";
    }

}
