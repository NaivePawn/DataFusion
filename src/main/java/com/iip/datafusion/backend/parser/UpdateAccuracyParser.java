package com.iip.datafusion.backend.parser;

import com.iip.datafusion.backend.jdbchelper.JDBCHelper;
import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import com.iip.datafusion.dgs.model.accuracy.KeyValue;
import com.iip.datafusion.dgs.model.accuracy.UpdateAccuracyConfiguration;
import com.iip.datafusion.util.dbutil.DataSourceRouterManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateAccuracyParser implements Parser {

    private final static JdbcTemplate jdbcTemplate = JDBCHelper.getJdbcTemplate();

    public static UpdateAccuracyJob parser(UpdateAccuracyConfiguration updateAccuracyConfiguration) throws Exception{

        List<List<KeyValue>> attributeValues = updateAccuracyConfiguration.getAttributeValues();
        List<KeyValue> newValues = updateAccuracyConfiguration.getNewValues();

        List<String> sqlList = new ArrayList<>();

        DataSourceRouterManager.setCurrentDataSourceKey(updateAccuracyConfiguration.getDataSourceId());

        Map<String,String> columnType = new HashMap<>();
        String sqltype = String.format("SELECT * FROM %s LIMIT 0",updateAccuracyConfiguration.getTableName());
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqltype);
        SqlRowSetMetaData sqlRsmd = sqlRowSet.getMetaData();
        for(int i=1;i<=sqlRsmd.getColumnCount();i++){
            columnType.put(sqlRsmd.getColumnName(i),sqlRsmd.getColumnTypeName(i));
        }
        //System.out.println(columnType);

        if (attributeValues == null || attributeValues.size() <= 0) {
            throw new Exception("传入记录不能为空");
        }

        if (newValues == null || newValues.size() <= 0) {
            throw new Exception("传入新值不能为空");
        }

        for(int i = 0;i < attributeValues.size();i++){

            KeyValue newValue = newValues.get(i);
            String columnName = newValue.getKey().substring(4,newValue.getKey().length());
            String columnValue = newValue.getValue();
            List<KeyValue> attributeValue = attributeValues.get(i);
            for(int j = 0;j < attributeValue.size();j++){
                if(attributeValue.get(j).getKey().equals(columnName)){
                    attributeValue.get(j).setValue(columnValue);
                    break;
                }
            }

            String columnClause = "(";
            String valueClause = "(";
            for(int j = 0;j < attributeValue.size();j++){
                String key = attributeValue.get(j).getKey();
                String value = attributeValue.get(j).getValue();
                if (key != null && key.trim() != "") {
                    columnClause += key + ",";
                    //if(columnType.get(key).equals("CHAR")||columnType.get(key).equals("VARCHAR")||columnType.get(key).equals("TEXT")||columnType.get(key).equals("LONGTEXT")){
                    if(columnType.get(key).contains("CHAR")||columnType.get(key).contains("TEXT")){
                        valueClause += "'" + value + "'" + ",";
                    }
                    else{
                        valueClause += value + ",";
                    }
                }
                else{
                    throw new Exception("属性不能为空");
                }
            }

            columnClause = columnClause.substring(0, columnClause.length() - 1);
            columnClause += ")";
            valueClause = valueClause.substring(0, valueClause.length() - 1);
            valueClause += ");";

            String sql = String.format("REPLACE INTO %s %s VALUES %s",updateAccuracyConfiguration.getTableName(),columnClause,valueClause);
            //System.out.println(sql);
            sqlList.add(sql);
        }

        UpdateAccuracyJob updateAccuracyJob = new UpdateAccuracyJob(updateAccuracyConfiguration.getDataSourceId(),
                                                                    updateAccuracyConfiguration.getTableName(),
                                                                    sqlList);
        return updateAccuracyJob;
    }
}

