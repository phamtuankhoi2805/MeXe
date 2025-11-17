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

    @GetMapping("/dich-vu-pin")
    public String hienThiDichVuPin(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dich-vu");
        return "dich-vu-pin";
    }

    @GetMapping("/dich-vu-hau-mai")
    public String hienThiDichVuHauMai(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dich-vu");
        return "dich-vu-hau-mai";
    }

    @GetMapping("/ve-chung-toi")
    public String hienThiVeChungToi(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gioi-thieu");
        return "ve-chung-toi";
    }

    @GetMapping("/tin-tuc")
    public String hienThiTinTuc(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "tin-tuc");
        return "tin-tuc";
    }

    @GetMapping("/dang-ky/tai-khoan")
    public String hienThiDangKyTaiKhoan(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-ky");
        return "dang-ky-tai-khoan";
    }

    @GetMapping("/dang-ky/lai-thu")
    public String hienThiDangKyLaiThu(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "dang-ky");
        return "dang-ky-lai-thu";
    }

    @GetMapping("/dat-mua/xe-may-dien")
    public String hienThiTrangDatMuaXeMay(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "san-pham");
        return "dat-mua-xe-may";
    }

    @GetMapping("/gio-hang")
    public String hienThiGioHang(Model moHinh) {
        moHinh.addAttribute(TRANG_DANG_CHON, "gio-hang");
        return "gio-hang";
    }
}
