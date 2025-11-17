package com.example.asmproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String SECTION_KEY = "adminSection";

    @GetMapping
    public String hienThiDashboard(Model model) {
        model.addAttribute(SECTION_KEY, "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/san-pham")
    public String quanLySanPham(Model model) {
        model.addAttribute(SECTION_KEY, "san-pham");
        return "admin/san-pham";
    }

    @GetMapping("/don-hang")
    public String quanLyDonHang(Model model) {
        model.addAttribute(SECTION_KEY, "don-hang");
        return "admin/don-hang";
    }
}

