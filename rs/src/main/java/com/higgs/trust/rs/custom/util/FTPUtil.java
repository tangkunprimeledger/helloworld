package com.higgs.trust.rs.custom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.ftp.FtpClient;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by liuyu on 17/12/8.
 * TODO: sun.net.ftp.FtpClient是内部专用 API, 可能会在未来发行版中删除
 */
public class FTPUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FTPUtil.class);

    /**
     * 服务器连接(匿名登录)
     *
     * @param ip   服务器IP
     * @param port 服务器端口
     * @param path 服务器路径
     */
    public static FtpClient connectServer(String ip, int port, String path) {
        FtpClient ftpClient = null;
        try {
            ftpClient = FtpClient.create();
            SocketAddress addr = new InetSocketAddress(ip, port);
            ftpClient.connect(addr);
            ftpClient.login("anonymous", "liuyu@primeledger.cn".toCharArray());
            if (path != null && path.length() != 0) {
                ftpClient.changeDirectory(path);
            }
        } catch (Exception e) {
            LOG.error("[connectServer] has error", e);
            return null;
        }
        return ftpClient;
    }

    /**
     * 服务器连接
     *
     * @param ip       服务器IP
     * @param port     服务器端口
     * @param user     用户名
     * @param password 密码
     * @param path     服务器路径
     */
    public static FtpClient connectServer(String ip, int port, String user, String password, String path) {
        FtpClient ftpClient = null;
        try {
            ftpClient = FtpClient.create();
            SocketAddress addr = new InetSocketAddress(ip, port);
            ftpClient.connect(addr);
            if (user != null) {
                ftpClient.login(user, password.toCharArray());
            }
            if (path != null && path.length() != 0) {
                ftpClient.changeDirectory(path);
            }
        } catch (Exception e) {
            LOG.error("[connectServer] has error", e);
            return null;
        }
        return ftpClient;
    }

    /**
     * 上传文件
     *
     * @param ftpClient
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     */
    public static boolean upload(FtpClient ftpClient, String localFile, String remoteFile) {
        if (ftpClient == null) {
            return false;
        }
        OutputStream os = null;
        FileInputStream is = null;
        try {
            os = ftpClient.putFileStream(remoteFile);
            is = new FileInputStream(localFile);
            byte[] bytes = new byte[1024];
            int c;
            while ((c = is.read(bytes)) != -1) {
                os.write(bytes, 0, c);
            }
        } catch (Exception e) {
            LOG.error("[upload] has error ", e);
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            try {
                ftpClient.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    /**
     * 文件下载
     *
     * @param ftpClient
     * @param remoteFile 远程文件
     * @param localFile  本地文件
     */
    public static boolean download(FtpClient ftpClient, String remoteFile, String localFile) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = ftpClient.getFileStream(remoteFile);
            os = new FileOutputStream(localFile);
            byte[] bytes = new byte[1024];
            int c;
            while ((c = is.read(bytes)) != -1) {
                os.write(bytes, 0, c);
            }
        } catch (Exception e) {
            LOG.error("[download] has error ", e);
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            try {
                ftpClient.close();
            } catch (IOException e) {
            }
        }
        return true;
    }

    /**
     * scp
     * <p>
     * scp config_dev.json 10.200.172.98:/data/home/admin/prime-mng-tools/profiles/test.json
     *
     * @param src
     * @param dest
     */
    public static void scp(String src, String dest) {
        InputStreamReader ir = null;
        LineNumberReader input = null;
        try {
            LOG.info("[scp]is start");
            Process proc = Runtime.getRuntime().exec("scp " + src + " " + dest);
            proc.waitFor();
            ir = new InputStreamReader(proc.getInputStream());
            input = new LineNumberReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                LOG.info("[scp]result:" + line);
            }
            ir = new InputStreamReader(proc.getErrorStream());
            input = new LineNumberReader(ir);
            while ((line = input.readLine()) != null) {
                LOG.info("[changeIp]error result:" + line);
            }
            LOG.info("[scp]is end");
        } catch (Exception e) {
            LOG.error("[scp]is error", e);
        } finally {
            if (ir != null) {
                try {
                    ir.close();
                } catch (IOException e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void main(String[] args) {
        String ip = "10.200.172.95";
        int port = 21;
        FtpClient ftp = FTPUtil.connectServer(ip, port, "pub");
        boolean r = FTPUtil.upload(ftp, "/Users/liuyu/Downloads/test.txt", "test.txt");
        System.out.println("upload.r:" + r);

        if (r) {
            ftp = FTPUtil.connectServer(ip, port, "pub");
            r = FTPUtil.download(ftp, "test.txt", "/Users/liuyu/Downloads/test_new.txt");
            System.out.println("download.r:" + r);
        }
    }
}

