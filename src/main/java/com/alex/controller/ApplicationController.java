package com.alex.controller;

import ch.qos.logback.core.model.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {

    @GetMapping(path = {"/", "/home"})
    public String home(Model model) {
        return "dashboard";
    }
}
