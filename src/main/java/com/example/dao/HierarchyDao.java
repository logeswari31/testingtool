package com.example.dao;

import com.example.config.DynamicJdbcTemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class HierarchyDao {

    @Autowired
    private DynamicJdbcTemplateProvider jdbcTemplateProvider;

    public List<Map<String, Object>> getAllParentIds(String objectName, String dbName) {
        var jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate(dbName);

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd_M_uuuu");

        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM user_tables WHERE table_name LIKE 'RCA_HIER_HIS_RCI_%'",
                String.class
        );

        System.out.println("Taken Hierarchy table list!");

        if (tableNames.isEmpty()) return List.of();

        List<String> filteredTables = tableNames.stream()
                .filter(name -> {
                    try {
                        String[] parts = name.split("RCA_HIER_HIS_RCI_");
                        if (parts.length > 1) {
                            LocalDate tableDate = LocalDate.parse(parts[1], formatter);
                            return !tableDate.isBefore(thirtyDaysAgo);
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        if (filteredTables.isEmpty()) return List.of();

        Long childId = jdbcTemplate.queryForObject(
                "SELECT id FROM rca_rci WHERE objectname = ?",
                new Object[]{objectName},
                Long.class
        );

        if (childId == null) return List.of();

        StringBuilder unionQuery = new StringBuilder();
        for (String table : filteredTables) {
            unionQuery.append("SELECT parentid, childid FROM ").append(table).append(" UNION ALL ");
        }
        unionQuery.setLength(unionQuery.length() - " UNION ALL ".length());

        String finalQuery = String.format("""
                SELECT id as rciid, objecttype, eventtypespecificuniquekey, referobjtype, objectid, objectname, additionalinfo,
                       to_date('01/01/1970 05:30:00','DD/MM/YYYY HH24:MI:SS') + (firsteventcomputedtime /1000/60/60/24) as firsteventcomputedtime,
                       to_date('01/01/1970 05:30:00','DD/MM/YYYY HH24:MI:SS') + (recenteventcomputedtime /1000/60/60/24) as recenteventcomputedtime
                FROM rca_rci 
                WHERE referObjType = 'ALARM' and id IN (
                    SELECT parentid FROM (
                        WITH combined_hierarchy AS (
                            %s
                        )
                        SELECT parentid
                        FROM combined_hierarchy
                        START WITH childid = %d
                        CONNECT BY PRIOR parentid = childid
                    ) 
                )
                """, unionQuery.toString(), childId);

        System.out.println("Final Query:\n" + finalQuery);

        return jdbcTemplate.queryForList(finalQuery);
    }

    public List<Map<String, Object>> getProblemDetailsLast10Days(String dbName) {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getJdbcTemplate(dbName);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d_M_yyyy");
        StringBuilder unionQuery = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            String tableName = "RCA_HIER_HIS_RCI_" + formatter.format(date);

            if (i > 0) unionQuery.append(" UNION ALL ");

            unionQuery.append("SELECT DISTINCT ")
                    .append("r1.objecttype, r1.eventtypespecificuniquekey, r1.referobjtype, r1.sourceofrci, ")
                    .append("r2.objecttype, r2.eventtypespecificuniquekey, r2.referobjtype, r2.sourceofrci, ")
                    .append("h.srcofrelationship, h.relationshiptype ")
                    .append("FROM ").append(tableName).append(" h, rca_rci r1, rca_rci r2 ")
                    .append("WHERE h.parentid = r1.id AND h.childid = r2.id ");
        }

        String finalQuery = unionQuery.append(" ORDER BY 1,2,3,4,5,6,7,8,9,10").toString();

        return ((JdbcTemplate) jdbcTemplate).queryForList(finalQuery);
    }

}
