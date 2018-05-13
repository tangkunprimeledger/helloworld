package com.higgs.trust.rs.custom.util;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class FileHelper {
    private final static Logger LOG = Logger.getLogger(FileHelper.class);
    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    /**
     * 获取临时路径
     *
     * @return
     */
    public static String getTempPath(){
        String classPath = FileHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            if(classPath.startsWith("file:")){
                classPath = classPath.substring(classPath.indexOf(":") + 1);
            }
            if(classPath.indexOf(".") != -1){
                classPath = classPath.substring(0, classPath.indexOf("."));
                classPath = classPath.substring(0, classPath.lastIndexOf("/") + 1);
            }
        }catch (Exception e){
            LOG.error("[getTempPath] has error",e);
        }
        return classPath;
    }
    /**
     * 逐行读取文件
     *
     * @param filePath
     * @return
     */
    public static List<String> readFiles(String filePath) {
        List<String> datas = new ArrayList<>();
        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(filePath);
            br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {
                datas.add(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
        return datas;
    }

    /**
     * 文件写入
     */
    public static boolean writeFiles(String filePath, String datas, String encode) {
        try {
            RandomAccessFile randomFile = new RandomAccessFile(new File(filePath), "rw");
            long fileLength = randomFile.length();
            randomFile.seek(fileLength);
            datas = datas + "\r\n";
            randomFile.write(datas.getBytes(encode));
            randomFile.close();
            return true;
        } catch (Exception e) {
            LOG.error("[saveFile]has error", e);
        }
        return false;
    }

    /**
     * 文件压缩
     *
     * @param srcPath
     * @param targetPath
     */
    public static boolean compress(String srcPath, String targetPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(srcPath);
            fos = new FileOutputStream(targetPath);
            compress(fis, fos);
            fos.flush();
            return true;
        } catch (Exception e) {
            LOG.error("[compress]has error", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    /**
     * 数据压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os) {
        GZIPOutputStream gos = null;
        try {
            //压缩比率
            gos = new GZIPOutputStream(os) {{
                def.setLevel(Deflater.BEST_COMPRESSION);
            }};
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != -1) {
                gos.write(data, 0, count);
            }
            gos.finish();
            gos.flush();
        } catch (Exception e) {
            LOG.error("[compress] has error", e);
        } finally {
            if (gos != null) try {
                gos.close();
            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] args) {
        compress("/Users/liuyu/Downloads/icon.jpg", "/Users/liuyu/Downloads/tests.gz");
    }
}
