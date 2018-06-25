package bftsmart.reconfiguration;

import bftsmart.tom.util.TOMUtil;
import com.higgs.trust.consensus.bftsmartcustom.started.custom.CustomKeyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Objects;

/**
 * @author: zhouyafeng
 * @create: 2018/05/30 15:02
 * @description:
 */
@Component
public class SendRCMessage {

    private static final Logger log = LoggerFactory.getLogger(SendRCMessage.class);
    private RCMessage rcMessage;

    @Autowired
    private CustomKeyLoader customKeyLoader;

    public void add(int num, String ip, int port) {
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
        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(0, rcMessage.getNum() + ":" + rcMessage.getIp() + ":" + rcMessage.getPort());
        rcMessage.setProperties(hashtable);
    }

    public void remove(int num) {
        if (num < 0) {
            log.error("This value cannot be less than 0: {}", num);
            return;
        }
        //构建消息
        rcMessage = new RCMessage();
        rcMessage.setNum(num);
        rcMessage.setOperation("rem");
        Hashtable<Integer, String> hashtable = new Hashtable<>();
        hashtable.put(1, String.valueOf(num));
        rcMessage.setProperties(hashtable);

    }

    public void sendToTTP(String ip, int port, int ttpId) {
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
        try {
            Socket s = new Socket(ip, port);

            OutputStream os = s.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            //设置TTP的编号
            rcMessage.setSender(ttpId);
            rcMessage.setSignature(TOMUtil.signMessage(customKeyLoader.loadPrivateKey(), rcMessage.toString().getBytes()));

            objectOutputStream.writeObject(rcMessage);
            objectOutputStream.flush();
            objectOutputStream.close();
            s.close();
        } catch (Exception e) {
            log.error("Failed to get the private key : ", e);
            e.printStackTrace();
        }
    }
}