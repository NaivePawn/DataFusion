package com.iip.datafusion.backend.job.accuracy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iip.datafusion.backend.job.Job;
import com.iip.datafusion.backend.job.JobBase;
import java.util.List;

public class UpdateAccuracyJob extends JobBase implements Job {

    private String dataSourceId;
    private String tableName;
    private List<String> sqlList;

    public UpdateAccuracyJob(String dataSourceId, String tableName, List<String> sqlList) {
        this.dataSourceId = dataSourceId;
        this.tableName = tableName;
        this.sqlList = sqlList;
    }

    @JsonIgnore
    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    @JsonIgnore
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @JsonIgnore
    public List<String> getSqlList() {
        return sqlList;
    }

    public void setSqlList(List<String> sqlList) {
        this.sqlList = sqlList;
    }

    @Override
    public String getDescription() {
        return ""+dataSourceId+"."+tableName+": "+sqlList;
    }
}
