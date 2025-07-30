package com.example.service;

import java.util.List;
import java.util.Map;

public interface HierarchyService {
    List<Map<String, Object>> getAllParentIds(String objectName, String dbName);

}

