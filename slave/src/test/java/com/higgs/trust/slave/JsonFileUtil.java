package com.higgs.trust.slave;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.java.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * JsonFileUtil
 *
 * @author shenqingyan
 * @create 2018年04月10日16:53
 */
@Log
public class JsonFileUtil {
    //将文件内容返回为list
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
        } else if (file.exists()){
            list.add(bufferedReader(file.toString()));
        } else{
            log.info("no such file");
        }
        return list;
    }

    //读取文件内容并返回string
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

    //    将string转换为json对象
    public static JSONObject stringToJsonObject(String str) {
        JSONObject json = new JSONObject();
        try {
            json = JSONObject.parseObject(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    // 将List<String>转为List<JSONObject>
    public static List<JSONObject> listStringToJson(List<String> list) {
        Iterator it = list.iterator();
        List<JSONObject> listobj = new LinkedList<>();
        while (it.hasNext()) {
            listobj.add(stringToJsonObject(it.next().toString()));
        }
        return listobj;
    }

    public static String findJsonFile(String url){
        String pathProj = "./src/test/resources/"+url;
        String clazzPath = "./test-classes/"+url;
        File fileProj = new File(pathProj);
        File fileClazz = new File(clazzPath);
        if (fileProj.exists()){
            return pathProj;
        }else if(fileClazz.exists()){
            return clazzPath;
        }else {
            return null;
        }
    }

    //Json文件转为Object[][]
    public static Object[][] jsonFileToArry(String filepath) {
        List parList = new ArrayList();
        List<String> list = new LinkedList<>();
        parList = JsonFileUtil.readJsonFile(list, filepath);
        HashMap<String, Object>[][] arrmap = new HashMap[parList.size()][1];
        if (parList.size() > 0) {
            for (int i = 0; i < parList.size(); i++) {
                arrmap[i][0] = new HashMap<>();
            }
        } else {
            System.out.println("no data in test file");
        }
        for (int j = 0; j < parList.size(); j++) {
            String jsonstr = parList.get(j).toString();
            Map<String, Object> jsonMap = JSON.parseObject(jsonstr,Map.class);
            for(String key : jsonMap.keySet()){
                Object value = jsonMap.get(key);
                arrmap[j][0].put(key,value);
            }
        }
        return arrmap;
    }

}


