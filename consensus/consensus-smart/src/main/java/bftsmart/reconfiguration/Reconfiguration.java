/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bftsmart.reconfiguration;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.util.TOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author eduardo
 */
public class Reconfiguration {

    private static Logger log = LoggerFactory.getLogger(Reconfiguration.class);

    private ReconfigureRequest request;
    private ServiceProxy proxy;
    private int id;

    public Reconfiguration(int id) {
        this.id = id;
        //proxy = new ServiceProxy(id);
        //request = new ReconfigureRequest(id);
    }

    public void connect() {
        if (proxy == null) {
            proxy = new ServiceProxy(id);
        }
    }

    public void addServer(int id, String ip, int port, byte[] sign) {
        this.setReconfiguration(ServerViewController.ADD_SERVER, id + ":" + ip + ":" + port, sign);
    }

    public void removeServer(int id, byte[] sign) {
        this.setReconfiguration(ServerViewController.REMOVE_SERVER, String.valueOf(id), sign);
    }

    //    public void setF(int f, byte[] sign, String nodeName){
    //      this.setReconfiguration(ServerViewController.CHANGE_F,String.valueOf(f), sign, nodeName);
    //    }

    public void setReconfiguration(int prop, String value, byte[] sign) {
        if (request == null) {
            //request = new ReconfigureRequest(proxy.getViewManager().getStaticConf().getProcessId());
            request = new ReconfigureRequest(id);
        }
        //        request.setNumber(Integer.valueOf(value));
        //        request.setOtherSignature(sign);
        request.setProperty(prop, value);
    }

    public ReconfigureReply execute() {
        byte[] signature = new byte[0];
        try {
            signature =
                TOMUtil.signMessage(new RSAKeyLoader(7001, "", true).loadPrivateKey(), request.toString().getBytes());
        } catch (Exception e) {
            log.error("获取TTP私钥失败");
        }
        request.setSignature(signature);
        byte[] reply = proxy.invoke(TOMUtil.getBytes(request), TOMMessageType.RECONFIG);
        request = null;
        return (ReconfigureReply)TOMUtil.getObject(reply);
    }

    public void close() {
        proxy.close();
        proxy = null;
    }

}
