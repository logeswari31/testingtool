package com.example.controller;

import com.example.dao.HierarchyDao;
import com.example.service.HierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hierarchy")
public class HierarchyController {

    @Autowired
    private HierarchyService service;

    @Autowired
    private HierarchyDao hierarchyDao;

    @GetMapping("/parents")
    public List<Map<String, Object>> getParentIds(@RequestParam String objectName, @RequestParam String dbName) {
        return service.getAllParentIds(objectName, dbName);
    }

    @GetMapping("/problem-details")
    public ResponseEntity<List<Map<String, Object>>> getProblemDetails(@RequestParam String dbName) {
        List<Map<String, Object>> result = hierarchyDao.getProblemDetailsLast10Days(dbName);
        return ResponseEntity.ok(result);
    }
}

