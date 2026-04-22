package com.example.security_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "redirect:/login.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "redirect:/admin.html";
    }

    @GetMapping("/real-admin")
    public String realAdminPage() {
        return "redirect:/real-admin.html";
    }
}