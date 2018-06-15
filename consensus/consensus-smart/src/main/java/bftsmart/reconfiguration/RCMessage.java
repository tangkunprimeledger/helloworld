package bftsmart.reconfiguration;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author: zhouyafeng
 * @create: 2018/05/30 14:52
 * @description:
 */
public class RCMessage implements Serializable {
    private int sender;
    private String operation;
    private int num;
    private String ip;
    private int port;
    private Hashtable<Integer,String> properties = new Hashtable<Integer,String>();
    private byte[] signature;

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Hashtable<Integer, String> getProperties() {
        return properties;
    }

    public void setProperties(Hashtable<Integer, String> properties) {
        this.properties = properties;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public String toString(){
        String ret = "Sender :"+ sender+";";
        Iterator<Integer> it = properties.keySet().iterator() ;
        while(it.hasNext()){
            int key = it.next();
            String value = properties.get(key);
            ret = ret+key+value;
        }
        return ret;
    }
}