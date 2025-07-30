package com.example.config;

import com.example.config.DbConfig.DbDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Autowired
    private DbConfig dbConfig;

    @Bean
    public Map<String, DataSource> dataSources() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        for (Map.Entry<String, DbDetails> entry : dbConfig.getDbs().entrySet()) {
            DbDetails db = entry.getValue();
            DataSource ds = DataSourceBuilder.create()
                    .url(db.getUrl())
                    .username(db.getUsername())
                    .password(db.getPassword())
                    .driverClassName(db.getDriverClassName())
                    .build();
            dataSourceMap.put(entry.getKey(), ds);
        }
        return dataSourceMap;
    }
}
