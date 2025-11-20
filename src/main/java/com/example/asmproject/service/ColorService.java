package com.example.asmproject.service;

import com.example.asmproject.model.Color;
import com.example.asmproject.repository.ColorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ColorService {
    
    @Autowired
    private ColorRepository colorRepository;
    
    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }
    
    public Optional<Color> getColorById(Long id) {
        return colorRepository.findById(id);
    }
    
    public Color saveColor(Color color) {
        if (color.getId() == null && colorRepository.existsByName(color.getName())) {
            throw new RuntimeException("Màu sắc đã tồn tại");
        }
        return colorRepository.save(color);
    }
    
    public void deleteColor(Long id) {
        colorRepository.deleteById(id);
    }
}

