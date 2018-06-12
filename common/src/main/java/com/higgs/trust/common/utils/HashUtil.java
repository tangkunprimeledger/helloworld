package com.higgs.trust.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by liuyu on 17/12/7.
 */
public class HashUtil {
    private static final Logger LOG = LoggerFactory.getLogger(HashUtil.class);

    /**
     * 利用java原生的摘要实现SHA256加密
     *
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256S(String str) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 将byte转为16进制
     *
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }

    /**
     * 读取文件的hash值
     *
     * @param file
     * @return
     */
    public static String getHashForFile(File file) {
        InputStream is = null;
        MessageDigest complete = null;
        try {
            complete = MessageDigest.getInstance("MD5"); //如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int numRead;
            do {
                numRead = is.read(buffer);//从文件读到buffer，最多装满buffer
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);//用读到的字节进行MD5的计算，第二个参数是偏移量
                }
            } while (numRead != -1);
        } catch (Exception e) {
            LOG.error("[getHashForFile] has error", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        if (complete == null) {
            LOG.info("[getHashForFile] MessageDigest is null");
            return null;
        }
        return byte2Hex(complete.digest());
    }

    public static void main(String[] args) {
        String s = getSHA256S("abcdef");
        System.out.println(s);
        System.out.println(s.length());
//        String hash = getHashForFile(new File("/Users/liuyu/Downloads/327a761a9cdc803fcabd791309099730.gz"));
//        System.out.println(hash);

    }
}
