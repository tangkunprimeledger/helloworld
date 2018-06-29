package com.higgs.trust.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by young001 on 17/3/24.
 */
public class IpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtil.class);

    private static byte[] int2byte(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xff & i);
        bytes[1] = (byte) ((0xff00 & i) >> 8);
        bytes[2] = (byte) ((0xff0000 & i) >> 16);
        bytes[3] = (byte) ((0xff000000 & i) >> 24);
        return bytes;
    }

    private static int byte2Int(byte[] bytes) {
        int n = bytes[0] & 0xFF;
        n |= ((bytes[1] << 8) & 0xFF00);
        n |= ((bytes[2] << 16) & 0xFF0000);
        n |= ((bytes[3] << 24) & 0xFF000000);
        return n;
    }

    public static int ip2Int(String strIp) {
        String[] ss = strIp.split("\\.");
        if (ss.length != 4) {
            return 0;
        }
        byte[] bytes = new byte[ss.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(ss[i]);
        }
        return byte2Int(bytes);
    }

    /**
     * 将int型ip转成String型ip
     *
     * @param intIp
     * @return
     */
    public static String int2Ip(int intIp) {
        byte[] bytes = int2byte(intIp);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(bytes[i] & 0xFF);
            if (i < 3) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    public static String getHostIp() throws UnknownHostException {
        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getLocalHost();
            if ("127.0.0.1".equals(netAddress.getHostAddress())) {
                LOGGER.warn("获取的ip为127.0.0.1");
                throw new UnknownHostException("127.0.0.1");
            }
        } catch (UnknownHostException e) {
            LOGGER.error("get host ip fail", e);
            throw e;
        }
        if (null == netAddress) {
            return null;
        }
        String ip = netAddress.getHostAddress(); //get the ip address
        return ip;
    }

    public static int getHostIpInt() throws UnknownHostException {
        String ip = getHostIp();
        return ip2Int(ip);
    }

    public static void main(String[] args) {
        System.out.println(int2Ip(16777343));
        System.out.println(int2Ip(1510291438));

//        System.out.println(int2Ip(16920420));
    }

}
