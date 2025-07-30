package com.example.controller;

import com.example.config.DbConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private DbConfig dbConfig;

    @GetMapping("/dbs")
    public List<String> getDbs() {
        return dbConfig.getDbs().keySet().stream().collect(Collectors.toList());
    }
}
