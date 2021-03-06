package com.iip.datafusion.dgs.model.accuracy;

import com.iip.datafusion.dgs.dao.AccuracyDao;
import com.iip.datafusion.util.jsonutil.Result;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AccuracyCheckUnit {

    @Autowired
    private AccuracyDao accuracyDao;

    public Result doCheck(String tableName, FormulaParam formulaParam){
        String whereClause = formulaParam.getWhereClause();
        List<ColumnAttributeValue> columnAttributeValues = formulaParam.getColumnAttributeValues();

        String newWhereClause = whereClause;
        if(!columnAttributeValues.isEmpty()){
            for(ColumnAttributeValue cav : columnAttributeValues){
                String clause = "(CASE " + cav.getColumn();
                List<AttributeValue> attributeValues = cav.getAttributeValues();
                for(AttributeValue av : attributeValues){
                    clause += " WHEN '" + av.getAttribute() + "' THEN " + av.getValue();
                }
                clause += " END)";
                newWhereClause = newWhereClause.replaceAll(cav.getColumn(),clause);
            }
        }
        String[] splits = newWhereClause.split("=");
        String newColumnName = "new_" + splits[0].trim();
        String selectClause = "* , " + splits[1].trim() + " AS " + newColumnName;
        newWhereClause = "NOT(" + newWhereClause + ")";

        try{
            SqlRowSet sqlRowSet = accuracyDao.doSelect(tableName,selectClause,newWhereClause);
            ArrayList<String> columnNames = accuracyDao.getTableColumnList(tableName);
            JSONArray data_items = new JSONArray();
            while(sqlRowSet.next()) {
                JSONObject item = new JSONObject();
                for (String name : columnNames){
                    item.put(name,sqlRowSet.getString(name));
                }
                item.put(newColumnName,sqlRowSet.getString(newColumnName));
                data_items.add(item);
            }
            JSONObject result_data = new JSONObject();
            result_data.put("items", data_items);

            Result result = new Result(1,null,result_data.toString());
            return result;
        }catch (Exception e){
            Result result = new Result(0,"出现内部错误",null);
            return result;
        }
    }

    public Result doCheck(String tableName, ConditionParam conditionParam){
        String columnName = conditionParam.getColumnName();
        List<ConditionValue> conditionValues = conditionParam.getConditionValues();

        try {
            ArrayList<String> columnNames = accuracyDao.getTableColumnList(tableName);
            String columnType = "";
            for(int i = 0; i < columnNames.size();i++){
                if(columnNames.get(i).equals(columnName)){
                    columnType = accuracyDao.getColumnType(tableName,i);
                    System.out.println(columnName);
                    System.out.println(columnType);
                    break;
                }
            }

            JSONArray data_items = new JSONArray();
            for(ConditionValue cv : conditionValues) {
                String condition = cv.getCondition();
                String value = cv.getValue();

                String whereClause;
                if(columnType.equals("VARCHAR") || columnType.equals("CHAR")) {
                    whereClause = "NOT(CASE WHEN " + condition + " THEN " + columnName + " = '" + value + "' END)";
                }
                else{
                    whereClause = "NOT(CASE WHEN " + condition + " THEN " + columnName + " = " + value + " END)";
                }
                String selectClause = "*";

                SqlRowSet sqlRowSet = accuracyDao.doSelect(tableName, selectClause, whereClause);

                String newColumnName = "new_" + columnName;
                while(sqlRowSet.next()){
                    JSONObject item = new JSONObject();
                    for (String name : columnNames){
                        item.put(name,sqlRowSet.getString(name));
                    }
                    item.put(newColumnName,value);
                    data_items.add(item);
                }
            }
            JSONObject result_data = new JSONObject();
            result_data.put("items",data_items);

            Result result = new Result(1,null,result_data.toString());
            return result;

        }catch(Exception e){
            Result result = new Result(0,"出现内部错误",null);
            return result;
        }
    }

    public Result doCheck(String tableName, LengthParam lengthParam){
        String columnName = lengthParam.getColumnName();
        String length = lengthParam.getLength();

        String selectClause = "*";
        String whereClause = "length("+ columnName + ") <> " + length;

        try{
            SqlRowSet sqlRowSet = accuracyDao.doSelect(tableName,selectClause,whereClause);
            String newColumnName = "new_" + columnName;
            String newVlaue = columnName+"长度应为"+length;
            JSONObject result_data = getResultData(tableName,sqlRowSet,newColumnName,newVlaue);

            Result result = new Result(1,null,result_data.toString());
            return result;
        }catch (Exception e){
            Result result = new Result(0,"出现内部错误",null);
            return result;
        }
    }

    public Result doCheck(String tableName, RangeParam rangeParam){
        String columnName = rangeParam.getColumnName();
        String whereClause = rangeParam.getWhereClause();

        String newWhereClause = "NOT(" + whereClause + ")";
        String selectClause = "*";

        try {
            SqlRowSet sqlRowSet = accuracyDao.doSelect(tableName,selectClause,newWhereClause);
            String newColumnName = "new_" + columnName;
            String neaValue = whereClause;
            JSONObject result_data = getResultData(tableName,sqlRowSet,newColumnName,neaValue);

            Result result = new Result(1,null,result_data.toString());
            return result;
        }catch (Exception e) {
            Result result = new Result(0, "出现内部错误", null);
            return result;
        }
    }

    public Result doCheck(String tableName, EmailParam emailParam){
        String columnName = emailParam.getColumnName();

        String whereClause = columnName +" REGEXP '^[a-zA-Z0-9]+[a-zA-Z0-9_-]*@[a-zA-Z0-9]+([\\.][a-zA-Z0-9]+){1,}$'";
        whereClause = "NOT(" + whereClause + ")";
        String selectClause = "*";

        try{
            SqlRowSet sqlRowSet = accuracyDao.doSelect(tableName,selectClause,whereClause);
            String newColumnName = "new_" + columnName;
            String neaValue = "邮箱格式不正确";
            JSONObject result_data = getResultData(tableName,sqlRowSet,newColumnName,neaValue);

            Result result = new Result(1,null,result_data.toString());
            return result;
        }catch (Exception e) {
            Result result = new Result(0,"出现内部错误",null);
            return result;
        }
    }

    private JSONObject getResultData(String tableName,SqlRowSet sqlRowSet, String newColumnName, String newValue) {
        ArrayList<String> columnNames = accuracyDao.getTableColumnList(tableName);
        JSONArray data_items = new JSONArray();
        while (sqlRowSet.next()){
            JSONObject item = new JSONObject();
            for (String name : columnNames){
                item.put(name,sqlRowSet.getString(name));
            }
            item.put(newColumnName,newValue);
            data_items.add(item);
        }
        JSONObject result_data = new JSONObject();
        result_data.put("items",data_items);

        return result_data;
    }
}
