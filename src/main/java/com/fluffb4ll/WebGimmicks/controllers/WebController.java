package com.fluffb4ll.WebGimmicks.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping("/")
    public String getMainPage() {
        return "index.html";
    }
}
