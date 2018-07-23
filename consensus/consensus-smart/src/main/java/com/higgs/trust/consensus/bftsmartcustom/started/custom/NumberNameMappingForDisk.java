package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import com.higgs.trust.consensus.bftsmartcustom.started.custom.config.SmartConfig;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;

/**
 * @author: zhouyafeng
 * @create: 2018/07/15 10:06
 * @description:
 */

@Component
public class NumberNameMappingForDisk implements NumberNameMapping {

    private String path;

    @Override
    public Map<String, String> getMapping() {
        //从disk读取映射对象
        File f = new File(getPath());
        if (!f.exists()) {
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Map<String, String> map = (Map<String, String>) ois.readObject();
            ois.close();

            return map;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean addMapping(Map<String, String> map) {
        if (map.isEmpty()) {
            return false;
        }
        File f = new File(getPath());
        if (f.exists()) {
            f.delete();
        }
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(map);
            oos.flush();
            oos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getPath() {
        String sep = System.getProperty("file.separator");
        SmartConfig smartConfig = SpringUtil.getBean(SmartConfig.class);
        path = smartConfig.getDefaultDir() + sep + "mapping";
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        path = path + sep + "numberNameMapping";
        return path;
    }

}