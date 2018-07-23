package com.higgs.trust.consensus.bftsmartcustom.started.custom;

import bftsmart.reconfiguration.RCMessage;
import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.TOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: zhouyafeng
 * @create: 2018/05/30 15:02
 * @description:
 */
public class SendRCMessage {

    private static final Logger log = LoggerFactory.getLogger(SendRCMessage.class);
    private RCMessage rcMessage;

    public void add(int num, String ip, int port, String nodeName) {
        if (num < 0) {
            log.error("This value cannot be less than 0: {}", num);
            return;
        }
        if (StringUtils.isEmpty(ip)) {
            log.error("ip can not be null:{}", ip);
            return;
        }
        if (port < 0) {
            log.error("This value cannot be less than 0: {}", port);
            return;
        }
        //构建消息
        rcMessage = new RCMessage();
        rcMessage.setNum(num);
        rcMessage.setIp(ip);
        rcMessage.setPort(port);
        rcMessage.setOperation("add");
        rcMessage.setNodeName(nodeName);
        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(0, rcMessage.getNum() + ":" + rcMessage.getIp() + ":" + rcMessage.getPort());
        rcMessage.setProperties(hashtable);
    }

    public void remove(int num, String nodeName) {
        if (num < 0) {
            log.error("This value cannot be less than 0: {}", num);
            return;
        }
        //构建消息
        rcMessage = new RCMessage();
        rcMessage.setNum(num);
        rcMessage.setOperation("rem");
        rcMessage.setNodeName(nodeName);
        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(1, String.valueOf(num));
        rcMessage.setProperties(hashtable);

    }

    public void sendToTTP(String ip, int port, int ttpId, RSAKeyLoader rsaKeyLoader) {
        if (ttpId < 0) {
            log.error("This value cannot be less than 0: {}", ttpId);
            return;
        }
        if (StringUtils.isEmpty(ip)) {
            log.error("ip can not be null:{}", ip);
            return;
        }
        if (port < 0) {
            log.error("This value cannot be less than 0: {}", port);
            return;
        }
        if (Objects.isNull(this.rcMessage)) {
            log.error("The message cannot be sent because they are null");
            return;
        }
        Socket s = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader reader = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            s = new Socket(ip, port);
            is = s.getInputStream();
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = s.getOutputStream();
            objectOutputStream = new ObjectOutputStream(os);
            //设置TTP的编号
            rcMessage.setSender(ttpId);
            rcMessage.setSignature(TOMUtil.signMessage(rsaKeyLoader.loadPrivateKey(), rcMessage.toString().getBytes()));
            log.info("leave replica message {}", rcMessage.toString());
            objectOutputStream.writeObject(rcMessage);
            while (true) {
                String res = reader.readLine();
                if("success".equals(res)){
                    log.info("Successful configuration update");
                    break;
                } else if("fail".equals(res)){
                    log.info("Configuration update failed");
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(10);
            }
            reader.close();
            objectOutputStream.flush();
            objectOutputStream.close();
            s.close();
        } catch (Exception e) {
            log.error("Failed to get the private key : ", e);
            e.printStackTrace();
        }
    }
}