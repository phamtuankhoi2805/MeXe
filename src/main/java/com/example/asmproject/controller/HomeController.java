package com.example.asmproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final String TRANG_DANG_CHON = "trangDangChon";

    @GetMapping("/")
    public String hienThiTrangChu(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        return "trang-chu";
    }

    @GetMapping("/san-pham/vero-x")
    public String hienThiVeroX(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        return "chi-tiet-san-pham";
    }

    @GetMapping("/phu-kien")
    public String hienThiPhuKien(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "phu-kien");
        return "phu-kien";
    }
}
