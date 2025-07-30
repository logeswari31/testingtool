package com.example.service;

import com.example.dao.HierarchyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HierarchyServiceImpl implements HierarchyService {

    @Autowired
    private HierarchyDao hierarchyDao;

    @Override
    public List<Map<String, Object>> getAllParentIds(String objectName, String dbName) {
        return hierarchyDao.getAllParentIds(objectName, dbName);
    }
}
