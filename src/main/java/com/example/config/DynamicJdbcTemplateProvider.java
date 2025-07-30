package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicJdbcTemplateProvider {

    @Autowired
    private Map<String, DataSource> dataSources;

    private final Map<String, JdbcTemplate> jdbcTemplateCache = new ConcurrentHashMap<>();

    public JdbcTemplate getJdbcTemplate(String dbName) {
        if (!dataSources.containsKey(dbName)) {
            throw new IllegalArgumentException("Database not configured: " + dbName);
        }

        return jdbcTemplateCache.computeIfAbsent(dbName, name -> new JdbcTemplate(dataSources.get(name)));
    }
}
