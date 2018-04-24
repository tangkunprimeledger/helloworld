package com.higgs.trust.tester.jsonutil;

import com.alibaba.fastjson.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import lombok.extern.java.Log;
/**
 * JsonFileUtil
 *
 * @author shenqingyan
 * @create 2018年04月10日16:53
 */
@Log
public class JsonFileUtil {
    /**
     * @desc 将文件内容返回为list
     * @param
     **/
    public static List<String> readJsonFile(List<String> list, String filepath) {
        File file = new File(filepath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length == 0) {
                return list;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        readJsonFile(list, file2.getAbsolutePath());
                    } else {
                        list.add(bufferedReader(file2.toString()));
                    }
                }
                return list;
            }
        } else {
            list.add(bufferedReader(file.toString()));
        }
        return list;
    }

    /**
     * @desc 读取文件内容并返回string
     * @param
     **/
    public static String bufferedReader(String file) {
        String laststr = "";
        try {
            File filename = new File(file);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                laststr = laststr + line;
            }
            br.close();
            return laststr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return laststr;
    }
    /**
     * @desc 将string转换为json对象
     * @param
     **/
    public static JSONObject stringToJsonObject(String str) {
        JSONObject json = new JSONObject();
        try {
            json = JSONObject.parseObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
    /**
     * @desc 将List<String>转为List<JSONObject>
     * @param
     **/
    public static List<JSONObject> listStringToJson(List<String> list) {
        Iterator it = list.iterator();
        List<JSONObject> listobj = new LinkedList<>();
        while (it.hasNext()) {
            listobj.add(stringToJsonObject(it.next().toString()));
        }
        return listobj;
    }


    /**
     * @desc 将ResultSet转为jsonobj
     * @param
     **/
    public static JSONArray resultSetToJson(ResultSet set) throws SQLException,JSONException{

        JSONArray array = new JSONArray();
        ResultSetMetaData metaData = set.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (set.next()){
            JSONObject jsonObject = new JSONObject();
            for (int i = 1; i<= columnCount; i++){
                String columnName = metaData.getColumnLabel(i);
                String value = set.getString(columnName);
                jsonObject.put(columnName, value);

            }
            array.add(jsonObject);
        }
        return array;

    }

    /**
     * @desc 递归遍历json，将所有的key-value放入map,包括根节点
     * @param json JSONObject
     **/
    public static HashMap<String, Object> jsonToMap(JSONObject json, HashMap<String, Object> map){

        for (Map.Entry<String, Object> entry : json.entrySet()){
            String str = entry.getValue().getClass().getTypeName();
            if (str == "com.alibaba.fastjson.JSONObject"){
                map.put(entry.getKey(),entry.getValue());
                jsonToMap((JSONObject)entry.getValue(), map);
            }else if(str == "com.alibaba.fastjson.JSONArray"){
                JSONArray ja = (JSONArray)entry.getValue();
                for (Object o : ja){
                    JSONObject jo = (JSONObject)o;
                    jsonToMap(jo,map);
                }
            }else{
                map.put(entry.getKey(),entry.getValue());
            }
        }
        log.info("jsonobj translate to map successfully");
        return map;
    }

    /**
     * @desc 递归遍历json，将json node节点的key-value放入map,不包括根节点
     * @param json JSONObject
     **/
    public static HashMap<String, Object> jsonNodeToMap(JSONObject json, HashMap<String, Object> map){

        for (Map.Entry<String, Object> entry : json.entrySet()){
            String str = entry.getValue().getClass().getTypeName();
            if (str == "com.alibaba.fastjson.JSONObject"){
                jsonToMap((JSONObject)entry.getValue(), map);
            }else if(str == "com.alibaba.fastjson.JSONArray"){
                JSONArray ja = (JSONArray)entry.getValue();
                for (Object o : ja){
                    JSONObject jo = (JSONObject)o;
                    jsonToMap(jo,map);
                }
            }else {
                map.put(entry.getKey(),entry.getValue());
            }
        }
        log.info("jsonobj translate to map successfully");
        return map;
    }
    /**
     * @desc Json文件转为Object[][]
     * @param
     **/
    public static Object[][] jsonFileToArry(String filepath) {
        List parList = new ArrayList();
        List<String> list = new LinkedList<>();
        parList = JsonFileUtil.readJsonFile(list, filepath);
        HashMap<String, String>[][] arrmap = new HashMap[parList.size()][1];
        if (parList.size() > 0) {
            for (int i = 0; i < parList.size(); i++) {
                arrmap[i][0] = new HashMap<>();
            }
        } else {
            log.info("no data in test file");
        }
        for (int j = 0; j < parList.size(); j++) {
            String jsonstr = parList.get(j).toString().replaceAll("null","\"--\"");
            LinkedHashMap<String, Object> jsonMap = JSON.parseObject(jsonstr, new TypeReference<LinkedHashMap<String, Object>>(){});
            for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                String tmp = entry.getValue().toString().replaceAll("\"--\"","null");
                entry.setValue(tmp);
                arrmap[j][0].put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return arrmap;
    }

}


