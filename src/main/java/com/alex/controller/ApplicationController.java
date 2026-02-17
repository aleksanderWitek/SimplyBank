package com.alex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApplicationController {

    @GetMapping(path = {"/", "/home"})
    public String home() {
        return "dashboard";
    }

    @GetMapping(path = "/transactions")
    public String transactions() {
        return "transactions";
    }

    @GetMapping(path = "/new-transaction")
    public String newTransaction() {
        return "new-transaction";
    }
}
