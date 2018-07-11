package com.iip.datafusion.backend.parser;

import com.iip.datafusion.backend.job.accuracy.UpdateAccuracyJob;
import com.iip.datafusion.dgs.model.accuracy.KeyValue;
import com.iip.datafusion.dgs.model.accuracy.UpdateAccuracyConfiguration;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateAccuracyParser implements Parser {

    public static UpdateAccuracyJob parser(UpdateAccuracyConfiguration updateAccuracyConfiguration) throws Exception{

        List<List<KeyValue>> attributeValues = updateAccuracyConfiguration.getAttributeValues();
        List<KeyValue> newValues = updateAccuracyConfiguration.getNewValues();

        List<String> sqlList = new ArrayList<>();

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
                    valueClause += value + ",";
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
            System.out.println(sql);
            sqlList.add(sql);
        }

        UpdateAccuracyJob updateAccuracyJob = new UpdateAccuracyJob(updateAccuracyConfiguration.getDataSourceId(),
                                                                    updateAccuracyConfiguration.getTableName(),
                                                                    sqlList);
        return updateAccuracyJob;
    }
}

