package com.iip.datafusion.backend.parser;

import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import com.iip.datafusion.dgs.model.accuracy.UpdateAccuracyConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateAccuracyParser implements Parser {

    public static UpdateAccuracyJob parser(UpdateAccuracyConfiguration updateAccuracyConfiguration) throws Exception{

        List<Map<String,String>> attributeValues = updateAccuracyConfiguration.getAttributeValues();
        List<Map<String,String>> newValues = updateAccuracyConfiguration.getNewValues();

        List<String> sqlList = new ArrayList<>();

        if (attributeValues == null || attributeValues.size() <= 0) {
            throw new Exception("传入记录不能为空");
        }

        if (newValues == null || newValues.size() <= 0) {
            throw new Exception("传入新值不能为空");
        }

        for(int i = 0;i < attributeValues.size();i++){

            Map<String,String> newValue = newValues.get(i);
            String columnName = "";
            String value = "";
            for(String key : newValue.keySet()){
                columnName = key.substring(3,key.length());
                value = newValue.get(key);
            }

            Map<String,String> attributeValue = attributeValues.get(i);
            if(attributeValue.containsKey(columnName)){
                attributeValue.put(columnName,value);
            }
            else{
                throw new Exception("字段不存在");
            }

            String columnClause = "(";
            String valueClause = "(";

            for(String key : attributeValue.keySet()){
                if (key != null && key.trim() != "") {
                    columnClause += key + ",";
                    valueClause += attributeValue.get(key) + ",";
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
            sqlList.add(sql);
        }

        UpdateAccuracyJob updateAccuracyJob = new UpdateAccuracyJob(updateAccuracyConfiguration.getDataSourceId(),
                                                                    updateAccuracyConfiguration.getTableName(),
                                                                    sqlList);
        return updateAccuracyJob;
    }
}

