package com.example.asmproject.controller.api.admin;

import com.example.asmproject.model.Color;
import com.example.asmproject.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/colors")
public class AdminColorController {
    
    @Autowired
    private ColorService colorService;
    
    @GetMapping
    public ResponseEntity<List<Color>> getAllColors() {
        return ResponseEntity.ok(colorService.getAllColors());
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createColor(@RequestBody Color color) {
        Map<String, Object> response = new HashMap<>();
        try {
            Color saved = colorService.saveColor(color);
            response.put("success", true);
            response.put("message", "Tạo màu sắc thành công.");
            response.put("color", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateColor(@PathVariable Long id,
                                                           @RequestBody Color color) {
        Map<String, Object> response = new HashMap<>();
        try {
            color.setId(id);
            Color updated = colorService.saveColor(color);
            response.put("success", true);
            response.put("message", "Cập nhật màu sắc thành công.");
            response.put("color", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteColor(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            colorService.deleteColor(id);
            response.put("success", true);
            response.put("message", "Xóa màu sắc thành công.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

