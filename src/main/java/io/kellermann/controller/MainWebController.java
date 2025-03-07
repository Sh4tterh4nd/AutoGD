package io.kellermann.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainWebController {

    @GetMapping({"/", "/index"})
    public String index(Model model, Principal principal) {

        return "index";
    }

}
