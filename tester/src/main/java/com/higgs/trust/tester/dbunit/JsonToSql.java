package com.higgs.trust.tester.dbunit;


import com.alibaba.fastjson.JSONObject;

/**
 * @author shenqingyan
 * @create 2018/4/17 15:47
 * @desc translate Json to sql sentence
 **/
public class JsonToSql {
    /**
     * @desc json转查询语句
     * @param json JSON
     **/
    public String jsonToQuerySql(JSONObject json){
        String sql = "select " + json.get("listname") + " from " + json.get("tablename") + " where " + json.get("condition") +";";
        return sql;
    }

    /**
     * @desc json转删除语句
     * @param
     **/
    public String jsonToDeleteSql(JSONObject json){
        String sql = "delete from " + json.get("tablename")  +  " where " + json.get("condition" +";");
        return sql;
    }

    /**
     * @desc json转insert语句
     * @param
     **/
    public String jsonToInsertSql(JSONObject json){
        String sql = "insert into " + json.get("tablename") + "(" + json.get("key") + ")" + " values " + json.get("value")+";";
        return sql;
    }

}
