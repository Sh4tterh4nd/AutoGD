package io.kellermann.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller()
@RequestMapping("status")
public class StatusWebController {

    @GetMapping()
    public String index(@RequestParam(required = false) Integer id, Model model) {
        return "status";
    }
}
