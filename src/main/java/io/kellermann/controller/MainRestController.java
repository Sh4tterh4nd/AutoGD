package io.kellermann.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class MainRestController {

    @GetMapping
    public String test() {
        return "Hello";
    }

}
