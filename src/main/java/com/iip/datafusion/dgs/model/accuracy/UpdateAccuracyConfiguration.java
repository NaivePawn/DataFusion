package com.iip.datafusion.dgs.model.accuracy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.iip.datafusion.backend.configuration.Configuration;

import java.util.List;
import java.util.Map;

public class UpdateAccuracyConfiguration implements Configuration {

    @JsonProperty("dataSourceId")
    private String dataSourceId;

    @JsonProperty("tableName")
    private String tableName;

    @JsonProperty("attributeValues")
    private List<Map<String,String>> attributeValues;

    @JsonProperty("newValues")
    private List<Map<String,String>> newValues;

    @JsonCreator
    public UpdateAccuracyConfiguration(@JsonProperty("dataSourceId") String dataSourceId,
                                 @JsonProperty("tableName") String tableName,
                                 @JsonProperty("paramStrings") List<Map<String,String>> attributeValues,
                                 @JsonProperty("newValues") List<Map<String,String>> newValues){
        this.dataSourceId = dataSourceId;
        this.tableName = tableName;
        this.attributeValues = attributeValues;
        this.newValues = newValues;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Map<String, String>> getAttributeValues() {
        return attributeValues;
    }

    public void setAttributeValues(List<Map<String, String>> attributeValues) {
        this.attributeValues = attributeValues;
    }

    public List<Map<String, String>> getNewValues() {
        return newValues;
    }

    public void setNewValues(List<Map<String, String>> newValues) {
        this.newValues = newValues;
    }
}
